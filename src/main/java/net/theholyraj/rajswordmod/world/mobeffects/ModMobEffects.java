package net.theholyraj.rajswordmod.world.mobeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.mobeffects.custom.AntiArmorEffect;
import net.theholyraj.rajswordmod.world.mobeffects.custom.HolyFireEffect;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SwordMod.MODID);

    public static final RegistryObject<HolyFireEffect>HOLY_FIRE = EFFECTS.register("holy_fire",
            ()-> new HolyFireEffect(MobEffectCategory.HARMFUL,1));

    public static final RegistryObject<AntiArmorEffect>ANTI_ARMOR = EFFECTS.register("anti_armor",
            ()-> new AntiArmorEffect(MobEffectCategory.HARMFUL,16777215));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
