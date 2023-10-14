package com.github.salandora.rideableravagers.entity;

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
	boolean isBred();
	void setBred(boolean bred);

	int getBreedingAge();
	void setBreedingAge(int ticks);

	boolean isBreedingItem(ItemStack stack);

	default void eat(Player player, InteractionHand hand, ItemStack stack) {
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
	}

	default boolean canEat() { return this.getLoveTicks() <= 0; }

	void lovePlayer(@Nullable Player player);

	void setLoveTicks(int loveTicks);

	int getLoveTicks();

	@Nullable
	ServerPlayer getLovingPlayer();

	default boolean isInLove() { return this.getLoveTicks() > 0; }

	default void resetLoveTicks() { this.setLoveTicks(0); }

	Mob createChild(ServerLevel world, BreedableEntity other);

	default void breed(ServerLevel world, BreedableEntity other) {
		Mob child = this.createChild(world, other);
		if (child != null) {
			child.setBaby(true);
			child.moveTo(((Entity) this).getX(), ((Entity) this).getY(), ((Entity) this).getZ(), 0.0F, 0.0F);
			this.breed(world, other, child);
			world.addFreshEntityWithPassengers(child);
		}
	}

	default void breed(ServerLevel world, BreedableEntity other, @Nullable Mob baby)  {
		Optional.ofNullable(this.getLovingPlayer()).or(() -> Optional.ofNullable(other.getLovingPlayer())).ifPresent(player -> {
			player.awardStat(Stats.ANIMALS_BRED);
			//Criteria.BRED_ANIMALS.trigger(player, this, other, baby);
		});
		this.setBreedingAge(6000);
		other.setBreedingAge(6000);
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
