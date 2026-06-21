package com.shier.dittohms.ability.handlers

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object StrengthHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level   = player.serverLevel()
        val hit     = player.pick(5.0, 1.0f, false)
        if (hit.type == HitResult.Type.MISS) return false
        val blockHit = hit as? BlockHitResult ?: return false

        val pos   = blockHit.blockPos
        val state = level.getBlockState(pos)

        if (state.isAir) return false
        if (!state.fluidState.isEmpty) {
            player.sendSystemMessage(Component.literal("§cCan't move fluids."))
            return false
        }

        val dir    = player.direction
        val newPos = pos.relative(dir)
        val target = level.getBlockState(newPos)
        if (!target.canBeReplaced()) {
            player.sendSystemMessage(Component.literal("§cNo space to push the block."))
            return false
        }

        // Save block entity NBT before removing (allows moving chests, barrels, etc.)
        val blockEntity: BlockEntity? = level.getBlockEntity(pos)
        val savedNbt = blockEntity?.saveWithFullMetadata(level.registryAccess())

        // Move block
        level.setBlock(newPos, state, 3)
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
        level.levelEvent(2001, pos, Block.getId(state))

        // Restore block entity data at new position if present
        if (savedNbt != null) {
            level.getBlockEntity(newPos)?.loadWithComponents(savedNbt, level.registryAccess())
        }

        return true
    }
}
