package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object DiveHandler {
    fun execute(player: ServerPlayer): Boolean {
        player.addEffect(MobEffectInstance(MobEffects.WATER_BREATHING, 1200, 0))
        player.addEffect(MobEffectInstance(MobEffects.DOLPHINS_GRACE, 1200, 0))
        return true
    }
}
