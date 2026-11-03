package net.theholyraj.rajswordmod.world.item;

import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.item.custom.*;
import net.theholyraj.rajswordmod.world.item.util.ModTiers;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SwordMod.MODID);


    public static final RegistryObject<DeflectSwordItem> DEFLECT_SWORD = ITEMS.register("deflect_sword",
            () -> new DeflectSwordItem(ModTiers.FABLED,
                    () -> (ModCommonConfigs.ARROW_RENDER_DAMAGE.get()-1),
                    () -> (ModCommonConfigs.ARROW_RENDER_ATTACK_SPEED.get()-4),
                    new Item.Properties().fireResistant()));

    public static final RegistryObject<SentinelSwordItem> SENTINEL_SWORD = ITEMS.register("sentinel_sword",
            () -> new SentinelSwordItem(ModTiers.FABLED,
                    () -> (ModCommonConfigs.SENTINEL_SWORD_DAMAGE.get()-1),
                    () -> (ModCommonConfigs.SENTINEL_SWORD_ATTACK_SPEED.get()-4),
                    new Item.Properties().fireResistant()));
    public static final RegistryObject<KnockbackSwordItem> KNOCKBACK_SWORD = ITEMS.register("knockback_sword",
            () -> new KnockbackSwordItem(ModTiers.FABLED,
                    () -> (ModCommonConfigs.KNOCKBACK_SWORD_DAMAGE.get()-1),
                    () -> (ModCommonConfigs.KNOCKBACK_SWORD_ATTACK_SPEED.get()-4),
                    new Item.Properties().fireResistant()));
    public static final RegistryObject<HolySwordItem> HOLY_SWORD = ITEMS.register("holy_sword",
            () -> new HolySwordItem(ModTiers.FABLED,
                    () -> (ModCommonConfigs.HOLY_SWORD_DAMAGE.get()-1),
                    () -> (ModCommonConfigs.HOLY_SWORD_ATTACK_SPEED.get()-4),
                    new Item.Properties().fireResistant()));
    public static final RegistryObject<GaiaBladeItem> GAIA_SWORD = ITEMS.register("gaia_sword",
            () -> new GaiaBladeItem(ModTiers.FABLED,
                    () -> (ModCommonConfigs.GAIA_BLADE_DAMAGE.get()-1),
                    () -> (ModCommonConfigs.GAIA_BLADE_ATTACK_SPEED.get()-4),
                    new Item.Properties().fireResistant()));
    public static final RegistryObject<BloodGorgerSwordItem> BLOOD_SWORD = ITEMS.register("blood_sword",
            () -> new BloodGorgerSwordItem(ModTiers.FABLED,
                    () -> (ModCommonConfigs.BLOOD_GORGER_DAMAGE.get()-1),
                    () -> (ModCommonConfigs.BLOOD_GORGER_ATTACK_SPEED.get()-4),
                    new Item.Properties().fireResistant()));
    public static final RegistryObject<AntiArmorSwordItem> ANTI_ARMOR_SWORD = ITEMS.register("anti_armor_sword",
            () -> new AntiArmorSwordItem(ModTiers.FABLED,
                    () -> (ModCommonConfigs.ANTI_ARMOR_DAMAGE.get()-1),
                    () -> (ModCommonConfigs.ANTI_ARMOR_ATTACK_SPEED.get()-4),
                    new Item.Properties().fireResistant()));

    public static final RegistryObject<ShadowCloneSwordItem> SHADOW_CLONE_SWORD = ITEMS.register("shadow_clone_sword",
            () -> new ShadowCloneSwordItem(ModTiers.FABLED,
                    () -> (ModCommonConfigs.DARK_DECEIVER_DAMAGE.get()-1),
                    () -> (ModCommonConfigs.DARK_DECEIVER_ATTACK_SPEED.get()-4),
    new Item.Properties().fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
