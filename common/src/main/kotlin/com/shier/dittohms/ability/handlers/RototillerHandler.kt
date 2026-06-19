package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks

object RototillerHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val center = player.blockPosition().below()

        for (dx in -1..1) for (dz in -1..1) {
            val pos = center.offset(dx, 0, dz)
            val state = level.getBlockState(pos)
            val aboveState = level.getBlockState(pos.above())

            val tillable = state.`is`(Blocks.DIRT) || state.`is`(Blocks.GRASS_BLOCK) ||
                state.`is`(Blocks.COARSE_DIRT) || state.`is`(Blocks.DIRT_PATH)

            if (tillable && aboveState.isAir) {
                level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 3)
            }
        }
        return true
    }
}
