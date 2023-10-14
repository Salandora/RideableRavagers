package com.github.salandora.rideableravagers.entity.ai.goal;

import com.github.salandora.rideableravagers.entity.BreedableEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;

public class BreedableMateGoal extends Goal {
	private static final TargetingConditions VALID_MATE_PREDICATE = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight();
	protected final BreedableEntity breedableEntity;
	protected final Mob entity;
	private final Class<? extends LivingEntity> entityClass;
	protected final Level world;
	@Nullable
	protected LivingEntity mate;
	private int timer;
	private final double speed;

	public BreedableMateGoal(Mob entity, double speed) {
		this(entity, speed, entity.getClass());
	}

	public BreedableMateGoal(Mob entity, double speed, Class<? extends LivingEntity> entityClass) {
		this.breedableEntity = (BreedableEntity) entity;
		this.entity = entity;
		this.world = entity.level();
		this.entityClass = entityClass;
		this.speed = speed;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!this.breedableEntity.isInLove()) {
			return false;
		} else {
			this.mate = this.findMate();
			return this.mate != null;
		}
	}

	@Override
	public boolean canContinueToUse() {
		return this.mate.isAlive() && ((BreedableEntity) this.mate).isInLove() && this.timer < 60;
	}

	@Override
	public void stop() {
		this.mate = null;
		this.timer = 0;
	}

	@Override
	public void tick() {
		this.entity.getLookControl().setLookAt(this.mate, 10.0F, (float)this.entity.getMaxHeadXRot());
		this.entity.getNavigation().moveTo(this.mate, this.speed);
		++this.timer;
		if (this.timer >= this.adjustedTickDelay(60) && this.entity.distanceToSqr(this.mate) < 9.0) {
			this.breed();
		}
	}

	@Nullable
	private LivingEntity findMate() {
		List<? extends LivingEntity> list = this.world.getNearbyEntities(this.entityClass, VALID_MATE_PREDICATE, this.entity, this.entity.getBoundingBox().inflate(8.0));
		double d = Double.MAX_VALUE;
		LivingEntity mate = null;

		for(LivingEntity mate2 : list) {
			if (mate2 instanceof BreedableEntity breedablemate && this.breedableEntity.canBreedWith(breedablemate) && this.entity.distanceToSqr(mate2) < d) {
				mate = mate2;
				d = this.entity.distanceToSqr(mate2);
			}
		}

		return mate;
	}

	protected void breed() {
		this.breedableEntity.breed((ServerLevel)this.world, (BreedableEntity) this.mate);
	}
}
