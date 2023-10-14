package com.github.salandora.rideableravagers.entity.ai.goal;

import com.github.salandora.rideableravagers.entity.Tamed;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Ravager;

public class RavagerAttackWithOwnerGoal extends TargetGoal {
	private final Tamed tameable;
	private LivingEntity attacking;
	private int lastAttackTime;

	public RavagerAttackWithOwnerGoal(Ravager tameable) {
		super(tameable, false);
		this.tameable = (Tamed) tameable;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	@Override
	public boolean canUse() {
		if (this.tameable.isTamed()) {
			LivingEntity livingEntity = this.tameable.getOwner();
			if (livingEntity == null) {
				return false;
			} else {
				this.attacking = livingEntity.getLastHurtMob();
				int i = livingEntity.getLastHurtMobTimestamp();
				return i != this.lastAttackTime
						&& this.canAttack(this.attacking, TargetingConditions.DEFAULT)
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
			this.lastAttackTime = livingEntity.getLastHurtMobTimestamp();
		}

		super.start();
	}
}
