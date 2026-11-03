package net.theholyraj.rajswordmod.world.item.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.enchantment.ModEnchantments;
import net.theholyraj.rajswordmod.world.item.ModItems;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SwordMod.MODID);

    public static final RegistryObject<CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("sword_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SENTINEL_SWORD.get()))
                    .title(Component.translatable("creativetab.sword_tab"))
                    .displayItems((displayParameters, output) -> {
                        output.accept(ModItems.DEFLECT_SWORD.get());
                        output.accept(ModItems.SENTINEL_SWORD.get());
                        output.accept(ModItems.KNOCKBACK_SWORD.get());
                        output.accept(ModItems.HOLY_SWORD.get());
                        output.accept(ModItems.GAIA_SWORD.get());
                        output.accept(ModItems.BLOOD_SWORD.get());
                        output.accept(ModItems.ANTI_ARMOR_SWORD.get());
                        output.accept(ModItems.SHADOW_CLONE_SWORD.get());

                        for (int i = 1; i <= ModEnchantments.ARMOURED.get().getMaxLevel(); i++) {
                            output.accept(EnchantedBookItem.createForEnchantment(
                                    new EnchantmentInstance(ModEnchantments.ARMOURED.get(), i)
                            ));
                        }
                        for (int i = 1; i <= ModEnchantments.REACH.get().getMaxLevel(); i++) {
                            output.accept(EnchantedBookItem.createForEnchantment(
                                    new EnchantmentInstance(ModEnchantments.REACH.get(), i)
                            ));
                        }
                        for (int i = 1; i <= ModEnchantments.BACKSTABBER.get().getMaxLevel(); i++) {
                            output.accept(EnchantedBookItem.createForEnchantment(
                                    new EnchantmentInstance(ModEnchantments.BACKSTABBER.get(), i)
                            ));
                        }
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
