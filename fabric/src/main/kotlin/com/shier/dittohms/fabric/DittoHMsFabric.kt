package com.shier.dittohms.fabric

import com.shier.dittohms.DittoHMsCommon
import net.fabricmc.api.ModInitializer

class DittoHMsFabric : ModInitializer {
    override fun onInitialize() {
        DittoHMsCommon.init()
    }
}
