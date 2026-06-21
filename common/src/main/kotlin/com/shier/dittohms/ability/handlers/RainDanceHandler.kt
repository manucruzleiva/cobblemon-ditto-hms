package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.server.level.ServerPlayer

object RainDanceHandler {
    fun execute(player: ServerPlayer): Boolean {
        val cfg      = HMsConfig.get(DittoAbility.RAIN_DANCE)
        val duration = cfg.power.coerceAtLeast(400)
        // Rain (no thunder) for configured duration
        player.server.overworld().setWeatherParameters(0, duration, true, false)
        return true
    }
}
