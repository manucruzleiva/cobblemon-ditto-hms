package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.BaseFireBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.TntBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object EmberHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level   = player.serverLevel()
        val hit     = player.pick(5.0, 1.0f, false)
        var ignited = false

        if (hit.type == HitResult.Type.BLOCK) {
            val blockHit    = hit as BlockHitResult
            val targetPos   = blockHit.blockPos
            val targetState = level.getBlockState(targetPos)
            val firePos     = targetPos.relative(blockHit.direction)

            when {
                targetState.block is TntBlock -> {
                    // Use static explode helper to prime TNT
                    TntBlock.explode(level, targetPos)
                    level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 11)
                    ignited = true
                }
                BaseFireBlock.canBePlacedAt(level, firePos, blockHit.direction) -> {
                    level.setBlock(firePos, BaseFireBlock.getState(level, firePos), 11)
                    level.playSound(null, firePos, SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.BLOCKS, 1.0f, level.random.nextFloat() * 0.4f + 0.8f)
                    ignited = true
                }
            }
        }

        // Ignite entities in front of the player
        val look = player.lookAngle
        level.getEntities(player,
            player.boundingBox.expandTowards(look.scale(4.0)).inflate(0.5)
        ) { it.isAlive && it != player }.forEach { entity ->
            val toEnt = entity.position().subtract(player.position()).normalize()
            if (toEnt.dot(look) > 0.85) {
                entity.igniteForTicks(100) // 5 seconds
                ignited = true
            }
        }

        return ignited
    }
}
