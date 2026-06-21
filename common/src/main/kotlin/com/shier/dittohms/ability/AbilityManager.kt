package com.shier.dittohms.ability

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.shier.dittohms.ability.handlers.*
import com.shier.dittohms.config.HMsConfig
import com.shier.dittohms.effect.HMEffects
import com.shier.dittohms.storage.HMsSavedData
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.util.DefaultRandomPos
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.item.Items
import kotlin.math.sqrt
import java.util.UUID

object AbilityManager {

    private val handlers: Map<DittoAbility, (ServerPlayer) -> Boolean> = mapOf(
        DittoAbility.WATER_GUN        to WaterGunHandler::execute,
        DittoAbility.LEAFAGE          to LeafageHandler::execute,
        DittoAbility.CUT              to CutHandler::execute,
        DittoAbility.ROCK_SMASH       to RockSmashHandler::execute,
        DittoAbility.ROTOTILLER       to RototillerHandler::execute,
        DittoAbility.CAMOUFLAGE       to CamouflageHandler::execute,
        DittoAbility.STRENGTH         to StrengthHandler::execute,
        DittoAbility.WATERFALL        to WaterfallHandler::execute,
        DittoAbility.MAGNET_RISE      to MagnetRiseHandler::execute,
        DittoAbility.EMBER            to EmberHandler::execute,
        DittoAbility.BULLET_SEED      to BulletSeedHandler::execute,
        DittoAbility.TELEPORT         to TeleportHandler::execute,
        DittoAbility.FLY              to FlyHandler::execute,
        DittoAbility.RAIN_DANCE       to RainDanceHandler::execute,
        DittoAbility.SUNNY_DAY        to SunnyDayHandler::execute,
        DittoAbility.REST             to RestHandler::execute,
        DittoAbility.DIG              to DigHandler::execute,
        DittoAbility.EXPLOSION        to ExplosionHandler::execute,
        DittoAbility.THUNDER          to ThunderHandler::execute,
        DittoAbility.STRING_SHOT      to StringShotHandler::execute,
        DittoAbility.DEFOG            to DefogHandler::execute,
        DittoAbility.CRAB_HAMMER      to CrabHammerHandler::execute,
        DittoAbility.REVIVAL_BLESSING to RevivalBlessingHandler::execute,
        DittoAbility.CHARM            to CharmHandler::execute,
    )

    private val cooldowns = mutableMapOf<UUID, MutableMap<DittoAbility, Long>>()

    private val HARDEN_ARMOR_ID      = ResourceLocation.fromNamespaceAndPath("cobblemon_ditto_hms", "harden_armor")
    private val HARDEN_TOUGHNESS_ID  = ResourceLocation.fromNamespaceAndPath("cobblemon_ditto_hms", "harden_toughness")

    fun isLearned(player: ServerPlayer, ability: DittoAbility): Boolean =
        HMsSavedData.get(player.server).isLearned(player.uuid, ability.ordinal)

    fun useAbility(player: ServerPlayer, ability: DittoAbility): Boolean {
        val data = HMsSavedData.get(player.server)

        if (!data.isLearned(player.uuid, ability.ordinal)) {
            player.displayClientMessage(Component.literal("§cYou haven't learned §f${ability.displayName}§c yet."), true)
            return false
        }

        if (ability.isPassive) {
            val enabled = data.togglePassive(player.uuid, ability.ordinal)
            if (!enabled) removePassiveEffects(player, ability)
            AbilityFx.playToggle(player, ability, enabled)
            player.displayClientMessage(
                Component.literal(if (enabled) "§a✔ ${ability.displayName} ENABLED" else "§7✖ ${ability.displayName} disabled"),
                true,
            )
            return true
        }

        val cfg      = HMsConfig.get(ability)
        val tickNow  = player.level().gameTime
        val cd       = cooldowns.getOrPut(player.uuid) { mutableMapOf() }
        val cdExpires = cd[ability] ?: 0L
        if (tickNow < cdExpires) {
            val remaining = ((cdExpires - tickNow) / 20).coerceAtLeast(1)
            player.displayClientMessage(Component.literal("§e${ability.displayName} on cooldown (§f${remaining}s§e)"), true)
            return false
        }

        if (cfg.hungerCost > 0 && player.foodData.foodLevel < cfg.hungerCost) {
            player.displayClientMessage(Component.literal("§cNot enough hunger! (need §f${cfg.hungerCost}§c)"), true)
            return false
        }

        val handler = handlers[ability] ?: return false
        val success = handler(player)

        if (success) {
            if (cfg.hungerCost > 0) player.foodData.addExhaustion(cfg.hungerCost * 4.0f)
            if (cfg.cooldownTicks > 0) cd[ability] = tickNow + cfg.cooldownTicks
            AbilityFx.play(player, ability)
            player.displayClientMessage(Component.literal("§a✦ ${ability.displayName}!"), true)
        }
        return success
    }

