package net.theholyraj.rajswordmod.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.enchantment.ModEnchantments;
import net.theholyraj.rajswordmod.world.item.custom.AntiArmorSwordItem;
import net.theholyraj.rajswordmod.world.item.custom.KnockbackSwordItem;

@Mod.EventBusSubscriber(modid = SwordMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientForgeEvents {
    private static final ResourceLocation BACKSTABBER_GUI_IMAGE = new ResourceLocation(SwordMod.MODID, "textures/gui/backstabber_gui.png");

    @SubscribeEvent
    public static void onFOVUpdate(ViewportEvent.ComputeFov event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getUseItem();
        if (player.isUsingItem() && stack.getItem() instanceof KnockbackSwordItem
                || player.isUsingItem() && stack.getItem() instanceof AntiArmorSwordItem) {
            float maxCharge;
            if (stack.getItem() instanceof AntiArmorSwordItem){
                maxCharge = (float) ModCommonConfigs.ANTI_ARMOR_CHARGE_DURATION.get();
            }
            else {
                maxCharge = (float) ModCommonConfigs.KNOCKBACK_SWORD_CHARGE_TIME.get();
            }
            int ticksUsed = player.getTicksUsingItem();
            float fovModifier = (float) ticksUsed / maxCharge;
            if (fovModifier > 1.0F) {
                fovModifier = 1.0F;
            }
            else {
                fovModifier *= fovModifier;
            }
            event.setFOV(event.getFOV() * (1.0F - fovModifier * 0.15F));
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getUseItem();
        Item item = stack.getItem();
        boolean shouldShake = (item instanceof KnockbackSwordItem && KnockbackSwordItem.isUpgraded(stack))
                || (item instanceof AntiArmorSwordItem && AntiArmorSwordItem.isUpgraded(stack));
        if (shouldShake) {
            int useTicks = player.getTicksUsingItem();
            float intensity = Math.min((float) useTicks / 30.0f, 1.0f);
            float shakeStrength = 0.4f * intensity;
            event.setPitch(event.getPitch() + (player.getRandom().nextFloat() - 0.5f) * shakeStrength);
            event.setYaw(event.getYaw() + (player.getRandom().nextFloat() - 0.5f) * shakeStrength);
            event.setRoll(event.getRoll() + (player.getRandom().nextFloat() - 0.5f) * (shakeStrength * 1.5f));
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getItemStack().getItem() instanceof KnockbackSwordItem
                || event.getItemStack().getItem() instanceof AntiArmorSwordItem){
            float maxCharge;
            if (event.getItemStack().getItem() instanceof KnockbackSwordItem){
                 maxCharge = (float) ModCommonConfigs.KNOCKBACK_SWORD_CHARGE_TIME.get();
            }
            else {
                maxCharge = (float) ModCommonConfigs.ANTI_ARMOR_CHARGE_DURATION.get();
            }
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            int ticksUsed = player.getTicksUsingItem();
            float shakeModifier = (float) ticksUsed / maxCharge;
            if (shakeModifier > 1){
                shakeModifier = 1;
            }
            if (player.isUsingItem()){
                float shake = (float)Math.sin(System.currentTimeMillis() * 0.05) * 0.01f;
                shake = shake * shakeModifier;
                event.getPoseStack().translate(shake, 0, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id() != VanillaGuiOverlay.CROSSHAIR.id()){
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        int enchantLevel = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.BACKSTABBER.get(), player.getMainHandItem());

        if (player == null
                || !(minecraft.hitResult instanceof EntityHitResult hitResult)
                || !(hitResult.getEntity() instanceof LivingEntity entity)
                || enchantLevel <= 0){
            return;
        }
        if (!canBackstab(player,entity)){
            return;
        }
        GuiGraphics gui = event.getGuiGraphics();

        int x = event.getWindow().getGuiScaledWidth() / 2;
        int y = event.getWindow().getGuiScaledHeight() / 2;
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x-4, y - 8, 0);
        pose.scale(2,2,1);

        gui.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        gui.blit(BACKSTABBER_GUI_IMAGE, -5, 0, 0, 0, 4, 8, 4, 8);
        gui.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        pose.popPose();
    }

    public static boolean canBackstab(Player player, LivingEntity victim) {
        Vec3 targetForward = victim.getLookAngle();
        targetForward = new Vec3(
                targetForward.x,
                0.0,
                targetForward.z
        );

        if (targetForward.lengthSqr() < 1.0E-6) {
            return false;
        }
        Vec3 distanceToAttacker = player.getBoundingBox().getCenter()
                .subtract(victim.getBoundingBox().getCenter());
        distanceToAttacker = new Vec3(distanceToAttacker.x,0, distanceToAttacker.z);

        if (distanceToAttacker.lengthSqr() < 1.0E-6) {
            return false;
        }

        targetForward = targetForward.normalize();
        distanceToAttacker = distanceToAttacker.normalize();

        if (targetForward.dot(distanceToAttacker) >=-0.5){
            return false;
        }
        return true;
    }
}