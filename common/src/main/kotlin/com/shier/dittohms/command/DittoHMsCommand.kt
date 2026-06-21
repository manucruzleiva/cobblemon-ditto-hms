package com.shier.dittohms.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.shier.dittohms.ability.AbilityManager
import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import com.shier.dittohms.item.HMItems
import com.shier.dittohms.storage.HMsSavedData
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object DittoHMsCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val root = Commands.literal("dittohm")

        // /dittohm use <ability>
        val use = Commands.literal("use")
        for (ability in DittoAbility.entries) {
            use.then(Commands.literal(ability.id).executes { ctx ->
                val player = ctx.source.playerOrException
                if (AbilityManager.useAbility(player, ability)) 1 else 0
            })
        }
        root.then(use)

        // /dittohms list
        root.then(Commands.literal("list").executes { ctx ->
            val player = ctx.source.playerOrException
            val data = HMsSavedData.get(player.server)
            ctx.source.sendSuccess({ Component.literal("§6=== Ditto HMs ===") }, false)
            for (ability in DittoAbility.entries) {
                val learned = data.isLearned(player.uuid, ability.ordinal)
                val cfg = HMsConfig.get(ability)
                val passiveInfo = if (ability.isPassive) {
                    val on = data.isPassiveEnabled(player.uuid, ability.ordinal)
                    " §7[passive: ${if (on) "§aON" else "§cOFF"}§7]"
                } else ""
                val statusColor = if (learned) "§a" else "§7"
                val pool = ability.unlockPool.first()
                val lock = if (learned) "§a✔" else "§7✘ (give ${ability.triggerItem().description.string} to $pool)"
                ctx.source.sendSuccess({
                    Component.literal("§f${ability.displayName} $statusColor[$lock§r$statusColor]$passiveInfo §8• §ehunger:${cfg.hungerCost} cd:${cfg.cooldownTicks}t")
                }, false)
            }
            1
        })

        // /dittohms give <ability> <player>  (op-only)
        val give = Commands.literal("give").requires { it.hasPermission(2) }
        for (ability in DittoAbility.entries) {
            give.then(Commands.literal(ability.id)
                .then(Commands.argument("target", EntityArgument.player()).executes { ctx ->
                    val target = EntityArgument.getPlayer(ctx, "target")
                    val disc = HMItems.forAbility(ability) ?: return@executes 0
                    val stack = ItemStack(disc)
                    if (!target.inventory.add(stack)) target.drop(stack, false)
                    ctx.source.sendSuccess({ Component.literal("§aGave §f${ability.displayName} HM Disc §ato §f${target.name.string}§a.") }, true)
                    1
                }))
        }
        root.then(give)

        // /dittohms config <ability> hunger|cooldown|power <value>  (op-only)
        val config = Commands.literal("config").requires { it.hasPermission(2) }
        for (ability in DittoAbility.entries) {
            val abilityNode = Commands.literal(ability.id)
            abilityNode.then(Commands.literal("hunger")
                .then(Commands.argument("value", IntegerArgumentType.integer(0, 20)).executes { ctx ->
                    val v = IntegerArgumentType.getInteger(ctx, "value")
                    HMsConfig.set(ability, hungerCost = v)
                    ctx.source.sendSuccess({ Component.literal("§a${ability.displayName} hunger → $v") }, true); 1
                }))
            abilityNode.then(Commands.literal("cooldown")
                .then(Commands.argument("value", IntegerArgumentType.integer(0, 24000)).executes { ctx ->
                    val v = IntegerArgumentType.getInteger(ctx, "value")
                    HMsConfig.set(ability, cooldownTicks = v)
                    ctx.source.sendSuccess({ Component.literal("§a${ability.displayName} cooldown → ${v}t") }, true); 1
                }))
            abilityNode.then(Commands.literal("power")
                .then(Commands.argument("value", IntegerArgumentType.integer(0, 512)).executes { ctx ->
                    val v = IntegerArgumentType.getInteger(ctx, "value")
                    HMsConfig.set(ability, power = v)
                    ctx.source.sendSuccess({ Component.literal("§a${ability.displayName} power → $v") }, true); 1
                }))
            if (ability.isPassive) abilityNode.then(Commands.literal("hungerblock")
                .then(Commands.argument("value", IntegerArgumentType.integer(0, 20)).executes { ctx ->
                    val v = IntegerArgumentType.getInteger(ctx, "value")
                    HMsConfig.set(ability, hungerBlock = v)
                    ctx.source.sendSuccess({ Component.literal("§a${ability.displayName} hungerBlock → $v") }, true); 1
                }))
            abilityNode.then(Commands.literal("reset").executes { ctx ->
                HMsConfig.reset(ability)
                ctx.source.sendSuccess({ Component.literal("§a${ability.displayName} config reset.") }, true); 1
            })
            config.then(abilityNode)
        }
        config.then(Commands.literal("reset_all").executes { ctx ->
            HMsConfig.resetAll()
            ctx.source.sendSuccess({ Component.literal("§aConfig reset for all abilities.") }, true); 1
        })
        root.then(config)

        // /dittohm select <ability>  — sets active ability in HM Case (also called by chat menu click events)
        val select = Commands.literal("select")
        for (ability in DittoAbility.entries) {
            select.then(Commands.literal(ability.id).executes { ctx ->
                val player = ctx.source.playerOrException
                val caseSlot = com.shier.dittohms.item.HMCaseItem.findInHotbar(player)
                    ?: run {
                        ctx.source.sendFailure(Component.literal("You don't have an HM Case in your hotbar!"))
                        return@executes 0
                    }
                val caseStack = player.inventory.getItem(caseSlot)
                if (!HMsSavedData.get(player.server).isLearned(player.uuid, ability.ordinal)) {
                    ctx.source.sendFailure(Component.literal("You haven't learned ${ability.displayName} yet."))
                    return@executes 0
                }
                com.shier.dittohms.item.HMCaseItem.setActive(caseStack, ability)
                ctx.source.sendSuccess({ Component.literal("§aActive HM → §f${ability.displayName}§a!") }, false)
                1
            })
        }
        root.then(select)

        // /dittohms learn_all [player]  — op-only testing shortcut: learns every HM instantly
        root.then(
            Commands.literal("learn_all")
                .requires { it.hasPermission(2) }
                .executes { ctx ->
                    val player = ctx.source.playerOrException
                    learnAll(player)
                    ctx.source.sendSuccess({ Component.literal("§a✔ All ${DittoAbility.entries.size} HMs learned for §f${player.name.string}§a.") }, true)
                    1
                }
                .then(
                    Commands.argument("target", EntityArgument.player()).executes { ctx ->
                        val target = EntityArgument.getPlayer(ctx, "target")
                        learnAll(target)
                        ctx.source.sendSuccess({ Component.literal("§a✔ All HMs learned for §f${target.name.string}§a.") }, true)
                        1
                    },
                ),
        )

        // /dittohm forget <ability> [player]  — op-only: unlearn a specific HM
        val forget = Commands.literal("forget").requires { it.hasPermission(2) }
        for (ability in DittoAbility.entries) {
            forget.then(Commands.literal(ability.id)
                .executes { ctx ->
                    val player = ctx.source.playerOrException
                    forgetAbility(player, ability)
                    ctx.source.sendSuccess({ Component.literal("§a${ability.displayName} forgotten by §f${player.name.string}§a.") }, true); 1
                }
                .then(Commands.argument("target", EntityArgument.player()).executes { ctx ->
                    val target = EntityArgument.getPlayer(ctx, "target")
                    forgetAbility(target, ability)
                    ctx.source.sendSuccess({ Component.literal("§a${ability.displayName} forgotten by §f${target.name.string}§a.") }, true); 1
                }))
        }
        root.then(forget)

        // /dittohm forget_all [player]  — op-only: unlearn all HMs
        root.then(Commands.literal("forget_all").requires { it.hasPermission(2) }
            .executes { ctx ->
                val player = ctx.source.playerOrException
                forgetAll(player)
                ctx.source.sendSuccess({ Component.literal("§aAll HMs forgotten for §f${player.name.string}§a.") }, true); 1
            }
            .then(Commands.argument("target", EntityArgument.player()).executes { ctx ->
                val target = EntityArgument.getPlayer(ctx, "target")
                forgetAll(target)
                ctx.source.sendSuccess({ Component.literal("§aAll HMs forgotten for §f${target.name.string}§a.") }, true); 1
            }))

        dispatcher.register(root)
    }

    private fun learnAll(player: net.minecraft.server.level.ServerPlayer) {
        val data = HMsSavedData.get(player.server)
        for (ability in DittoAbility.entries) {
            data.learn(player.uuid, ability.ordinal)
            // Do NOT auto-enable passives — player enables manually from HM Case
        }
    }

    private fun forgetAbility(player: net.minecraft.server.level.ServerPlayer, ability: DittoAbility) {
        val data = HMsSavedData.get(player.server)
        // Unlearn by clearing the bit
        val current = data.getLearnedOrdinals(player.uuid)
        val mask    = current and (1 shl ability.ordinal).inv()
        // Write back — use learn/unlearn via reflection on internal map
        // Since SavedData only has learn(), we repurpose by writing 0 bit:
        // Access the learned map via the setPassiveEnabled approach (indirect)
        // Simpler: expose a forget method via the SavedData
        data.forget(player.uuid, ability.ordinal)
        if (ability.isPassive) data.setPassiveEnabled(player.uuid, ability.ordinal, false)
    }

    private fun forgetAll(player: net.minecraft.server.level.ServerPlayer) {
        val data = HMsSavedData.get(player.server)
        for (ability in DittoAbility.entries) {
            data.forget(player.uuid, ability.ordinal)
            if (ability.isPassive) data.setPassiveEnabled(player.uuid, ability.ordinal, false)
        }
    }
}
