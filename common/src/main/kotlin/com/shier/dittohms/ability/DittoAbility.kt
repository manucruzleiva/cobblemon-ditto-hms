package com.shier.dittohms.ability

enum class DittoAbility(
    val id: String,
    val displayName: String,
    val unlockSpecies: String,
    val hungerCost: Int,
    val cooldownTicks: Int,
) {
    WATER_GUN("water_gun", "Water Gun", "squirtle", 1, 40),
    LEAFAGE("leafage", "Leafage", "bulbasaur", 1, 40),
    CUT("cut", "Cut", "scyther", 2, 60),
    ROCK_SMASH("rock_smash", "Rock Smash", "hitmonchan", 2, 100),
    ROTOTILLER("rototiller", "Rototiller", "drilbur", 1, 40),
    JUMP("jump", "Jump", "magikarp", 2, 120),
    SURF("surf", "Surf", "lapras", 3, 120),
    CAMOUFLAGE("camouflage", "Camouflage", "zorua", 3, 200),
    STOCKPILE_WATER("stockpile_water", "Stockpile Water", "wooper", 2, 60),
    STRENGTH("strength", "Strength", "machoke", 2, 120),
    ROLLOUT("rollout", "Rollout", "graveler", 3, 160),
    GLIDE("glide", "Glide", "dragonite", 2, 120),
    WATERFALL("waterfall", "Waterfall", "gyarados", 2, 120),
    MAGNET_RISE("magnet_rise", "Magnet Rise", "magnemite", 3, 160),
    DIVE("dive", "Dive", "vaporeon", 3, 120);

    companion object {
        fun fromId(id: String): DittoAbility? = entries.find { it.id == id }
    }
}
