package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object RockSmashHandler {
    private val ORE_TAGS = listOf(
        BlockTags.COAL_ORES, BlockTags.IRON_ORES, BlockTags.GOLD_ORES,
        BlockTags.DIAMOND_ORES, BlockTags.EMERALD_ORES, BlockTags.LAPIS_ORES,
        BlockTags.REDSTONE_ORES, BlockTags.COPPER_ORES,
    )

    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val hit   = player.pick(6.0, 1.0f, false)
        if (hit.type == HitResult.Type.MISS) return false
        val blockHit   = hit as? BlockHitResult ?: return false
        val startPos   = blockHit.blockPos
        val startState = level.getBlockState(startPos)
        if (!startState.`is`(BlockTags.MINEABLE_WITH_PICKAXE)) return false

        val pick  = ItemStack(Items.NETHERITE_PICKAXE)
        val isOre = ORE_TAGS.any { startState.`is`(it) }

        if (isOre) {
            val max  = HMsConfig.get(DittoAbility.ROCK_SMASH).power.coerceAtLeast(1).coerceAtMost(128)
            val vein = findConnectedOres(level, startPos, startState, max)
            for (pos in vein) breakBlock(level, pos, player, pick)
        } else {
            breakBlock(level, startPos, player, pick)
        }
        return true
    }

    private fun breakBlock(level: ServerLevel, pos: BlockPos, player: ServerPlayer, pick: ItemStack) {
        val state = level.getBlockState(pos)
        if (state.isAir) return
        Block.dropResources(state, level, pos, null, player, pick)
        level.removeBlock(pos, false)
        level.levelEvent(2001, pos, Block.getId(state))
    }

    private fun findConnectedOres(level: ServerLevel, start: BlockPos, startState: BlockState, max: Int): Set<BlockPos> {
        val targetBlock = startState.block
        val found = LinkedHashSet<BlockPos>()
        val queue = ArrayDeque<BlockPos>()
        queue.add(start)
        while (queue.isNotEmpty() && found.size < max) {
            val pos = queue.removeFirst()
            if (pos in found) continue
            if (level.getBlockState(pos).block != targetBlock) continue
            found.add(pos)
            for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                queue.add(pos.offset(dx, dy, dz))
            }
        }
        return found
    }
}
