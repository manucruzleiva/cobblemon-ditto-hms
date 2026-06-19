package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks

object StockpileWaterHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val center = player.blockPosition()

        for (dx in -3..3) for (dz in -3..3) {
            for (dy in -1..0) {
                val pos = center.offset(dx, dy, dz)
                val state = level.getBlockState(pos)
                val below = level.getBlockState(pos.below())

                if (state.isAir && !below.isAir) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3)
                }
            }
        }
        return true
    }
}
