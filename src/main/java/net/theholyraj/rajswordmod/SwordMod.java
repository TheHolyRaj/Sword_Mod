package net.theholyraj.rajswordmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.theholyraj.rajswordmod.client.entity.AntiArmourAttackEntityRenderer;
import net.theholyraj.rajswordmod.client.entity.CloneEntityRenderer;
import net.theholyraj.rajswordmod.client.entity.GaiaProjectileRenderer;
import net.theholyraj.rajswordmod.client.item.ModItemProperties;
import net.theholyraj.rajswordmod.client.particle.ModParticles;
import net.theholyraj.rajswordmod.client.particle.custom.*;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.enchantment.ModEnchantments;
import net.theholyraj.rajswordmod.world.entity.ModEntities;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.ModCreativeModeTabs;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.client.keybinding.ModKeyBindings;
import net.theholyraj.rajswordmod.world.mobeffects.ModMobEffects;
import org.slf4j.Logger;

@Mod(SwordMod.MODID)
public class SwordMod {
    public static final String MODID = "rajswordmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public SwordMod(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticles.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfigs.SPEC, "TheEightRenownedBlades-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    public static ResourceLocation loc(String s) {
        return new ResourceLocation(MODID, s);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event){
    }
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        ModCapabilities.registerCapabilities(event);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents{
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.GAIA_PROJECTILE.get(), GaiaProjectileRenderer::new);
            EntityRenderers.register(ModEntities.ANTI_ARMOUR_ATTACK_ENTITY.get(), AntiArmourAttackEntityRenderer::new);
            EntityRenderers.register(ModEntities.CLONE_ENTITY.get(), CloneEntityRenderer::new);
            ModItemProperties.makeProperties();
        }
        @SubscribeEvent
        public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
            Minecraft.getInstance().particleEngine.register(ModParticles.DEFLECT_PARTICLES.get(),
                    DeflectParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.ANTI_ARMOUR_ATTACK_PARTICLES.get(),
                    AntiArmourAttackParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.HOLY_EXPLOSION_PARTICLES.get(),
                    HolyExplosionParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.HOLY_FIRE_PARTICLES.get(),
                    HolyFireParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.GAIA_BLADE_PARTICLES.get(),
                    GaiaBladeParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.SENTINEL_SHOCKWAVE_PARTICLES.get(),
                    SentinelShockwaveParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.SENTINEL_DEFENCE_PARTICLES.get(),
                    SentinelDefenceParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.KNOCKBACK_SWORD_PARTICLES.get(),
                    KnockbackSwordParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.KNOCKBACK_SUCTION_PARTICLES.get(),
                    KnockbackSuctionParticle.Provider::new);
        }
        @SubscribeEvent
        public static void registerKeybinds(RegisterKeyMappingsEvent event){
            event.register(ModKeyBindings.INSTANCE.awakenSwordKey);

        }
    }
}
