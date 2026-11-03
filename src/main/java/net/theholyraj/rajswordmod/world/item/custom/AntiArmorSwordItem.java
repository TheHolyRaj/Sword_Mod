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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.UpgradeSwordSERVERS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.entity.ModEntities;
import net.theholyraj.rajswordmod.world.entity.custom.AntiArmourAttackEntity;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;

import java.util.List;
import java.util.function.Supplier;

public class AntiArmorSwordItem extends UpgradeSwordItem {
    private final Supplier<Double> damageSupplier;
    private final Supplier<Double> speedSupplier;

    public AntiArmorSwordItem(Tier pTier, Supplier<Double> damage, Supplier<Double> speed, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
        this.damageSupplier = damage;
        this.speedSupplier = speed;
    }

    ////////////////////////////////////////////////////////////////ABILITY//////////////////////////////////////////////////////////////////////////////////////////////
    private void performChargeAttack(ItemStack stack, Level level, Player player, int realTimeCharged) {
        if (!level.isClientSide()){
            playSound(player);

            float charge = Math.min(1, (float)realTimeCharged / (float)ModCommonConfigs.ANTI_ARMOR_CHARGE_DURATION.get());
            Vec3 chargeChangedForwardView = player.getForward().multiply(charge,charge,charge);
            Vec3 forwardLaunch = chargeChangedForwardView.multiply(1.2,1,1.2).add(0,.33*charge,0);

            player.setDeltaMovement(forwardLaunch);
            player.hurtMarked = true;

            AntiArmourAttackEntity attack = new AntiArmourAttackEntity(ModEntities.ANTI_ARMOUR_ATTACK_ENTITY.get(), level);

            attack.setCharge(charge);
            attack.setOwner(player);
            if (isUpgraded(stack)){
                attack.setIsUpgraded();
            }
            Vec3 look = player.getLookAngle();
            Vec3 position = player.position()
                    .add(look.scale(2.75))
                    .add(0, 0, 0);
            attack.setPos(position.x, position.y, position.z);
            attack.setXRot(player.getXRot());
            attack.setYRot(player.getYRot());

            level.addFreshEntity(attack);
            player.getCooldowns().addCooldown(this, ModCommonConfigs.ANTI_ARMOR_CHARGE_COOLDOWN.get());
            if (!player.isCreative()){
                EquipmentSlot slot = player.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(slot));
            }
        }
    }
    /// ////////////////////////////////////////////////////////////USE///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int getUseDuration(ItemStack s) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.MAIN_HAND) {
            pPlayer.startUsingItem(pUsedHand);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        pLivingEntity.setDeltaMovement(pLivingEntity.getDeltaMovement().multiply(0.15D, 1.0D, 0.15D));
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        int realTimeCharged = 72000 - pTimeCharged;
        if (pLivingEntity instanceof Player player){
            performChargeAttack(pStack,pLevel,player,realTimeCharged);

        }
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
                if (cap.isUpgraded()) {
                    UpgradeSwordTimer.tick(pStack, serverPlayer);
                }
            });
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }


    public static Enchantment getUpgradeEnchantment(){
        return Enchantments.SHARPNESS;
    }

    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {

            customData.setData(Math.min(data, ModCommonConfigs.ANTI_ARMOR_UPGRADE_AMOUNT.get()));
        });
    }
    public static void setUpgraded(ItemStack stack, boolean data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {
            if (customData.isUpgraded() != data) {
                UpgradeSwordTimer.setUpgradeTimer(stack, ModCommonConfigs.ANTI_ARMOR_UPGRADE_TIME.get());
                customData.setUpgraded(data);
            }
        });
    }

    @Override
    public void resetUpgradeState(ItemStack stack) {
        super.resetUpgradeState(stack);
        setUpgradeData(stack, 0);
    }

    public static boolean isUpgraded(ItemStack stack) {
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY)
                .map(cap -> cap.isUpgraded())
                .orElse(false);
    }

    public static boolean canUpgrade(ItemStack stack){
        int sharpnessLevel = EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack);
        return !(sharpnessLevel ==0);
    }


    @Override
    public void tryUpgradeLogic(ItemStack stack, ServerPlayer player) {
        if (stack.is(ModItems.ANTI_ARMOR_SWORD.get()) && canUpgrade(stack) && !isUpgraded(stack)) {
            boolean explosionCheck = false;

            if (getUpgradeData(stack) >= ModCommonConfigs.ANTI_ARMOR_UPGRADE_AMOUNT.get()){explosionCheck = true;}
            if (player.isCreative()){explosionCheck = true;}
            if (explosionCheck) {
                AntiArmorSwordItem.setUpgraded(stack, true);

                ModMessages.sendToClients(new UpgradeSwordSERVERS2CPacket(player.getId()));

                if (player.level() instanceof ServerLevel serverLevel){
                    Vec3 look = player.getLookAngle();
                    Vec3 spawnPos = player.position().add(0, player.getEyeHeight(),0).add(look.scale(0.5D));
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, spawnPos.x, spawnPos.y, spawnPos.z,
                            3, 0.1, 0.1, 0.1, 0.01);
                    serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                            SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, .6F, .5F);
                }
            }
        }
    }

    /// ////////////////////////////////////////////////////////////ATTRIBUTES/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            double bonus = isUpgraded(stack) ? ModCommonConfigs.ANTI_ARMOR_UPGRADE_BONUS_DAMAGE.get() : 0;
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
   private void playSound(Player player){
       player.level().playSound(null, player.blockPosition(), ModSounds.ANTI_ARMOUR_CHARGE.get(), SoundSource.PLAYERS, 0.4f, 1f);
   }
    /// ////////////////////////////////////////////////////////////ITEM VISUALS//////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Component getName(ItemStack pStack) {
        if (isUpgraded(pStack)) return Component.translatable("item.rajswordmod.anti_armour_sword_awakened").withStyle(s -> s.withColor(0xD9E0E3));
        return super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = isUpgraded(stack) ? "awakened" : "unawakened";
        tooltip.add(Component.translatable("rajswordmod.hovertext.anti_armour_sword_" + key + "_lore").withStyle(s -> s.withColor(0xBFC7CB).withItalic(true)));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("rajswordmod.hovertext.hold_shift"));
        } else {
            tooltip.add(Component.translatable("rajswordmod.hovertext.anti_armour_sword_" + key));
            if (!isUpgraded(stack)) {
                if (EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack) == 0) {
                    tooltip.add(Component.translatable("rajswordmod.tooltip.anti_armour_sword_enchant"));
                } else {
                    int count = getUpgradeData(stack);
                    int req = ModCommonConfigs.ANTI_ARMOR_UPGRADE_AMOUNT.get();
                    tooltip.add(count < req ? Component.translatable("rajswordmod.tooltip.anti_armour_count_no", count, req) : Component.translatable("rajswordmod.tooltip.anti_armor_count_yes"));
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
            int maxTime = ModCommonConfigs.ANTI_ARMOR_UPGRADE_TIME.get();
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
