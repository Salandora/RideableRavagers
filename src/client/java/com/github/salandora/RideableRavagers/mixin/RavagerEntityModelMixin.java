package com.github.salandora.RideableRavagers.mixin;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.RavagerEntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.RavagerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RavagerEntityModel.class)
public abstract class RavagerEntityModelMixin extends SinglePartEntityModel<RavagerEntity> {
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		if (this.child) {
			matrices.push();
			float f = 1.0F / 2.0f;
			matrices.scale(f, f, f);
			matrices.translate(0.0F, 24.0F / 16.0F, 0.0F);
			super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
			matrices.pop();
		} else {
			super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
		}
	}
}
