package com.shier.dittohms.neoforge

import com.shier.dittohms.DittoHMsCommon
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(DittoHMsCommon.MOD_ID)
class DittoHMsNeoForge(modBus: IEventBus, modContainer: ModContainer) {
    init {
        DittoHMsCommon.init()
    }
}
