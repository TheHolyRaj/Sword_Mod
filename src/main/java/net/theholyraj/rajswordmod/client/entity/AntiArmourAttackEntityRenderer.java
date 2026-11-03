package net.theholyraj.rajswordmod.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.theholyraj.rajswordmod.world.entity.custom.AntiArmourAttackEntity;

public class AntiArmourAttackEntityRenderer extends EntityRenderer<AntiArmourAttackEntity> {

    public AntiArmourAttackEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AntiArmourAttackEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(AntiArmourAttackEntity entity) {
        return null;
    }
}
