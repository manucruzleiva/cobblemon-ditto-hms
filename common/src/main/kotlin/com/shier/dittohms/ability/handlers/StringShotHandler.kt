package com.shier.dittohms.ability.handlers

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.core.BlockPos

/** Places 2 cobweb blocks directly behind the player (foot + head level), decays after 1 second. */
object StringShotHandler {

    private val decaying = mutableListOf<Triple<BlockPos, ResourceKey<Level>, Long>>()

    fun execute(player: ServerPlayer): Boolean {
        val level  = player.serverLevel()
        val behind = player.blockPosition().relative(player.direction.opposite, 1)
        val now    = player.level().gameTime
        var placed = 0

        // Foot level
        for (dy in 0..1) {
            val pos   = behind.above(dy)
            val state = level.getBlockState(pos)
            if (state.isAir || state.canBeReplaced()) {
                level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3)
                decaying.add(Triple(pos, level.dimension(), now + 20L))
                placed++
            }
        }
        return placed > 0
    }

    fun tick(server: MinecraftServer) {
        val now  = server.overworld().gameTime
        val iter = decaying.iterator()
        while (iter.hasNext()) {
            val (pos, levelKey, expiry) = iter.next()
            if (now < expiry) continue
            val level = server.getLevel(levelKey) ?: run { iter.remove(); continue }
            if (level.getBlockState(pos).`is`(Blocks.COBWEB)) level.removeBlock(pos, false)
            iter.remove()
        }
    }
}
