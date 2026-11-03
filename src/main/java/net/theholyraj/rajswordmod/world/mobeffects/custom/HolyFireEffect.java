package net.theholyraj.rajswordmod.world.mobeffects.custom;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.client.particle.ModParticles;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.HolyFireParticleS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.explosion.HolyExplosion;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.mobeffects.ModMobEffects;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class HolyFireEffect extends MobEffect {
    public HolyFireEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        float holyDamage = ((pLivingEntity.getMaxHealth() / 100) * ModCommonConfigs.HOLY_FIRE_DAMAGE.get());
        if (holyDamage < ModCommonConfigs.MIN_HOLY_FIRE_DAMAGE.get()) {
            holyDamage = ModCommonConfigs.MIN_HOLY_FIRE_DAMAGE.get();
        }
        if (holyDamage > ModCommonConfigs.MAX_HOLY_FIRE_DAMAGE.get()){
            holyDamage = ModCommonConfigs.MAX_HOLY_FIRE_DAMAGE.get();
        }
        pLivingEntity.hurt(pLivingEntity.level().damageSources().magic(), holyDamage);

        // PARTICLE LOGIC
        double minX = pLivingEntity.getBoundingBox().minX;
        double minY = pLivingEntity.getBoundingBox().minY;
        double minZ = pLivingEntity.getBoundingBox().minZ;
        double maxX = pLivingEntity.getBoundingBox().maxX;
        double maxY = pLivingEntity.getBoundingBox().maxY;
        double maxZ = pLivingEntity.getBoundingBox().maxZ;

        double x = minX + (maxX - minX) * pLivingEntity.getRandom().nextDouble();
        double y = minY + (maxY - minY) * pLivingEntity.getRandom().nextDouble();
        double z = minZ + (maxZ - minZ) * pLivingEntity.getRandom().nextDouble();

        if (y < pLivingEntity.getY() + (pLivingEntity.getBbHeight() / 2)) {
            y = pLivingEntity.getY() + (pLivingEntity.getBbHeight() / 2);
        }

        ModMessages.sendToClients(new HolyFireParticleS2CPacket(x, y, z));
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 10 == 0;
    }
    @SubscribeEvent
    public static void doHolyExplosion(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.getMobType() != MobType.UNDEAD) return;
        MobEffectInstance effect = victim.getEffect(ModMobEffects.HOLY_FIRE.get());
        if (effect != null && effect.getAmplifier() >= 2) {
            Level level = victim.level();
            float chainRadius = 3.0f;
            HolyExplosion explosion = new HolyExplosion(
                    level,
                    null,
                    victim.getX(),
                    victim.getY() + victim.getBbHeight() / 2,
                    victim.getZ(),
                    chainRadius,
                    true
            );
            explosion.explode();
            explosion.finalizeExplosion(true);
        }
    }
}
