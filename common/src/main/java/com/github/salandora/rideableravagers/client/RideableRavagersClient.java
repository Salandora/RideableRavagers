package com.github.salandora.rideableravagers.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class RideableRavagersClient {
	public static final ModelLayerLocation RAVAGER_BABY = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("rideableravagers", "ravager_baby"), "main");

	public static LayerDefinition createBabyRavagerLayer() {
		LayerDefinition layerDefinition = RavagerModel.createBodyLayer();
		float scaleFactor = 1.0F / 2.0f;
		// poseStack.translate(0.0F, 24.0F / 16.0F, 0.0F);
		return layerDefinition.apply(MeshTransformer.scaling(scaleFactor));
	}
}
