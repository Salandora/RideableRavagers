package com.github.salandora.RideableRavagers.entity.ai.goal;

import com.github.salandora.RideableRavagers.entity.Tamed;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.RavagerEntity;

import java.util.EnumSet;

public class RavagerAttackWithOwnerGoal extends TrackTargetGoal {
	private final Tamed tameable;
	private LivingEntity attacking;
	private int lastAttackTime;

	public RavagerAttackWithOwnerGoal(RavagerEntity tameable) {
		super(tameable, false);
		this.tameable = (Tamed) tameable;
		this.setControls(EnumSet.of(Goal.Control.TARGET));
	}

	@Override
	public boolean canStart() {
		if (this.tameable.isTamed()) {
			LivingEntity livingEntity = this.tameable.getOwner();
			if (livingEntity == null) {
				return false;
			} else {
				this.attacking = livingEntity.getAttacking();
				int i = livingEntity.getLastAttackTime();
				return i != this.lastAttackTime
						&& this.canTrack(this.attacking, TargetPredicate.DEFAULT)
						&& this.tameable.canAttackWithOwner(this.attacking, livingEntity);
			}
		} else {
			return false;
		}
	}

	@Override
	public void start() {
		this.mob.setTarget(this.attacking);
		LivingEntity livingEntity = this.tameable.getOwner();
		if (livingEntity != null) {
			this.lastAttackTime = livingEntity.getLastAttackTime();
		}

		super.start();
	}
}
