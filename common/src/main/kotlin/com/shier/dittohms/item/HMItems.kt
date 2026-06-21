package com.shier.dittohms.item

import com.shier.dittohms.ability.DittoAbility
import net.minecraft.world.item.Item

/** Platform-agnostic registry. Populated by Fabric/NeoForge loaders at startup. */
object HMItems {
    val BY_ABILITY: MutableMap<DittoAbility, HMDiscItem> = LinkedHashMap()
    var HM_CASE: HMCaseItem? = null

    fun register(ability: DittoAbility, item: HMDiscItem) { BY_ABILITY[ability] = item }
    fun registerCase(item: HMCaseItem) { HM_CASE = item }

    fun forAbility(ability: DittoAbility): HMDiscItem? = BY_ABILITY[ability]
    fun findAbility(item: Item): DittoAbility? = BY_ABILITY.entries.find { it.value == item }?.key
}
