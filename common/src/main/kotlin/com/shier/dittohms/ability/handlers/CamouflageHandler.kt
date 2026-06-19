package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object CamouflageHandler {
    fun execute(player: ServerPlayer): Boolean {
        player.addEffect(MobEffectInstance(MobEffects.INVISIBILITY, 600, 0))
        return true
    }
}
