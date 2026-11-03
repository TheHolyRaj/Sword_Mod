package net.theholyraj.rajswordmod.client.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theholyraj.rajswordmod.SwordMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SwordMod.MODID);

    public static final RegistryObject<SoundEvent> PROJECTILE_SLASH = registerSoundEvents("projectile_slash");

    public static final RegistryObject<SoundEvent> CHARGE_KNOCKBACK = registerSoundEvents("charge_knockback");

    public static final RegistryObject<SoundEvent> HOLY_EXPLOSION = registerSoundEvents("holy_explosion");

    public static final RegistryObject<SoundEvent> GAIA_SWING = registerSoundEvents("gaia_swing");

    public static final RegistryObject<SoundEvent> GAIA_HIT = registerSoundEvents("gaia_hit");

    public static final RegistryObject<SoundEvent> ANTI_ARMOUR_CHARGE = registerSoundEvents("anti_armour_charge");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        ResourceLocation id = new ResourceLocation(SwordMod.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
