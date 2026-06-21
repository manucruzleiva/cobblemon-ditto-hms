package com.shier.dittohms.ability.handlers

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import java.util.UUID

/**
 * Manages Magnet Rise's mayfly-based hover.
 * (Fly uses FlyHandler's own Elytra session management instead.)
 */
object FlightManager {

    private val magnetSessions = mutableMapOf<UUID, Long>() // UUID -> expiry tick

    fun grantMagnet(player: ServerPlayer, durationTicks: Long) {
        magnetSessions[player.uuid] = player.level().gameTime + durationTicks
        player.abilities.mayfly = true
        player.abilities.flying = true
        player.onUpdateAbilities()
    }

    fun tick(server: MinecraftServer) {
        val now  = server.overworld().gameTime
        val iter = magnetSessions.iterator()
        while (iter.hasNext()) {
            val (uuid, expiry) = iter.next()
            val player = server.playerList.getPlayer(uuid)
            if (player == null || now >= expiry) {
                player?.let { revoke(it) }
                iter.remove()
                continue
            }
            // Magnezone feel: slow falling + resistance while hovering
            player.addEffect(MobEffectInstance(MobEffects.SLOW_FALLING,      5, 0, true, false, false))
            player.addEffect(MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 0, true, false, false))
        }
    }

    private fun revoke(player: ServerPlayer) {
        if (!player.isCreative && !player.isSpectator) {
            player.abilities.mayfly = false
            player.abilities.flying = false
            player.onUpdateAbilities()
        }
    }
}
