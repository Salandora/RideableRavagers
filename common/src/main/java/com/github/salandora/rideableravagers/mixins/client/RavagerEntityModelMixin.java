package com.github.salandora.rideableravagers.mixins.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RavagerModel.class)
public abstract class RavagerEntityModelMixin extends EntityModel<RavagerRenderState> {
	protected RavagerEntityModelMixin(ModelPart modelPart) {
		super(modelPart);
	}
}
