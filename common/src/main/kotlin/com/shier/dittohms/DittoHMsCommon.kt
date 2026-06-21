package com.shier.dittohms

import com.shier.dittohms.config.HMsConfig

object DittoHMsCommon {
    const val MOD_ID = "cobblemon_ditto_hms"

    fun init() {
        HMsConfig.init()
        DittoHMs.LOGGER.info("Cobblemon Ditto HMs initialized!")
    }
}
