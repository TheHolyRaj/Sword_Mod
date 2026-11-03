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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.UpgradeSwordSERVERS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.enchantment.ModEnchantments;
import net.theholyraj.rajswordmod.world.entity.custom.GaiaProjectileEntity;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class GaiaBladeItem extends UpgradeSwordItem {
    private final Supplier<Double> damageSupplier;
    private final Supplier<Double> speedSupplier;

    public GaiaBladeItem(Tier pTier, Supplier<Double> damage, Supplier<Double> speed, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
        this.damageSupplier = damage;
        this.speedSupplier = speed;
    }

    ////////////////////////////////////////////////////////////////ABILITY//////////////////////////////////////////////////////////////////////////////////////////////
    private void handleGaiaAbility(Player player, ItemStack stack) {
        Level level = player.level();

        if (!level.isClientSide() && player.getAttackStrengthScale(0.0F) >= 1.0F) {
            float baseSpeed = 2F;
            int reachLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.REACH.get(), stack);
            float finalSpeed = baseSpeed + (reachLevel * .35F);
            GaiaProjectileEntity projectile = new GaiaProjectileEntity(level, player, stack, finalSpeed);


            projectile.xo = projectile.getX();
            projectile.yo = projectile.getY();
            projectile.zo = projectile.getZ();

            level.addFreshEntity(projectile);

            playSound(level, player);

            if (!player.isCreative()){
                EquipmentSlot slot = player.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(slot));
            }
        }
    }
    /// ////////////////////////////////////////////////////////////USE///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (pAttacker instanceof Player player) {
            handleGaiaAbility(player, pStack);
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            handleGaiaAbility(player, stack);
        }
        return super.onEntitySwing(stack, entity);
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
        if (stack.is(ModItems.GAIA_SWORD.get()) && canUpgrade(stack) && !isUpgraded(stack)) {
            boolean canAwaken = player.isCreative() || getUpgradeData(stack) >= ModCommonConfigs.GAIA_BLADE_UPGRADE_AMOUNT.get();

            if (canAwaken) {
                setUpgraded(stack, true);
                ModMessages.sendToClients(new UpgradeSwordSERVERS2CPacket(player.getId()));

                if (player.level() instanceof ServerLevel serverLevel) {
                    Vec3 look = player.getLookAngle();
                    Vec3 spawnPos = player.position().add(0, player.getEyeHeight(), 0).add(look.scale(0.5D));
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, spawnPos.x, spawnPos.y, spawnPos.z, 3, 0.1, 0.1, 0.1, 0.01);
                    serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, .6F, .5F);
                }
            }
        }
    }

    public static boolean isUpgraded(ItemStack stack) {
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(c -> c.isUpgraded()).orElse(false);
    }

    public static void setUpgraded(ItemStack stack, boolean upgraded) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(c -> {
            if (c.isUpgraded() != upgraded) {
                c.setUpgraded(upgraded);
                if (upgraded) {
                    UpgradeSwordTimer.setUpgradeTimer(stack, ModCommonConfigs.GAIA_BLADE_UPGRADE_TIME.get());
                } else {
                    setUpgradeData(stack, 0);
                }
            }
        });
    }

    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {

            customData.setData(Math.min(data, ModCommonConfigs.GAIA_BLADE_UPGRADE_AMOUNT.get()));
        });
    }
    public static void addUpgradeAmount(ItemStack stack, int amount){
        if (getUpgradeData(stack) < ModCommonConfigs.GAIA_BLADE_UPGRADE_AMOUNT.get()){
            int currentAmount = getUpgradeData(stack);
            setUpgradeData(stack, currentAmount+amount);
        }
    }

    public static boolean canUpgrade(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack) > 0;
    }

    public static Enchantment getUpgradeEnchantment() {
        return ModEnchantments.REACH.get();
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
            double bonus = isUpgraded(stack) ? ModCommonConfigs.GAIA_BLADE_UPGRADE_BONUS_DAMAGE.get() : 0;
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
    ///  ////////////////////////////////////////////////////////////VISUALS+SOUNDS//////////////////////////////////////////////////////////////////////////////////////
    private void playSound(Level level, Player player){
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GAIA_SWING.get(), SoundSource.PLAYERS, 1.0F, 1.5F);
    }
    /// ////////////////////////////////////////////////////////////ITEM VISUALS//////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Component getName(ItemStack pStack) {
        if (isUpgraded(pStack)) return Component.translatable("item.rajswordmod.gaia_sword_awakened").withStyle(s -> s.withColor(0x55FF55));
        return super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = isUpgraded(stack) ? "awakened" : "unawakened";
        tooltip.add(Component.translatable("rajswordmod.hovertext.gaia_sword_" + key + "_lore").withStyle(s -> s.withColor(0xAAFFAA).withItalic(true)));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("rajswordmod.hovertext.hold_shift"));
        } else {
            tooltip.add(Component.translatable("rajswordmod.hovertext.gaia_sword_" + key));
            if (!isUpgraded(stack)) {
                if (EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), stack) == 0) {
                    tooltip.add(Component.translatable("rajswordmod.tooltip.gaia_enchant"));
                } else {
                    int count = getUpgradeData(stack);
                    int req = ModCommonConfigs.GAIA_BLADE_UPGRADE_AMOUNT.get();
                    tooltip.add(count < req ? Component.translatable("rajswordmod.tooltip.projectiles_hits_no", count, req) : Component.translatable("rajswordmod.tooltip.projectiles_hits_yes"));
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
            int maxTime = ModCommonConfigs.GAIA_BLADE_UPGRADE_TIME.get();
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
