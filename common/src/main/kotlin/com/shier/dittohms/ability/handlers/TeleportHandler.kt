package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object TeleportHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level  = player.serverLevel()
        val cfg    = HMsConfig.get(DittoAbility.TELEPORT)
        val maxDist = cfg.power.coerceAtLeast(5).toDouble()

        val hit = player.pick(maxDist, 1.0f, false)

        val rawDest: Vec3 = when (hit.type) {
            HitResult.Type.BLOCK -> {
                val b = hit as BlockHitResult
                // Stand on top of the hit block face
                Vec3(b.blockPos.x + 0.5, b.blockPos.y + 1.0 + (if (b.direction.stepY < 0) -1 else 0), b.blockPos.z + 0.5)
            }
            HitResult.Type.MISS -> {
                // Max range in look direction
                val look = player.lookAngle
                Vec3(player.x + look.x * maxDist, player.y + look.y * maxDist, player.z + look.z * maxDist)
            }
            else -> return false
        }

        val safe = findSafe(level, rawDest) ?: run {
            player.sendSystemMessage(Component.literal("§cNo safe landing spot found!"))
            return false
        }

        // Sound at origin
        level.playSound(null, player.x, player.y, player.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f)
        player.teleportTo(safe.x, safe.y, safe.z)
        // Sound at destination
        level.playSound(null, safe.x, safe.y, safe.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f)

        // Extra hunger drain: ability manager applies hungerCost, but we also want the 1/4 total (5 food)
        // hungerCost is set to 5 in config, so AbilityManager handles it correctly via addExhaustion(5*4=20)
        return true
    }

    private fun findSafe(level: ServerLevel, target: Vec3): Vec3? {
        val baseX = target.x
        val baseZ = target.z
        for (dy in 0..5) {
            val y = target.y.toInt() + dy
            val pos = BlockPos.containing(baseX, y.toDouble(), baseZ)
            if (level.getBlockState(pos).isAir && level.getBlockState(pos.above()).isAir) {
                return Vec3(baseX, y.toDouble(), baseZ)
            }
        }
        // Try searching downward too
        for (dy in 1..5) {
            val y = target.y.toInt() - dy
            val pos = BlockPos.containing(baseX, y.toDouble(), baseZ)
            if (level.getBlockState(pos).isAir && level.getBlockState(pos.above()).isAir &&
                !level.getBlockState(pos.below()).isAir
            ) {
                return Vec3(baseX, y.toDouble(), baseZ)
            }
        }
        return null
    }
}
