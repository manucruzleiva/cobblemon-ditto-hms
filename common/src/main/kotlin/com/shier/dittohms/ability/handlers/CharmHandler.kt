package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.Vec3
import java.util.UUID

object CharmHandler {

    data class CharmData(val playerUUID: UUID, val expiry: Long)
    private val charmed = mutableMapOf<UUID, CharmData>()

    fun execute(player: ServerPlayer): Boolean {
        val cfg    = HMsConfig.get(DittoAbility.CHARM)
        val target = findTarget(player)
        if (target == null) {
            player.displayClientMessage(Component.literal("§cNo entity in your line of sight."), true)
            return false
        }
        if (target !is Mob) {
            player.displayClientMessage(Component.literal("§cThis entity cannot be charmed."), true)
            return false
        }
        val duration = cfg.power.toLong().coerceAtLeast(200L)
        charmed[target.uuid] = CharmData(player.uuid, player.level().gameTime + duration)
        player.displayClientMessage(
            Component.literal("§d${target.name.string} §7is charmed! It will follow you."), true
        )
        return true
    }

    fun tick(server: MinecraftServer) {
        val now  = server.overworld().gameTime
        val iter = charmed.iterator()
        while (iter.hasNext()) {
            val (entityUUID, data) = iter.next()
            if (now >= data.expiry) { iter.remove(); continue }

            val owner = server.playerList.getPlayer(data.playerUUID)
            if (owner == null) { iter.remove(); continue }

            var found = false
            for (level in server.allLevels) {
                val entity = level.getEntity(entityUUID) ?: continue
                found = true
                if (!entity.isAlive) { iter.remove(); break }
                if (entity is Mob) {
                    val dist = entity.distanceTo(owner)
                    if (dist > 3.0) entity.navigation.moveTo(owner, 1.4)
                    else            entity.navigation.stop()
                }
                break
            }
            if (!found) iter.remove()
        }
    }

    private fun findTarget(player: ServerPlayer): LivingEntity? {
        val level = player.serverLevel()
        val start = player.eyePosition
        val dir   = player.lookAngle
        val end   = start.add(dir.scale(10.0))
        val aabb  = player.boundingBox.expandTowards(dir.scale(10.0)).inflate(1.5)
        val result = ProjectileUtil.getEntityHitResult(
            level, player, start, end, aabb,
            { it is LivingEntity && it != player && !it.isSpectator },
            0.5f,
        )
        return result?.entity as? LivingEntity
    }
}
