package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object RototillerHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val hit   = player.pick(4.0, 1.0f, false)

        val target = when {
            hit.type == HitResult.Type.BLOCK -> (hit as BlockHitResult).blockPos
            else -> player.blockPosition().below() // fallback: block below player
        }

        val state = level.getBlockState(target)
        val above = level.getBlockState(target.above())
        val tillable = state.`is`(Blocks.DIRT) || state.`is`(Blocks.GRASS_BLOCK) ||
            state.`is`(Blocks.COARSE_DIRT) || state.`is`(Blocks.DIRT_PATH)

        if (tillable && above.isAir) {
            level.setBlock(target, Blocks.FARMLAND.defaultBlockState(), 3)
            return true
        }
        return false
    }
}
