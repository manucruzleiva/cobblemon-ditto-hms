package com.shier.dittohms.neoforge

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
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

@Mod(DittoHMsCommon.MOD_ID)
class DittoHMsNeoForge(modBus: IEventBus, modContainer: ModContainer) {

    private val itemReg: DeferredRegister<Item> =
        DeferredRegister.create(Registries.ITEM, DittoHMsCommon.MOD_ID)
    private val tabReg: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DittoHMsCommon.MOD_ID)
    private val effectReg: DeferredRegister<MobEffect> =
        DeferredRegister.create(Registries.MOB_EFFECT, DittoHMsCommon.MOD_ID)

    private val holders = mutableMapOf<DittoAbility, DeferredHolder<Item, HMDiscItem>>()
    private val caseHolder = itemReg.register("hm_case", Supplier { HMCaseItem(Item.Properties().stacksTo(1)) })

    init {
        HMsConfig.configFile = FMLPaths.CONFIGDIR.get().resolve("cobblemon_ditto_hms.json").toFile()
        DittoHMsCommon.init()

        // Register HM disc items
        for (ability in DittoAbility.entries) {
            val captured = ability
            val holder = itemReg.register(
                "hm_${ability.id}",
                Supplier { HMDiscItem(captured, Item.Properties().stacksTo(1)) },
            )
            @Suppress("UNCHECKED_CAST")
            holders[ability] = holder as DeferredHolder<Item, HMDiscItem>
        }
        itemReg.register(modBus)

        // Register per-toggle HUD indicator effects
        for (ability in DittoAbility.entries) {
            if (!ability.isPassive) continue
            val captured = ability
            val holder = effectReg.register(
                "hm_${ability.id}",
                Supplier { HMIndicatorEffect(HMEffects.COLORS[captured] ?: 0xFFFFFF) },
            )
            HMEffects.register(ability, holder)
        }
        effectReg.register(modBus)

        // Register creative tab
        tabReg.register("hm_abilities", Supplier {
            CreativeModeTab.builder()
                .title(Component.literal("Ditto HMs"))
                .icon { ItemStack(HMItems.forAbility(DittoAbility.WATER_GUN) ?: Items.BARRIER) }
                .displayItems { _, output ->
                    HMItems.HM_CASE?.let { output.accept(ItemStack(it)) }
                    DittoAbility.entries.forEach { ability ->
                        HMItems.forAbility(ability)?.let { output.accept(ItemStack(it)) }
                    }
                }
                .build()
        })
        tabReg.register(modBus)

        modBus.addListener { _: FMLCommonSetupEvent ->
            for (ability in DittoAbility.entries) {
                holders[ability]?.let { HMItems.register(ability, it.get()) }
            }
            HMItems.registerCase(caseHolder.get())
        }

        NeoForge.EVENT_BUS.addListener { event: RegisterCommandsEvent ->
            DittoHMsCommand.register(event.dispatcher)
        }

        NeoForge.EVENT_BUS.addListener { event: ServerTickEvent.Post ->
            AbilityManager.tick(event.server)
        }

        NeoForge.EVENT_BUS.addListener { event: PlayerInteractEvent.EntityInteract ->
            val player = event.entity
            if (!player.level().isClientSide && player.isShiftKeyDown) {
                val handled = HMInteractionHandler.onEntityUse(player, player.level(), event.hand, event.target)
                if (handled) {
                    event.isCanceled = true
                    event.cancellationResult = InteractionResult.SUCCESS
                }
            }
        }
    }
}
