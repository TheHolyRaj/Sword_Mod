package net.theholyraj.rajswordmod.world.explosion;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.data.ForgeEntityTypeTagsProvider;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.client.particle.ModParticles;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.damage.DamageSources;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.mobeffects.ModMobEffects;
import net.theholyraj.rajswordmod.world.mobeffects.custom.HolyFireEffect;
import net.theholyraj.rajswordmod.world.mobeffects.custom.HolyFireEffectInstance;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class HolyExplosion extends Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
    private final boolean isUpgraded;

    public HolyExplosion(Level world, Entity entity, double x, double y, double z, float size, boolean isUpgraded) {
        super(world, entity, x, y, z, size, false, BlockInteraction.KEEP);
        this.level = world;
        this.source = entity;
        this.radius = size;
        this.x = x;
        this.y = y;
        this.z = z;
        this.damageSource = world.damageSources().source(DamageSources.HOLY);
        this.damageCalculator = EXPLOSION_DAMAGE_CALCULATOR;
        this.isUpgraded = isUpgraded;
    }


    @Override
    public void explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        float f2 = this.radius * 2.0F;
        int k1 = Mth.floor(this.x - (double)f2 - 1.0D);
        int l1 = Mth.floor(this.x + (double)f2 + 1.0D);
        int i2 = Mth.floor(this.y - (double)f2 - 1.0D);
        int i1 = Mth.floor(this.y + (double)f2 + 1.0D);
        int j2 = Mth.floor(this.z - (double)f2 - 1.0D);
        int j1 = Mth.floor(this.z + (double)f2 + 1.0D);
        List<Entity> list = this.level.getEntities(this.source, new AABB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, this, list, f2);
        Vec3 vec3 = new Vec3(this.x, this.y, this.z);

        for(int k2 = 0; k2 < list.size(); ++k2) {
            Entity entity = list.get(k2);
            if (!entity.ignoreExplosion()) {
                double d12 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f2;
                if (d12 <= 1.0D) {
                    double d5 = entity.getX() - this.x;
                    double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                    double d9 = entity.getZ() - this.z;
                    double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d13 != 0.0D) {
                        entity.hurt(this.damageSource,10f);
                    }
                }
            }
        }

    }

    @Override
    public boolean interactsWithBlocks() {
        return false;
    }

    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        if (spawnParticles && this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.HOLY_EXPLOSION_PARTICLES.get(),
                    this.x, this.y, this.z, 0, this.radius, 0.0D, 0.0D, 1.0D);

            serverLevel.playSound(null, this.x, this.y, this.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 1.0F);
            serverLevel.playSound(null, this.x, this.y, this.z,
                    ModSounds.HOLY_EXPLOSION.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        }
    }

    @Override
    public DamageSource getDamageSource() {
        return super.getDamageSource();
    }

    /////////////////////////////////////////////////////////EVENT//////////////////////
    @SubscribeEvent
    public static void undeadDamageEvent(ExplosionEvent.Detonate event) {
        if (event.getExplosion() instanceof HolyExplosion explosion) {
            List<Entity> entities = event.getAffectedEntities();
            entities.removeIf(mob -> !(mob instanceof LivingEntity entity));
            entities.removeIf(mob -> mob instanceof LivingEntity entity && entity.getMobType() != MobType.UNDEAD);
            for (Entity entity : entities) {
                if (entity instanceof WitherBoss witherBoss) {
                    int power = 1;
                    if (explosion.isUpgraded) {
                        power = 2;
                    }
                    HolyFireEffectInstance effectInstance = new HolyFireEffectInstance(ModMobEffects.HOLY_FIRE.get(), ModCommonConfigs.HOLY_FIRE_DURATION.get(), power);
                    witherBoss.forceAddEffect(effectInstance, witherBoss);
                }
                if (entity instanceof LivingEntity livingEntity) {
                    if (livingEntity.getMobType() == MobType.UNDEAD) {
                        int power = 1;
                        if (explosion.isUpgraded) {
                            power = 2;
                        }
                        livingEntity.addEffect(new HolyFireEffectInstance(ModMobEffects.HOLY_FIRE.get(), ModCommonConfigs.HOLY_FIRE_DURATION.get(), power));
                    }
                }
            }
        }
    }
}
