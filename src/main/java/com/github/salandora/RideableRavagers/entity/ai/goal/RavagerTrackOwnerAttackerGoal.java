package com.github.salandora.RideableRavagers.entity.ai.goal;

import com.github.salandora.RideableRavagers.entity.Tamed;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.RavagerEntity;

import java.util.EnumSet;

public class RavagerTrackOwnerAttackerGoal extends TrackTargetGoal {
	private final Tamed tameable;
	private LivingEntity attacker;
	private int lastAttackedTime;

	public RavagerTrackOwnerAttackerGoal(RavagerEntity tameable) {
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
				this.attacker = livingEntity.getAttacker();
				int i = livingEntity.getLastAttackedTime();
				return i != this.lastAttackedTime
						&& this.canTrack(this.attacker, TargetPredicate.DEFAULT)
						&& this.tameable.canAttackWithOwner(this.attacker, livingEntity);
			}
		} else {
			return false;
		}
	}

	@Override
	public void start() {
		this.mob.setTarget(this.attacker);
		LivingEntity livingEntity = this.tameable.getOwner();
		if (livingEntity != null) {
			this.lastAttackedTime = livingEntity.getLastAttackedTime();
		}

		super.start();
	}
}
