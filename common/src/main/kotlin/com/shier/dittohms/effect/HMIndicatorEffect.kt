package com.shier.dittohms.effect

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory

/**
 * A do-nothing status effect used purely as a HUD indicator: while a toggle HM is
 * enabled, the player carries the matching effect so its icon shows in the status
 * bar, making it obvious at a glance which HM toggles are on. The real gameplay
 * effects are applied separately (with their own icons hidden).
 */
class HMIndicatorEffect(color: Int) : MobEffect(MobEffectCategory.BENEFICIAL, color)
