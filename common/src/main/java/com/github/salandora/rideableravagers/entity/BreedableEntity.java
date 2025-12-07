package com.github.salandora.rideableravagers.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

public interface BreedableEntity {
	boolean rideableRavagers$isBred();
	void rideableRavagers$setBred(boolean bred);

	int rideableRavagers$getBreedingAge();
	void rideableRavagers$setBreedingAge(int ticks);

	boolean rideableRavagers$isBreedingItem(ItemStack stack);

	default void eat(@NotNull Player player, InteractionHand hand, ItemStack stack) {
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
	}

	default boolean canEat() { return this.rideableRavagers$getLoveTicks() <= 0; }

	void rideableRavagers$lovePlayer(@Nullable Player player);

	void rideableRavagers$setLoveTicks(int loveTicks);

	int rideableRavagers$getLoveTicks();

	@Nullable
	ServerPlayer rideableRavagers$getLovingPlayer();

	default boolean isInLove() { return this.rideableRavagers$getLoveTicks() > 0; }

	default void resetLoveTicks() { this.rideableRavagers$setLoveTicks(0); }

	Mob rideableRavagers$createChild(ServerLevel world, BreedableEntity other);

	default void breed(ServerLevel world, BreedableEntity other) {
		Mob child = this.rideableRavagers$createChild(world, other);
		if (child != null) {
			child.setBaby(true);
			child.snapTo(((Entity) this).getX(), ((Entity) this).getY(), ((Entity) this).getZ(), 0.0F, 0.0F);
			this.breed(world, other, child);
			world.addFreshEntityWithPassengers(child);
		}
	}

	default void breed(@NotNull ServerLevel world, @NotNull BreedableEntity other, @Nullable Mob baby)  {
		Optional.ofNullable(this.rideableRavagers$getLovingPlayer()).or(() -> Optional.ofNullable(other.rideableRavagers$getLovingPlayer())).ifPresent(player -> {
			player.awardStat(Stats.ANIMALS_BRED);
			//Criteria.BRED_ANIMALS.trigger(player, this, other, baby);
		});
		this.rideableRavagers$setBreedingAge(6000);
		other.rideableRavagers$setBreedingAge(6000);
		this.resetLoveTicks();
		other.resetLoveTicks();
		world.broadcastEntityEvent((Entity) this, EntityEvent.IN_LOVE_HEARTS);
		if (world.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
			world.addFreshEntity(new ExperienceOrb(world, ((Entity) this).getX(), ((Entity) this).getY(), ((Entity) this).getZ(), ((LivingEntity) this).getRandom().nextInt(7) + 1));
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
