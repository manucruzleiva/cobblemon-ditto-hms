package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.FluidTags
import java.util.UUID

/**
 * Waterfall — bubble-column upward push (like soul sand underwater).
 * No Levitation effect; applies continuous upward velocity while active.
 */
object WaterfallHandler {
    private val sessions = mutableMapOf<UUID, Long>() // UUID -> expiry tick

    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val pos   = player.blockPosition()
        val inWater = level.getFluidState(pos).`is`(FluidTags.WATER) ||
            level.getFluidState(pos.above()).`is`(FluidTags.WATER)

        if (!inWater) {
            player.sendSystemMessage(Component.literal("§cYou must be in water to use Waterfall!"))
            return false
        }

        val duration = HMsConfig.get(DittoAbility.WATERFALL).power.coerceAtLeast(20).toLong()
        sessions[player.uuid] = player.level().gameTime + duration
        return true
    }

    fun tick(server: MinecraftServer) {
        val now  = server.overworld().gameTime
        val iter = sessions.iterator()
        while (iter.hasNext()) {
            val (uuid, expiry) = iter.next()
            val player = server.playerList.getPlayer(uuid)
            if (player == null || now >= expiry) { iter.remove(); continue }

            // Apply bubble-column-style upward velocity, capped so it doesn't launch forever
            val m = player.deltaMovement
            if (m.y < 0.4) {
                player.setDeltaMovement(m.x, (m.y + 0.04).coerceAtMost(0.4), m.z)
            }
        }
    }
}
