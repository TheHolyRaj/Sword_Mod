package net.theholyraj.rajswordmod.world.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.DeflectParticleS2CPacket;
import net.theholyraj.rajswordmod.network.packet.UpgradeSwordSERVERS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class DeflectSwordItem extends UpgradeSwordItem {
    private final Supplier<Double> damageSupplier;
    private final Supplier<Double> speedSupplier;

    public DeflectSwordItem(Tier pTier, Supplier<Double> damage, Supplier<Double> speed, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
        this.damageSupplier = damage;
        this.speedSupplier = speed;
    }
    /// /////////////////////////////////////////////////////////////ABILITY//////////////////////////////////////////////////////////////////////////////////////////////
    private void deleteNearbyProjectiles(Player player, ItemStack stack) {
        double radius = 2.0D + (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, stack) * 0.5D);
        List<Projectile> projectiles = getSortedProjectiles(player, radius);

        for (Projectile projectile : projectiles) {
            if (projectile.distanceTo(player) <= radius) {
                if (!player.level().isClientSide() && !stack.isEmpty()) {
                    playDeflectEffects(player, projectile);
                    projectile.discard();

                    if (!player.isCreative()) {
                        EquipmentSlot slot = player.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                        stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(slot));
                    }
                    if (getUpgradeData(stack) < ModCommonConfigs.ARROW_RENDER_UPGRADE_AMOUNT.get()) {
                        addUpgradeAmount(stack, 1);
                    }
                }
            }
        }
    }

    private void upgradedDeleteNearbyProjectiles(Player player, ItemStack stack) {
        double radius = 2.0D + (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, stack) * 0.5D);
        List<Projectile> projectiles = getSortedProjectiles(player, radius);

        for (Projectile projectile : projectiles) {
            if (projectile.getOwner() == player || projectile.distanceTo(player) > radius) continue;

            if (!player.level().isClientSide()) {
                Entity shooter = projectile.getOwner();
                Vec3 reflectDir;

                if (shooter != null && shooter.isAlive()) {
                    reflectDir = shooter.position().add(0, shooter.getBbHeight() * 0.5, 0).subtract(projectile.position()).normalize();
                } else {
                    reflectDir = projectile.getDeltaMovement().scale(-1).normalize();
                }

                double speed = Math.max(0.5, projectile.getDeltaMovement().length());
                projectile.setOwner(player);
                projectile.shoot(reflectDir.x, reflectDir.y, reflectDir.z, (float) speed, 0.5F);

                if (projectile instanceof AbstractHurtingProjectile hurting) {
                    hurting.xPower = reflectDir.x * 0.1;
                    hurting.yPower = reflectDir.y * 0.1;
                    hurting.zPower = reflectDir.z * 0.1;
                }

                projectile.hasImpulse = true;
                projectile.hurtMarked = true;
                playDeflectEffects(player, projectile);
            }
        }
    }

    private List<Projectile> getSortedProjectiles(Player player, double radius) {
        AABB area = player.getBoundingBox().inflate(radius);
        return player.level().getEntitiesOfClass(Projectile.class, area, p -> p.getOwner() != player)
                .stream()
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .toList();
    }
    /// ////////////////////////////////////////////////////////////USE///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.MAIN_HAND) pPlayer.startUsingItem(pUsedHand);
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        if (pLivingEntity instanceof Player player) {
            if (isUpgraded(pStack)) upgradedDeleteNearbyProjectiles(player, pStack);
            else deleteNearbyProjectiles(player, pStack);
        }
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 9999999;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }
    /// ////////////////////////////////////////////////////////////UPGRADE METHODS//////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!pLevel.isClientSide && pEntity instanceof ServerPlayer serverPlayer) {
            pStack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(cap -> {
                if (cap.isUpgraded()) UpgradeSwordTimer.tick(pStack, serverPlayer);
            });
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public void tryUpgradeLogic(ItemStack stack, ServerPlayer player) {
        if (stack.is(ModItems.DEFLECT_SWORD.get()) && canUpgrade(stack) && !isUpgraded(stack)) {
            if (player.isCreative() || getUpgradeData(stack) >= ModCommonConfigs.ARROW_RENDER_UPGRADE_AMOUNT.get()) {
                setUpgraded(stack, true);
                ModMessages.sendToClients(new UpgradeSwordSERVERS2CPacket(player.getId()));

                if (player.level() instanceof ServerLevel sl) {
                    Vec3 p = player.getEyePosition();
                    sl.sendParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 3, 0.1, 0.1, 0.1, 0.01);
                    sl.playSound(null, p.x, p.y, p.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, .6F, .5F);
                }
            }
        }
    }

    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {

            customData.setData(Math.min(data, ModCommonConfigs.ARROW_RENDER_UPGRADE_AMOUNT.get()));
        });
    }

    public static boolean canUpgrade(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, stack) > 0;
    }


    public static Enchantment getUpgradeEnchantment() {
        return Enchantments.SWEEPING_EDGE;
    }

    @Override
    public void resetUpgradeState(ItemStack stack) {
        super.resetUpgradeState(stack);
        setUpgradeData(stack, 0);
    }

    public static boolean isUpgraded(ItemStack stack) {
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(c -> c.isUpgraded()).orElse(false);
    }

    public static void setUpgraded(ItemStack stack, boolean upgraded) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(c -> {
            if (c.isUpgraded() != upgraded) {
                UpgradeSwordTimer.setUpgradeTimer(stack, ModCommonConfigs.ARROW_RENDER_UPGRADE_TIME.get());
                c.setUpgraded(upgraded);
            }
        });
    }
    /// ////////////////////////////////////////////////////////////ATTRIBUTES/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            double bonus = isUpgraded(stack) ? ModCommonConfigs.ARROW_RENDER_UPGRADE_BONUS_DAMAGE.get() : 0;
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damageSupplier.get() + bonus, AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", speedSupplier.get(), AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damageSupplier.get(), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", speedSupplier.get(), AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }
    /// ////////////////////////////////////////////////////////////VISUALS+SOUNDS//////////////////////////////////////////////////////////////////////////////////////
    private void playDeflectEffects(Player player, Projectile projectile) {
        player.level().playSound(player, projectile.blockPosition(), ModSounds.PROJECTILE_SLASH.get(), SoundSource.PLAYERS, 0.1f, 1f);
        Vec3 away = projectile.position().subtract(player.getEyePosition(1.0f)).normalize();
        ModMessages.sendToClients(new DeflectParticleS2CPacket(projectile.getX(), projectile.getY(), projectile.getZ(), away.x, away.y, away.z));
    }
    /// ////////////////////////////////////////////////////////////ITEM VISUALS//////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Component getName(ItemStack pStack) {
        if (isUpgraded(pStack))
            return Component.translatable("item.rajswordmod.deflect_sword_awakened").withStyle(s -> s.withColor(0x9E7FFF));
        return super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = isUpgraded(stack) ? "awakened" : "unawakened";
        tooltip.add(Component.translatable("rajswordmod.hovertext.deflect_sword_" + key + "_lore").withStyle(s -> s.withColor(0xBFA5FF).withItalic(true)));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("rajswordmod.hovertext.hold_shift"));
        } else {
            tooltip.add(Component.translatable("rajswordmod.hovertext.deflect_sword_" + key));
            if (!isUpgraded(stack)) {
                if (EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack) == 0) {
                    tooltip.add(Component.translatable("rajswordmod.tooltip.arrow_render_enchant"));
                } else {
                    int count = getUpgradeData(stack);
                    int req = ModCommonConfigs.ARROW_RENDER_UPGRADE_AMOUNT.get();
                    tooltip.add(count < req ? Component.translatable("rajswordmod.tooltip.projectiles_count_no", count, req) : Component.translatable("rajswordmod.tooltip.projectiles_count_yes"));
                }
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return isUpgraded(pStack) || super.isBarVisible(pStack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (isUpgraded(stack)) {
            int remainingTime = UpgradeSwordTimer.getUpgradeTimer(stack);
            int maxTime = ModCommonConfigs.ARROW_RENDER_UPGRADE_TIME.get();
            return Math.round(13.0F * (float) remainingTime / (float) maxTime);
        }
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isUpgraded(stack) ? 0xFF00FF : super.getBarColor(stack);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return !isUpgraded(stack);
    }
    /// ////////////////////////////////////////////////////////////EVENTS//////////////////////////////////////////////////////////////////////////////////////
    @SubscribeEvent
    public static void projectileFailsafe(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult result && result.getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();
            if (stack.is(ModItems.DEFLECT_SWORD.get()) && player.isUsingItem()) {
                Projectile projectile = event.getProjectile();
                Vec3 away = projectile.position().subtract(player.getEyePosition(1.0f)).normalize();

                ModMessages.sendToClients(new DeflectParticleS2CPacket(projectile.getX(), projectile.getY(), projectile.getZ(), away.x, away.y, away.z));
                event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);

                if (!player.isCreative()) stack.setDamageValue(stack.getDamageValue() + 1);
            }
        }
    }
}