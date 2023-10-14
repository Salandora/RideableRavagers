package com.github.salandora.rideableravagers.mixins;

import com.github.salandora.rideableravagers.entity.Tamed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolem.class)
public class IronGolemEntityMixin extends AbstractGolem {
	protected IronGolemEntityMixin(EntityType<? extends AbstractGolem> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "registerGoals", at = @At("TAIL"))
	private void ridableravagers$initGoals(CallbackInfo ci) {
		this.targetSelector.getAvailableGoals().stream()
				.filter(goal -> goal.getGoal() instanceof net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<?>)
				.map(goal -> (NearestAttackableTargetGoal<?>)goal.getGoal())
				.forEach(goal -> {
					NearestAttackableTargetGoalAccessor accessor = (NearestAttackableTargetGoalAccessor)goal;
					Class<?> targetClazz = accessor.getTargetType();
					if (targetClazz == Mob.class) {
						accessor.getTargetConditions().selector(entity -> {
								boolean tamed = entity instanceof Tamed tamedEntity && tamedEntity.isTamed();
								return entity instanceof Enemy && !(entity instanceof Creeper) && !tamed;
							}
						);
					}
				});
	}
}
