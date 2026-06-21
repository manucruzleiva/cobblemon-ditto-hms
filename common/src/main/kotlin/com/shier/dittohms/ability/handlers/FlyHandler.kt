package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.storage.HMsSavedData
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.server.level.ServerPlayer

/**
 * Fly no longer equips an Elytra. Instead it launches the player skyward and
 * relies on the GLIDE passive for the actual gliding physics:
 *   • You must have learned GLIDE to use Fly.
 *   • Using Fly auto-enables the GLIDE toggle.
 *   • Each subsequent use while airborne acts as a firework-style boost.
 */
object FlyHandler {

    fun execute(player: ServerPlayer): Boolean {
        val data = HMsSavedData.get(player.server)

        // Gate: Fly requires Glide to be learned.
        if (!data.isLearned(player.uuid, DittoAbility.GLIDE.ordinal)) {
            player.displayClientMessage(
                Component.literal("§cYou need to learn §fGlide§c before you can use §fFly§c!"), true,
            )
            return false
        }

        // Auto-enable Glide so the gliding physics kick in.
        if (!data.isPassiveEnabled(player.uuid, DittoAbility.GLIDE.ordinal)) {
            data.setPassiveEnabled(player.uuid, DittoAbility.GLIDE.ordinal, true)
            player.displayClientMessage(Component.literal("§aGlide engaged."), true)
        }

        val look = player.lookAngle
        val cur  = player.deltaMovement
        if (player.onGround() || cur.y < 0.1) {
            // Take-off: big upward launch with a little forward momentum.
            setAndSync(player, look.x * 0.8, 0.95, look.z * 0.8)
        } else {
            // Airborne boost: thrust toward where you're looking (firework-like).
            setAndSync(
                player,
                cur.x + look.x * 0.6,
                (cur.y + look.y * 0.4 + 0.25).coerceIn(-0.3, 0.8),
                cur.z + look.z * 0.6,
            )
        }
        player.fallDistance = 0f
        return true
    }

    private fun setAndSync(player: ServerPlayer, x: Double, y: Double, z: Double) {
        player.setDeltaMovement(x, y, z)
        player.connection.send(ClientboundSetEntityMotionPacket(player))
    }
}
