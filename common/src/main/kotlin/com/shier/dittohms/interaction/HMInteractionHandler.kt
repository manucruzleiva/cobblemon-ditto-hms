package com.shier.dittohms.interaction

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.item.HMItems
import com.shier.dittohms.storage.HMsSavedData
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object HMInteractionHandler {

    /**
     * Called on sneak+right-click of any entity.
     * Returns true if interaction was consumed (player received an HM disc).
     */
    fun onEntityUse(player: Player, level: Level, hand: InteractionHand, entity: Entity): Boolean {
        if (level.isClientSide) return false
        if (hand != InteractionHand.MAIN_HAND) return false
        if (!player.isShiftKeyDown) return false
        if (entity !is PokemonEntity) return false

        val serverPlayer = player as? ServerPlayer ?: return false
        val heldStack = player.getItemInHand(hand)
        if (heldStack.isEmpty) return false

        val speciesPath = entity.pokemon.species.resourceIdentifier.path
        val ability = DittoAbility.fromTriggerAndSpecies(heldStack.item, speciesPath) ?: return false

        val data = HMsSavedData.get(serverPlayer.server)
        if (data.isLearned(player.uuid, ability.ordinal)) {
            player.displayClientMessage(
                Component.literal("§eYou already know §f${ability.displayName}§e."),
                true,
            )
            return true
        }

        if (!player.hasInfiniteMaterials()) heldStack.shrink(1)
        val disc = HMItems.forAbility(ability)
        if (disc != null) {
            val discStack = ItemStack(disc)
            if (!player.inventory.add(discStack)) player.drop(discStack, false)
        }
        player.displayClientMessage(
            Component.literal("§6${entity.pokemon.species.name} §rgave you §b${ability.displayName} HM Disc§r!"),
            true,
        )
        return true
    }
}
