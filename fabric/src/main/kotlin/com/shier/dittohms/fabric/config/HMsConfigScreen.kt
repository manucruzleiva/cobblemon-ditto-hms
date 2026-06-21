package com.shier.dittohms.fabric.config

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object HMsConfigScreen {
    fun build(parent: Screen?): Screen {
        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Cobblemon Ditto HM — Config"))
            .setSavingRunnable { HMsConfig.save() }

        val eb = builder.entryBuilder()

        for (ability in DittoAbility.entries) {
            val cat = builder.getOrCreateCategory(Component.literal(ability.displayName))
            val cfg = HMsConfig.get(ability)

            if (!ability.isPassive) {
                cat.addEntry(
                    eb.startIntSlider(Component.literal("Hunger Cost"), cfg.hungerCost, 0, 20)
                        .setDefaultValue(cfg.hungerCost)
                        .setTooltip(Component.literal("Costo de hambre al usar la habilidad (0 = gratis)."))
                        .setSaveConsumer { HMsConfig.set(ability, hungerCost = it) }
                        .build(),
                )
                cat.addEntry(
                    eb.startIntSlider(Component.literal("Cooldown (ticks)"), cfg.cooldownTicks, 0, 24000)
                        .setDefaultValue(cfg.cooldownTicks)
                        .setTooltip(Component.literal("Ticks entre usos (20 ticks = 1 segundo)."))
                        .setSaveConsumer { HMsConfig.set(ability, cooldownTicks = it) }
                        .build(),
                )
            }

            cat.addEntry(
                eb.startIntSlider(Component.literal("Power"), cfg.power, 0, 512)
                    .setDefaultValue(cfg.power)
                    .setTooltip(Component.literal("Potencia de la habilidad (radio, duración, cantidad, etc)."))
                    .setSaveConsumer { HMsConfig.set(ability, power = it) }
                    .build(),
            )
        }

        return builder.build()
    }
}
