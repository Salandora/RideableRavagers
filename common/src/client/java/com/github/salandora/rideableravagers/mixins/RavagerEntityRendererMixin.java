package com.github.salandora.rideableravagers.mixins;

import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.monster.Ravager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RavagerRenderer.class)
public abstract class RavagerEntityRendererMixin extends MobRenderer<Ravager, RavagerModel> {
	public RavagerEntityRendererMixin(EntityRendererProvider.Context context, RavagerModel entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/monster/Ravager;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
	public void rideableravagers$getTexture(Ravager ravagerEntity, CallbackInfoReturnable<ResourceLocation> cir) {
		if (!((Saddleable)ravagerEntity).isSaddled()) {
			cir.setReturnValue(new ResourceLocation("rideableravagers", "textures/entity/illager/ravager.png"));
		}
	}
}
