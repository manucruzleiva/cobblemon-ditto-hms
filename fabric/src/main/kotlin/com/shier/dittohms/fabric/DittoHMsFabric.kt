package com.shier.dittohms.fabric

import com.shier.dittohms.DittoHMsCommon
import com.shier.dittohms.ability.AbilityManager
import com.shier.dittohms.ability.DittoAbility
import com.shier.dittohms.command.DittoHMsCommand
import com.shier.dittohms.config.HMsConfig
import com.shier.dittohms.effect.HMEffects
import com.shier.dittohms.effect.HMIndicatorEffect
import com.shier.dittohms.interaction.HMInteractionHandler
import com.shier.dittohms.item.HMCaseItem
import com.shier.dittohms.item.HMDiscItem
import com.shier.dittohms.item.HMItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class DittoHMsFabric : ModInitializer {
    override fun onInitialize() {
        HMsConfig.configFile = FabricLoader.getInstance().configDir.resolve("cobblemon_ditto_hms.json").toFile()
        DittoHMsCommon.init()

        // Register HM Case
        val hmCase = HMCaseItem(Item.Properties().stacksTo(1))
        Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(DittoHMsCommon.MOD_ID, "hm_case"), hmCase)
        HMItems.registerCase(hmCase)

        // Register HM disc items
        for (ability in DittoAbility.entries) {
            val item = HMDiscItem(ability, Item.Properties().stacksTo(1))
            Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(DittoHMsCommon.MOD_ID, "hm_${ability.id}"),
                item,
            )
            HMItems.register(ability, item)
        }

        // Register per-toggle HUD indicator effects
        for (ability in DittoAbility.entries) {
            if (!ability.isPassive) continue
            val effect = HMIndicatorEffect(HMEffects.COLORS[ability] ?: 0xFFFFFF)
            val holder = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(DittoHMsCommon.MOD_ID, "hm_${ability.id}"),
                effect,
            )
            HMEffects.register(ability, holder)
        }

        // Creative tab using Fabric API's FabricItemGroup builder
        val tabKey = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(DittoHMsCommon.MOD_ID, "hm_abilities"),
        )
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            tabKey.location(),
            FabricItemGroup.builder()
                .title(Component.literal("Ditto HMs"))
                .icon { ItemStack(HMItems.forAbility(DittoAbility.WATER_GUN) ?: Items.BARRIER) }
                .build(),
        )
        // Populate items via Fabric's ItemGroupEvents (avoids SAM conversion issues)
        ItemGroupEvents.modifyEntriesEvent(tabKey).register { entries ->
            HMItems.HM_CASE?.let { entries.accept(ItemStack(it)) }
            DittoAbility.entries.forEach { ability ->
                HMItems.forAbility(ability)?.let { entries.accept(ItemStack(it)) }
            }
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            DittoHMsCommand.register(dispatcher)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            AbilityManager.tick(server)
        }

        UseEntityCallback.EVENT.register { player, world, hand, entity, _ ->
            if (!world.isClientSide && player.isShiftKeyDown &&
                HMInteractionHandler.onEntityUse(player, world, hand, entity)
            ) InteractionResult.SUCCESS else InteractionResult.PASS
        }
    }
}
