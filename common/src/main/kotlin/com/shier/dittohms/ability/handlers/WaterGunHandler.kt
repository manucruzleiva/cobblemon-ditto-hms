package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object WaterGunHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val radius = HMsConfig.get(DittoAbility.WATER_GUN).power
        val origin = player.blockPosition()

        // Extinguish fire in radius
        var extinguished = 0
        for (x in -radius..radius) for (y in -radius..radius) for (z in -radius..radius) {
            val pos = origin.offset(x, y, z)
            val state = level.getBlockState(pos)
            if (state.`is`(Blocks.FIRE) || state.`is`(Blocks.SOUL_FIRE)) {
                level.removeBlock(pos, false)
                extinguished++
            }
        }

        val hit = player.pick(6.0, 1.0f, true)
        if (hit.type != HitResult.Type.MISS) {
            val blockHit = hit as? BlockHitResult
            if (blockHit != null) {
                val targetState = level.getBlockState(blockHit.blockPos)

                // Wet sponge
                if (targetState.`is`(Blocks.SPONGE)) {
                    level.setBlock(blockHit.blockPos, Blocks.WET_SPONGE.defaultBlockState(), 3)
                    player.sendSystemMessage(Component.literal("§bSponge soaked!"))
                    return true
                }

                // Place water in adjacent air
                val placePos = blockHit.blockPos.relative(blockHit.direction)
                if (level.getBlockState(placePos).isAir) {
                    level.setBlock(placePos, Blocks.WATER.defaultBlockState(), 3)
                }
            }
        }

        if (extinguished > 0) player.sendSystemMessage(Component.literal("§bExtinguished $extinguished fire block(s)."))
        return true
    }
}
