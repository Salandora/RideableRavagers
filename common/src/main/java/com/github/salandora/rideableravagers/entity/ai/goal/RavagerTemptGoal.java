package com.github.salandora.rideableravagers.entity.ai.goal;

import com.github.salandora.rideableravagers.mixins.TemptGoalAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.crafting.Ingredient;

public class RavagerTemptGoal extends TemptGoal {
	private final Ravager ravager;

	public RavagerTemptGoal(Ravager entity, double speed, Ingredient food, boolean canBeScared) {
		super(entity, speed, food, canBeScared);
		this.ravager = entity;
		((TemptGoalAccessor)this).getTargetingConditions().selector(this::shouldFollow2);
	}

	public boolean shouldFollow2(LivingEntity entity) {
		return ((OwnableEntity)this.ravager).getOwnerUUID() != null && ((OwnableEntity)this.ravager).getOwner() == entity && ((TemptGoalAccessor)this).invokeShouldFollow(entity);
	}
}
