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
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.UpgradeSwordSERVERS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.enchantment.ModEnchantments;
import net.theholyraj.rajswordmod.world.explosion.HolyExplosion;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.holysword.HolyCapabilityProvider;
import net.theholyraj.rajswordmod.world.item.util.holysword.IHolySwordData;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class HolySwordItem extends UpgradeSwordItem {
    private final Supplier<Double> damageSupplier;
    private final Supplier<Double> speedSupplier;

    public HolySwordItem(Tier pTier, Supplier<Double> damage, Supplier<Double> speed, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
        this.damageSupplier = damage;
        this.speedSupplier = speed;
    }
    ////////////////////////////////////////////////////////////////ABILITY//////////////////////////////////////////////////////////////////////////////////////////////
   private void attemptHolyExplosion(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker){
       Random random = new Random();
       float chance = getCustomData(pStack);

       if (random.nextFloat(0,100) <= chance){
           int smiteLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, pStack);
           float explosionSize = 2.0f + (smiteLevel * 0.225f);
           HolyExplosion explosion = new HolyExplosion(pAttacker.level(), pAttacker, pTarget.position().x, pTarget.position().y + pTarget.getBbHeight()/2, pTarget.position().z, explosionSize, isUpgraded(pStack));
           explosion.explode();
           explosion.finalizeExplosion(true);
           if (getUpgradeData(pStack) < ModCommonConfigs.HOLY_SWORD_UPGRADE_AMOUNT.get()) {
               if (!isUpgraded(pStack)){
                   addUpgradeAmount(pStack, 1);
               }
           }
           setCustomData(pStack, (float) (double) ModCommonConfigs.HOLY_EXPLOSION_CHANCE.get());
       }
       else {
           setCustomData(pStack, ModCommonConfigs.HOLY_EXPLOSION_CHANCE.get().floatValue());
       }
   }
    /// ////////////////////////////////////////////////////////////USE///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (pTarget.getMobType() == MobType.UNDEAD){
            attemptHolyExplosion(pStack,pTarget,pAttacker);
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }
    /// ////////////////////////////////////////////////////////////CUSTOM HOLYSWORD CAPABILITY METHODS/////////////////////////////////////////////////////////////////////////////////////////
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new HolyCapabilityProvider();
    }

    public void setCustomData(ItemStack stack, float data) {
        stack.getCapability(ModCapabilities.HOLY_DATA_CAPABILITY).ifPresent(cap -> cap.setData(data));
    }

    public float getCustomData(ItemStack stack) {
        return stack.getCapability(ModCapabilities.HOLY_DATA_CAPABILITY).map(IHolySwordData::getData).orElse(2.5f);
    }
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag nbt = super.getShareTag(stack);
        if (nbt == null) nbt = new CompoundTag();
        final CompoundTag finalNbt = nbt;
        stack.getCapability(ModCapabilities.HOLY_DATA_CAPABILITY).ifPresent(cap -> {
            CompoundTag capTag = new CompoundTag();
            cap.writeToNBT(capTag);
            finalNbt.put("holy_data", capTag);
        });
        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null && nbt.contains("holy_data")) {
            stack.getCapability(ModCapabilities.HOLY_DATA_CAPABILITY).ifPresent(cap -> cap.readFromNBT(nbt.getCompound("holy_data")));
        }
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

    @Override
    public void tryUpgradeLogic(ItemStack stack, ServerPlayer player) {
        if (stack.is(ModItems.HOLY_SWORD.get()) && canUpgrade(stack) && !isUpgraded(stack)) {
            boolean explosionCheck = false;

            if (getUpgradeData(stack) >= ModCommonConfigs.HOLY_SWORD_UPGRADE_AMOUNT.get()) { explosionCheck = true; }
            if (player.isCreative()) { explosionCheck = true; }

            if (explosionCheck) {
                HolySwordItem.setUpgraded(stack, true);

                ModMessages.sendToClients(new UpgradeSwordSERVERS2CPacket(player.getId()));

                if (player.level() instanceof ServerLevel serverLevel){
                    Vec3 look = player.getLookAngle();
                    Vec3 spawnPos = player.position().add(0, player.getEyeHeight(),0).add(look.scale(0.5D));
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, spawnPos.x, spawnPos.y, spawnPos.z, 3, 0.1, 0.1, 0.1, 0.01);
                    serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, .6F, .5F);
                }
            }
        }
    }

    public static boolean canUpgrade(ItemStack stack){
        int smiteLevel = EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack);
        return smiteLevel > 0;
    }

    public static Enchantment getUpgradeEnchantment(){
        return Enchantments.SMITE;
    }
    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {
            customData.setData(Math.min(data, ModCommonConfigs.HOLY_SWORD_UPGRADE_AMOUNT.get()));
        });
    }

    public static int getUpgradeData(ItemStack stack) {
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(cap -> cap.getData()).orElse(0);
    }

    public static void setUpgraded(ItemStack stack, boolean data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(cap -> {
            if (cap.isUpgraded() != data) {
                UpgradeSwordTimer.setUpgradeTimer(stack, ModCommonConfigs.HOLY_SWORD_UPGRADE_TIME.get());
                cap.setUpgraded(data);
            }
        });
    }

    public static boolean isUpgraded(ItemStack stack) {
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(cap -> cap.isUpgraded()).orElse(false);
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
            double bonus = isUpgraded(stack) ? ModCommonConfigs.HOLY_SWORD_UPGRADE_BONUS_DAMAGE.get() : 0;
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
    /// ////////////////////////////////////////////////////////////ITEM VISUALS//////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Component getName(ItemStack stack) {
        if (isUpgraded(stack)) return Component.translatable("item.rajswordmod.holy_sword_awakened").withStyle(ChatFormatting.YELLOW);
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean upgraded = isUpgraded(stack);
        String key = upgraded ? "awakened" : "unawakened";
        tooltip.add(Component.translatable("rajswordmod.hovertext.holy_sword_" + key + "_lore").withStyle(s -> s.withColor(0xFFFFBB).withItalic(true)));
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("rajswordmod.hovertext.hold_shift"));
        } else {
            tooltip.add(Component.translatable("rajswordmod.hovertext.holy_sword_" + key));
            if (!upgraded) {
                if (!canUpgrade(stack)) tooltip.add(Component.translatable("rajswordmod.tooltip.holy_enchant"));
                else {
                    int current = getUpgradeData(stack);
                    int needed = ModCommonConfigs.HOLY_SWORD_UPGRADE_AMOUNT.get();
                    tooltip.add(current < needed ? Component.translatable("rajswordmod.tooltip.holy_explosion_no", current, needed) : Component.translatable("rajswordmod.tooltip.holy_explosion_yes"));
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
            int maxTime = ModCommonConfigs.HOLY_SWORD_UPGRADE_TIME.get();
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
    public static void doHolyExplosion(LivingDeathEvent event){
        LivingEntity entity = event.getEntity();
        if (!event.getSource().isIndirect()){
            if (event.getSource().getEntity() instanceof Player player){
                if (player.getMainHandItem().is(ModItems.HOLY_SWORD.get())){
                    if (entity.getMobType() == MobType.UNDEAD){
                        int smiteLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, player.getMainHandItem());
                        float explosionSize = 2.0f + (smiteLevel * 0.225f);
                        HolyExplosion explosion = new HolyExplosion(player.level(), player, entity.position().x, entity.position().y + entity.getBbHeight()/2, entity.position().z, explosionSize, isUpgraded(player.getMainHandItem()));
                        explosion.explode();
                        explosion.finalizeExplosion(true);
                        if (getUpgradeData(player.getMainHandItem()) < ModCommonConfigs.HOLY_SWORD_UPGRADE_AMOUNT.get()) {
                            if (!HolySwordItem.isUpgraded(player.getMainHandItem())){
                                addUpgradeAmount(player.getMainHandItem(), 1);
                            }
                        }
                    }
                }
            }
        }
    }

}