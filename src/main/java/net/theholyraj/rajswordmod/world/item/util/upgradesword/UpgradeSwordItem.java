package net.theholyraj.rajswordmod.world.item.util.upgradesword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class UpgradeSwordItem extends SwordItem {
    public static Enchantment upgradeEnchant;
    public UpgradeSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);

    }
    public static final String SWORD_UUID_TAG = "SwordUUID";

    public static String getSwordUUID(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(SWORD_UUID_TAG)) {
            String uniqueID = UUID.randomUUID().toString();
            nbt.putString(SWORD_UUID_TAG, uniqueID);
            stack.setTag(nbt);
        }
        return nbt.getString(SWORD_UUID_TAG);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new UpgradeCapabilityProvider();
    }

    public static boolean canUpgrade(ItemStack stack){
        int sweepLevel = EnchantmentHelper.getItemEnchantmentLevel(upgradeEnchant, stack);
        return !(sweepLevel==0);
    }

    public static void setUpgradeData(ItemStack stack, int data) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {
            customData.setData(data);
        });
    }

    public static int getUpgradeData(ItemStack stack) {
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(IUpgradeSwordData::getData).orElse(0);
    }

    public static boolean isUpgraded(ItemStack stack){
        return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).map(IUpgradeSwordData::isUpgraded).orElse(false);
    }
    public static void setUpgraded(ItemStack stack, boolean data) {
    }
    public static Enchantment getUpgradeEnchantment(){
        return upgradeEnchant;
    }
    public static void addUpgradeAmount(ItemStack stack, int amount){
        int currentAmount = getUpgradeData(stack);
        setUpgradeData(stack, currentAmount+amount);
    }
        public void tryUpgradeLogic(ItemStack stack, ServerPlayer player) {
    }

    public void resetUpgradeState(ItemStack stack) {
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(customData -> {
            customData.setUpgraded(false);
        });
        stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY).ifPresent(customData -> {
            customData.setUpgraded(false);
            customData.clearStoredDamage();
        });
    }

    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag nbt = super.getShareTag(stack);
        if (nbt == null) {
            nbt = new CompoundTag();
        }
        final CompoundTag finalNbt = nbt;
        stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(cap -> {
            CompoundTag capTag = new CompoundTag();
            cap.writeToNBT(capTag);
            finalNbt.put("upgrade_data", capTag);
            finalNbt.putBoolean("isUpgraded", cap.isUpgraded());
            if (cap.isUpgraded()) {
                finalNbt.putInt("timer", UpgradeSwordTimer.getUpgradeTimer(stack));
            }
        });

        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null && nbt.contains("upgrade_data")) {
            stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY).ifPresent(cap -> {
                cap.readFromNBT(nbt.getCompound("upgrade_data"));
                if (nbt.contains("isUpgraded")) {
                    cap.setUpgraded(nbt.getBoolean("isUpgraded"));
                }
                if (cap.isUpgraded() && nbt.contains("timer")) {
                    UpgradeSwordTimer.setUpgradeTimer(stack, nbt.getInt("timer"));
                }
            });
        }
    }
}
