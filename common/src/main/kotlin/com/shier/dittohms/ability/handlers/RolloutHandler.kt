package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object RolloutHandler {
    fun execute(player: ServerPlayer): Boolean {
        player.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2))
        player.addEffect(MobEffectInstance(MobEffects.DIG_SPEED, 200, 1))
        return true
    }
}
