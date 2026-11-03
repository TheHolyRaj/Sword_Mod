package net.theholyraj.rajswordmod.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.theholyraj.rajswordmod.world.entity.custom.CloneEntity;

import java.util.UUID;

public class CloneEntityRenderer extends HumanoidMobRenderer<CloneEntity, CloneEntityModel> {
    private final CloneEntityModel wideModel;
    private final CloneEntityModel slimModel;

    public CloneEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new CloneEntityModel(pContext.bakeLayer(ModModelLayers.CLONE_ENTITY_LAYER), false), 0.5f);
        this.wideModel =this.model;
        this.slimModel = new CloneEntityModel(pContext.bakeLayer(ModModelLayers.CLONE_ENTITY_LAYER_SLIM), true);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel(pContext.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel(pContext.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                pContext.getModelManager()));
    }

    @Override
    public void render(CloneEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        this.model = isSlim(pEntity) ? slimModel : wideModel;
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    private boolean isSlim(CloneEntity entity){
        UUID uuid = entity.getPlayerUUID();
        if (uuid == null) return false;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            PlayerInfo info = connection.getPlayerInfo(uuid);
            if (info != null) return "slim".equals(info.getModelName());
        }
        return "slim".equals(DefaultPlayerSkin.getSkinModelName(uuid));
    }
    @Override
    public ResourceLocation getTextureLocation(CloneEntity pEntity) {
        UUID uuid = pEntity.getPlayerUUID();
        if (uuid == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null){
            PlayerInfo info = connection.getPlayerInfo(pEntity.getPlayerUUID());
            if (info != null){
                return info.getSkinLocation();
            }
        }
        return DefaultPlayerSkin.getDefaultSkin(uuid);
    }
}
