package net.theholyraj.rajswordmod.world.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.client.particle.ModParticles;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.item.custom.AntiArmorSwordItem;
import net.theholyraj.rajswordmod.world.item.util.antiarmoursword.ArmourStealManager;
import net.theholyraj.rajswordmod.world.mobeffects.ModMobEffects;

import java.util.*;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class AntiArmourAttackEntity extends Entity {
    private static final Map<String, Integer> invincibilityTimers = new HashMap<>();
    private static void addPlayer(String playerID, int time){
        invincibilityTimers.put(playerID, time);
    }
    public static void timerTick(ServerPlayer player) {
        String playerID = player.getUUID().toString();
        int time = invincibilityTimers.getOrDefault(playerID, 0);
        if (time > 1) {
            invincibilityTimers.put(playerID, time - 1);
        } else if (time == 1) {
            invincibilityTimers.remove(playerID);
        }
    }

    private static final EntityDataAccessor<OptionalInt> OWNER_ID =
            SynchedEntityData.defineId(
                    AntiArmourAttackEntity.class,
                    EntityDataSerializers.OPTIONAL_UNSIGNED_INT
            );
    private static final EntityDataAccessor<Float> CHARGE =
            SynchedEntityData.defineId(
                    AntiArmourAttackEntity.class,
                    EntityDataSerializers.FLOAT
            );
    private static final EntityDataAccessor<Boolean> LANDEDATTACK =
            SynchedEntityData.defineId(
                    AntiArmourAttackEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    public boolean isUpgraded = false;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 17;
    private final Set<Integer> hitEntities = new HashSet<>();
    private boolean particleSpawned = false;
    public AntiArmourAttackEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        lifetime++;
        if (lifetime >= (MAX_LIFETIME * getCharge())) {
            discard();
            return;
        }
        Player owner = getOwner();
        if (owner == null) {
            discard();
            return;
        }
        //client
        if (level().isClientSide) {
            setLocation(owner);
            if (!particleSpawned) {
                Vec3 center = getBoundingBox().getCenter();
                level().addParticle(
                        ModParticles.ANTI_ARMOUR_ATTACK_PARTICLES.get(),
                        center.x,
                        center.y+1,
                        center.z,
                        getId(),
                        0,
                        0);
                particleSpawned = true;
            }
        }
        //server
        else {
            setLocation(owner);
            if (owner instanceof ServerPlayer serverOwner) {
                timerTick(serverOwner);
            }
            List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox());
            for (LivingEntity entity : entities) {
                if (hitEntities.contains(entity.getId()) || entity == owner) {
                    continue;
                }
                hitEntities.add(entity.getId());
                attack(entity);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        Player owner = getOwner();

        if (owner != null) {
            invincibilityTimers.remove(owner.getUUID().toString());
        }
        super.remove(reason);
    }

    private void setLocation(Player owner){
        Vec3 look = owner.getLookAngle();
        Vec3 position = owner.getEyePosition().add(look.scale(2));
        setPos(position.x, position.y-1, position.z);
        setXRot(owner.getXRot());
        setYRot(owner.getYRot());
    }

    private void attack(LivingEntity entity){
        if (!this.getLandedAttack()){
            Player owner = getOwner();
            owner.setDeltaMovement(Vec3.ZERO);

            Vec3 backwards = owner.getLookAngle().scale(-1.5f);
            owner.setDeltaMovement(backwards);

            owner.hurtMarked = true;
        }
        this.setLandedAttack(true);
        float damageToDeal = ModCommonConfigs.ANTI_ARMOR_CHARGE_DAMAGE.get();
        damageToDeal = damageToDeal * getCharge();
        entity.hurt(level().damageSources().playerAttack(getOwner()),damageToDeal);

        if (getCharge() == 1){
            MobEffectInstance effectInstance = entity.getEffect(ModMobEffects.ANTI_ARMOR.get());
            double armourStolen = 0;
            double toughnessStolen = 0;

            if (effectInstance != null) {
                int amplifier = effectInstance.getAmplifier();
                int currentLevel = amplifier + 1;
                effectInstance=new MobEffectInstance(
                        ModMobEffects.ANTI_ARMOR.get(),
                        ModCommonConfigs.ANTI_ARMOR_EFFECT_DURATION.get(),
                        Math.min(3,currentLevel)
                );
                int nextLevel = currentLevel + 1;

                if (entity.getAttribute(Attributes.ARMOR) != null){
                    double totalArmour = entity.getAttribute(Attributes.ARMOR).getValue();
                    double reduction = ModCommonConfigs.ANTI_ARMOR_EFFECT_REDUCTION.get() * ( Math.min(3,currentLevel));
                    double armorWithoutDebuff = totalArmour / (1.0 - reduction);
                    double reductionAmountPercentage = ModCommonConfigs.ANTI_ARMOR_EFFECT_REDUCTION.get() * ( Math.min(3,nextLevel));
                    armourStolen = armorWithoutDebuff * reductionAmountPercentage;
                }
                if (entity.getAttribute(Attributes.ARMOR_TOUGHNESS) != null){
                    double totalToughness = entity.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();
                    double reduction = ModCommonConfigs.ANTI_ARMOR_EFFECT_REDUCTION.get() * ( Math.min(3,currentLevel));
                    double toughnessWithoutDebuff = totalToughness / (1.0 - reduction);
                    double reductionAmountPercentage = ModCommonConfigs.ANTI_ARMOR_EFFECT_REDUCTION.get() * ( Math.min(3,nextLevel));
                    toughnessStolen = toughnessWithoutDebuff * reductionAmountPercentage;
                }
            }
            else {
                effectInstance=new MobEffectInstance(
                        ModMobEffects.ANTI_ARMOR.get(),
                        ModCommonConfigs.ANTI_ARMOR_EFFECT_DURATION.get(),
                        0);

                if (entity.getAttribute(Attributes.ARMOR) != null){
                    double totalArmour = entity.getAttribute(Attributes.ARMOR).getValue();
                    double reductionAmountPercentage = ModCommonConfigs.ANTI_ARMOR_EFFECT_REDUCTION.get();
                    armourStolen = totalArmour * reductionAmountPercentage;
                }
                if (entity.getAttribute(Attributes.ARMOR_TOUGHNESS) != null){
                    double totalArmour = entity.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();
                    double reductionAmountPercentage = ModCommonConfigs.ANTI_ARMOR_EFFECT_REDUCTION.get();
                    toughnessStolen = totalArmour * reductionAmountPercentage;
                }
            }
            entity.addEffect(effectInstance);

            if (getOwner().getMainHandItem().getItem() instanceof AntiArmorSwordItem ){
                AntiArmorSwordItem.addUpgradeAmount(getOwner().getMainHandItem(), 1);

                if (isUpgraded && (armourStolen != 0 || toughnessStolen != 0)){
                    ArmourStealManager.addPlayerToMap(getOwner().getUUID(), entity.getUUID(), armourStolen,toughnessStolen);
                }
            }
        }
    }

    public void setIsUpgraded() {
        this.isUpgraded = true;
    }

    public void setOwner(Player player) {
        int time = Math.max(0, (int)(MAX_LIFETIME * getCharge() - 1));
        addPlayer(player.getUUID().toString(), time);
        this.entityData.set(OWNER_ID, OptionalInt.of(player.getId()));
    }

    public Player getOwner() {
        OptionalInt ownerId = this.entityData.get(OWNER_ID);
        if (ownerId.isPresent()) {
            Entity entity = this.level().getEntity(ownerId.getAsInt());

            if (entity instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    public void setCharge(float charge) {
        this.entityData.set(CHARGE, Mth.clamp(charge, 0.0f, 1.0f));
    }

    public float getCharge() {
        return this.entityData.get(CHARGE);
    }
    public void setLandedAttack(boolean landedAttack){
        this.entityData.set(LANDEDATTACK, landedAttack);
    }
    public boolean getLandedAttack(){
        return this.entityData.get(LANDEDATTACK);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, OptionalInt.empty());
        this.entityData.define(CHARGE,1.0f);
        this.entityData.define(LANDEDATTACK,false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            invincibilityTimers.remove(player.getUUID().toString());
        }
    }

    @SubscribeEvent
    public static void invinvibilityDuringChrageEvent(LivingAttackEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String playerID = player.getUUID().toString();

        if (AntiArmourAttackEntity.invincibilityTimers.containsKey(playerID)) {
            event.setCanceled(true);
        }
    }
}
