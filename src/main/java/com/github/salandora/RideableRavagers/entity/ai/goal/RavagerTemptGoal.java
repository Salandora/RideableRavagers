package com.github.salandora.RideableRavagers.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.recipe.Ingredient;

public class RavagerTemptGoal extends TemptGoal {
	private final RavagerEntity ravager;

	public RavagerTemptGoal(RavagerEntity entity, double speed, Ingredient food, boolean canBeScared) {
		super(entity, speed, food, canBeScared);
		this.ravager = entity;
	}

	@Override
	public boolean isTemptedBy(LivingEntity entity) {
		return ((Tameable)this.ravager).getOwnerUuid() != null && ((Tameable)this.ravager).getOwner() == entity && super.isTemptedBy(entity);
	}
}
