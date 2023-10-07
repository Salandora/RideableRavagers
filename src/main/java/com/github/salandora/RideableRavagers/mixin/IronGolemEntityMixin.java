package com.github.salandora.RideableRavagers.mixin;

import com.github.salandora.RideableRavagers.entity.Tamed;
import com.github.salandora.RideableRavagers.entity.ai.goal.ActiveTargetAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolemEntity.class)
public class IronGolemEntityMixin extends GolemEntity {
	protected IronGolemEntityMixin(EntityType<? extends GolemEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initGoals", at = @At("TAIL"))
	private void ridableravagers$initGoals(CallbackInfo ci) {
		this.targetSelector.getGoals().stream()
				.filter(goal -> goal.getGoal() instanceof ActiveTargetGoal<?>)
				.map(goal -> (ActiveTargetGoal<?>)goal.getGoal())
				.forEach(goal -> {
					ActiveTargetAccessor accessor = (ActiveTargetAccessor)goal;
					Class<?> targetClazz = accessor.getTargetClass();
					if (targetClazz == MobEntity.class) {
						accessor.setPredicate(entity -> {
								boolean tamed = entity instanceof Tamed tamedEntity && tamedEntity.isTamed();
								return entity instanceof Monster && !(entity instanceof CreeperEntity) && !tamed;
							}
						);
					}
				});
	}
}
