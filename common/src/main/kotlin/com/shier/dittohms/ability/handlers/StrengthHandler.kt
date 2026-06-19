package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object StrengthHandler {
    fun execute(player: ServerPlayer): Boolean {
        player.addEffect(MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1))
        return true
    }
}
