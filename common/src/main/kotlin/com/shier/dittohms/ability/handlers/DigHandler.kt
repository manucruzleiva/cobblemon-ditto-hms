package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object DigHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()

        // Haste III — netherite shovel tier speed for fast digging
        player.addEffect(MobEffectInstance(MobEffects.DIG_SPEED, 300, 2))

        val hit = player.pick(5.0, 1.0f, false)
        if (hit.type == HitResult.Type.MISS) return true
        val blockHit = hit as? BlockHitResult ?: return true

        val pos   = blockHit.blockPos
        val state = level.getBlockState(pos)
        if (state.isAir) return true

        if (state.`is`(BlockTags.MINEABLE_WITH_SHOVEL)) {
            val shovel = ItemStack(Items.NETHERITE_SHOVEL)
            Block.dropResources(state, level, pos, null, player, shovel)
            level.removeBlock(pos, false)
            level.levelEvent(2001, pos, Block.getId(state))
        }
        return true
    }
}
