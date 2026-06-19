package com.shier.dittohms

import com.shier.dittohms.ability.AbilityManager
import com.shier.dittohms.command.DittoHMsCommand
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.TickEvent
import org.slf4j.LoggerFactory

object DittoHMs {
    @JvmField
    val LOGGER = LoggerFactory.getLogger("Cobblemon Ditto HMs")

    fun init() {
        TickEvent.SERVER_POST.register(TickEvent.Server { server ->
            AbilityManager.tick(server)
        })

        CommandRegistrationEvent.EVENT.register(
            CommandRegistrationEvent { dispatcher, _, _ -> DittoHMsCommand.register(dispatcher) },
        )

        LOGGER.info("Cobblemon Ditto HMs initialized — use /dittohms to unleash Ditto's power!")
    }
}
