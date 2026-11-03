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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.UpgradeSwordSERVERS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.enchantment.ModEnchantments;
import net.theholyraj.rajswordmod.world.entity.ModEntities;
import net.theholyraj.rajswordmod.world.entity.custom.CloneEntity;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class ShadowCloneSwordItem extends UpgradeSwordItem {
    private final Supplier<Double> damageSupplier;
    private final Supplier<Double> speedSupplier;

    public ShadowCloneSwordItem(Tier pTier, Supplier<Double> damage, Supplier<Double> speed, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
        this.damageSupplier = damage;
        this.speedSupplier = speed;
    }

    ////////////////////////////////////////////////////////////////ABILITY//////////////////////////////////////////////////////////////////////////////////////////////
    private void spawnCloneEntity(Level pLevel, Player pPlayer, InteractionHand pUsedHand){
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        int backstabLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BACKSTABBER.get(), stack);
        int bonusTime = ModCommonConfigs.DARK_DECEIVER_ENCHANT_BONUS_TIME.get() * backstabLevel;
        CloneEntity entity = new CloneEntity(ModEntities.CLONE_ENTITY.get(),pLevel);
        entity.bonusLifeTime = bonusTime;
        if (ShadowCloneSwordItem.isUpgraded(stack)){
            entity.setUpgraded();
            entity.addCopyOfPlayersEquipment(pPlayer);
        }
        entity.setPlayerUUID(pPlayer.getUUID());
        Vec3 pos = pPlayer.position();
        entity.moveTo(pos.x, pos.y, pos.z, pPlayer.getYRot(), pPlayer.getXRot());
        pLevel.addFreshEntity(entity);
        tauntEnemiesToClone(pPlayer,entity, pUsedHand);

        pPlayer.getCooldowns().addCooldown(this, ModCommonConfigs.DARK_DECEIVER_COOLDOWN.get()+bonusTime);
        pPlayer.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ModCommonConfigs.SHADOW_CLONE_LIFESPAN.get()+bonusTime,1));
        if (!pPlayer.isCreative()){
            EquipmentSlot slot = pPlayer.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, pPlayer, swordEntity -> swordEntity.broadcastBreakEvent(slot));
        }
    }

    private void tauntEnemiesToClone(Player player, CloneEntity clone, InteractionHand pUsedHand){
        Vec3 center = new Vec3(player.blockPosition().getX(),player.blockPosition().getY(),player.blockPosition().getZ());
        List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, new AABB(center,center).inflate(50), e-> true).stream()
                .sorted(Comparator.comparingDouble(ent -> ent.distanceToSqr(center))).toList();

        for (Mob mob: mobs){
            if (mob.getTarget() == player){
                clone.addTargetToTargetSelector(mob);
                addUpgradeAmount(player.getItemInHand(pUsedHand), 1);
                mob.setTarget(clone);
            }
        }
    }
    /// ////////////////////////////////////////////////////////////USE///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            spawnCloneEntity(pLevel, pPlayer, pUsedHand);
        }
        spawnSmokeParticles(pLevel,pPlayer);
        playSound(pLevel, pPlayer);
        return super.use(pLevel, pPlayer, pUsedHand);
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
        return ModEnchantments.BACKSTABBER.get();
    }

    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {

            customData.setData(Math.min(data, ModCommonConfigs.DARK_DECEIVER_UPGRADE_AMOUNT.get()));
        });
    }
    @Override public void resetUpgradeState(ItemStack s) {
        super.resetUpgradeState(s); setUpgradeData(s, 0);
    }

    public static void setUpgraded(ItemStack stack, boolean data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {
            if (customData.isUpgraded() != data) {
                UpgradeSwordTimer.setUpgradeTimer(stack, ModCommonConfigs.ANTI_ARMOR_UPGRADE_TIME.get());
                customData.setUpgraded(data);
            }
        });
    }
    public static boolean isUpgraded(ItemStack stack) {
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY)
                .map(cap -> cap.isUpgraded())
                .orElse(false);
    }

    public static boolean canUpgrade(ItemStack stack){
        int backstabLevel = EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack);
        return !(backstabLevel ==0);
    }
    @Override
    public void tryUpgradeLogic(ItemStack stack, ServerPlayer player) {
        if (stack.is(ModItems.SHADOW_CLONE_SWORD.get()) && canUpgrade(stack) && !isUpgraded(stack)) {
            boolean explosionCheck = false;

            if (getUpgradeData(stack) >= ModCommonConfigs.ANTI_ARMOR_UPGRADE_AMOUNT.get()){explosionCheck = true;}
            if (player.isCreative()){explosionCheck = true;}
            if (explosionCheck) {
                ShadowCloneSwordItem.setUpgraded(stack, true);

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
            double bonus = isUpgraded(stack) ? ModCommonConfigs.DARK_DECEIVER_UPGRADE_BONUS_DAMAGE.get() : 0;
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
    private void playSound(Level pLevel, Player pPlayer){
        pLevel.playSound(pPlayer,pPlayer.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f,0.6f);

    }
    private void spawnSmokeParticles(Level level, Player player) {
        double radius = 1.3;
        int particleCount = 20;

        for (int i = 0; i < particleCount; i++) {
            double xOffset = (level.random.nextDouble() - 0.5) * 2 * radius;
            double zOffset = (level.random.nextDouble() - 0.5) * 2 * radius;
            double yOffset = (level.random.nextDouble() - 0.5) * 2 * 0.5;
            double x = player.getX() + xOffset;
            double y = player.getY() + 0.5 + yOffset;
            double z = player.getZ() + zOffset;

            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.1, 0.0);
        }
    }
    /// ////////////////////////////////////////////////////////////ITEM VISUALS//////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Component getName(ItemStack pStack) {
        if (isUpgraded(pStack)) return Component.translatable("item.rajswordmod.shadow_clone_sword_awakened").withStyle(s -> s.withColor(0x202020));
        return super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = isUpgraded(stack) ? "awakened" : "unawakened";
        tooltip.add(Component.translatable("rajswordmod.hovertext.shadow_clone_sword_" + key + "_lore").withStyle(s -> s.withColor(0x808080).withItalic(true)));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("rajswordmod.hovertext.hold_shift"));
        } else {
            tooltip.add(Component.translatable("rajswordmod.hovertext.shadow_clone_sword_" + key));
            if (!isUpgraded(stack)) {
                if (EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack) == 0) {
                    tooltip.add(Component.translatable("rajswordmod.tooltip.shadow_clone_sword_enchant"));
                } else {
                    int count = getUpgradeData(stack);
                    int req = ModCommonConfigs.DARK_DECEIVER_UPGRADE_AMOUNT.get();
                    tooltip.add(count < req ? Component.translatable("rajswordmod.tooltip.shadow_clone_count_no", count, req) : Component.translatable("rajswordmod.tooltip.shadow_clone_count_yes"));
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
            int maxTime = ModCommonConfigs.DARK_DECEIVER_UPGRADE_TIME.get();
            return Math.round(13.0F * (float) remainingTime / (float) maxTime);
        }
        return super.getBarWidth(stack);
    }
    public static void addUpgradeAmount(ItemStack stack, int amount){
        int currentAmount = getUpgradeData(stack);
        setUpgradeData(stack, Math.min(currentAmount+amount, ModCommonConfigs.DARK_DECEIVER_UPGRADE_AMOUNT.get()));
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
