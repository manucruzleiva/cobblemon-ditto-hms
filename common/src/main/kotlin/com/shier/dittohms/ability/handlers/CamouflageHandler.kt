package com.shier.dittohms.ability.handlers

import com.mojang.math.Transformation
import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Display.BlockDisplay
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID

object CamouflageHandler {

    private val SCALE_ID = ResourceLocation.fromNamespaceAndPath("cobblemon_ditto_hms", "camouflage_scale")

    data class DisguiseData(val displayUUID: UUID?, val expiry: Long)
    private val disguises = mutableMapOf<UUID, DisguiseData>()

    // ── Public API ────────────────────────────────────────────────────────────

    fun execute(player: ServerPlayer): Boolean {
        val level    = player.serverLevel()
        val cfg      = HMsConfig.get(DittoAbility.CAMOUFLAGE)
        val duration = cfg.power.coerceAtLeast(20)

        // Clean up any running disguise without touching the disguises map yet
        disguises[player.uuid]?.let { cleanupEffects(player, it) }

        val hit = player.pick(20.0, 1.0f, false)
        var displayUUID: UUID? = null

        when (hit.type) {

            HitResult.Type.BLOCK -> {
                val blockPos   = (hit as BlockHitResult).blockPos
                val blockState = level.getBlockState(blockPos)
                if (blockState.isAir) return false

                val bd = BlockDisplay(EntityType.BLOCK_DISPLAY, level)
                bd.setPos(player.x, player.y, player.z)
                applyBlockState(bd, blockState)
                applyTransformation(bd, Transformation(
                    Vector3f(-0.3f, 0f, -0.3f),
                    Quaternionf(),
                    Vector3f(0.6f, 1.8f, 0.6f),
                    Quaternionf(),
                ))
                level.addFreshEntity(bd)
                displayUUID = bd.uuid

                player.addEffect(MobEffectInstance(MobEffects.INVISIBILITY, duration + 40, 0, false, false, false))
            }

            HitResult.Type.ENTITY -> {
                val target = (hit as EntityHitResult).entity

                // Spawn a real, inert copy of the entity that follows the player — a true morph.
                displayUUID = spawnEntityCopy(player, target)

                if (displayUUID == null) {
                    // Fallback (e.g. players, which can't be created via EntityType.create):
                    // scale to match width + wear the entity's name tag.
                    val scaleRatio = (target.bbWidth / 0.6).coerceIn(0.1, 5.0) - 1.0
                    player.getAttribute(Attributes.SCALE)?.let { attr ->
                        attr.removeModifier(SCALE_ID)
                        if (scaleRatio != 0.0) attr.addOrUpdateTransientModifier(
                            net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                SCALE_ID, scaleRatio,
                                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                            )
                        )
                    }
                    player.customName          = target.name
                    player.isCustomNameVisible = true
                }

                // Hide the player's own skin so only the copy shows
                player.addEffect(MobEffectInstance(MobEffects.INVISIBILITY, duration + 40, 0, false, false, false))
            }

            else -> return false
        }

        disguises[player.uuid] = DisguiseData(displayUUID, player.level().gameTime + duration)
        return true
    }

    /** Called from AbilityManager when camouflage is toggled off or player leaves. */
    fun removeDisguise(player: ServerPlayer) {
        val data = disguises.remove(player.uuid) ?: return
        cleanupEffects(player, data)
    }

    fun tickScaleExpiry(server: MinecraftServer) {
        val now  = server.overworld().gameTime
        val iter = disguises.iterator()
        while (iter.hasNext()) {
            val (uuid, data) = iter.next()
            val player = server.playerList.getPlayer(uuid)

            if (player == null || now >= data.expiry) {
                if (player != null) cleanupEffects(player, data)
                iter.remove()
                continue
            }

            // Move the follow-entity (block or mob copy) to track the player each tick
            data.displayUUID?.let { uid ->
                val ent = player.serverLevel().getEntity(uid)
                if (ent == null) {
                    // The disguise entity vanished (chunk unload, killed, etc.) — end the disguise
                    cleanupEffects(player, data)
                    iter.remove()
                    return@let
                }
                if (ent is LivingEntity) {
                    ent.moveTo(player.x, player.y, player.z, player.yBodyRot, 0f)
                    ent.yBodyRot = player.yBodyRot
                    ent.yHeadRot = player.yHeadRot
                    ent.setYRot(player.yBodyRot)
                } else {
                    ent.setPos(player.x, player.y, player.z)
                }
            }
        }
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /**
     * Spawns an inert clone of [target] at the player and returns its UUID, or null if the
     * entity type can't be instantiated (e.g. players). The clone copies the target's NBT so
     * variants/equipment match, then is frozen (NoAI, no gravity, silent, invulnerable).
     */
    private fun spawnEntityCopy(player: ServerPlayer, target: Entity): UUID? {
        val level = player.serverLevel()
        val copy  = target.type.create(level) ?: return null
        runCatching {
            val tag = target.saveWithoutId(CompoundTag())
            tag.remove("UUID")
            tag.remove("Pos")
            tag.remove("Motion")
            copy.load(tag)
        }
        copy.setPos(player.x, player.y, player.z)
        copy.isSilent       = true
        copy.isNoGravity    = true
        copy.isInvulnerable = true
        if (copy is Mob) {
            copy.isNoAi = true
            copy.setPersistenceRequired()
        }
        return if (level.addFreshEntity(copy)) copy.uuid else null
    }

    /**
     * Removes all side-effects of a disguise. Does NOT modify [disguises] — safe inside a loop.
     */
    private fun cleanupEffects(player: ServerPlayer, data: DisguiseData) {
        data.displayUUID?.let { uid -> player.serverLevel().getEntity(uid)?.discard() }
        player.getAttribute(Attributes.SCALE)?.removeModifier(SCALE_ID)
        player.customName          = null
        player.isCustomNameVisible = false
        player.getEffect(MobEffects.INVISIBILITY)?.let {
            if (!it.isAmbient) player.removeEffect(MobEffects.INVISIBILITY)
        }
    }

    // Reflection: Display.setTransformation is private in Loom MC 1.21.1 mappings
    private val setTransformationMethod by lazy {
        runCatching {
            Display::class.java.getDeclaredMethod("setTransformation", Transformation::class.java)
                .also { it.isAccessible = true }
        }.getOrNull()
    }

    private fun applyTransformation(bd: BlockDisplay, t: Transformation) {
        runCatching { setTransformationMethod?.invoke(bd, t) }
    }

    // Reflection: BlockDisplay.setBlockState / DATA_BLOCK_STATE_ID (private in Loom mappings)
    private val blockStateAccessorField by lazy {
        runCatching {
            BlockDisplay::class.java.getDeclaredField("DATA_BLOCK_STATE_ID")
                .also { it.isAccessible = true }
        }.getOrNull()
    }

    private fun applyBlockState(bd: BlockDisplay, state: BlockState) {
        for (methodName in listOf("setBlockState", "m_8180_", "a")) {
            runCatching {
                BlockDisplay::class.java.getDeclaredMethod(methodName, BlockState::class.java)
                    .also { it.isAccessible = true }
                    .invoke(bd, state)
                return
            }
        }
        runCatching {
            @Suppress("UNCHECKED_CAST")
            val acc = blockStateAccessorField?.get(null)
                as? net.minecraft.network.syncher.EntityDataAccessor<BlockState> ?: return
            bd.entityData.set(acc, state)
        }
    }
}
