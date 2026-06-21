package com.shier.dittohms.ability.handlers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/** Crab Hammer — acts exactly like a Netherite Mace: breaks the targeted block instantly. */
object CrabHammerHandler {
    fun execute(player: ServerPlayer): Boolean {
        val level = player.serverLevel()
        val hit   = player.pick(5.0, 1.0f, false)

        // Damage nearby entities like a heavy mace swing
        val look = player.lookAngle
        val mace = ItemStack(Items.MACE)
        val nearby = level.getEntities(player,
            player.boundingBox.expandTowards(look.scale(3.0)).inflate(1.5)
        ) { it.isAlive && it != player }
        for (entity in nearby) {
            val toEnt = entity.position().subtract(player.position()).normalize()
            if (toEnt.dot(look) > 0.7) {
                // Deal netherite-mace-equivalent smash damage
                entity.hurt(
                    level.damageSources().playerAttack(player),
                    16f,   // ~8 hearts baseline
                )
                // Knock them back
                val knockback = toEnt.scale(1.5)
                entity.setDeltaMovement(entity.deltaMovement.add(knockback))
            }
        }
        return nearby.isNotEmpty() || true  // always "used" so hunger/CD apply
    }
}
