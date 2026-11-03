package net.theholyraj.rajswordmod.world.entity.custom;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class CloneEntity extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = SynchedEntityData.defineId(CloneEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public boolean isUpgraded;
    public int bonusLifeTime;

    public CloneEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.isUpgraded = false;
        this.bonusLifeTime = 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(1,new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(1, new UpgradedAttackGoal(this, 1f,true));

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true)
        );
    }
    public static AttributeSupplier.Builder createAttributes(){
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.4f)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()){
            for (int i = 0; i < 6; i++) {
                this.level().addParticle(
                        ParticleTypes.SMOKE,
                        this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
                        0.0, 0.05, 0.0
                );
            }
        }
        if (this.tickCount >= ModCommonConfigs.SHADOW_CLONE_LIFESPAN.get() + this.bonusLifeTime) {
            if (this.level().isClientSide()){

            }
            else {
                this.kill();
            }
        }
    }

    @Override
    public void aiStep() {
        this.updateSwingTime();
        super.aiStep();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PLAYER_UUID, Optional.empty());
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.entityData.set(PLAYER_UUID, Optional.ofNullable(playerUUID));
    }
    public UUID getPlayerUUID() {
        return this.entityData.get(PLAYER_UUID).orElse(null);
    }
    public void setUpgraded(){
        this.isUpgraded = true;
    }
    public void addCopyOfPlayersEquipment(Player player){
        this.setItemSlot(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
        this.setItemSlot(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
        this.setItemSlot(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
        this.setItemSlot(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));
        this.setItemSlot(EquipmentSlot.MAINHAND, player.getItemBySlot(EquipmentSlot.MAINHAND));
        this.setItemSlot(EquipmentSlot.OFFHAND, player.getItemBySlot(EquipmentSlot.OFFHAND));

    }
    public void addTargetToTargetSelector(LivingEntity entity){
        this.targetSelector.addGoal(2,  new NearestAttackableTargetGoal<>(this, entity.getClass(), true));
    }
    ///////////////////////////////////////////////////////GOALS//////////////////////////////////////////////////////////////
    public class UpgradedAttackGoal extends MeleeAttackGoal {
        CloneEntity entity;

        public UpgradedAttackGoal(PathfinderMob pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
            super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
            this.entity = (CloneEntity) pMob;
        }

        @Override
        public boolean canUse() {
            return (super.canUse() && entity.isUpgraded);
        }

        @Override
        public void tick() {
            super.tick();
        }
    }
}
