package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object MagnetRiseHandler {

    fun execute(player: ServerPlayer): Boolean {
        // Robust airborne check: onGround flag OR a solid block just below the feet
        val belowBox  = player.boundingBox.move(0.0, -0.2, 0.0)
        val supported = player.onGround() || !player.level().noCollision(player, belowBox)
        if (supported) {
            player.displayClientMessage(
                Component.literal("§cYou must be airborne to use Magnet Rise!"), true
            )
            return false
        }
        val cfg      = HMsConfig.get(DittoAbility.MAGNET_RISE)
        val duration = cfg.power.coerceAtLeast(20)
        player.addEffect(MobEffectInstance(MobEffects.LEVITATION,   duration, 1, false, true,  true))
        player.addEffect(MobEffectInstance(MobEffects.SLOW_FALLING, duration, 0, false, false, true))
        return true
    }
}
