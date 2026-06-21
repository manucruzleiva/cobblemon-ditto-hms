package com.shier.dittohms.ability.handlers

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.server.level.ServerPlayer

object RevivalBlessingHandler {
    fun execute(player: ServerPlayer): Boolean {
        // Fully heal and revive all Pokémon in the player's party
        val party = try { Cobblemon.storage.getParty(player) } catch (_: Exception) { return false }
        var healed = 0
        for (pokemon in party) {
            if (pokemon.currentHealth < pokemon.maxHealth || pokemon.isFainted()) {
                pokemon.currentHealth = pokemon.maxHealth
                pokemon.status = null  // clear faint / paralysis / poison / etc.
                healed++
            }
        }

        // Drain ALL hunger — the cost of blessing life
        player.foodData.foodLevel = 0
        player.foodData.addExhaustion(1000f)  // also drain remaining saturation

        return healed > 0 || true  // always "succeeds" so CD is applied
    }
}
