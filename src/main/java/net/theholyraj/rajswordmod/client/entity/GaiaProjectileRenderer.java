package net.theholyraj.rajswordmod.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.entity.custom.DashProjectileEntity;
import net.theholyraj.rajswordmod.world.entity.custom.GaiaProjectileEntity;

public class GaiaProjectileRenderer extends EntityRenderer<GaiaProjectileEntity> {
    // --- NORMAL TEXTURES ---
    private static final ResourceLocation FRAME_1 = new ResourceLocation(SwordMod.MODID, "textures/entity/gaia_projectile.png");
    private static final ResourceLocation FRAME_2 = new ResourceLocation(SwordMod.MODID, "textures/entity/gaia_projectile2.png");
    private static final ResourceLocation FRAME_3 = new ResourceLocation(SwordMod.MODID, "textures/entity/gaia_projectile3.png");
    private static final ResourceLocation FRAME_4 = new ResourceLocation(SwordMod.MODID, "textures/entity/gaia_projectile4.png");

    // --- AWAKENED TEXTURES ---
    private static final ResourceLocation AWAKENED_1 = new ResourceLocation(SwordMod.MODID, "textures/entity/gaia_projectile_awakened1.png");
    private static final ResourceLocation AWAKENED_2 = new ResourceLocation(SwordMod.MODID, "textures/entity/gaia_projectile_awakened2.png");
    private static final ResourceLocation AWAKENED_3 = new ResourceLocation(SwordMod.MODID, "textures/entity/gaia_projectile_awakened3.png");
    private static final ResourceLocation AWAKENED_4 = new ResourceLocation(SwordMod.MODID, "textures/entity/gaia_projectile_awakened4.png");

    public GaiaProjectileModel<GaiaProjectileEntity> model;

    public GaiaProjectileRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        model = new GaiaProjectileModel<>(pContext.bakeLayer(ModModelLayers.GAIA_PROJECTILE_LAYER));
    }

    @Override
    public void render(GaiaProjectileEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.tickCount < 2) return;

        int brightness = 0xF000F0;
        VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityCutout(getTextureLocation(pEntity)));

        pPoseStack.pushPose();

        float f = Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot());
        float g = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());

        pPoseStack.mulPose(Axis.YP.rotationDegrees(f - 90.0F));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(g));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0F));

        pPoseStack.translate(0.0, -1.4, 0.0);

        model.renderToBuffer(pPoseStack, vertexConsumer, brightness, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);

        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, brightness);
    }

    @Override
    public ResourceLocation getTextureLocation(GaiaProjectileEntity entity) {
        int age = entity.tickCount;
        boolean upgraded = entity.isUpgraded();
        int speed = 4;
        int frame = (age / speed) % 4;

        if (upgraded) {
            return switch (frame) {
                case 0 -> AWAKENED_1;
                case 1 -> AWAKENED_2;
                case 2 -> AWAKENED_3;
                default -> AWAKENED_4;
            };
        } else {
            return switch (frame) {
                case 0 -> FRAME_1;
                case 1 -> FRAME_2;
                case 2 -> FRAME_3;
                default -> FRAME_4;
            };
        }
    }
}