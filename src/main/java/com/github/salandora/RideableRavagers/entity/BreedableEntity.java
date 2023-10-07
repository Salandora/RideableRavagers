package com.github.salandora.RideableRavagers.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface BreedableEntity {
	boolean isBred();
	void setBred(boolean bred);

	int getBreedingAge();
	void setBreedingAge(int ticks);

	boolean isBreedingItem(ItemStack stack);

	default void eat(PlayerEntity player, Hand hand, ItemStack stack) {
		if (!player.getAbilities().creativeMode) {
			stack.decrement(1);
		}
	}

	default boolean canEat() { return this.getLoveTicks() <= 0; }

	void lovePlayer(@Nullable PlayerEntity player);

	void setLoveTicks(int loveTicks);

	int getLoveTicks();

	@Nullable
	ServerPlayerEntity getLovingPlayer();

	default boolean isInLove() { return this.getLoveTicks() > 0; }

	default void resetLoveTicks() { this.setLoveTicks(0); }

	MobEntity createChild(ServerWorld world, BreedableEntity other);

	default void breed(ServerWorld world, BreedableEntity other) {
		MobEntity child = this.createChild(world, other);
		if (child != null) {
			child.setBaby(true);
			child.refreshPositionAndAngles(((Entity) this).getX(), ((Entity) this).getY(), ((Entity) this).getZ(), 0.0F, 0.0F);
			this.breed(world, other, child);
			world.spawnEntityAndPassengers(child);
		}
	}

	default void breed(ServerWorld world, BreedableEntity other, @Nullable MobEntity baby)  {
		Optional.ofNullable(this.getLovingPlayer()).or(() -> Optional.ofNullable(other.getLovingPlayer())).ifPresent(player -> {
			player.incrementStat(Stats.ANIMALS_BRED);
			//Criteria.BRED_ANIMALS.trigger(player, this, other, baby);
		});
		this.setBreedingAge(6000);
		other.setBreedingAge(6000);
		this.resetLoveTicks();
		other.resetLoveTicks();
		world.sendEntityStatus((Entity) this, EntityStatuses.ADD_BREEDING_PARTICLES);
		if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
			world.spawnEntity(new ExperienceOrbEntity(world, ((Entity) this).getX(), ((Entity) this).getY(), ((Entity) this).getZ(), ((LivingEntity) this).getRandom().nextInt(7) + 1));
		}
	}

	default boolean canBreedWith(BreedableEntity other) {
		if (other == this) {
			return false;
		} else if (other.getClass() != this.getClass()) {
			return false;
		} else {
			return this.isInLove() && other.isInLove();
		}
	}
}
