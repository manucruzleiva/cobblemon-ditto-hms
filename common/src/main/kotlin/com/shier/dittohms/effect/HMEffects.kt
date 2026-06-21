package com.shier.dittohms.effect

import com.shier.dittohms.ability.DittoAbility
import net.minecraft.core.Holder
import net.minecraft.world.effect.MobEffect

/**
 * Platform-agnostic registry of the per-toggle HUD indicator effects. Each toggle HM
 * gets its own registered [HMIndicatorEffect] (id `hm_<ability>`), so it appears as a
 * distinct icon in the player's status-effect bar while enabled.
 *
 * Loaders populate [BY_TOGGLE] at startup. Effect icon textures live in
 * `assets/cobblemon_ditto_hms/textures/mob_effect/hm_<ability>.png`.
 */
object HMEffects {

    /** Distinct tint per toggle (matches the icon textures). */
    val COLORS: Map<DittoAbility, Int> = mapOf(
        DittoAbility.JUMP            to 0x4CAF50,
        DittoAbility.SURF            to 0x29B6F6,
        DittoAbility.ROLLOUT        to 0x787882,
        DittoAbility.DIVE            to 0x0089A7,
        DittoAbility.FLASH           to 0xFFCA28,
        DittoAbility.ROCK_CLIMB      to 0x8D6E46,
        DittoAbility.MEAN_LOOK       to 0x8E44AD,
        DittoAbility.HARDEN          to 0x78C8DC,
        DittoAbility.GLIDE           to 0xECEFF1,
        DittoAbility.BURNING_BULWARK to 0xE65A1E,
    )

    val BY_TOGGLE: MutableMap<DittoAbility, Holder<MobEffect>> = LinkedHashMap()

    fun register(ability: DittoAbility, holder: Holder<MobEffect>) { BY_TOGGLE[ability] = holder }

    fun get(ability: DittoAbility): Holder<MobEffect>? = BY_TOGGLE[ability]
}
