package com.github.salandora.RideableRavagers.entity.ai.goal;

import com.github.salandora.RideableRavagers.entity.BreedableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class BreedableMateGoal extends Goal {
	private static final TargetPredicate VALID_MATE_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(8.0).ignoreVisibility();
	protected final BreedableEntity breedableEntity;
	protected final MobEntity entity;
	private final Class<? extends LivingEntity> entityClass;
	protected final World world;
	@Nullable
	protected LivingEntity mate;
	private int timer;
	private final double speed;

	public BreedableMateGoal(MobEntity entity, double speed) {
		this(entity, speed, entity.getClass());
	}

	public BreedableMateGoal(MobEntity entity, double speed, Class<? extends LivingEntity> entityClass) {
		this.breedableEntity = (BreedableEntity) entity;
		this.entity = entity;
		this.world = entity.getWorld();
		this.entityClass = entityClass;
		this.speed = speed;
		this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
	}

	@Override
	public boolean canStart() {
		if (!this.breedableEntity.isInLove()) {
			return false;
		} else {
			this.mate = this.findMate();
			return this.mate != null;
		}
	}

	@Override
	public boolean shouldContinue() {
		return this.mate.isAlive() && ((BreedableEntity) this.mate).isInLove() && this.timer < 60;
	}

	@Override
	public void stop() {
		this.mate = null;
		this.timer = 0;
	}

	@Override
	public void tick() {
		this.entity.getLookControl().lookAt(this.mate, 10.0F, (float)this.entity.getMaxLookPitchChange());
		this.entity.getNavigation().startMovingTo(this.mate, this.speed);
		++this.timer;
		if (this.timer >= this.getTickCount(60) && this.entity.squaredDistanceTo(this.mate) < 9.0) {
			this.breed();
		}
	}

	@Nullable
	private LivingEntity findMate() {
		List<? extends LivingEntity> list = this.world.getTargets(this.entityClass, VALID_MATE_PREDICATE, this.entity, this.entity.getBoundingBox().expand(8.0));
		double d = Double.MAX_VALUE;
		LivingEntity mate = null;

		for(LivingEntity mate2 : list) {
			if (mate2 instanceof BreedableEntity breedablemate && this.breedableEntity.canBreedWith(breedablemate) && this.entity.squaredDistanceTo(mate2) < d) {
				mate = mate2;
				d = this.entity.squaredDistanceTo(mate2);
			}
		}

		return mate;
	}

	protected void breed() {
		this.breedableEntity.breed((ServerWorld)this.world, (BreedableEntity) this.mate);
	}
}
