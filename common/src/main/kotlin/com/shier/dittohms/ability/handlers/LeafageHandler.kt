package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.CropBlock

object LeafageHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level  = player.serverLevel()
        val origin = player.blockPosition()
        val radius = HMsConfig.get(DittoAbility.LEAFAGE).power
        var applied = 0

        for (x in -radius..radius) for (z in -radius..radius) {
            for (dy in 2 downTo -1) {
                val pos   = origin.offset(x, dy, z)
                val state = level.getBlockState(pos)
                val block = state.block

                // Only fully bonemeal actual crops (CropBlock subclasses).
                // Grass, saplings, flowers etc. are intentionally excluded.
                if (block is CropBlock && block.isValidBonemealTarget(level, pos, state)) {
                    var iterations = 0
                    while (iterations < 20) {
                        val current = level.getBlockState(pos)
                        if (!block.isValidBonemealTarget(level, pos, current)) break
                        block.performBonemeal(level, level.random, pos, current)
                        iterations++
                    }
                    if (iterations > 0) applied++
                    break
                }
            }
        }
        return applied > 0
    }
}
