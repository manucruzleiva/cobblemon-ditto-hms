package com.shier.dittohms.ability.handlers

import com.shier.dittohms.ability.DittoAbility
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

/**
 * Central particle + sound feedback for every HM. Keeps the individual handlers
 * focused on mechanics — anything cosmetic that should fire on *use* (active) or
 * *toggle on/off* (passive) lives here.
 */
object AbilityFx {

    /** Burst of feedback when an active HM is used (or a passive is fired from a disc). */
    fun play(player: ServerPlayer, ability: DittoAbility) {
        val level = player.serverLevel()
        val px = player.x
        val py = player.y + 1.0
        val pz = player.z
        level.sendParticles(particleFor(ability), px, py, pz, 24, 0.5, 0.7, 0.5, 0.05)
        // A little ring at the player's feet too, so it reads from a distance.
        level.sendParticles(particleFor(ability), px, player.y + 0.1, pz, 12, 0.6, 0.1, 0.6, 0.02)
        playSoundFor(level, ability, px, player.y, pz)
    }

    /** Feedback when a toggle HM is switched on or off. */
    fun playToggle(player: ServerPlayer, ability: DittoAbility, enabled: Boolean) {
        val level = player.serverLevel()
        val px = player.x
        val py = player.y + 1.0
        val pz = player.z
        if (enabled) {
            level.sendParticles(particleFor(ability), px, py, pz, 30, 0.5, 0.9, 0.5, 0.06)
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 14, 0.5, 0.9, 0.5, 0.02)
            level.playSound(null, px, player.y, pz, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 1.5f)
        } else {
            level.sendParticles(ParticleTypes.SMOKE, px, py, pz, 20, 0.4, 0.6, 0.4, 0.02)
            level.playSound(null, px, player.y, pz, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6f, 0.8f)
        }
    }

    private fun particleFor(a: DittoAbility): ParticleOptions = when (a) {
        DittoAbility.WATER_GUN, DittoAbility.WATERFALL, DittoAbility.RAIN_DANCE,
        DittoAbility.SURF, DittoAbility.DIVE        -> ParticleTypes.SPLASH
        DittoAbility.LEAFAGE, DittoAbility.ROTOTILLER,
        DittoAbility.BULLET_SEED                    -> ParticleTypes.HAPPY_VILLAGER
        DittoAbility.CUT, DittoAbility.ROCK_SMASH,
        DittoAbility.DIG, DittoAbility.ROCK_CLIMB,
        DittoAbility.ROLLOUT                        -> ParticleTypes.CRIT
        DittoAbility.STRENGTH, DittoAbility.CRAB_HAMMER,
        DittoAbility.HARDEN                         -> ParticleTypes.ENCHANTED_HIT
        DittoAbility.CAMOUFLAGE, DittoAbility.MEAN_LOOK -> ParticleTypes.WITCH
        DittoAbility.MAGNET_RISE, DittoAbility.THUNDER -> ParticleTypes.ELECTRIC_SPARK
        DittoAbility.EMBER, DittoAbility.EXPLOSION,
        DittoAbility.BURNING_BULWARK               -> ParticleTypes.FLAME
        DittoAbility.TELEPORT                       -> ParticleTypes.PORTAL
        DittoAbility.FLY, DittoAbility.GLIDE,
        DittoAbility.DEFOG, DittoAbility.JUMP       -> ParticleTypes.CLOUD
        DittoAbility.SUNNY_DAY, DittoAbility.FLASH  -> ParticleTypes.END_ROD
        DittoAbility.REST, DittoAbility.CHARM       -> ParticleTypes.HEART
        DittoAbility.REVIVAL_BLESSING              -> ParticleTypes.TOTEM_OF_UNDYING
        DittoAbility.STRING_SHOT                    -> ParticleTypes.ITEM_SLIME
    }

    // Each branch calls playSound directly so the correct overload is resolved per
    // constant (the SoundEvents fields are a mix of SoundEvent / Holder<SoundEvent>).
    private fun playSoundFor(level: ServerLevel, a: DittoAbility, x: Double, y: Double, z: Double) {
        val src = SoundSource.PLAYERS
        when (a) {
            DittoAbility.WATER_GUN, DittoAbility.WATERFALL, DittoAbility.SURF, DittoAbility.DIVE
                                          -> level.playSound(null, x, y, z, SoundEvents.PLAYER_SPLASH, src, 0.9f, 1.0f)
            DittoAbility.RAIN_DANCE       -> level.playSound(null, x, y, z, SoundEvents.WEATHER_RAIN, src, 0.9f, 1.0f)
            DittoAbility.LEAFAGE, DittoAbility.ROTOTILLER, DittoAbility.BULLET_SEED
                                          -> level.playSound(null, x, y, z, SoundEvents.GRASS_PLACE, src, 0.9f, 1.0f)
            DittoAbility.CUT, DittoAbility.STRENGTH, DittoAbility.CRAB_HAMMER
                                          -> level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_SWEEP, src, 0.9f, 1.0f)
            DittoAbility.ROCK_SMASH, DittoAbility.DIG
                                          -> level.playSound(null, x, y, z, SoundEvents.STONE_BREAK, src, 0.9f, 1.0f)
            DittoAbility.CAMOUFLAGE, DittoAbility.MEAN_LOOK
                                          -> level.playSound(null, x, y, z, SoundEvents.ILLUSIONER_CAST_SPELL, src, 0.9f, 1.0f)
            DittoAbility.MAGNET_RISE      -> level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, src, 0.9f, 1.6f)
            DittoAbility.THUNDER          -> level.playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER, src, 0.9f, 1.0f)
            DittoAbility.EMBER            -> level.playSound(null, x, y, z, SoundEvents.FLINTANDSTEEL_USE, src, 0.9f, 1.0f)
            DittoAbility.EXPLOSION        -> level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, src, 0.9f, 1.0f)
            DittoAbility.TELEPORT         -> level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, src, 0.9f, 1.0f)
            DittoAbility.FLY              -> level.playSound(null, x, y, z, SoundEvents.FIREWORK_ROCKET_LAUNCH, src, 0.9f, 1.0f)
            DittoAbility.SUNNY_DAY        -> level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, src, 0.9f, 1.0f)
            DittoAbility.REST, DittoAbility.CHARM, DittoAbility.REVIVAL_BLESSING
                                          -> level.playSound(null, x, y, z, SoundEvents.PLAYER_LEVELUP, src, 0.9f, 1.0f)
            DittoAbility.DEFOG            -> level.playSound(null, x, y, z, SoundEvents.PHANTOM_FLAP, src, 0.9f, 1.0f)
            DittoAbility.STRING_SHOT      -> level.playSound(null, x, y, z, SoundEvents.SLIME_BLOCK_PLACE, src, 0.9f, 1.0f)
            else                          -> level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, src, 0.9f, 1.0f)
        }
    }
}
