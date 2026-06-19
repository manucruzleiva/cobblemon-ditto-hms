package com.shier.dittohms.command

import com.mojang.brigadier.CommandDispatcher
import com.shier.dittohms.ability.AbilityManager
import com.shier.dittohms.ability.DittoAbility
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object DittoHMsCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val root = Commands.literal("dittohms")

        // /dittohms use <ability>
        val use = Commands.literal("use")
        for (ability in DittoAbility.entries) {
            use.then(
                Commands.literal(ability.id).executes { ctx ->
                    val player = ctx.source.playerOrException
                    if (AbilityManager.useAbility(player, ability)) 1 else 0
                },
            )
        }
        root.then(use)

        // /dittohms list
        root.then(
            Commands.literal("list").executes { ctx ->
                val player = ctx.source.playerOrException
                ctx.source.sendSuccess({ Component.literal("§6=== Ditto HMs ===") }, false)
                for (ability in DittoAbility.entries) {
                    val unlocked = AbilityManager.isUnlocked(player, ability)
                    val statusColor = if (unlocked) "§a" else "§7"
                    val lock = if (unlocked) "§a✔" else "§7✘ needs ${ability.unlockSpecies}"
                    ctx.source.sendSuccess({
                        Component.literal(
                            "§f${ability.displayName} $statusColor[$lock§r$statusColor] §8• §ehunger:${ability.hungerCost}",
                        )
                    }, false)
                }
                1
            },
        )

        dispatcher.register(root)
    }
}
