package net.theholyraj.rajswordmod.world.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.enchantment.custom.ArmouredEnchantment;
import net.theholyraj.rajswordmod.world.enchantment.custom.BackStabberEnchantment;
import net.theholyraj.rajswordmod.world.enchantment.custom.ReachEnchantment;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, SwordMod.MODID);

    public static final RegistryObject<Enchantment> ARMOURED =
            ENCHANTMENTS.register("armoured",
                    () -> new ArmouredEnchantment(
                            Enchantment.Rarity.RARE,
                            EnchantmentCategory.WEAPON,
                            net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    ));
    public static final RegistryObject<Enchantment> REACH =
            ENCHANTMENTS.register("reach",
                    () -> new ReachEnchantment(
                            Enchantment.Rarity.RARE,
                            EnchantmentCategory.WEAPON,
                            net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    ));
    public static final RegistryObject<Enchantment> BACKSTABBER =
            ENCHANTMENTS.register("backstabber",
                    () -> new BackStabberEnchantment(
                            Enchantment.Rarity.RARE,
                            EnchantmentCategory.WEAPON,
                            net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    ));

    public static void register(IEventBus bus) {
        ENCHANTMENTS.register(bus);
    }
}
