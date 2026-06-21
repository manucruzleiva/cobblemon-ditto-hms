package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object FlashHandler {
    fun execute(player: ServerPlayer): Boolean {
        val existing = player.getEffect(MobEffects.NIGHT_VISION)
        if (existing != null) {
            player.removeEffect(MobEffects.NIGHT_VISION)
        } else {
            // Very long duration = effectively permanent until toggled off
            player.addEffect(MobEffectInstance(MobEffects.NIGHT_VISION, 999999, 0, false, false, true))
        }
        return true
    }
}
