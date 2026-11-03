package net.theholyraj.rajswordmod.world.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.entity.ModEntities;
import net.theholyraj.rajswordmod.world.item.ModItems;

public class DashProjectileEntity extends Projectile {
    private static final EntityDataAccessor<Boolean> HIT =
            SynchedEntityData.defineId(DashProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private int counter = 0;

    public DashProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    public DashProjectileEntity(Level pLevel, Player player) {
        super(ModEntities.DASH_PROJECTILE.get(), pLevel);
        setOwner(player);
        Vec3 blockpos = player.position();
        double d0 = blockpos.x() ;
        double d1 = blockpos.y() + 1.75D;
        double d2 = blockpos.z() ;
        this.moveTo(d0, d1, d2, this.getYRot(), this.getXRot());
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.scale(0.5));
    }

    @Override
    public void tick() {
        super.tick();
        if(this.entityData.get(HIT)) {
            if(this.tickCount >= counter) {
                this.discard();
            }
        }

        if (this.tickCount >= 25) {
            this.remove(RemovalReason.DISCARDED);
        }

        Vec3 deltaMovement = this.getDeltaMovement();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult))
            this.onHit(hitresult);

        double nextX = this.getX() + deltaMovement.x;
        double nextY = this.getY() + deltaMovement.y;
        double nextZ = this.getZ() + deltaMovement.z;
        this.updateRotation();

        if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
            this.discard();
        } else if (this.isInWaterOrBubble()) {
            this.discard();
        } else {
            this.setDeltaMovement(deltaMovement.scale(0.99F));
            this.setPos(nextX, nextY, nextZ);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity hitEntity = hitResult.getEntity();
        Entity owner = this.getOwner();

        if(hitEntity == owner && this.level().isClientSide()) {
            return;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
                2F, 1F);

        float damage = 8;
        LivingEntity livingentity = owner instanceof LivingEntity ? (LivingEntity)owner : null;
        hitEntity.hurt(this.damageSources().mobProjectile(this, livingentity), damage);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if(this.level().isClientSide()) {
            return;
        }

        if(hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult entityHitResult) {
            Entity hit = entityHitResult.getEntity();
            Entity owner = this.getOwner();
            if(owner != hit) {
                this.entityData.set(HIT, true);
                counter = this.tickCount + 5;
            }
        } else {
            this.entityData.set(HIT, true);
            counter = this.tickCount + 5;
        }
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(HIT, false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
