package com.shier.dittohms.item

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import com.shier.dittohms.storage.HMsSavedData
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore

/**
 * 6-row chest GUI — two clearly separated sections (no spacer/header panes):
 *
 *  Rows 0-2 [0-23]  : 24 active HM slots
 *  Row 3   [24-35]  : empty divider band
 *  Rows 4-5 [36-45] : 10 toggle HM slots
 *  [46-53]          : empty
 *
 * Controls:
 *  • Left-click an HM  → pick it up onto the cursor (drag); click any slot in the
 *    SAME section to drop it there, swapping positions.
 *  • Right-click an HM → set it active (actives) / turn it on or off (toggles).
 */
class HMCaseMenu(
    syncId: Int,
    playerInventory: Inventory,
    private val display: SimpleContainer,
    val caseStack: ItemStack,
    private val serverPlayer: ServerPlayer,
) : ChestMenu(MenuType.GENERIC_9x6, syncId, playerInventory, display, 6) {

    // Per-session mutable ordering (resets to default on menu open)
    private val activeOrdering: MutableList<DittoAbility> =
        DittoAbility.entries.filter { !it.isPassive }.toMutableList()
    private val toggleOrdering: MutableList<DittoAbility> =
        DittoAbility.entries.filter {  it.isPassive }.toMutableList()

    // While moving an HM: section ("active"/"toggle") + the index it was picked up from.
    private var dragFrom: Pair<String, Int>? = null

    companion object {
        const val ACTIVE_START = 0
        const val ACTIVE_END   = 23   // 24 active HMs (rows 0-2)
        const val TOGGLE_START = 36
        const val TOGGLE_END   = 45   // 10 toggle HMs (rows 4-5)

        fun open(player: ServerPlayer, caseStack: ItemStack) {
            val display = SimpleContainer(54)
            player.openMenu(object : MenuProvider {
                override fun getDisplayName() = Component.literal("§6HM Case")
                override fun createMenu(syncId: Int, inv: Inventory, p: Player) =
                    HMCaseMenu(syncId, inv, display, caseStack, player)
            })
        }
    }

    init { refresh() }

    // ── Population ────────────────────────────────────────────────────────────

    fun refresh() {
        val data   = HMsSavedData.get(serverPlayer.server)
        val active = HMCaseItem.getActive(caseStack)

        for (i in 0 until activeOrdering.size) {
            val ability   = activeOrdering[i]
            val learned   = data.isLearned(serverPlayer.uuid, ability.ordinal)
            val isActive  = ability == active
            val isDragged = dragFrom == ("active" to i)
            display.setItem(ACTIVE_START + i, if (!learned) notLearnedGlass(ability)
                                              else makeActiveSlot(ability, isActive, isDragged))
        }

        for (i in 0 until toggleOrdering.size) {
            val ability  = toggleOrdering[i]
            val learned  = data.isLearned(serverPlayer.uuid, ability.ordinal)
            val enabled  = learned && data.isPassiveEnabled(serverPlayer.uuid, ability.ordinal)
            val isDragged = dragFrom == ("toggle" to i)
            display.setItem(TOGGLE_START + i, if (!learned) notLearnedGlass(ability)
                                              else makeToggleSlot(ability, enabled, isDragged))
        }

        // Empty divider band + trailing slots (no spacer items).
        for (i in (ACTIVE_END + 1) until TOGGLE_START) display.setItem(i, ItemStack.EMPTY)
        for (i in (TOGGLE_END + 1)..53)               display.setItem(i, ItemStack.EMPTY)
    }

    // ── Click handling ────────────────────────────────────────────────────────

    override fun clicked(slotId: Int, button: Int, action: ClickType, player: Player) {
        // Ignore the vanilla click-drag-distribute gesture.
        if (action == ClickType.QUICK_CRAFT) return

        // Carrying an HM to move it: the next click drops it.
        val drag = dragFrom
        if (drag != null) {
            val section = sectionOf(slotId)
            if (section == drag.first) {
                val order = orderingFor(section)
                val index = slotId - sectionStart(section)
                if (index in order.indices && index != drag.second) {
                    val t = order[drag.second]; order[drag.second] = order[index]; order[index] = t
                }
            }
            endDrag()
            return
        }

        val section = sectionOf(slotId)
        if (section == null) {
            // Not an HM slot. Block our own (empty/divider) container slots so nothing can be
            // dropped into them; let the player freely manage their own inventory below.
            if (slotId in 0 until display.containerSize) return
            super.clicked(slotId, button, action, player)
            return
        }

        val order   = orderingFor(section)
        val index   = slotId - sectionStart(section)
        val ability = order.getOrNull(index) ?: return
        val data    = HMsSavedData.get(serverPlayer.server)
        if (!data.isLearned(serverPlayer.uuid, ability.ordinal)) {
            serverPlayer.displayClientMessage(Component.literal("§cYou haven't learned §f${ability.displayName}§c yet!"), true)
            return
        }

        when {
            // Left-click with an empty cursor → grab the HM to move it.
            action == ClickType.PICKUP && button == 0 && carried.isEmpty -> beginDrag(section, index)
            // Right-click (or shift-click) → use / toggle it.
            button == 1 || action == ClickType.QUICK_MOVE -> {
                if (section == "active") activate(ability) else toggle(ability)
            }
        }
    }

    private fun beginDrag(section: String, index: Int) {
        dragFrom = section to index
        val ability = orderingFor(section)[index]
        val cursor  = iconStack(ability)
        cursor.set(DataComponents.CUSTOM_NAME,
            Component.literal("§e✦ Moving §f${ability.displayName}§e — click a slot"))
        cursor.set(DataComponents.LORE, lore("§7Click any ${section} slot to drop it here"))
        setCarried(cursor)
        refresh(); broadcastChanges()
    }

    private fun endDrag() {
        dragFrom = null
        setCarried(ItemStack.EMPTY)
        refresh(); broadcastChanges()
    }

    private fun activate(ability: DittoAbility) {
        val alreadyActive = HMCaseItem.getActive(caseStack) == ability
        HMCaseItem.setActive(caseStack, if (alreadyActive) null else ability)
        serverPlayer.inventory.setChanged()
        refresh(); broadcastChanges()
        serverPlayer.displayClientMessage(
            Component.literal(if (!alreadyActive) "§aActive → §f${ability.displayName}" else "§7Deselected."), true,
        )
    }

    private fun toggle(ability: DittoAbility) {
        val enabled = HMsSavedData.get(serverPlayer.server).togglePassive(serverPlayer.uuid, ability.ordinal)
        refresh(); broadcastChanges()
        serverPlayer.displayClientMessage(
            Component.literal(if (enabled) "§a${ability.displayName} ON" else "§7${ability.displayName} OFF"), true,
        )
    }

    private fun sectionOf(slotId: Int): String? = when (slotId) {
        in ACTIVE_START..ACTIVE_END -> "active"
        in TOGGLE_START..TOGGLE_END -> "toggle"
        else -> null
    }

    private fun sectionStart(section: String) = if (section == "active") ACTIVE_START else TOGGLE_START

    private fun orderingFor(section: String) = if (section == "active") activeOrdering else toggleOrdering

    override fun quickMoveStack(player: Player, index: Int) = ItemStack.EMPTY
    override fun stillValid(player: Player) = true

    override fun removed(player: Player) {
        // Never drop the cosmetic move-icon if the GUI is closed mid-drag.
        if (dragFrom != null) { dragFrom = null; setCarried(ItemStack.EMPTY) }
        super.removed(player)
    }

    // ── Slot builders ─────────────────────────────────────────────────────────

    /** The slot icon is the actual HM disc (custom TM-disc art + trigger overlay). */
    private fun iconStack(ability: DittoAbility): ItemStack =
        ItemStack(HMItems.forAbility(ability) ?: ability.triggerItem())

    private fun makeActiveSlot(ability: DittoAbility, isActive: Boolean, isDragged: Boolean): ItemStack {
        val stack = iconStack(ability)
        stack.set(DataComponents.CUSTOM_NAME,
            when {
                isDragged  -> Component.literal("§e✦ §f${ability.displayName} §e[moving…]")
                isActive   -> Component.literal("§e▶ §f${ability.displayName} §7[active]")
                else       -> Component.literal("§f${ability.displayName}")
            }
        )
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, isActive || isDragged)
        // The disc's own tooltip already shows the description + hunger/cooldown.
        stack.set(DataComponents.LORE, lore(
            if (isActive) "§7Right-click to deselect  §8|  §eLeft-drag to move"
            else          "§eRight-click to set active  §8|  §eLeft-drag to move"
        ))
        return stack
    }

    private fun makeToggleSlot(ability: DittoAbility, enabled: Boolean, isDragged: Boolean): ItemStack {
        val stack = iconStack(ability)
        stack.set(DataComponents.CUSTOM_NAME,
            when {
                isDragged -> Component.literal("§e✦ §f${ability.displayName} §e[moving…]")
                enabled   -> Component.literal("§a✔ ${ability.displayName} §2§l[ON]")
                else      -> Component.literal("§7${ability.displayName} §8[OFF]")
            }
        )
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, enabled || isDragged)
        val cfg   = HMsConfig.get(ability)
        val lines = mutableListOf<String>()
        if (cfg.hungerBlock > 0) lines += "§6Hunger blocked: §f${cfg.hungerBlock}"
        lines += "§7Right-click to ${if (enabled) "§cdisable" else "§aenable"}  §8|  §eLeft-drag to move"
        stack.set(DataComponents.LORE, lore(*lines.toTypedArray()))
        return stack
    }

    private fun notLearnedGlass(ability: DittoAbility): ItemStack {
        val stack = ItemStack(Items.GRAY_STAINED_GLASS_PANE)
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("§8${ability.displayName}"))
        stack.set(DataComponents.LORE, lore(
            "§8Not yet learned",
            "§8Explore to discover how to unlock it!",
        ))
        return stack
    }

    private fun lore(vararg lines: String) =
        ItemLore(lines.map { Component.literal(it) as Component })
}
