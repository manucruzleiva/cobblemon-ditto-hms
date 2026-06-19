package com.shier.dittohms.ability.handlers

import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags

object CutHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val dir = player.direction
        val perp = if (dir == Direction.NORTH || dir == Direction.SOUTH) Direction.EAST else Direction.NORTH

        val origin = player.blockPosition()
        for (forward in 1..4) {
            for (side in -1..1) {
                for (up in 0..3) {
                    val target = origin.relative(dir, forward).relative(perp, side).above(up)
                    val state = level.getBlockState(target)
                    if (state.`is`(BlockTags.LOGS) || state.`is`(BlockTags.LEAVES) ||
                        state.`is`(BlockTags.BAMBOO_BLOCKS) || state.`is`(BlockTags.REPLACEABLE_BY_TREES)
                    ) {
                        level.destroyBlock(target, true, player)
                    }
                }
            }
        }
        return true
    }
}
