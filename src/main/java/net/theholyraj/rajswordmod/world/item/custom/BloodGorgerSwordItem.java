package net.theholyraj.rajswordmod.world.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.mobeffects.ModMobEffects;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.UpgradeSwordSERVERS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.bloodsword.BloodCapabilityProvider;
import net.theholyraj.rajswordmod.world.item.util.bloodsword.IBloodSwordData;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;


import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class BloodGorgerSwordItem extends UpgradeSwordItem {
    private final Supplier<Double> damageSupplier;
    private final Supplier<Double> speedSupplier;

    public BloodGorgerSwordItem(Tier pTier, Supplier<Double> damage, Supplier<Double> speed, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
        this.damageSupplier = damage;
        this.speedSupplier = speed;
    }
    ////////////////////////////////////////////////////////////////ABILITY//////////////////////////////////////////////////////////////////////////////////////////////
    private void healFromStoredBlood(Level level, Player player, ItemStack stack){
        if (!level.isClientSide()) {
            float customData = BloodGorgerSwordItem.getCustomData(stack);

            if (customData > 0) {
                player.heal(1.0F);
                if (getUpgradeData(stack) < ModCommonConfigs.BLOOD_GORGER_UPGRADE_AMOUNT.get()) {
                    addUpgradeAmount(stack, 1);
                }
                BloodGorgerSwordItem.setCustomData(stack, Math.max(customData - 1.0F, 0.0F));
            }
            else {
                player.stopUsingItem();
            }
        }

        if (isUpgraded(stack)){
            stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(customData -> {
                //
                for(UUID uuid : customData.getStoredDamageMap().keySet()) {
                    Entity entity = null;
                    if (level instanceof ServerLevel serverLevel) {
                        entity = serverLevel.getEntity(uuid);
                    }
                    if(entity instanceof LivingEntity target) {
                        float stored = customData.getStoredDamage(uuid);
                        float damage = Math.min(stored, ModCommonConfigs.BLOOD_GORGER_UPGRADE_DAMAGE_TICK.get());
                        if (damage > 0){
                            target.hurt(player.damageSources().playerAttack(player), damage);
                            customData.removeStoredDamage(uuid, damage);
                        }
                    }
                }
                customData.removeEmptyEntites();
            });
        }
    }
    /// ////////////////////////////////////////////////////////////USE///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.MAIN_HAND) {
            float customData = BloodGorgerSwordItem.getCustomData(pPlayer.getItemInHand(pUsedHand));
            if (customData > 0) {
                pPlayer.startUsingItem(pUsedHand);
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        int baseHealSpeed = ModCommonConfigs.BLOOD_GORGER_HEAL_SPEED.get();
        boolean isFullHealth = pLivingEntity.getHealth() >= pLivingEntity.getMaxHealth();

        int currentInterval = isFullHealth ? (baseHealSpeed * 3) : baseHealSpeed;

        if (pRemainingUseDuration % currentInterval == 0) {
            playSound(pLevel, pLivingEntity);

            if (pLivingEntity instanceof Player player){
                healFromStoredBlood(pLevel,player, pStack);
            }
        }
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, 20);
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }

    /// ////////////////////////////////////////////////////////////BLOODGORGE CUSTOM CAPABILITY METHODS//////////////////////////////////////////////////////////////////////////////////////////////
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new BloodCapabilityProvider();
    }

    public static void setCustomData(ItemStack stack, float data) {
        stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(customData -> {
            customData.setData(Math.min(data, ModCommonConfigs.BLOOD_GORGER_UPGRADE_AMOUNT.get()));
        });
    }
    public static int getUpgradeData(ItemStack stack) {
        return stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).map(IBloodSwordData::getUpgradeData).orElse(0);
    }

    public static float getCustomData(ItemStack stack) {
        return stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).map(IBloodSwordData::getData).orElse(0f);
    }
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag nbt = super.getShareTag(stack);
        if (nbt == null) {
            nbt = new CompoundTag();
        }
        final CompoundTag finalNbt = nbt;
        stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(cap -> {
            CompoundTag capTag = new CompoundTag();
            cap.writeToNBT(capTag);
            finalNbt.put("blood_data", capTag);
        });
        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null && nbt.contains("blood_data")) {
            stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(cap -> {
                cap.readFromNBT(nbt.getCompound("blood_data"));
            });
        }
    }
    ///////////////////////////////////////////////////////////////UPGRADE METHODS//////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!pLevel.isClientSide && pEntity instanceof ServerPlayer serverPlayer) {
            pStack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(cap -> {
                if (cap.isUpgraded()) {
                    UpgradeSwordTimer.tick(pStack, serverPlayer);
                }
            });
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    public static Enchantment getUpgradeEnchantment() {
        return Enchantments.MOB_LOOTING;
    }

    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(customData -> {
            customData.setUpgradeData(data);
        });
    }

    public static void setUpgraded(ItemStack stack, boolean data) {
        stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(customData -> {
            if (customData.isUpgraded() != data) {
                UpgradeSwordTimer.setUpgradeTimer(stack, ModCommonConfigs.BLOOD_GORGER_UPGRADE_TIME.get());
                customData.setUpgraded(data);
            }
        });
    }

    public static void addUpgradeAmount(ItemStack stack, int amount){
        int currentAmount = getUpgradeData(stack);
        setUpgradeData(stack, currentAmount+amount);
    }

    @Override
    public void resetUpgradeState(ItemStack stack) {
        stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(customData -> {
            customData.setUpgraded(false);
            customData.setUpgradeData(0);
            customData.clearStoredDamage();
        });
    }

    public static boolean isUpgraded(ItemStack stack) {
        return stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY)
                .map(cap -> cap.isUpgraded())
                .orElse(false);
    }

    public static boolean canUpgrade(ItemStack stack) {
        int lootinfLevel = EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack);
        return !(lootinfLevel == 0);
    }


    @Override
    public void tryUpgradeLogic(ItemStack stack, ServerPlayer player) {
        if (stack.is(ModItems.BLOOD_SWORD.get()) && canUpgrade(stack) && !isUpgraded(stack)) {
            boolean explosionCheck = false;
            if (getUpgradeData(stack) >= ModCommonConfigs.BLOOD_GORGER_UPGRADE_AMOUNT.get()) {
                explosionCheck = true;
            }
            if (player.isCreative()) {
                explosionCheck = true;
            }
            if (explosionCheck) {
                BloodGorgerSwordItem.setUpgraded(stack, true);

                ModMessages.sendToClients(new UpgradeSwordSERVERS2CPacket(player.getId()));

                if (player.level() instanceof ServerLevel serverLevel) {
                    Vec3 look = player.getLookAngle();
                    Vec3 spawnPos = player.position().add(0, player.getEyeHeight(), 0).add(look.scale(0.5D));
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
            double bonus = isUpgraded(stack) ? ModCommonConfigs.BLOOD_GORGER_UPGRADE_BONUS_DAMAGE.get() : 0;
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
    private void playSound(Level pLevel, LivingEntity pLivingEntity){
       float pitch = (pLevel.random.nextFloat() * 0.1F + 0.5F);

       pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(),
               SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, pitch);
   }
   /// ////////////////////////////////////////////////////////////ITEM VISUALS//////////////////////////////////////////////////////////////////////////////////////

   @Override
   public Component getName(ItemStack pStack) {
       if (isUpgraded(pStack)) return Component.translatable("item.rajswordmod.blood_sword_awakened").withStyle(s -> s.withColor(0x8B0000));
       return super.getName(pStack);
   }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = isUpgraded(stack) ? "awakened" : "unawakened";
        tooltip.add(Component.translatable("rajswordmod.hovertext.blood_sword_" + key + "_lore").withStyle(s -> s.withColor(0x9E3030).withItalic(true)));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("rajswordmod.hovertext.hold_shift"));
        } else {
            tooltip.add(Component.translatable("rajswordmod.hovertext.blood_sword_" + key));
            if (!isUpgraded(stack)) {
                if (EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack) == 0) {
                    tooltip.add(Component.translatable("rajswordmod.tooltip.blood_gorger_enchant"));
                } else {
                    int count = getUpgradeData(stack);
                    int req = ModCommonConfigs.BLOOD_GORGER_UPGRADE_AMOUNT.get();
                    tooltip.add(count < req ? Component.translatable("rajswordmod.tooltip.blood_count_no", count, req) : Component.translatable("rajswordmod.tooltip.blood_count_yes"));
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
            int maxTime = ModCommonConfigs.BLOOD_GORGER_UPGRADE_TIME.get();
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
    public static void storeDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();

            if (stack.is(ModItems.BLOOD_SWORD.get())) {
                float currentStored = BloodGorgerSwordItem.getCustomData(stack);

                float maxStorage = ModCommonConfigs.BLOOD_GORGER_STORED_HEALTH_MAX.get().floatValue();
                float baseLifesteal = ModCommonConfigs.BLOOD_GORGER_LIFESTEAL_PERCENT.get().floatValue();
                int lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, stack);
                float lifestealMult = baseLifesteal + (lootingLevel * 0.05f);

                if (currentStored < maxStorage) {
                    float addedHealth = event.getAmount() * lifestealMult;
                    float newTotal = currentStored + addedHealth;
                    float finalData = Math.min(newTotal, maxStorage);
                    BloodGorgerSwordItem.setCustomData(stack, finalData);
                }
            }
        }
    }
    @SubscribeEvent
    public static void upgradeStoreEnemyDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();

            if (stack.is(ModItems.BLOOD_SWORD.get())) {
                stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(data -> {

                    if (data.isUpgraded()) {
                        float configMult = ModCommonConfigs.BLOOD_GORGER_UPGRADE_DAMAGE_STORED_PERCENTAGE.get().floatValue();
                        float damage = event.getAmount()*configMult;
                        if (player.isUsingItem() && player.getUseItem().is(ModItems.BLOOD_SWORD.get())) {
                        }
                        else {
                            data.addStoredDamage(
                                    event.getEntity().getUUID(),
                                    damage
                            );
                        }
                    }
                });
            }
        }
    }
}
