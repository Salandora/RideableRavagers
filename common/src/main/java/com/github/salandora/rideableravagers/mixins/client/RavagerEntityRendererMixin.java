package com.github.salandora.rideableravagers.mixins.client;

import com.github.salandora.rideableravagers.client.RideableRavagersClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RavagerRenderer.class)
public abstract class RavagerEntityRendererMixin extends MobRenderer<Ravager, RavagerRenderState, RavagerModel> {
	@Unique
	private static final ResourceLocation UNSADDLED_TEXTURE = ResourceLocation.fromNamespaceAndPath("rideableravagers", "textures/entity/illager/ravager.png");

	@Unique
	private RavagerModel rideableRavagers$adultModel;
	@Unique
	private RavagerModel rideableRavagers$babyModel;

	@Unique
	private ItemStack rideableRavagers$isSaddle;

	public RavagerEntityRendererMixin(EntityRendererProvider.Context context, RavagerModel entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(
			method = "<init>",
			at = @At("RETURN")
	)
	public void rideableRavagers$addBabyModel(EntityRendererProvider.Context context, CallbackInfo ci) {
		this.rideableRavagers$adultModel = this.model;
		this.rideableRavagers$babyModel = new RavagerModel(context.bakeLayer(RideableRavagersClient.RAVAGER_BABY));
	}

	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/monster/Ravager;Lnet/minecraft/client/renderer/entity/state/RavagerRenderState;F)V",
			at = @At("HEAD")
	)
	public void rideableRavagers$extractRenderState(Ravager ravager, RavagerRenderState ravagerRenderState, float f, CallbackInfo ci) {
		this.rideableRavagers$isSaddle = ravager.getItemBySlot(EquipmentSlot.SADDLE);
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/RavagerRenderState;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
	public void rideableravagers$getTexture(RavagerRenderState ravagerRenderState, CallbackInfoReturnable<ResourceLocation> cir) {
		if (rideableRavagers$isSaddle.isEmpty()) {
			cir.setReturnValue(UNSADDLED_TEXTURE);
		}
	}

	@Override
	public void render(RavagerRenderState livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.model = livingEntityRenderState.isBaby ? this.rideableRavagers$babyModel : this.rideableRavagers$adultModel;
		super.render(livingEntityRenderState, poseStack, multiBufferSource, i);
	}
}
