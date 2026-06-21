package com.shier.dittohms.storage

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

class HMsSavedData private constructor(
    private val learned: MutableMap<UUID, Int> = mutableMapOf(),
    private val enabled: MutableMap<UUID, Int> = mutableMapOf(),
) : SavedData() {

    fun isLearned(uuid: UUID, ordinal: Int): Boolean =
        learned.getOrDefault(uuid, 0) and (1 shl ordinal) != 0

    fun learn(uuid: UUID, ordinal: Int) {
        learned[uuid] = learned.getOrDefault(uuid, 0) or (1 shl ordinal)
        setDirty()
    }

    fun forget(uuid: UUID, ordinal: Int) {
        learned[uuid] = learned.getOrDefault(uuid, 0) and (1 shl ordinal).inv()
        setDirty()
    }

    fun isPassiveEnabled(uuid: UUID, ordinal: Int): Boolean =
        enabled.getOrDefault(uuid, 0) and (1 shl ordinal) != 0

    fun setPassiveEnabled(uuid: UUID, ordinal: Int, value: Boolean) {
        val current = enabled.getOrDefault(uuid, 0)
        enabled[uuid] = if (value) current or (1 shl ordinal) else current and (1 shl ordinal).inv()
        setDirty()
    }

    fun togglePassive(uuid: UUID, ordinal: Int): Boolean {
        val current = enabled.getOrDefault(uuid, 0)
        val toggled = current xor (1 shl ordinal)
        enabled[uuid] = toggled
        setDirty()
        return toggled and (1 shl ordinal) != 0
    }

    fun getLearnedOrdinals(uuid: UUID): Int = learned.getOrDefault(uuid, 0)

    override fun save(tag: CompoundTag, registries: net.minecraft.core.HolderLookup.Provider): CompoundTag {
        val learnedTag = CompoundTag()
        learned.forEach { (uuid, mask) -> learnedTag.putInt(uuid.toString(), mask) }
        tag.put("learned", learnedTag)

        val enabledTag = CompoundTag()
        enabled.forEach { (uuid, mask) -> enabledTag.putInt(uuid.toString(), mask) }
        tag.put("enabled", enabledTag)

        return tag
    }

    companion object {
        private fun load(tag: CompoundTag, provider: net.minecraft.core.HolderLookup.Provider): HMsSavedData {
            val learned = mutableMapOf<UUID, Int>()
            val enabled = mutableMapOf<UUID, Int>()

            tag.getCompound("learned").allKeys.forEach { key ->
                runCatching { UUID.fromString(key) }.getOrNull()?.let { uuid ->
                    learned[uuid] = tag.getCompound("learned").getInt(key)
                }
            }
            tag.getCompound("enabled").allKeys.forEach { key ->
                runCatching { UUID.fromString(key) }.getOrNull()?.let { uuid ->
                    enabled[uuid] = tag.getCompound("enabled").getInt(key)
                }
            }

            return HMsSavedData(learned, enabled)
        }

        private val FACTORY = SavedData.Factory(
            { HMsSavedData() },
            ::load,
            net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES,
        )

        fun get(server: MinecraftServer): HMsSavedData =
            server.overworld().dataStorage.computeIfAbsent(FACTORY, "ditto_hms")
    }
}
