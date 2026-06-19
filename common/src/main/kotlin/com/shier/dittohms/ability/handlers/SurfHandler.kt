package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object SurfHandler {
    fun execute(player: ServerPlayer): Boolean {
        player.addEffect(MobEffectInstance(MobEffects.DOLPHINS_GRACE, 600, 0))
        player.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1))
        return true
    }
}
