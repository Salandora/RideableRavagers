package com.github.salandora.RideableRavagers.mixin;

import com.github.salandora.RideableRavagers.entity.ai.goal.ActiveTargetAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(ActiveTargetGoal.class)
public abstract class ActiveTargetGoalMixin extends TrackTargetGoal implements ActiveTargetAccessor {
	@Shadow protected TargetPredicate targetPredicate;

	@Shadow @Final protected Class<? extends LivingEntity> targetClass;

	public ActiveTargetGoalMixin(MobEntity mob, boolean checkVisibility) {
		super(mob, checkVisibility);
	}

	@Override
	public Class<? extends LivingEntity> getTargetClass() {
		return this.targetClass;
	}

	@Override
	public void setPredicate(@Nullable Predicate<LivingEntity> predicate) {
		this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(predicate);
	}
}