    fun getLearned(player: ServerPlayer): List<DittoAbility> {
        val data = HMsSavedData.get(player.server)
        return DittoAbility.entries.filter { data.isLearned(player.uuid, it.ordinal) }
    }

    // ── Passive tick ──────────────────────────────────────────────────────────

    fun tickPassives(server: MinecraftServer) {
        val data = HMsSavedData.get(server)
        for (player in server.playerList.players) {
            for (ability in DittoAbility.entries) {
                if (!ability.isPassive) continue
                if (!data.isLearned(player.uuid, ability.ordinal)) continue
                if (data.isPassiveEnabled(player.uuid, ability.ordinal)) {
                    applyPassive(player, ability)
                    applyIndicator(player, ability)
                } else {
                    removePassiveEffects(player, ability)
                    removeIndicator(player, ability)
                }
            }
        }
    }

    private fun applyPassive(player: ServerPlayer, ability: DittoAbility) {
        val cfg = HMsConfig.get(ability)

        // Helper: refresh an ambient effect only when it's about to expire or missing.
        // showIcon = false — the toggle's own HM indicator effect carries the HUD icon.
        fun refreshAmbient(h: Holder<MobEffect>, amplifier: Int = 0, duration: Int = 200) {
            val ex = player.getEffect(h)
            if (ex == null || !ex.isAmbient || ex.duration < 5 || ex.amplifier != amplifier)
                player.addEffect(MobEffectInstance(h, duration, amplifier, true, false, false))
        }

        when (ability) {
            DittoAbility.JUMP    -> refreshAmbient(MobEffects.JUMP, (cfg.power - 1).coerceAtLeast(0)) // power = level (4 → JB IV)

            DittoAbility.SURF    -> refreshAmbient(MobEffects.DOLPHINS_GRACE) // active = Dolphin's Grace anywhere

            DittoAbility.ROLLOUT -> refreshAmbient(MobEffects.MOVEMENT_SPEED, cfg.power)

            DittoAbility.DIVE   -> {
                refreshAmbient(MobEffects.WATER_BREATHING)
                if (player.isUnderWater) {
                    val m     = player.deltaMovement
                    val horiz = sqrt(m.x * m.x + m.z * m.z)
                    // Gentle sink ONLY while idle and already drifting down — the moment you
                    // swim (any direction, including up → m.y >= 0) it stops, so it never
                    // fights your swimming.
                    if (horiz < 0.10 && m.y < 0.0) {
                        player.setDeltaMovement(m.x, (m.y - 0.035).coerceAtLeast(-0.32), m.z)
                        player.connection.send(ClientboundSetEntityMotionPacket(player))
                    }
                }
            }

            DittoAbility.FLASH  -> {
                val nv = player.getEffect(MobEffects.NIGHT_VISION)
                if (nv == null || !nv.isAmbient || nv.duration < 200)
                    player.addEffect(MobEffectInstance(MobEffects.NIGHT_VISION, Int.MAX_VALUE, 0, true, false, false))
            }

            DittoAbility.ROCK_CLIMB -> {
                // Climbing physics are handled in tick(); the HUD icon is the HM indicator effect.
            }

            DittoAbility.MEAN_LOOK  -> {
                val radius = cfg.power.toDouble()
                val level  = player.serverLevel()
                // Make hostile mobs FLEE via pathfinding — exactly like creepers running from
                // a cat. Never affects Pokémon, friendly/passive mobs, or players.
                level.getEntities(player, player.boundingBox.inflate(radius))
                { it.isAlive && it is Enemy && it is PathfinderMob && it !is PokemonEntity }
                    .forEach { e ->
                        val mob  = e as PathfinderMob
                        val dist = mob.distanceTo(player)
                        if (dist > radius) return@forEach
                        val nav = mob.navigation
                        // Pick a new escape route only when the mob isn't already running one.
                        if (nav.isDone) {
                            val away = DefaultRandomPos.getPosAway(mob, 16, 7, player.position())
                            if (away != null) nav.moveTo(away.x, away.y, away.z, 1.0)
                        }
                        // Sprint when the player gets close, like a panicking creeper.
                        nav.setSpeedModifier(if (dist < 7.0) 1.45 else 1.1)
                    }
            }

            DittoAbility.HARDEN -> {
                player.getAttribute(Attributes.ARMOR)?.let { inst ->
                    if (inst.getModifier(HARDEN_ARMOR_ID) == null)
                        inst.addOrUpdateTransientModifier(
                            AttributeModifier(HARDEN_ARMOR_ID, 20.0, AttributeModifier.Operation.ADD_VALUE)
                        )
                }
                player.getAttribute(Attributes.ARMOR_TOUGHNESS)?.let { inst ->
                    if (inst.getModifier(HARDEN_TOUGHNESS_ID) == null)
                        inst.addOrUpdateTransientModifier(
                            AttributeModifier(HARDEN_TOUGHNESS_ID, 8.0, AttributeModifier.Operation.ADD_VALUE)
                        )
                }
                refreshAmbient(MobEffects.DAMAGE_RESISTANCE)
            }

            DittoAbility.GLIDE -> {
                // Real elytra gliding, no Elytra item: re-assert the fall-flying flag every
                // tick while airborne. Vanilla's updateFallFlying() clears it (no elytra), but
                // that runs during the entity tick — AFTER the per-tick entity-data broadcast —
                // so the value the client receives each tick is the one we set here.
                if (!player.onGround() && !player.isInWater && !player.isInLava && !player.abilities.flying) {
                    player.startFallFlying()
                    player.resetFallDistance() // we manage fall damage; client does the gliding
                }
            }

            DittoAbility.BURNING_BULWARK -> {
                refreshAmbient(MobEffects.FIRE_RESISTANCE)
                // Thorns aura: hostile mobs that close into melee range get scorched.
                if (player.tickCount % 10 == 0) {
                    val level  = player.serverLevel()
                    val thorns = cfg.power.toFloat().coerceAtLeast(1f)
                    level.getEntities(player, player.boundingBox.inflate(1.6))
                    { it.isAlive && it is Enemy && it !is PokemonEntity }
                        .forEach { mob ->
                            (mob as? LivingEntity)?.let {
                                it.igniteForTicks(60)
                                it.hurt(player.damageSources().thorns(player), thorns)
                            }
                        }
                }
            }

            else -> {}
        }
    }

