package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.Blocks

object LeafageHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val center = player.blockPosition()

        for (dx in -2..2) for (dz in -2..2) {
            val ground = center.offset(dx, -1, dz)
            val groundState = level.getBlockState(ground)

            if (groundState.`is`(Blocks.DIRT) || groundState.`is`(Blocks.COARSE_DIRT) ||
                groundState.`is`(Blocks.ROOTED_DIRT)
            ) {
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 3)
            }

            val plantPos = center.offset(dx, 0, dz)
            val plantState = level.getBlockState(plantPos)
            val block = plantState.block
            if (block is BonemealableBlock &&
                block.isValidBonemealTarget(level, plantPos, plantState)
            ) {
                block.performBonemeal(level, level.random, plantPos, plantState)
            }
        }
        return true
    }
}
