package net.theholyraj.rajswordmod.world.item.custom;

import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.DeflectParticleS2CPacket;
import net.theholyraj.rajswordmod.world.entity.custom.DashProjectileEntity;
import net.theholyraj.rajswordmod.world.item.ModItems;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class DeflectSwordItem extends SwordItem {
    public DeflectSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.MAIN_HAND){
            pPlayer.startUsingItem(pUsedHand);

        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (!pLevel.isClientSide() && pLivingEntity instanceof Player player) {
            if (pStack.hasTag()){
                if (pStack.hasTag() && pStack.getTag().getBoolean("using")){
                    player.getCooldowns().addCooldown(this, 50);
                    DashProjectileEntity projectile = new DashProjectileEntity(pLevel, player);
                    projectile.setSword(pStack);
                    projectile.setPlayer(player);
                    projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0);
                    pLevel.addFreshEntity(projectile);
                    pLevel.playSound(player,player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,1,1);
                }
                pStack.getTag().putBoolean("using", false);
            }
            else {
                pStack.setTag(new CompoundTag());
                pStack.getTag().putBoolean("using", false);
            }
        }else if (pLivingEntity instanceof Player player){
            if (pStack.hasTag() && pStack.getTag().getBoolean("using")){
                Vec3 playerLook = player.getViewVector(1);
                Vec3 dashVec = new Vec3(playerLook.x(), playerLook.y(), playerLook.z());
                player.setDeltaMovement(dashVec);
                pLevel.playSound(player,player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,1,1);
            }
        }

        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        if (pLivingEntity instanceof Player player){
            if (pRemainingUseDuration < 71960){
                deleteNearbyProjectiles(player, pStack);
                if (pStack.hasTag()){
                    pStack.getTag().putBoolean("using", true);
                }
                else {
                    pStack.setTag(new CompoundTag());
                    pStack.getTag().putBoolean("using", true);
                }
            }
            if (pRemainingUseDuration == 71960 && pLevel.isClientSide()){
                pLevel.playSound((Player)pLivingEntity,pLivingEntity.blockPosition(), ModSounds.SWITCH.get(), SoundSource.PLAYERS,1f,1f);
            }
        }
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    private void deleteNearbyProjectiles(Player player, ItemStack stack){
        Vec3 center = new Vec3(player.blockPosition().getX(),player.blockPosition().getY(),player.blockPosition().getZ());
        List<Projectile> projectiles = player.level().getEntitiesOfClass(Projectile.class, new AABB(center,center).inflate(6), e-> true).stream()
                .sorted(Comparator.comparingDouble(ent -> ent.distanceToSqr(center))).toList();

        for (Projectile projectile: projectiles){
            if (projectile.distanceToSqr(player) < 7){
                player.level().playSound(null, projectile.blockPosition(), ModSounds.PROJECTILE_SLASH.get(), SoundSource.PLAYERS,0.1f,1f);
                if (!player.level().isClientSide()){
                    ModMessages.sendToClients(new DeflectParticleS2CPacket(projectile.getX(),projectile.getY(),projectile.getZ()));
                    projectile.remove(Entity.RemovalReason.DISCARDED);
                    stack.setDamageValue(1);
                }
            }
        }
    }
    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }

    //////////////////////////////////////////EVENT//////////////////////////////////////////////////////
    @SubscribeEvent
    public static void projectileFailsafe(ProjectileImpactEvent event){
        if (event.getRayTraceResult() instanceof EntityHitResult result){
            if (result.getEntity() instanceof Player player){
                if (!player.level().isClientSide() && player.getMainHandItem().is(ModItems.DEFLECT_SWORD.get())){
                    if (player.getMainHandItem().hasTag() && player.getMainHandItem().getTag().contains("using")){
                        if (player.getMainHandItem().getTag().getBoolean("using")){
                            Projectile projectile = event.getProjectile();
                            ModMessages.sendToClients(new DeflectParticleS2CPacket(projectile.getX(),projectile.getY(),projectile.getZ()));
                            event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
                            player.getMainHandItem().setDamageValue(1);
                        }
                    }
                }
            }
        }
    }
}
