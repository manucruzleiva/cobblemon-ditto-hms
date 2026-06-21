package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object RockClimbHandler {
    fun execute(player: ServerPlayer): Boolean {
        val cfg      = HMsConfig.get(DittoAbility.ROCK_CLIMB)
        val duration = cfg.power.coerceAtLeast(20)
        // Jump Boost IV + Slow Falling + Speed II — lets the player scale cliffs rapidly
        player.addEffect(MobEffectInstance(MobEffects.JUMP, duration, 3, false, true, true))
        player.addEffect(MobEffectInstance(MobEffects.SLOW_FALLING, duration, 0, false, true, true))
        player.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1, false, true, true))
        return true
    }
}
