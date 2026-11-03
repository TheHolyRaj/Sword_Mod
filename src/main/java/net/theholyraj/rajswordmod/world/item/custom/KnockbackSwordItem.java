package net.theholyraj.rajswordmod.world.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.theholyraj.rajswordmod.client.particle.ModParticles;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.UpgradeSwordSERVERS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class KnockbackSwordItem extends UpgradeSwordItem {
    private final Supplier<Double> damageSupplier;
    private final Supplier<Double> speedSupplier;

    public KnockbackSwordItem(Tier pTier, Supplier<Double> damage, Supplier<Double> speed, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
        this.damageSupplier = damage;
        this.speedSupplier = speed;
    }
    ////////////////////////////////////////////////////////////////ABILITY//////////////////////////////////////////////////////////////////////////////////////////////
    private void newKnockbackAttack(Level pLevel, Player pPlayer, int pCharge, ItemStack pStack){
        Vec3 view = pPlayer.getViewVector(1f).normalize();
        Vec3 pos = pPlayer.getEyePosition(1.0f);
        if (pLevel.isClientSide) {
            spawnKnockbackParticles(pLevel, view, pos);
        }
        playSound(pLevel, pPlayer);
        if (!pLevel.isClientSide()){
            if (!pPlayer.isCreative()){
                EquipmentSlot slot = pPlayer.getMainHandItem() == pStack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                pStack.hurtAndBreak(1, pPlayer, entity -> entity.broadcastBreakEvent(slot));
            }
        }

        double reach = 4;
        double radius = 1.0;
        Vec3 end = pos.add(view.scale(reach));
        AABB attackArea = new AABB(pos, end).inflate(radius);
        for (Entity entity : pLevel.getEntities(pPlayer, attackArea)){
            if (!(entity instanceof LivingEntity hitEntity)){
                continue;
            }
            Vec3 hitEntitiesArrow = hitEntity.getBoundingBox().getCenter().subtract(pos);
            double forward = hitEntitiesArrow.dot(view);
            if (forward < 0 || forward > reach){
                continue;
            }

            double lateralFromArrow = hitEntitiesArrow.subtract(view.scale(forward)).length();
            if (lateralFromArrow - (hitEntity.getBbWidth() / 2) > radius){
                continue;
            }

            if (!pLevel.isClientSide){
                float damage = 4;
                double knockbackEnchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, pPlayer);
                double launchPower = ((pCharge * 0.5) + 1.0 + (knockbackEnchantLevel * 0.3))
                        * (ModCommonConfigs.KNOCKBACK_SWORD_POWER.get() / 100.0);
                Vec3 launchVel = new Vec3(view.x * launchPower, Math.max(0.5, view.y) * launchPower, view.z * launchPower);
                if (hitEntity.hurt(pLevel.damageSources().playerAttack(pPlayer), damage)) {
                    if (!isUpgraded(pStack)) addUpgradeAmount(pStack, 1);
                }
                hitEntity.setDeltaMovement(launchVel);
                hitEntity.hurtMarked = true;
            }
        }
    }
    private void pullEntities(Level level, Player player, float powerMult) {
        Vec3 look = player.getLookAngle();
        Vec3 suctionCenter = player.getEyePosition().subtract(0, 1.0, 0).add(look.scale(2.5));
        double radius = 4.0;
        AABB zone = new AABB(suctionCenter.subtract(radius, radius, radius), suctionCenter.add(radius, radius, radius));

        for (Entity entity : level.getEntities(player, zone)) {
            if (entity instanceof LivingEntity target && !target.isAlliedTo(player)) {
                Vec3 pullVec = suctionCenter.subtract(target.position());
                double distance = pullVec.length();
                if (distance <= radius) {
                    Vec3 dirToTarget = target.position().subtract(player.position()).normalize();
                    if (player.getLookAngle().dot(dirToTarget) > -0.1) {
                        if (distance > 0.6) {
                            target.setDeltaMovement(target.getDeltaMovement().add(pullVec.normalize().scale(0.45D * powerMult)));
                        } else {
                            target.setDeltaMovement(target.getDeltaMovement().multiply(0.01D, 0.01D, 0.01D));
                        }
                        target.hurtMarked = true;
                    }
                }
            }
        }
    }
    /// ////////////////////////////////////////////////////////////USE///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (pUsedHand == InteractionHand.MAIN_HAND) {
            pPlayer.startUsingItem(pUsedHand);
            stack.getOrCreateTag().putInt("PStage", 0);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        int chargeTime = getUseDuration(pStack) - pRemainingUseDuration;
        int maxCharge = ModCommonConfigs.KNOCKBACK_SWORD_CHARGE_TIME.get();

        if (!pLevel.isClientSide && pLivingEntity instanceof Player player) {
            CompoundTag tag = pStack.getOrCreateTag();
            int pStage = tag.getInt("PStage");

            int stage1 = maxCharge / 3;
            int stage2 = (maxCharge / 3) * 2;
            int stage3 = maxCharge;

            if (chargeTime >= stage1 && pStage < 1) {
                triggerPulse(pLevel, player, tag, 1, 0.8f, pStack);
            }
            if (chargeTime >= stage2 && pStage < 2) {
                triggerPulse(pLevel, player, tag, 2, 1.2f, pStack);
            }
            if (chargeTime >= stage3 && pStage < 3) {
                triggerPulse(pLevel, player, tag, 3, 1.6f, pStack);
            }

            if (isUpgraded(pStack)){
                boolean isSucking = (chargeTime >= stage1 && chargeTime < stage1 + 10) ||
                        (chargeTime >= stage2 && chargeTime < stage2 + 10) ||
                        (chargeTime >= stage3 && chargeTime < stage3 + 10);
                if (isSucking) pullEntities(pLevel, player, 1.2f);
            }
        }
        pLivingEntity.setDeltaMovement(pLivingEntity.getDeltaMovement().multiply(0.15D, 1.0D, 0.15D));
    }
    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        int chargeTime = getUseDuration(pStack) - pTimeCharged;
        int maxCharge = ModCommonConfigs.KNOCKBACK_SWORD_CHARGE_TIME.get();


        if (pLivingEntity instanceof Player player) {
            int cd = isUpgraded(pStack) ? ModCommonConfigs.KNOCKBACK_SWORD_UPGRADED_COOLDOWN.get() : ModCommonConfigs.KNOCKBACK_SWORD_COOLDOWN.get();
            player.getCooldowns().addCooldown(this, cd);

            if (chargeTime >= maxCharge) newKnockbackAttack(pLevel, player, 3, pStack);
            else if (chargeTime >= (maxCharge / 3 * 2)) newKnockbackAttack(pLevel, player, 2, pStack);
            else if (chargeTime >= (maxCharge / 3)) newKnockbackAttack(pLevel, player, 1, pStack);
        }
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
    }
    @Override
    public int getUseDuration(ItemStack s) { return 72000; }

    @Override
    public UseAnim getUseAnimation(ItemStack s) { return UseAnim.BLOCK; }

    private void triggerPulse(Level level, Player player, CompoundTag tag, int stage, float pitch, ItemStack pStack) {
        if (isUpgraded(pStack)){
            spawnBigSuctionParticle((ServerLevel)level, player);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.CHARGE_KNOCKBACK.get(), SoundSource.PLAYERS, 1.0f, pitch);

        tag.putInt("PStage", stage);
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
        if (stack.is(ModItems.KNOCKBACK_SWORD.get()) && canUpgrade(stack) && !isUpgraded(stack)) {
            if (player.isCreative() || getUpgradeData(stack) >= ModCommonConfigs.KNOCKBACK_SWORD_UPGRADE_AMOUNT.get()) {
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

    public static boolean canUpgrade(ItemStack s) { return EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), s) > 0; }
    public static Enchantment getUpgradeEnchantment() { return Enchantments.KNOCKBACK; }

    public static boolean isUpgraded(ItemStack stack) { return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(c -> c.isUpgraded()).orElse(false); }

    public static void setUpgraded(ItemStack stack, boolean upgraded) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(c -> {
            if (c.isUpgraded() != upgraded) {
                UpgradeSwordTimer.setUpgradeTimer(stack, ModCommonConfigs.KNOCKBACK_SWORD_UPGRADE_TIME.get());
                c.setUpgraded(upgraded);
            }
        });
    }

    public static void addUpgradeAmount(ItemStack stack, int amount){
        setUpgradeData(stack, getUpgradeData(stack) + amount);
    }

    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {

            customData.setData(Math.min(data, ModCommonConfigs.KNOCKBACK_SWORD_UPGRADE_AMOUNT.get()));
        });
    }

    public static int getUpgradeData(ItemStack s) {
        return s.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(c -> c.getData()).orElse(0);
    }

    @Override
    public void resetUpgradeState(ItemStack stack) {
        super.resetUpgradeState(stack);
        setUpgradeData(stack, 0);
    }
    /// ////////////////////////////////////////////////////////////ATTRIBUTES/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            double bonus = isUpgraded(stack) ? ModCommonConfigs.KNOCKBACK_SWORD_UPGRADE_BONUS_DAMAGE.get() : 0;
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
    private void spawnBigSuctionParticle(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 suctionCenter = player.getEyePosition().add(look.scale(2.5));
        level.sendParticles(ModParticles.KNOCKBACK_SUCTION_PARTICLES.get(), suctionCenter.x, suctionCenter.y, suctionCenter.z, 0, look.x, look.y, look.z, 1.0);
    }
    private void spawnKnockbackParticles(Level pLevel, Vec3 view, Vec3 pos){
        double blastSpeed = 0.35;
        Vec3 blastVel = view.scale(blastSpeed);
        for (int i = 0; i < 4; i++) {
            Vec3 spawn = pos.add(view.scale(i * 0.4));
            pLevel.addParticle(ModParticles.KNOCKBACK_SWORD_PARTICLES.get(), spawn.x, spawn.y, spawn.z, blastVel.x, blastVel.y, blastVel.z);
        }
    }
    private void playSound(Level pLevel, Player player){
        pLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1f, 1f);

    }
    ////////////////////////////////////////////////////////////////ITEM VISUALS//////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Component getName(ItemStack stack) {
        if (isUpgraded(stack)) return Component.translatable("item.rajswordmod.knockback_sword_awakened").withStyle(ChatFormatting.BLUE);
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean upgraded = isUpgraded(stack);
        String key = upgraded ? "awakened" : "unawakened";
        tooltip.add(Component.translatable("rajswordmod.hovertext.knockback_sword_" + key + "_lore").withStyle(s -> s.withColor(0x7777FF).withItalic(true)));
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("rajswordmod.hovertext.hold_shift"));
        } else {
            tooltip.add(Component.translatable("rajswordmod.hovertext.knockback_sword_" + key));
            if (!upgraded) {
                if (!canUpgrade(stack)) tooltip.add(Component.translatable("rajswordmod.tooltip.knockback_enchant"));
                else {
                    int current = getUpgradeData(stack);
                    int needed = ModCommonConfigs.KNOCKBACK_SWORD_UPGRADE_AMOUNT.get();
                    tooltip.add(current < needed ? Component.translatable("rajswordmod.tooltip.legs_broken_no", current, needed) : Component.translatable("rajswordmod.tooltip.legs_broken_yes"));
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
        if (isUpgraded(stack)){
            int remainingTime = UpgradeSwordTimer.getUpgradeTimer(stack);
            int maxTime = ModCommonConfigs.KNOCKBACK_SWORD_UPGRADE_TIME.get();
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
}