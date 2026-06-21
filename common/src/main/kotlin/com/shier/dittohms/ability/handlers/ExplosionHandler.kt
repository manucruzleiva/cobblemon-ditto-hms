package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

object ExplosionHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val cfg   = HMsConfig.get(DittoAbility.EXPLOSION)
        val power = cfg.power.toFloat().coerceAtLeast(1f)

        // Save exact position to teleport back after explosion
        val savedPos = Vec3(player.x, player.y, player.z)

        val wasInvulnerable = player.isInvulnerable
        player.isInvulnerable = true
        player.health = 1f

        level.explode(null, player.x, player.y, player.z, power, Level.ExplosionInteraction.BLOCK)

        // Teleport player back to original position and zero velocity so they don't fly
        player.teleportTo(savedPos.x, savedPos.y, savedPos.z)
        player.setDeltaMovement(0.0, 0.0, 0.0)
        player.connection.send(ClientboundSetEntityMotionPacket(player))
        player.isInvulnerable = wasInvulnerable
        if (player.isAlive) player.health = 1f

        return true
    }
}