    // ── Toggle HUD indicators ──────────────────────────────────────────────────

    /** Keep the toggle's HM indicator effect topped up so its icon stays in the HUD. */
    private fun applyIndicator(player: ServerPlayer, ability: DittoAbility) {
        val ind = HMEffects.get(ability) ?: return
        val ex  = player.getEffect(ind)
        if (ex == null || ex.duration < 30)
            player.addEffect(MobEffectInstance(ind, 120, 0, true, false, true))
    }

    private fun removeIndicator(player: ServerPlayer, ability: DittoAbility) {
        val ind = HMEffects.get(ability) ?: return
        player.getEffect(ind)?.let { player.removeEffect(ind) }
    }

    private fun removePassiveEffects(player: ServerPlayer, ability: DittoAbility) {
        fun removeAmbient(h: Holder<MobEffect>) {
            player.getEffect(h)?.let { if (it.isAmbient) player.removeEffect(h) }
        }
        when (ability) {
            DittoAbility.JUMP        -> removeAmbient(MobEffects.JUMP)
            DittoAbility.SURF        -> removeAmbient(MobEffects.DOLPHINS_GRACE)
            DittoAbility.ROLLOUT     -> removeAmbient(MobEffects.MOVEMENT_SPEED)
            DittoAbility.DIVE        -> removeAmbient(MobEffects.WATER_BREATHING)
            DittoAbility.FLASH       -> removeAmbient(MobEffects.NIGHT_VISION)
            DittoAbility.ROCK_CLIMB  -> removeAmbient(MobEffects.SLOW_FALLING)
            DittoAbility.MEAN_LOOK   -> removeAmbient(MobEffects.LUCK)
            DittoAbility.GLIDE       -> {
                // Stop our forced glide, but leave a genuine Elytra flight alone.
                if (player.isFallFlying && player.getItemBySlot(EquipmentSlot.CHEST).item != Items.ELYTRA)
                    player.stopFallFlying()
            }
            DittoAbility.BURNING_BULWARK -> removeAmbient(MobEffects.FIRE_RESISTANCE)
            DittoAbility.HARDEN      -> {
                player.getAttribute(Attributes.ARMOR)?.removeModifier(HARDEN_ARMOR_ID)
                player.getAttribute(Attributes.ARMOR_TOUGHNESS)?.removeModifier(HARDEN_TOUGHNESS_ID)
                removeAmbient(MobEffects.DAMAGE_RESISTANCE)
            }
            else -> {}
        }
    }

