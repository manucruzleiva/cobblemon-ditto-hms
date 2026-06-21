package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.config.HMsConfig
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.phys.Vec3

object ThunderHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level  = player.serverLevel()
        val cfg    = HMsConfig.get(DittoAbility.THUNDER)
        val count  = cfg.power.coerceAtLeast(1).coerceAtMost(10)

        // Strike lightning bolts around the player (targets nearby enemies)
        val origin = player.position()
        repeat(count) { i ->
            val angle  = (i.toDouble() / count) * Math.PI * 2.0
            val radius = if (count == 1) 0.0 else 3.0
            val boltPos = Vec3(
                origin.x + Math.cos(angle) * radius,
                origin.y,
                origin.z + Math.sin(angle) * radius,
            )
            val bolt = LightningBolt(EntityType.LIGHTNING_BOLT, level)
            bolt.setPos(boltPos.x, boltPos.y, boltPos.z)
            bolt.setCause(player)
            level.addFreshEntity(bolt)
        }
        return true
    }
}
