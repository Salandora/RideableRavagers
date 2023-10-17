package com.github.salandora.rideableravagers.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.world.entity.monster.Ravager;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(RavagerModel.class)
public abstract class RavagerEntityModelMixin extends HierarchicalModel<Ravager> {
	@Override
	public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		if (this.young) {
			matrices.pushPose();
			float f = 1.0F / 2.0f;
			matrices.scale(f, f, f);
			matrices.translate(0.0F, 24.0F / 16.0F, 0.0F);
			super.renderToBuffer(matrices, vertices, light, overlay, red, green, blue, alpha);
			matrices.popPose();
		} else {
			super.renderToBuffer(matrices, vertices, light, overlay, red, green, blue, alpha);
		}
	}
}