    // ── Main tick ─────────────────────────────────────────────────────────────

    fun tick(server: MinecraftServer) {
        val online = server.playerList.players.map { it.uuid }.toSet()
        cooldowns.keys.removeIf { it !in online }
        tickPassives(server)
        CamouflageHandler.tickScaleExpiry(server)
        CharmHandler.tick(server)
        FlightManager.tick(server)
        BulletSeedHandler.tick(server)
        WaterfallHandler.tick(server)
        StringShotHandler.tick(server)

        val data = HMsSavedData.get(server)

        // Rock Climb: treat any wall you're facing as a ladder.
        for (player in server.playerList.players) {
            if (!data.isLearned(player.uuid, DittoAbility.ROCK_CLIMB.ordinal)) continue
            if (!data.isPassiveEnabled(player.uuid, DittoAbility.ROCK_CLIMB.ordinal)) continue
            if (player.onGround() || player.isInWater || player.isInLava || player.abilities.flying) continue

            val level  = player.level()
            val facing = player.direction          // horizontal cardinal direction
            val feet   = player.blockPosition().relative(facing)
            val head   = feet.above()
            val wall = !level.getBlockState(feet).getCollisionShape(level, feet).isEmpty ||
                       !level.getBlockState(head).getCollisionShape(level, head).isEmpty
            if (wall) {
                val m  = player.deltaMovement
                // Sneak to descend, otherwise climb. Dampen horizontal so you cling to the wall.
                val vy = if (player.isShiftKeyDown) -0.15 else 0.18
                player.setDeltaMovement(m.x * 0.2, vy, m.z * 0.2)
                player.connection.send(ClientboundSetEntityMotionPacket(player))
                player.fallDistance = 0f
            }
        }

        // Per-toggle hunger cap
        for (player in server.playerList.players) {
            val blocked = DittoAbility.entries.sumOf { ability ->
                if (!ability.isPassive) return@sumOf 0
                if (!data.isLearned(player.uuid, ability.ordinal)) return@sumOf 0
                if (!data.isPassiveEnabled(player.uuid, ability.ordinal)) return@sumOf 0
                HMsConfig.get(ability).hungerBlock.toLong()
            }.toInt()
            if (blocked > 0) {
                val cap = (20 - blocked).coerceAtLeast(2)
                if (player.foodData.foodLevel > cap) player.foodData.foodLevel = cap
                if (player.foodData.foodLevel >= cap)
                    player.addEffect(MobEffectInstance(MobEffects.REGENERATION, 5, 0, true, false, false))
            }
        }
    }
}
