package com.github.salandora.RideableRavagers.mixin;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.RavagerEntityRenderer;
import net.minecraft.client.render.entity.model.RavagerEntityModel;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RavagerEntityRenderer.class)
public abstract class RavagerEntityRendererMixin extends MobEntityRenderer<RavagerEntity, RavagerEntityModel> {
	public RavagerEntityRendererMixin(EntityRendererFactory.Context context, RavagerEntityModel entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(method = "getTexture(Lnet/minecraft/entity/mob/RavagerEntity;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
	public void rideableravagers$getTexture(RavagerEntity ravagerEntity, CallbackInfoReturnable<Identifier> cir) {
		if (!((Saddleable)ravagerEntity).isSaddled()) {
			cir.setReturnValue(new Identifier("rideableravagers", "textures/entity/illager/ravager.png"));
		}
	}
}
