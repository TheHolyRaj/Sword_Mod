package net.theholyraj.rajswordmod.world.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = SwordMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEnchantmentEffects {
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("73967812-326b-4e63-9f8d-4e2079f291e0");
    @SubscribeEvent
    public static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.isEnchanted()) {
            int level = stack.getEnchantmentLevel(ModEnchantments.ARMOURED.get());

            if (level > 0) {
                if (event.getSlotType() == EquipmentSlot.MAINHAND) {
                    UUID uuid = UUID.fromString("d51b8f90-7f6a-4c57-bbf2-4f5dbe8b5e9a");
                    AttributeModifier modifier = new AttributeModifier(
                            uuid, "armor_boost_enchant", ModCommonConfigs.ARMOURED_ENCHANT.get() * level,
                            AttributeModifier.Operation.ADDITION
                    );
                    event.addModifier(Attributes.ARMOR, modifier);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            Player player = event.player;
            ItemStack mainHand = player.getMainHandItem();

            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.REACH.get(), mainHand);
            AttributeInstance reachAttr = player.getAttribute(ForgeMod.ENTITY_REACH.get());

            if (reachAttr != null) {
                reachAttr.removeModifier(REACH_MODIFIER_UUID);

                if (level > 0) {
                    AttributeModifier modifier = new AttributeModifier(REACH_MODIFIER_UUID, "Enchantment Reach",
                            (double) level * ModCommonConfigs.REACH_ENCHANT.get(), AttributeModifier.Operation.ADDITION);
                    reachAttr.addPermanentModifier(modifier);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || player.level().isClientSide()) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        int enchantLevel = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.BACKSTABBER.get(), weapon);
        if (enchantLevel <= 0){
            return;
        }

        LivingEntity victim = event.getEntity();
        Vec3 distanceToAttacker = player.getBoundingBox().getCenter()
                .subtract(victim.getBoundingBox().getCenter());
        distanceToAttacker = new Vec3(distanceToAttacker.x,0, distanceToAttacker.z);

        if (distanceToAttacker.lengthSqr() < 1.0E-6) {
            return;
        }

        Vec3 targetForward = victim.getLookAngle();
        targetForward = new Vec3(
                targetForward.x,
                0.0,
                targetForward.z
        );

        if (targetForward.lengthSqr() < 1.0E-6) {
            return;
        }

        targetForward = targetForward.normalize();
        distanceToAttacker = distanceToAttacker.normalize();

        if (targetForward.dot(distanceToAttacker) >=-0.5){
            return;
        }

        float bonusMultiplier = 1.0F + (ModCommonConfigs.BACKSTABBER_ENCHANT.get() * enchantLevel);
        event.setAmount(event.getAmount() * bonusMultiplier);
    }
}
