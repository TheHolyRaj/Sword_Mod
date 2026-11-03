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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.client.particle.ModParticles;
import net.theholyraj.rajswordmod.network.ModMessages;
import net.theholyraj.rajswordmod.network.packet.UpgradeSwordSERVERS2CPacket;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.enchantment.ModEnchantments;
import net.theholyraj.rajswordmod.world.explosion.SentinelExplosion;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class SentinelSwordItem extends UpgradeSwordItem {

    private final Supplier<Double> damageSupplier;
    private final Supplier<Double> speedSupplier;

    public SentinelSwordItem(Tier pTier, Supplier<Double> damage, Supplier<Double> speed, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
        this.damageSupplier = damage;
        this.speedSupplier = speed;
    }

    ////////////////////////////////////////////////////////////////ABILITY//////////////////////////////////////////////////////////////////////////////////////////////
   private void upgradeAbilityExplosion(Player player, ItemStack stack, Level level){
       float blockedDamage = getAbsorbedDamage(stack);

       if (blockedDamage > 0) {
           float explosionRadius = Math.min(8.0f, 3.0f + (blockedDamage / 10f));
           Vec3 pos = new Vec3(player.getEyePosition().x, player.getEyePosition().y - 1f, player.getEyePosition().z);

           SentinelExplosion explosion = new SentinelExplosion(level, player, pos, explosionRadius, blockedDamage);
           explosion.explode();

           playSound(level,player);
       }
       resetAbsorbedDamage(stack);
       player.hurtMarked = true;
   }

    public static void addAbsorbedDamage(ItemStack stack, float amount) {
        stack.getOrCreateTag().putFloat("absorbedDamage", getAbsorbedDamage(stack) + amount);
    }

    public static float getAbsorbedDamage(ItemStack stack) {
        return stack.getOrCreateTag().getFloat("absorbedDamage");
    }

    public static void resetAbsorbedDamage(ItemStack stack) {
        stack.getOrCreateTag().putFloat("absorbedDamage", 0);
    }
    /// ////////////////////////////////////////////////////////////USE///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.MAIN_HAND) {
            pPlayer.startUsingItem(pUsedHand);
            pLevel.playSound(pPlayer, pPlayer.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5f, 2f);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        if (pLevel.isClientSide) {
            pLivingEntity.setDeltaMovement(Vec3.ZERO);
            pLevel.addParticle(ModParticles.SENTINEL_DEFENCE_PARTICLES.get(),
                    pLivingEntity.getX(), pLivingEntity.getY() + 1, pLivingEntity.getZ(), 0, 0, 0);
        }
        pLivingEntity.resetFallDistance();
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity instanceof Player player) {
            Level level = player.level();

            int guardedLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOURED.get(), stack);
            int baseCooldown = isUpgraded(stack) ?
                    ModCommonConfigs.SENTINEL_SWORD_UPGRADE_COOLDOWN.get() :
                    ModCommonConfigs.SENTINEL_SWORD_COOLDOWN.get();

            player.getCooldowns().addCooldown(this, Math.max(0, baseCooldown - (5 * guardedLevel)));

            if (isUpgraded(stack) && !level.isClientSide()) {
                upgradeAbilityExplosion(player, stack, level);
            }
        }
        super.onStopUsing(stack, entity, count);
    }

    @Override public int getUseDuration(ItemStack s) {
        return isUpgraded(s) ? ModCommonConfigs.SENTINEL_SWORD_UPGRADE_CHARGE_TIME.get() : ModCommonConfigs.SENTINEL_SWORD_CHARGE_TIME.get();
    }

    @Override public UseAnim getUseAnimation(ItemStack s) {
        return UseAnim.BLOCK;
    }
    /// ////////////////////////////////////////////////////////////UPGRADE METHODS//////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!pLevel.isClientSide && pEntity instanceof ServerPlayer sp) {
            pStack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(cap -> {
                if (cap.isUpgraded()) UpgradeSwordTimer.tick(pStack, sp);
            });
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public void tryUpgradeLogic(ItemStack stack, ServerPlayer player) {
        if (stack.is(ModItems.SENTINEL_SWORD.get()) && canUpgrade(stack) && !isUpgraded(stack)) {
            if (player.isCreative() || getUpgradeData(stack) >= ModCommonConfigs.SENTINEL_SWORD_UPGRADE_AMOUNT.get() * 100) {
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

    public static void addUpgradeAmount(ItemStack stack, float amount) {
        int current = getUpgradeData(stack);
        setUpgradeData(stack, current + Math.round(amount * 100));
    }

    public static boolean canUpgrade(ItemStack s) { return EnchantmentHelper.getItemEnchantmentLevel(getUpgradeEnchantment(), s) > 0; }
    public static Enchantment getUpgradeEnchantment() { return ModEnchantments.ARMOURED.get(); }
    @Override public void resetUpgradeState(ItemStack s) {
        super.resetUpgradeState(s); setUpgradeData(s, 0);
    }

    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {
            customData.setData(Math.min(data, ModCommonConfigs.SENTINEL_SWORD_UPGRADE_AMOUNT.get()*100));
        });
    }
    public static int getUpgradeData(ItemStack stack) {
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(c -> c.getData()).orElse(0);
    }
    public static void setUpgraded(ItemStack stack, boolean upgrade) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(c -> {
            if (c.isUpgraded() != upgrade) {
                UpgradeSwordTimer.setUpgradeTimer(stack, ModCommonConfigs.SENTINEL_SWORD_UPGRADE_TIME.get());
                c.setUpgraded(upgrade);
            }
        });
    }

    public static boolean isUpgraded(ItemStack s) { return s.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(c -> c.isUpgraded()).orElse(false); }

    /// ////////////////////////////////////////////////////////////ATTRIBUTES/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            double bonus = isUpgraded(stack) ? ModCommonConfigs.SENTINEL_SWORD_UPGRADE_BONUS_DAMAGE.get() : 0;
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
   private void playSound(Level level , Player player){
       level.playSound(null, player.getX(), player.getY(), player.getZ(),
               SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.5f, 0.5f);
   }
    /// ////////////////////////////////////////////////////////////ITEM VISUALS//////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Component getName(ItemStack stack) {
        if (isUpgraded(stack)) return Component.translatable("item.rajswordmod.sentinel_sword_awakened").withStyle(s -> s.withColor(0xC04B4B));
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean upgraded = isUpgraded(stack);
        String key = upgraded ? "awakened" : "unawakened";
        tooltip.add(Component.translatable("rajswordmod.hovertext.sentinel_sword_" + key + "_lore").withStyle(s -> s.withColor(0xCC6666).withItalic(true)));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("rajswordmod.hovertext.hold_shift"));
        } else {
            tooltip.add(Component.translatable("rajswordmod.hovertext.sentinel_sword_" + key));
            if (!upgraded) {
                if (!canUpgrade(stack)) {
                    tooltip.add(Component.translatable("rajswordmod.tooltip.armoured_enchant"));
                } else {
                    float current = getUpgradeData(stack) / 100f;
                    float needed = ModCommonConfigs.SENTINEL_SWORD_UPGRADE_AMOUNT.get();
                    tooltip.add(current < needed ? Component.translatable("rajswordmod.tooltip.blocked_damage_count_no", current, needed) : Component.translatable("rajswordmod.tooltip.blocked_damage_count_yes"));
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
            int maxTime = ModCommonConfigs.SENTINEL_SWORD_UPGRADE_TIME.get();
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
    public static void blockDamageEvent(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();
            if (stack.is(ModItems.SENTINEL_SWORD.get()) && player.isUsingItem()) {
                if (!player.isInvulnerable()) {
                    Level level = player.level();
                    if (isUpgraded(stack)) addAbsorbedDamage(stack, event.getAmount());

                    level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_BREAK, SoundSource.PLAYERS, 0.5f, 1f);

                    if (getUpgradeData(stack) < (ModCommonConfigs.SENTINEL_SWORD_UPGRADE_AMOUNT.get() * 100)) {
                        addUpgradeAmount(stack, event.getAmount());
                    }
                    Entity attacker = event.getSource().getEntity();
                    if (attacker instanceof LivingEntity livingAttacker) {
                        double x = player.getX() - livingAttacker.getX();
                        double z = player.getZ() - livingAttacker.getZ();
                        livingAttacker.knockback(1.0, x, z);
                    }
                    if (!player.isCreative()){
                        EquipmentSlot slot = player.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                        stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(slot));
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

}