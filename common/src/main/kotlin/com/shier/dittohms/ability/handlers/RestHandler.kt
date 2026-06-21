package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

object RestHandler {

    /**
     * UUID → gameTime when they requested rest.
     * A request expires after REQUEST_TIMEOUT ticks if others don't join.
     */
    private val pending = mutableMapOf<UUID, Long>()
    private const val REQUEST_TIMEOUT = 300L  // 15 seconds window

    fun execute(player: ServerPlayer): Boolean {
        val server    = player.server
        val overworld = server.overworld()
        val now       = overworld.gameTime

        // Expire stale requests
        pending.entries.removeIf { (_, t) -> now - t > REQUEST_TIMEOUT }

        val allPlayers = server.playerList.players

        // Singleplayer — execute immediately
        if (allPlayers.size <= 1) {
            performRest(player, overworld)
            return true
        }

        // Multiplayer — add current player to the pending pool
        pending[player.uuid] = now

        // Check if all players are either in the pending pool or sleeping
        val allReady = allPlayers.all { p -> p.uuid in pending || p.isSleeping }

        if (!allReady) {
            val have  = pending.size + allPlayers.count { it.isSleeping }
            val total = allPlayers.size
            player.sendSystemMessage(
                Component.literal("§eWaiting for all players to rest... (§f$have§e/§f$total§e)"),
            )
            allPlayers.filter { it.uuid != player.uuid }.forEach { other ->
                other.sendSystemMessage(
                    Component.literal("§6${player.name.string}§e wants to rest. (§f$have§e/§f$total§e) — use your HM Rest to agree."),
                )
            }
            return false  // no hunger/CD yet for the current player
        }

        // All players agreed — rest everyone
        val cfg = HMsConfig.get(DittoAbility.REST)
        allPlayers.forEach { p ->
            performRest(p, overworld)
            // Apply hunger and CD manually for the OTHER players
            if (p.uuid != player.uuid) {
                p.foodData.addExhaustion(cfg.hungerCost * 4.0f)
                // Note: cooldowns for others are NOT tracked here — they'll need to re-use Rest themselves
                // (fairness: each player manages their own CD via their own disc/case)
            }
        }
        pending.clear()
        return true  // AbilityManager handles hunger/CD for the triggering player normally
    }

    private fun performRest(player: ServerPlayer, overworld: ServerLevel) {
        player.health = player.maxHealth
        player.removeAllEffects()
        player.foodData.foodLevel = 1
        player.foodData.addExhaustion(100f)

        val dayTick  = overworld.dayTime % 24000L
        val fullDay  = overworld.dayTime / 24000L
        if (dayTick < 13000L) {
            overworld.setDayTime(fullDay * 24000L + 13000L)
        } else {
            overworld.setDayTime((fullDay + 1) * 24000L)
        }
        player.sendSystemMessage(Component.literal("§a...Ditto rested."))
    }
}
