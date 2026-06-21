package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.server.level.ServerPlayer

object SunnyDayHandler {
    fun execute(player: ServerPlayer): Boolean {
        val cfg      = HMsConfig.get(DittoAbility.SUNNY_DAY)
        val duration = cfg.power.coerceAtLeast(400)
        // Clear skies for configured duration
        player.server.overworld().setWeatherParameters(duration, 0, false, false)
        return true
    }
}
