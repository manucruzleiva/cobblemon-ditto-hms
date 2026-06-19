package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object WaterfallHandler {
    fun execute(player: ServerPlayer): Boolean {
        // Short levitation burst so the player can climb up a waterfall column
        player.addEffect(MobEffectInstance(MobEffects.LEVITATION, 60, 1))
        return true
    }
}
