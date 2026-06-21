package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.Shearable
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object CutHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level  = player.serverLevel()
        val maxBlocks = HMsConfig.get(DittoAbility.CUT).power.coerceAtLeast(1).coerceAtMost(256)
        var acted  = false

        // ── Block cutting (treecapitator / leaf & vine stripper) ───────────────
        val hit = player.pick(6.0, 1.0f, false)
        if (hit.type == HitResult.Type.BLOCK) {
            val blockHit   = hit as BlockHitResult
            val startState = level.getBlockState(blockHit.blockPos)
            val isLog  = startState.`is`(BlockTags.LOGS)
            val isLeaf = startState.`is`(BlockTags.LEAVES)
            val isVine = isVine(startState)

            if (isLog || isLeaf || isVine) {
                // When felling a tree, clear connected logs AND any leaves/vines tangled in it.
                val predicate: (BlockState) -> Boolean = when {
                    isLog  -> { s -> s.`is`(BlockTags.LOGS) || s.`is`(BlockTags.LEAVES) || isVine(s) }
                    isLeaf -> { s -> s.`is`(BlockTags.LEAVES) || isVine(s) }
                    else   -> { s -> isVine(s) }
                }
                val blocks = findConnected(level, blockHit.blockPos, maxBlocks, predicate)
                val tool   = if (isLog) ItemStack(Items.NETHERITE_AXE) else ItemStack(Items.SHEARS)
                for (pos in blocks) {
                    val state = level.getBlockState(pos)
                    if (state.isAir) continue
                    Block.dropResources(state, level, pos, null, player, tool)
                    level.removeBlock(pos, false)
                    level.levelEvent(2001, pos, Block.getId(state))
                }
                if (blocks.isNotEmpty()) acted = true
            }
        }

        // ── Shear nearby shearable entities ─────────────────────────────────────
        val box = player.boundingBox.expandTowards(player.lookAngle.scale(5.0)).inflate(2.0)
        val sheared = level.getEntities(player, box) { it is Shearable && it.readyForShearing() }
        for (entity in sheared) {
            (entity as Shearable).shear(SoundSource.PLAYERS)
            acted = true
        }

        return acted
    }

    /** Vines, cave vines, weeping/twisting vines and glow lichen — the "lianas" Cut should clear. */
    private fun isVine(state: BlockState): Boolean =
        state.`is`(BlockTags.CLIMBABLE) && !state.`is`(Blocks.LADDER) && !state.`is`(Blocks.SCAFFOLDING)

    private fun findConnected(
        level: ServerLevel,
        start: BlockPos,
        max: Int,
        matches: (BlockState) -> Boolean,
    ): Set<BlockPos> {
        val found = LinkedHashSet<BlockPos>()
        val queue = ArrayDeque<BlockPos>()
        queue.add(start)
        while (queue.isNotEmpty() && found.size < max) {
            val pos = queue.removeFirst()
            if (pos in found) continue
            if (!matches(level.getBlockState(pos))) continue
            found.add(pos)
            for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                queue.add(pos.offset(dx, dy, dz))
            }
        }
        return found
    }
}
