package com.shier.dittohms.ability.handlers

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks

object WaterGunHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val dir = player.direction
        val anchor = player.blockPosition().relative(dir, 2)

        for (pos in listOf(anchor, anchor.north(), anchor.south(), anchor.east(), anchor.west())) {
            val state = level.getBlockState(pos)
            if (state.isAir) {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3)
            }
        }

        val center = player.blockPosition()
        for (dx in -3..3) for (dy in -1..3) for (dz in -3..3) {
            val firePos = center.offset(dx, dy, dz)
            val state = level.getBlockState(firePos)
            if (state.`is`(Blocks.FIRE) || state.`is`(Blocks.SOUL_FIRE)) {
                level.removeBlock(firePos, false)
            }
        }
        return true
    }
}
