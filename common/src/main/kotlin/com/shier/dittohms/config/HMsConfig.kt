package com.shier.dittohms.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.shier.dittohms.DittoHMs
import com.shier.dittohms.ability.DittoAbility
import java.io.File

object HMsConfig {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    var configFile: File = File("config/cobblemon_ditto_hms.json")

    data class AbilityConfig(
        var hungerCost: Int,
        var cooldownTicks: Int,
        var power: Int,
        /** Toggles only: how many food points are blocked from the player's max hunger (default 2). */
        var hungerBlock: Int = 2,
    )

    private val defaults: Map<DittoAbility, AbilityConfig> = mapOf(
        // Active HMs (hungerBlock defaults to 0 for non-passive)
        DittoAbility.WATER_GUN        to AbilityConfig(1, 40,   3,  0),
        DittoAbility.LEAFAGE          to AbilityConfig(1, 40,   5,  0),
        DittoAbility.CUT              to AbilityConfig(2, 0,    128,0),
        DittoAbility.ROCK_SMASH       to AbilityConfig(2, 0,    32, 0),
        DittoAbility.ROTOTILLER       to AbilityConfig(1, 0,    1,  0),
        DittoAbility.CAMOUFLAGE       to AbilityConfig(3, 200,  6000,0), // power = duration (5 min)
        DittoAbility.STRENGTH         to AbilityConfig(2, 0,    1,  0),
        DittoAbility.WATERFALL        to AbilityConfig(2, 120,  40, 0),
        DittoAbility.EMBER            to AbilityConfig(1, 20,   5,  0),
        DittoAbility.BULLET_SEED      to AbilityConfig(2, 20,   5,  0),
        DittoAbility.TELEPORT         to AbilityConfig(5, 100,  30, 0),
        DittoAbility.FLY              to AbilityConfig(3, 20,   1200,0),
        DittoAbility.RAIN_DANCE       to AbilityConfig(5, 200,  6000,0),
        DittoAbility.SUNNY_DAY        to AbilityConfig(5, 200,  6000,0),
        DittoAbility.REST             to AbilityConfig(0, 12000,0,  0),
        DittoAbility.DIG              to AbilityConfig(2, 0,    1,  0),
        DittoAbility.EXPLOSION        to AbilityConfig(15,200,  4,  0),
        DittoAbility.THUNDER          to AbilityConfig(3, 100,  3,  0),
        DittoAbility.STRING_SHOT      to AbilityConfig(1, 40,   1,  0),
        DittoAbility.DEFOG            to AbilityConfig(2, 100,  10, 0),
        DittoAbility.CRAB_HAMMER      to AbilityConfig(3, 40,   1,  0),
        DittoAbility.REVIVAL_BLESSING to AbilityConfig(1, 24000,0,   0),
        DittoAbility.CHARM            to AbilityConfig(2, 60,   2400,0), // power = follow duration in ticks (2 min)
        DittoAbility.MAGNET_RISE      to AbilityConfig(2, 200,  100, 0), // now active: power = effect duration (5s)
        // Toggle HMs (hungerBlock = food points blocked from max)
        DittoAbility.JUMP             to AbilityConfig(0, 0,    4,  2), // power = Jump Boost level (4 = IV)
        DittoAbility.SURF             to AbilityConfig(0, 0,    0,  2),
        DittoAbility.ROLLOUT          to AbilityConfig(0, 0,    0,  2),
        DittoAbility.DIVE             to AbilityConfig(0, 0,    0,  2),
        DittoAbility.FLASH            to AbilityConfig(0, 0,    0,  2),
        DittoAbility.ROCK_CLIMB       to AbilityConfig(0, 0,    0,  2),
        DittoAbility.MEAN_LOOK        to AbilityConfig(0, 0,    30, 2), // power = repel radius
        DittoAbility.HARDEN           to AbilityConfig(0, 0,    0,  3),
        DittoAbility.GLIDE            to AbilityConfig(0, 0,    0,  2),
        DittoAbility.BURNING_BULWARK  to AbilityConfig(0, 0,    2,  4), // power = thorns damage; hungerBlock 4
    )

    private val entries: MutableMap<DittoAbility, AbilityConfig> = mutableMapOf()

    fun init() {
        defaults.forEach { (a, cfg) -> entries[a] = cfg.copy() }
        load()
    }

    fun get(ability: DittoAbility): AbilityConfig = entries[ability] ?: defaults[ability]!!.copy()

    fun set(ability: DittoAbility, hungerCost: Int? = null, cooldownTicks: Int? = null, power: Int? = null, hungerBlock: Int? = null) {
        val cfg = entries.getOrPut(ability) { defaults[ability]!!.copy() }
        hungerCost?.let    { cfg.hungerCost    = it.coerceIn(0, 20) }
        cooldownTicks?.let { cfg.cooldownTicks = it.coerceIn(0, 24000) }
        power?.let         { cfg.power         = it.coerceIn(0, 512) }
        hungerBlock?.let   { cfg.hungerBlock   = it.coerceIn(0, 20) }
        save()
    }

    fun reset(ability: DittoAbility) {
        entries[ability] = defaults[ability]!!.copy()
        save()
    }

    fun resetAll() {
        defaults.forEach { (a, cfg) -> entries[a] = cfg.copy() }
        save()
    }

    fun load() {
        if (!configFile.exists()) { save(); return }
        try {
            val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
            val raw = GSON.fromJson<Map<String, Map<String, Double>>>(configFile.readText(), type) ?: return
            raw.forEach { (id, values) ->
                val ability = DittoAbility.fromId(id) ?: return@forEach
                val cfg = entries.getOrPut(ability) { defaults[ability]!!.copy() }
                values["hungerCost"]?.toInt()?.let    { cfg.hungerCost    = it }
                values["cooldownTicks"]?.toInt()?.let { cfg.cooldownTicks = it }
                values["power"]?.toInt()?.let         { cfg.power         = it }
                values["hungerBlock"]?.toInt()?.let   { cfg.hungerBlock   = it }
            }
        } catch (e: Exception) {
            DittoHMs.LOGGER.warn("[DittoHMs] Failed to load config: ${e.message}")
        }
    }

    fun save() {
        configFile.parentFile?.mkdirs()
        val out = entries.mapKeys { it.key.id }.mapValues { (_, cfg) ->
            mapOf("hungerCost" to cfg.hungerCost, "cooldownTicks" to cfg.cooldownTicks, "power" to cfg.power, "hungerBlock" to cfg.hungerBlock)
        }
        configFile.writeText(GSON.toJson(out))
    }
}
