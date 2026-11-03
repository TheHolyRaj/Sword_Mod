package net.theholyraj.rajswordmod.world.entity.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.entity.ModEntities;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.custom.GaiaBladeItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GaiaProjectileEntity extends AbstractArrow {
    private static final EntityDataAccessor<Boolean> IS_RETURNING = SynchedEntityData.defineId(GaiaProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_UPGRADED = SynchedEntityData.defineId(GaiaProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private final Set<Entity> piercedEntities = new HashSet<>();
    private float swordDamage;
    private Vec3 flightDirection = Vec3.ZERO;
    private Vec3 lastHitPos = Vec3.ZERO;
    public boolean isUpgraded;

    public GaiaProjectileEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public GaiaProjectileEntity(Level level, Player player, ItemStack sword, float speed) {
        super(ModEntities.GAIA_PROJECTILE.get(), level);
        this.setOwner(player);
        this.setNoGravity(true);
        this.isUpgraded = GaiaBladeItem.isUpgraded(sword);
        this.entityData.set(IS_UPGRADED, this.isUpgraded);

        double attributeDamage = 0;
        var modifiers = sword.getAttributeModifiers(EquipmentSlot.MAINHAND);
        if (modifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
            for (AttributeModifier modifier : modifiers.get(Attributes.ATTACK_DAMAGE)) {
                attributeDamage += modifier.getAmount();
            }
        }

        float totalBase = (float) attributeDamage + 1.0F;
        float enchantmentBonus = EnchantmentHelper.getDamageBonus(sword, player.getMobType());
        float configMult = ModCommonConfigs.GAIA_BLADE_PROJECTILE_DAMAGE_PERCENTAGE.get().floatValue();
        this.swordDamage = (totalBase + enchantmentBonus) * configMult;

        Vec3 look = player.getLookAngle();
        this.setPos(player.getX(), player.getEyeY() - 0.3D, player.getZ());
        this.flightDirection = look.scale(speed);

        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.setInvisible(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_RETURNING, false);
        this.entityData.define(IS_UPGRADED, false);
    }

    @Override
    public void tick() {
        super.baseTick();
        if (this.tickCount > 1 && this.isInvisible()) {
            this.setInvisible(false);
        }

        if (!this.level().isClientSide && this.tickCount >= 12 && !this.entityData.get(IS_RETURNING)) {
            if (isUpgraded) {
                this.entityData.set(IS_RETURNING, true);
                this.piercedEntities.clear();
            } else {
                spawnDiscardParticles();
                this.discard();
                return;
            }
        }

        boolean returning = this.entityData.get(IS_RETURNING);
        Vec3 currentPos = this.position();
        Vec3 nextPos;

        if (returning && this.getOwner() instanceof Player player) {
            Vec3 target = player.position().add(0, 1.2, 0);
            double distToTarget = currentPos.distanceTo(target);

            if (distToTarget < 1.4) {
                spawnDiscardParticles();
                this.discard();
                return;
            }
            Vec3 dirToPlayer = target.subtract(currentPos).normalize();
            nextPos = currentPos.add(dirToPlayer.scale(1.2));
        } else {
            nextPos = currentPos.add(this.flightDirection);
            this.flightDirection = this.flightDirection.scale(0.95);
        }

        BlockHitResult blockHit = this.level().clip(new ClipContext(currentPos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.setPos(blockHit.getLocation());

            if (this.lastHitPos == null || blockHit.getLocation().distanceTo(this.lastHitPos) > 0.5D) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.GAIA_HIT.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.4f, 1.0f);
                spawnDiscardParticles();
                this.lastHitPos = blockHit.getLocation();
            }

            if (isUpgraded) {
                this.entityData.set(IS_RETURNING, true);
                this.piercedEntities.clear();
                this.flightDirection = Vec3.ZERO;

                if (returning) {
                    this.discard();
                }
            } else {
                this.discard();
            }
            return;
        }

        Vec3 moveVec = nextPos.subtract(currentPos);
        if (moveVec.lengthSqr() > 1.0E-7D) {
            this.setYRot((float) (Mth.atan2(moveVec.x, moveVec.z) * (180D / Math.PI)));
            this.setXRot((float) (Mth.atan2(moveVec.y, moveVec.horizontalDistance()) * (180D / Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.setPos(nextPos.x, nextPos.y, nextPos.z);

        if (!this.level().isClientSide) {
            this.checkCollisions();
        } else {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

    public boolean isUpgraded() {
        return this.entityData.get(IS_UPGRADED);
    }

    private void spawnDiscardParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.05D);
        }
    }

    private void checkCollisions() {
        Vec3 currentPos = this.position();
        Vec3 movement = this.flightDirection;

        if (this.entityData.get(IS_RETURNING) && this.getOwner() instanceof Player player) {
            movement = player.position().add(0, 1.2, 0).subtract(currentPos).normalize().scale(1.2);
        }

        AABB travelBox = this.getBoundingBox().expandTowards(movement).inflate(0.8D);
        List<Entity> targets = this.level().getEntities(this, travelBox, this::canHitEntity);

        for (Entity target : targets) {
            if (this.getOwner() instanceof Player player) {
                if (target == player) continue;

                if (!this.piercedEntities.contains(target)) {
                    if (target.getBoundingBox().inflate(0.3D).clip(currentPos, currentPos.add(movement)).isPresent()
                            || this.getBoundingBox().inflate(0.8D).intersects(target.getBoundingBox())) {
                        if (target.hurt(this.damageSources().arrow(this,player), this.swordDamage)) {
                            this.piercedEntities.add(target);
                            ItemStack mainHand = player.getMainHandItem();
                            if (mainHand.getItem() instanceof GaiaBladeItem) {
                                GaiaBladeItem.addUpgradeAmount(mainHand, 1);
                            }
                        }
                    }
                }
            }
        }
    }
}