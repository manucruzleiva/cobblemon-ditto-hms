package com.shier.dittohms.item

import com.shier.dittohms.ability.AbilityManager
import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import com.shier.dittohms.storage.HMsSavedData
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

/**
 * Permanent HM badge. Right-click once to learn the ability (disc stays).
 * Subsequent right-clicks use the ability. Learning is fully independent of
 * the HM Case — the case only provides quick-access to already-learned abilities.
 */
class HMDiscItem(val ability: DittoAbility, properties: Properties) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (level.isClientSide) return InteractionResultHolder.pass(player.getItemInHand(hand))
        val serverPlayer = player as? ServerPlayer ?: return InteractionResultHolder.pass(player.getItemInHand(hand))
        val stack = player.getItemInHand(hand)
        val data = HMsSavedData.get(serverPlayer.server)

        // First use: learn the ability — disc is CONSUMED
        if (!data.isLearned(player.uuid, ability.ordinal)) {
            data.learn(player.uuid, ability.ordinal)
            // Do NOT auto-enable passives — player enables them manually via HM Case
            if (!player.hasInfiniteMaterials()) stack.shrink(1) // consume the disc
            player.displayClientMessage(
                Component.literal("§a✔ Learned §f${ability.displayName}§a! Use it from your HM Case."),
                true, // action bar (not chat)
            )
            return InteractionResultHolder.success(stack)
        }

        // Already learned — activate the ability
        val success = AbilityManager.useAbility(serverPlayer, ability)
        if (success && !ability.isPassive) {
            val cd = HMsConfig.get(ability).cooldownTicks
            if (cd > 0) player.getCooldowns().addCooldown(this, cd)
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
    }

    override fun getName(stack: ItemStack): Component =
        Component.literal("§bHM: ${ability.displayName}")

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        val cfg = HMsConfig.get(ability)
        if (ability.description.isNotEmpty())
            tooltipComponents.add(Component.literal("§7${ability.description}"))
        if (ability.isPassive) {
            tooltipComponents.add(Component.literal("§9Toggle §7— right-click to turn on/off"))
        } else {
            if (cfg.hungerCost > 0) tooltipComponents.add(Component.literal("§7Hunger: §f${cfg.hungerCost}"))
            if (cfg.cooldownTicks > 0) tooltipComponents.add(Component.literal("§7Cooldown: §f${cfg.cooldownTicks / 20}s"))
        }
    }
}
