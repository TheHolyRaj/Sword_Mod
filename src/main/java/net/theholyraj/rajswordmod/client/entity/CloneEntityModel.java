package net.theholyraj.rajswordmod.client.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.entity.custom.CloneEntity;

import java.util.function.Function;

public class CloneEntityModel extends PlayerModel<CloneEntity> {
    public CloneEntityModel(ModelPart pRoot, boolean pSlim) {
        super(pRoot, pSlim);
    }
    public static LayerDefinition createBodyLayer(boolean isSlim) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(CubeDeformation.NONE, isSlim);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}
