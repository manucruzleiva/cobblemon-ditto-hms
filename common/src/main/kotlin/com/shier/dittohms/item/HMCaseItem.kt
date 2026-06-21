package com.shier.dittohms.item

import com.shier.dittohms.ability.AbilityManager
import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import com.shier.dittohms.storage.HMsSavedData
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.minecraft.nbt.CompoundTag

/**
 * The HM Case is a quick-launcher for any ability the player has already learned
 * independently (via HM Discs). It stores ONLY which ability is currently "active"
 * for right-click use. Learning abilities is completely independent of this item.
 */
class HMCaseItem(properties: Properties) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (level.isClientSide) return InteractionResultHolder.pass(player.getItemInHand(hand))
        val serverPlayer = player as? ServerPlayer ?: return InteractionResultHolder.pass(player.getItemInHand(hand))
        val stack = player.getItemInHand(hand)

        if (player.isShiftKeyDown) {
            HMCaseMenu.open(serverPlayer, stack)
            return InteractionResultHolder.success(stack)
        }

        val active = getActive(stack)
        if (active == null) {
            player.sendSystemMessage(Component.literal("§eNo active HM selected. §fShift+right-click§e to open the HM Case."))
            return InteractionResultHolder.fail(stack)
        }

        // useAbility handles cooldown/hunger internally — no item cooldown on the Case
        // so shift+right-click (GUI) is never blocked, even when the active ability is cooling down
        AbilityManager.useAbility(serverPlayer, active)
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
    }

    override fun getName(stack: ItemStack): Component {
        val active = getActive(stack)
        return if (active != null)
            Component.literal("§6HM Case §8[§f${active.displayName}§8]")
        else
            Component.literal("§6HM Case")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        val active = getActive(stack)
        if (active != null) tooltipComponents.add(Component.literal("§7Active: §a${active.displayName}"))
        tooltipComponents.add(Component.literal("§8Shift+right-click to manage"))
        tooltipComponents.add(Component.literal("§8Right-click to use active HM"))
    }

    companion object {
        private const val TAG_ACTIVE = "active"

        private fun tag(stack: ItemStack): CompoundTag =
            stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()

        private fun writeTag(stack: ItemStack, tag: CompoundTag) =
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))

        fun getActive(stack: ItemStack): DittoAbility? {
            val idx = tag(stack).getInt(TAG_ACTIVE) - 1
            return if (idx < 0 || idx >= DittoAbility.entries.size) null else DittoAbility.entries[idx]
        }

        fun setActive(stack: ItemStack, ability: DittoAbility?) {
            val t = tag(stack)
            t.putInt(TAG_ACTIVE, if (ability == null) 0 else ability.ordinal + 1)
            writeTag(stack, t)
        }

        fun findInHotbar(player: Player): Int? =
            (0 until 9).firstOrNull { player.inventory.getItem(it).item is HMCaseItem }
    }
}
