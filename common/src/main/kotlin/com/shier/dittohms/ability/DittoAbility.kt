package com.shier.dittohms.ability

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

enum class DittoAbility(
    val id: String,
    val displayName: String,
    val unlockPool: List<String>,
    val triggerItemSupplier: () -> Item,
    val isPassive: Boolean = false,
    val description: String = "",
) {
    // ── Active HMs ─────────────────────────────────────────────────────────────
    WATER_GUN    ("water_gun",    "Water Gun",      listOf("squirtle","totodile","mudkip"),              { Items.BLUE_DYE },
        description = "Sprays water ahead, placing water and extinguishing fire."),
    LEAFAGE      ("leafage",      "Leafage",         listOf("bulbasaur","chikorita","treecko","snivy"),   { Items.DANDELION },
        description = "Instantly grows every crop around you to full maturity — handy for fast harvests."),
    CUT          ("cut",          "Cut",             listOf("scyther","scizor","kartana"),                { Items.STICK },
        description = "Fells whole trees — logs, leaves and vines — and shears nearby mobs."),
    ROCK_SMASH   ("rock_smash",   "Rock Smash",      listOf("hitmonchan","machop","geodude"),             { Items.FLINT },
        description = "Instantly breaks the block you aim at, vein-mining entire ore veins."),
    ROTOTILLER   ("rototiller",   "Rototiller",      listOf("drilbur","excadrill"),                       { Items.COARSE_DIRT },
        description = "Tills the soil block you're aiming at into farmland."),
    CAMOUFLAGE   ("camouflage",   "Camouflage",      listOf("zorua","kecleon","ditto"),                   { Items.INK_SAC },
        description = "Morph into the block or entity you're looking at."),
    STRENGTH     ("strength",     "Strength",        listOf("machoke","machamp","conkeldurr"),            { Items.IRON_INGOT },
        description = "Pushes the block you're aiming at (carries chests/barrels with their contents)."),
    WATERFALL    ("waterfall",    "Waterfall",       listOf("gyarados","ludicolo","feraligatr"),          { Items.PRISMARINE_SHARD },
        description = "While in water, rides a strong upward current toward the surface."),
    MAGNET_RISE  ("magnet_rise",  "Magnet Rise",     listOf("magnemite","magneton","magnezone"),          { Items.IRON_NUGGET },
        description = "While in the air, keeps you floating for a few seconds — useful to cross gaps or break a fall."),
    EMBER        ("ember",        "Ember",           listOf("charmander","torchic","tepig"),              { Items.BLAZE_POWDER },
        description = "Ignites blocks and entities directly in front of you."),
    BULLET_SEED  ("bullet_seed",  "Bullet Seed",     listOf("seedot","nuzleaf","cacnea"),                { Items.WHEAT_SEEDS },
        description = "Fires a rapid barrage of seeds using your inventory."),
    TELEPORT     ("teleport",     "Teleport",        listOf("abra","kadabra","alakazam","ralts"),         { Items.ENDER_EYE },
        description = "Teleports you to the block in your crosshair."),
    FLY          ("fly",          "Fly",             listOf("pidgeot","charizard","dragonite"),           { Items.PHANTOM_MEMBRANE },
        description = "Launches you skyward and engages Glide (Glide must be learned)."),
    RAIN_DANCE   ("rain_dance",   "Rain Dance",      listOf("politoed","kyogre","golduck"),              { Items.PRISMARINE_CRYSTALS },
        description = "Summons rain for an extended period."),
    SUNNY_DAY    ("sunny_day",    "Sunny Day",       listOf("torkoal","sunkern","volcarona"),             { Items.SUNFLOWER },
        description = "Clears the weather, bringing bright sunshine."),
    REST         ("rest",         "Rest",            listOf("snorlax","komala","jigglypuff"),            { Items.APPLE },
        description = "Fully restores your health and skips the night (all players must agree in multiplayer)."),
    DIG          ("dig",          "Dig",             listOf("diglett","dugtrio","trapinch"),             { Items.DIAMOND_SHOVEL },
        description = "Lets you dig through dirt and sand rapidly, instantly clearing the block you aim at."),
    EXPLOSION    ("explosion",    "Explosion",       listOf("voltorb","electrode"),                       { Items.TNT },
        description = "Detonates a powerful explosion at your feet — you survive at 1 HP."),
    THUNDER      ("thunder",      "Thunder",         listOf("pikachu","raichu","zapdos"),                { Items.LIGHTNING_ROD },
        description = "Calls down lightning strikes all around you."),
    STRING_SHOT  ("string_shot",  "String Shot",     listOf("caterpie","spinarak","ariados"),            { Items.STRING },
        description = "Places a temporary web trap behind you."),
    DEFOG        ("defog",        "Defog",           listOf("togekiss","mantine","pelipper"),            { Items.GLASS },
        description = "Clears away any harmful conditions affecting you, leaving you fresh."),
    CRAB_HAMMER  ("crab_hammer",  "Crabhammer",      listOf("kingler","clawitzer"),                     { Items.MACE },
        description = "Delivers a crushing blow with massive knockback."),
    REVIVAL_BLESSING("revival_blessing","Revival Blessing",listOf("rabsca"),                           { Items.TOTEM_OF_UNDYING },
        description = "Fully heals and revives every Pokémon in your party (drains all your hunger)."),
    CHARM        ("charm",        "Charm",           listOf("jynx","sylveon","clefable"),               { Items.PINK_TULIP },
        description = "Charms an entity, making it follow you for a while."),

    // ── Toggle HMs ─────────────────────────────────────────────────────────────
    JUMP         ("jump",         "Jump",            listOf("magikarp","buneary","spoink"),             { Items.COD },              true,
        "Greatly increases your jump height."),
    SURF         ("surf",         "Surf",            listOf("lapras","kyogre","suicune"),               { Items.KELP },             true,
        "Lets you swim through water much faster."),
    ROLLOUT      ("rollout",      "Rollout",         listOf("graveler","golem","spheal"),               { Items.COBBLESTONE },      true,
        "Increases your movement speed."),
    DIVE         ("dive",         "Dive",            listOf("vaporeon","lapras","wailord"),             { Items.NAUTILUS_SHELL },   true,
        "Lets you breathe underwater and sink down to explore the depths."),
    FLASH        ("flash",        "Flash",           listOf("ampharos","jolteon","lanturn"),            { Items.GLOWSTONE_DUST },   true,
        "Lets you see clearly in the dark, even in caves and at night."),
    ROCK_CLIMB   ("rock_climb",   "Rock Climb",      listOf("rhydon","rhyperior","sneasel"),           { Items.CHAIN },            true,
        "Lets you scale any vertical wall."),
    MEAN_LOOK    ("mean_look",    "Mean Look",       listOf("mimikyu","gengar","noctowl"),              { Items.OMINOUS_BOTTLE },   true,
        "Continuously repels nearby hostile mobs (never Pokémon or friendly mobs)."),
    HARDEN       ("harden",       "Harden",          listOf("metapod","kakuna","silcoon","cascoon"),   { Items.SHIELD },           true,
        "Hardens your body to greatly reduce the damage you take."),
    GLIDE        ("glide",        "Glide",           listOf("dragonite","togekiss","aerodactyl"),      { Items.FEATHER },          true,
        "Lets you glide through the air after a jump or a fall."),
    BURNING_BULWARK("burning_bulwark","Burning Bulwark",listOf("gougingfire"),                          { Items.BLAZE_ROD },        true,
        "Makes you immune to fire and burns hostile mobs that get too close.");

    fun triggerItem(): Item = triggerItemSupplier()

    companion object {
        fun fromId(id: String): DittoAbility? = entries.find { it.id == id }
        fun fromTriggerAndSpecies(item: Item, species: String): DittoAbility? =
            entries.find { it.triggerItem() == item && species in it.unlockPool }
    }
}
