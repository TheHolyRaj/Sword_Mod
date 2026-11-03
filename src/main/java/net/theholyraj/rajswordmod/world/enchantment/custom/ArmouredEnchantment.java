package net.theholyraj.rajswordmod.world.enchantment.custom;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;


public class ArmouredEnchantment extends Enchantment {
    public ArmouredEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
