package net.theholyraj.rajswordmod.client.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.custom.BloodGorgerSwordItem;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeCapabilityProvider;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;

public class ModItemProperties {
    public static void makeProperties() {
        makeBlockingItem(ModItems.DEFLECT_SWORD.get());
        makeBlockingItem(ModItems.SENTINEL_SWORD.get());
        makeBlockingItem(ModItems.KNOCKBACK_SWORD.get());
        makeBlockingItem(ModItems.ANTI_ARMOR_SWORD.get());
        registerCustomProperty(ModItems.BLOOD_SWORD.get());

        makeUpgradingItem(ModItems.DEFLECT_SWORD.get());
        makeUpgradingItem(ModItems.SENTINEL_SWORD.get());
        makeUpgradingItem(ModItems.KNOCKBACK_SWORD.get());
        makeUpgradingItem(ModItems.HOLY_SWORD.get());
        makeUpgradingItem(ModItems.GAIA_SWORD.get());
        makeUpgradingItem(ModItems.ANTI_ARMOR_SWORD.get());
        makeUpgradingItem(ModItems.BLOOD_SWORD.get());
        makeUpgradingItem(ModItems.SHADOW_CLONE_SWORD.get());


    }


    private static void makeBlockingItem(Item item) {
        ItemProperties.register(item, new ResourceLocation("blocking"), (p_174575_, p_174576_, p_174577_, p_174578_) -> {
            return p_174577_ != null && p_174577_.isUsingItem() && p_174577_.getUseItem() == p_174575_ ? 1.0F : 0.0F;
        });
    }
    private static void makeUpgradingItem(Item item){
        ItemProperties.register(item, new ResourceLocation("upgraded"),
                (stack, level, entity, seed) -> {
            if (stack.getItem() instanceof UpgradeSwordItem && !(stack.getItem() instanceof BloodGorgerSwordItem)) {

                return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY)
                        .map(cap -> cap.isUpgraded() ? 1.0F : 0.0F)
                        .orElse(0.0F);
            }
            else if (stack.is(ModItems.SENTINEL_SWORD.get())){

                return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY)
                        .map(cap -> cap.isUpgraded() ? 1.0F : 0.0F)
                        .orElse(0.0F);
            }
            else if (stack.is(ModItems.KNOCKBACK_SWORD.get())){

                return stack.getCapability(ModCapabilities.UPGRADE_DATA_CAPABILITY)
                        .map(cap -> cap.isUpgraded() ? 1.0F : 0.0F)
                        .orElse(0.0F);
            }
            else if (stack.is(ModItems.BLOOD_SWORD.get())){
                return stack.getCapability(ModCapabilities.BLOOD_DATA_CAPABILITY)
                        .map(cap -> cap.isUpgraded() ? 1.0F : 0.0F)
                        .orElse(0.0F);
            }
            return 0.0F;
        });

    }

    private static void registerCustomProperty(Item item) {
        ItemProperties.register(item, new ResourceLocation("blood"), (stack, world, entity, seed) -> {
            if (stack.getItem() instanceof BloodGorgerSwordItem) {
                float customData = BloodGorgerSwordItem.getCustomData(stack);
                return (int)Math.min(customData / (ModCommonConfigs.BLOOD_GORGER_STORED_HEALTH_MAX.get()/10), 9);
            }
            return 0.0F;
        });
    }
}

