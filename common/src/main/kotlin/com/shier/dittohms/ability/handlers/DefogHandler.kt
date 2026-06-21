package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffects

/** Defog — clears the player's own negative status effects only. No weather or block changes. */
object DefogHandler {
    fun execute(player: ServerPlayer): Boolean {
        listOf(
            MobEffects.BLINDNESS,
            MobEffects.WEAKNESS,
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.HUNGER,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.UNLUCK,
            MobEffects.LEVITATION,
        ).forEach { player.removeEffect(it) }
        return true
    }
}
