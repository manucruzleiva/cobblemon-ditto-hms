package com.shier.dittohms.ability

import com.cobblemon.mod.common.Cobblemon
import com.shier.dittohms.ability.handlers.*
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

object AbilityManager {

    private val handlers: Map<DittoAbility, (ServerPlayer) -> Boolean> = mapOf(
        DittoAbility.WATER_GUN       to WaterGunHandler::execute,
        DittoAbility.LEAFAGE         to LeafageHandler::execute,
        DittoAbility.CUT             to CutHandler::execute,
        DittoAbility.ROCK_SMASH      to RockSmashHandler::execute,
        DittoAbility.ROTOTILLER      to RototillerHandler::execute,
        DittoAbility.JUMP            to JumpHandler::execute,
        DittoAbility.SURF            to SurfHandler::execute,
        DittoAbility.CAMOUFLAGE      to CamouflageHandler::execute,
        DittoAbility.STOCKPILE_WATER to StockpileWaterHandler::execute,
        DittoAbility.STRENGTH        to StrengthHandler::execute,
        DittoAbility.ROLLOUT         to RolloutHandler::execute,
        DittoAbility.GLIDE           to GlideHandler::execute,
        DittoAbility.WATERFALL       to WaterfallHandler::execute,
        DittoAbility.MAGNET_RISE     to MagnetRiseHandler::execute,
        DittoAbility.DIVE            to DiveHandler::execute,
    )

    // UUID -> (ability -> tick when cooldown expires)
    private val cooldowns = mutableMapOf<UUID, MutableMap<DittoAbility, Long>>()

    fun isUnlocked(player: ServerPlayer, ability: DittoAbility): Boolean {
        return try {
            val party = Cobblemon.storage.getParty(player)
            party.any { pokemon ->
                pokemon.species.resourceIdentifier.path == ability.unlockSpecies ||
                    pokemon.species.name.lowercase() == ability.unlockSpecies
            }
        } catch (_: Exception) {
            false
        }
    }

    fun useAbility(player: ServerPlayer, ability: DittoAbility): Boolean {
        if (!isUnlocked(player, ability)) {
            player.sendSystemMessage(
                Component.literal("§cYou need §f${ability.unlockSpecies.replaceFirstChar { it.uppercase() }}§c in your party to use §f${ability.displayName}§c!"),
            )
            return false
        }

        val tickNow = player.level().gameTime
        val cd = cooldowns.getOrPut(player.uuid) { mutableMapOf() }
        val cdExpires = cd[ability] ?: 0L
        if (tickNow < cdExpires) {
            val remaining = ((cdExpires - tickNow) / 20).coerceAtLeast(1)
            player.sendSystemMessage(Component.literal("§e${ability.displayName}§e is on cooldown (§f${remaining}s§e remaining)"))
            return false
        }

        if (player.foodData.foodLevel < ability.hungerCost) {
            player.sendSystemMessage(
                Component.literal("§cNot enough hunger to use §f${ability.displayName}§c! (need §f${ability.hungerCost}§c food level)"),
            )
            return false
        }

        val handler = handlers[ability] ?: return false
        val success = handler(player)

        if (success) {
            // Drain hunger: each 4 exhaustion = 1 food level (after saturation is burnt)
            player.foodData.addExhaustion(ability.hungerCost * 4.0f)
            cd[ability] = tickNow + ability.cooldownTicks
            player.sendSystemMessage(Component.literal("§aDitto used §f${ability.displayName}§a!"))
        }

        return success
    }

    fun getUnlocked(player: ServerPlayer): List<DittoAbility> =
        DittoAbility.entries.filter { isUnlocked(player, it) }

    fun tick(server: MinecraftServer) {
        val online = server.playerList.players.map { it.uuid }.toSet()
        cooldowns.keys.removeIf { it !in online }
    }
}
