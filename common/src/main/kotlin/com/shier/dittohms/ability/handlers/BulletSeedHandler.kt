package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.entity.projectile.Arrow
import java.lang.reflect.Field
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.UUID

object BulletSeedHandler {

    private fun isSeed(stack: ItemStack): Boolean =
        "seed" in BuiltInRegistries.ITEM.getKey(stack.item).path

    data class PendingShot(
        val playerUUID: UUID,
        var remaining: Int,
        val xRot: Float,
        val yRot: Float,
        var nextTick: Long,
        val firedArrows: MutableList<UUID> = mutableListOf(),
    )

    private val inGroundField: Field? by lazy {
        runCatching {
            AbstractArrow::class.java.getDeclaredField("inGround").also { it.isAccessible = true }
        }.getOrNull()
    }
    private fun isInGround(arrow: Arrow) = inGroundField?.getBoolean(arrow) ?: false

    private val queue   = mutableListOf<PendingShot>()
    // arrowUUID -> (levelKey, seed item to drop on block hit)
    private val tracked = mutableMapOf<UUID, Pair<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>, ItemStack>>()

    fun execute(player: ServerPlayer): Boolean {
        val cfg   = HMsConfig.get(DittoAbility.BULLET_SEED)
        val count = cfg.power.coerceAtLeast(1).coerceAtMost(15)

        if (countSeeds(player) == 0) {
            player.sendSystemMessage(Component.literal("§cYou need seeds to use Bullet Seed!"))
            return false
        }
        val shots = minOf(count, countSeeds(player))
        val seedItem = findSeedStack(player)?.item ?: Items.WHEAT_SEEDS

        removeSeeds(player, shots)
        queue.removeIf { it.playerUUID == player.uuid }
        queue.add(PendingShot(player.uuid, shots, player.xRot, player.yRot, player.level().gameTime + 2))
        return true
    }

    fun tick(server: MinecraftServer) {
        val now = server.overworld().gameTime

        // ── Fire pending shots ────────────────────────────────────────────────
        val fireIter = queue.iterator()
        while (fireIter.hasNext()) {
            val entry = fireIter.next()
            if (now < entry.nextTick) continue
            val player = server.playerList.getPlayer(entry.playerUUID)
            if (player == null) { fireIter.remove(); continue }

            val level = player.serverLevel()
            val arrow = Arrow(level, player, ItemStack(Items.WHEAT_SEEDS), null)
            val sH = (level.random.nextFloat() - 0.5f) * 5f
            val sV = (level.random.nextFloat() - 0.5f) * 2.5f
            arrow.shootFromRotation(player, entry.xRot + sV, entry.yRot + sH, 0f, 2.0f, 0.2f)
            arrow.baseDamage = 1.5
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED
            level.addFreshEntity(arrow)

            // Track this arrow to drop a seed if it hits a block
            tracked[arrow.uuid] = Pair(level.dimension(), ItemStack(Items.WHEAT_SEEDS))
            entry.firedArrows.add(arrow.uuid)

            entry.remaining--
            if (entry.remaining <= 0) fireIter.remove()
            else entry.nextTick = now + 3
        }

        // ── Check tracked arrows for block hits ───────────────────────────────
        val trackIter = tracked.iterator()
        while (trackIter.hasNext()) {
            val (arrowUUID, pair) = trackIter.next()
            val (levelKey, seedStack) = pair
            val level = server.getLevel(levelKey) ?: run { trackIter.remove(); continue }
            val entity = level.getEntity(arrowUUID)
            if (entity == null) { trackIter.remove(); continue }  // arrow already gone (hit entity, maybe)
            if (entity !is Arrow) { trackIter.remove(); continue }

            if (isInGround(entity)) {
                // Hit a block — drop the seed at impact location
                val drop = ItemEntity(level, entity.x, entity.y, entity.z, seedStack.copy())
                drop.setDefaultPickUpDelay()
                level.addFreshEntity(drop)
                entity.discard()
                trackIter.remove()
            } else if (now - (now % 100) > 100) {
                // Safety cleanup after ~5 seconds (100 ticks)
                trackIter.remove()
            }
        }
    }

    private fun countSeeds(player: ServerPlayer): Int {
        var n = 0
        for (i in 0 until player.inventory.containerSize) {
            val s = player.inventory.getItem(i); if (isSeed(s)) n += s.count
        }
        return n
    }

    private fun findSeedStack(player: ServerPlayer): ItemStack? {
        for (i in 0 until player.inventory.containerSize) {
            val s = player.inventory.getItem(i); if (isSeed(s)) return s
        }
        return null
    }

    private fun removeSeeds(player: ServerPlayer, amount: Int) {
        var left = amount
        for (i in 0 until player.inventory.containerSize) {
            if (left <= 0) break
            val s = player.inventory.getItem(i); if (!isSeed(s)) continue
            val take = minOf(left, s.count); s.shrink(take); left -= take
        }
    }
}
