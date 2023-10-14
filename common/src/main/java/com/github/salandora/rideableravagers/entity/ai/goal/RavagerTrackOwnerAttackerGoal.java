package com.github.salandora.rideableravagers.entity.ai.goal;

import com.github.salandora.rideableravagers.entity.Tamed;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Ravager;

public class RavagerTrackOwnerAttackerGoal extends TargetGoal {
	private final Tamed tameable;
	private LivingEntity attacker;
	private int lastAttackedTime;

	public RavagerTrackOwnerAttackerGoal(Ravager tameable) {
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
				this.attacker = livingEntity.getLastHurtByMob();
				int i = livingEntity.getLastHurtByMobTimestamp();
				return i != this.lastAttackedTime
						&& this.canAttack(this.attacker, TargetingConditions.DEFAULT)
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
			this.lastAttackedTime = livingEntity.getLastHurtByMobTimestamp();
		}

		super.start();
	}
}
