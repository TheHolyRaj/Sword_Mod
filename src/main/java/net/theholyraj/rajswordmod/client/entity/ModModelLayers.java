package net.theholyraj.rajswordmod.client.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.theholyraj.rajswordmod.SwordMod;

public class ModModelLayers {
     public static final ModelLayerLocation GAIA_PROJECTILE_LAYER = new ModelLayerLocation(
            new ResourceLocation(SwordMod.MODID, "gaia_projectile_layer"), "gaia_projectile_layer");

    public static final ModelLayerLocation CLONE_ENTITY_LAYER = new ModelLayerLocation(
            new ResourceLocation(SwordMod.MODID, "clone_entity_layer"), "clone_entity_layer");
    public static final ModelLayerLocation CLONE_ENTITY_LAYER_SLIM = new ModelLayerLocation(
            new ResourceLocation(SwordMod.MODID, "clone_entity_layer_slim"), "clone_entity_layer_slim");


}
