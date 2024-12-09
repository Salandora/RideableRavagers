package com.github.salandora.rideableravagers.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.world.entity.monster.Ravager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(RavagerModel.class)
public abstract class RavagerEntityModelMixin extends HierarchicalModel<Ravager> {
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		if (this.young) {
			poseStack.pushPose();
			float f = 1.0F / 2.0f;
			poseStack.scale(f, f, f);
			poseStack.translate(0.0F, 24.0F / 16.0F, 0.0F);
			super.renderToBuffer(poseStack, vertexConsumer, light, overlay, color);
			poseStack.popPose();
		} else {
			super.renderToBuffer(poseStack, vertexConsumer, light, overlay, color);
		}
	}
}
