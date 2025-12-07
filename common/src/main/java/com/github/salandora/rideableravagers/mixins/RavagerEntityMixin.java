package com.github.salandora.rideableravagers.mixins;

import com.github.salandora.rideableravagers.attachment.Attachments;
import com.github.salandora.rideableravagers.attachment.EntityAttachment;
import com.github.salandora.rideableravagers.entity.BreedableEntity;
import com.github.salandora.rideableravagers.entity.Tamable;
import com.github.salandora.rideableravagers.entity.ai.goal.BreedableMateGoal;
import com.github.salandora.rideableravagers.entity.ai.goal.RavagerAttackWithOwnerGoal;
import com.github.salandora.rideableravagers.entity.ai.goal.RavagerTemptGoal;
import com.github.salandora.rideableravagers.entity.ai.goal.RavagerTrackOwnerAttackerGoal;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static net.minecraft.world.entity.AgeableMob.getSpeedUpSecondsWhenFeeding;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(Ravager.class)
public abstract class RavagerEntityMixin extends Raider implements BreedableEntity, Tamable, OwnableEntity {
	@Unique
	private static final int rideableRavagers$TAMED_FLAG = 2;
	@Unique
	private static final int rideableRavagers$BABY_FLAG = 4;
	@Unique
	private static final int rideableRavagers$BRED_FLAG = 8;

	@Unique
	private int rideableRavagers$breedingAge;
	@Unique
	protected int rideableRavagers$forcedAge;
	@Unique
	private int rideableRavagers$loveTicks;

	@Unique
	@Nullable
	private EntityReference<ServerPlayer> rideableRavagers$loveCause;

	protected RavagerEntityMixin(EntityType<? extends Raider> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(
			method = "<init>",
			at = @At(value = "RETURN")
	)
	private void rideableRavagers$init(EntityType<? extends Raider> entityType, Level level, CallbackInfo ci) {
		EntityAttachment.INSTANCE.registerOnAttachmentSet(
				this,
				Attachments.RAVAGER_FLAGS,
				(oldData, newData) -> this.refreshDimensions()
		);
	}

	@WrapOperation(
			method = "createAttributes",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/monster/Monster;createMonsterAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;"
			)
	)
	private static AttributeSupplier.Builder rideableRavagers$createAttributes(Operation<AttributeSupplier.Builder> original) {
		AttributeSupplier.Builder builder = original.call();
		return builder.add(Attributes.TEMPT_RANGE, 10.0);
	}

	@Unique
	protected boolean rideableRavagers$getFlag(int bitmask) {
		return (EntityAttachment.INSTANCE.getData((Ravager) (Object) this, Attachments.RAVAGER_FLAGS) & bitmask) != 0;
	}
	@Unique
	protected void rideableRavagers$setFlag(int bitmask, boolean flag) {
		byte b = EntityAttachment.INSTANCE.getData((Ravager) (Object) this, Attachments.RAVAGER_FLAGS);
		if (flag) {
			EntityAttachment.INSTANCE.setData((Ravager) (Object) this, Attachments.RAVAGER_FLAGS, (byte)(b | bitmask));
		} else {
			EntityAttachment.INSTANCE.setData((Ravager) (Object) this, Attachments.RAVAGER_FLAGS, (byte)(b & ~bitmask));
		}
	}

	@Unique
	public boolean rideableRavagers$isBred() {
		return this.rideableRavagers$getFlag(rideableRavagers$BRED_FLAG);
	}
	@Unique
	public void rideableRavagers$setBred(boolean bred) {
		this.rideableRavagers$setFlag(rideableRavagers$BRED_FLAG, bred);
	}

	@Unique
	public boolean rideableRavagers$isTamed() {
		return this.rideableRavagers$getFlag(rideableRavagers$TAMED_FLAG);
	}
	@Unique
	public void rideableRavagers$setTamed(boolean tame) {
		this.rideableRavagers$setFlag(rideableRavagers$TAMED_FLAG, tame);
	}

	@Nullable
	public EntityReference<LivingEntity> getOwnerReference() {
		return EntityAttachment.INSTANCE.getData(this, Attachments.RAVAGER_OWNER).orElse(null);
	}

	@Unique
	public void rideableRavagers$setOwner(@Nullable LivingEntity livingEntity) {
		EntityAttachment.INSTANCE.setData(this, Attachments.RAVAGER_OWNER, Optional.ofNullable(livingEntity).map(EntityReference::new));
	}

	@Override
	public boolean requiresCustomPersistence() {
		return super.requiresCustomPersistence() || this.rideableRavagers$isBred();
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return super.shouldDespawnInPeaceful() && !this.rideableRavagers$isBred();
	}

	@Override
	public boolean rideableRavagers$canAttackWithOwner(LivingEntity target, LivingEntity owner) {
		if (target instanceof Creeper || target instanceof Ghast) {
			return false;
		} else if (target instanceof Tamable tamedEntity) {
			return !tamedEntity.rideableRavagers$isTamed() || tamedEntity.getOwner() != owner;
		} else if (target instanceof Wolf wolfEntity) {
			return !wolfEntity.isTame() || wolfEntity.getOwner() != owner;
		} else if (target instanceof Player && owner instanceof Player && !((Player)owner).canHarmPlayer((Player)target)) {
			return false;
		} else if (target instanceof AbstractHorse && ((AbstractHorse)target).isTamed()) {
			return false;
		} else {
			return !(target instanceof TamableAnimal) || !((TamableAnimal)target).isTame();
		}
	}

	@Unique
	public void rideableRavagers$growUp(int age, boolean overGrow) {
		int newAge = this.rideableRavagers$getBreedingAge();
		int oldAge = newAge;

		newAge += age * 20;
		if (newAge > 0) {
			newAge = 0;
		}

		int difference = newAge - oldAge;
		this.rideableRavagers$setBreedingAge(newAge);
		if (overGrow) {
			this.rideableRavagers$forcedAge += difference;
		}

		if (this.rideableRavagers$getBreedingAge() == 0) {
			this.rideableRavagers$setBreedingAge(this.rideableRavagers$forcedAge);
		}
	}

	@Override
	public int rideableRavagers$getBreedingAge() {
		if (this.level().isClientSide) {
			return this.rideableRavagers$getFlag(rideableRavagers$BABY_FLAG) ? -1 : 1;
		} else {
			return this.rideableRavagers$breedingAge;
		}
	}

	@Override
	public void rideableRavagers$setBreedingAge(int age) {
		int i = this.rideableRavagers$getBreedingAge();
		this.rideableRavagers$breedingAge = age;
		if (i < 0 && age >= 0 || i >= 0 && age < 0) {
			this.rideableRavagers$setFlag(rideableRavagers$BABY_FLAG, age < 0);
			this.rideableRavagers$onGrowUp();
		}
	}

	@SuppressWarnings("DataFlowIssue")
	@Unique
	protected void rideableRavagers$onGrowUp() {
		if (this.isBaby()) {
			this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3);
			this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50);
		} else if (this.isPassenger()) {
			this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12);
			this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);

			Entity var2 = this.getVehicle();
			if (var2 instanceof Boat boatEntity && !boatEntity.hasEnoughSpaceFor(this)) {
				this.stopRiding();
			}
		}
	}

	@Override
	public boolean isBaby() {
		return this.rideableRavagers$getBreedingAge() < 0;
	}

	@Override
	public void setBaby(boolean baby) {
		this.rideableRavagers$setBreedingAge(baby ? -24000 : 0);
	}

	@Override
	public boolean rideableRavagers$isBreedingItem(ItemStack stack) {
		return stack.is(Items.COOKED_BEEF);
	}

	@Override
	public void rideableRavagers$lovePlayer(@Nullable Player player) {
		this.rideableRavagers$loveTicks = 600;
		if (player instanceof ServerPlayer serverplayer) {
			this.rideableRavagers$loveCause = new EntityReference<>(serverplayer);
		}

		this.level().broadcastEntityEvent(this, EntityEvent.IN_LOVE_HEARTS);
	}

	@Override
	public void rideableRavagers$setLoveTicks(int loveTicks) {
		this.rideableRavagers$loveTicks = loveTicks;
	}

	@Override
	public int rideableRavagers$getLoveTicks() {
		return this.rideableRavagers$loveTicks;
	}

	@Override
	public @Nullable ServerPlayer rideableRavagers$getLovingPlayer() {
		return EntityReference.get(this.rideableRavagers$loveCause, uuid -> (ServerPlayer) level().getPlayerByUUID(uuid), ServerPlayer.class);
	}

	@Inject(
			method = "registerGoals",
			at = @At("TAIL")
	)
	private void rideableRavagers$initGoals(CallbackInfo ci) {
		Ravager ravager = (Ravager)(Object)this;
		this.goalSelector.addGoal(2, new BreedableMateGoal(this, 1.0, Ravager.class));
		this.goalSelector.addGoal(3, new RavagerTemptGoal(ravager, 1.25, Ingredient.of(Items.COOKED_BEEF), false));

		this.targetSelector.getAvailableGoals().stream()
				.filter(goal -> goal.getGoal() instanceof NearestAttackableTargetGoal<?>)
				.map(goal -> (NearestAttackableTargetGoal<?>)goal.getGoal())
				.forEach(goal -> {
					NearestAttackableTargetGoalAccessor accessor = (NearestAttackableTargetGoalAccessor)goal;
					Class<?> targetClazz = accessor.getTargetType();
					if (targetClazz == Player.class) {
						accessor.getTargetConditions().selector((entity, level) -> !this.rideableRavagers$isTamed());
					} else if (targetClazz == AbstractVillager.class) {
						accessor.getTargetConditions().selector((entity, level) -> !this.rideableRavagers$isTamed() && !entity.isBaby());
					} else if (targetClazz == IronGolem.class) {
						accessor.getTargetConditions().selector((entity, level) -> !this.rideableRavagers$isTamed());
					}
				});

		this.targetSelector.addGoal(1, new RavagerTrackOwnerAttackerGoal(ravager));
		this.targetSelector.addGoal(2, new RavagerAttackWithOwnerGoal(ravager));
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
		this.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
		return  super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
	}

	@Override
	public Mob rideableRavagers$createChild(ServerLevel level, BreedableEntity other) {
		Ravager entity = EntityType.RAVAGER.create(level, EntitySpawnReason.BREEDING);
		((BreedableEntity) entity).rideableRavagers$setBred(true);
		return entity;
	}

	@Override
	protected @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
		ItemStack itemStack = player.getItemInHand(hand);
		boolean breedingItem = this.rideableRavagers$isBreedingItem(itemStack);
		if (!breedingItem && this.rideableRavagers$isTamed() && this.isSaddled() && !this.isVehicle() && !this.isBaby() && !player.isSecondaryUseActive()) {
			if (!this.level().isClientSide) {
				this.rideableRavagers$putPlayerOnBack(player);
			}
			return InteractionResult.SUCCESS;
		} else if (breedingItem) {
			int i = this.rideableRavagers$getBreedingAge();
			if (!this.level().isClientSide && i == 0 && this.canEat()) {
				this.eat(player, hand, itemStack);
				this.rideableRavagers$lovePlayer(player);
				return InteractionResult.SUCCESS;
			}

			if (this.isBaby()) {
				// TODO: change to "Feed the baby Steaks to gain hearts as it grows to "gain it's trust". (Could use angry hearts from villagers with some random number eventually being the hearts when the Ravager grows.)"
				this.eat(player, hand, itemStack);
				if (!this.rideableRavagers$isTamed()) {
					this.level().broadcastEntityEvent(this, EntityEvent.IN_LOVE_HEARTS);
					this.rideableRavagers$setTamed(true);
					this.rideableRavagers$setOwner(player);
					this.setTarget(null);
				}

				this.rideableRavagers$growUp(getSpeedUpSecondsWhenFeeding(-i), true);
				return InteractionResult.SUCCESS;
			}

			if (this.level().isClientSide) {
				return InteractionResult.CONSUME;
			}
		}

		InteractionResult actionResult = super.mobInteract(player, hand);
		if (!actionResult.consumesAction()) {
			if (!this.isBaby() && this.isEquippableInSlot(itemStack, EquipmentSlot.SADDLE)) {
				return itemStack.interactLivingEntity(player, this, hand);
			}
			return InteractionResult.PASS;
		} else {
			return actionResult;
		}
	}

	@Override
	public boolean canUseSlot(EquipmentSlot equipmentSlot) {
		return equipmentSlot != EquipmentSlot.SADDLE ? super.canUseSlot(equipmentSlot) : this.isAlive() && !this.isBaby() && this.rideableRavagers$isTamed();
	}

	@Override
	protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
		return (equipmentSlot == EquipmentSlot.SADDLE) && this.rideableRavagers$isTamed() || super.canDispenserEquipIntoSlot(equipmentSlot);
	}

	@Override
	public boolean canBeLeashed() {
		return !this.isLeashed() && rideableRavagers$isBred();
	}

	@Override
	protected void tickRidden(@NotNull Player controllingPlayer, @NotNull Vec3 movementInput) {
		Vec2 vec2f = this.rideableRavagers$getControlledRotation(controllingPlayer);
		this.setRot(vec2f.y, vec2f.x);
		this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
		super.tickRidden(controllingPlayer, movementInput);
	}

	@Unique
	protected Vec2 rideableRavagers$getControlledRotation(LivingEntity controllingPassenger) {
		return new Vec2(controllingPassenger.getXRot() * 0.5F, controllingPassenger.getYRot());
	}

	@Override
	protected @NotNull Vec3 getRiddenInput(Player controllingPlayer, @NotNull Vec3 movementInput) {
		float f = controllingPlayer.xxa * 0.5F;
		float g = controllingPlayer.zza;
		if (g <= 0.0F) {
			g *= 0.25F;
		}

		return new Vec3(f, 0.0, g);
	}

	@Override
	protected float getRiddenSpeed(@NotNull Player controllingPlayer) {
		return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
	}

	@Override
	protected void dropEquipment(ServerLevel serverLevel) {
		super.dropEquipment(serverLevel);
		if (!this.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
			this.spawnAtLocation(serverLevel, this.getItemBySlot(EquipmentSlot.SADDLE));
		}
	}

	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		Entity var3 = this.getFirstPassenger();
		if (var3 instanceof Mob) {
			return (Mob)var3;
		} else {
			if (this.isSaddled()) {
				var3 = this.getFirstPassenger();
				if (var3 instanceof Player) {
					return (Player)var3;
				}
			}

			return null;
		}
	}

	@Unique
	@Nullable
	private Vec3 rideableRavagers$locateSafeDismountingPos(Vec3 offset, LivingEntity passenger) {
		double d = this.getX() + offset.x;
		double e = this.getBoundingBox().minY;
		double f = this.getZ() + offset.z;
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for(Pose entityPose : passenger.getDismountPoses()) {
			mutable.set(d, e, f);
			double g = this.getBoundingBox().maxY + 0.75;

			do {
				double h = this.level().getBlockFloorHeight(mutable);
				if (mutable.getY() + h > g) {
					break;
				}

				if (DismountHelper.isBlockFloorValid(h)) {
					AABB box = passenger.getLocalBoundsForPose(entityPose);
					Vec3 vec3d = new Vec3(d, mutable.getY() + h, f);
					if (DismountHelper.canDismountTo(this.level(), passenger, box.move(vec3d))) {
						passenger.setPose(entityPose);
						return vec3d;
					}
				}

				mutable.move(Direction.UP);
			} while(!(mutable.getY() < g));
		}

		return null;
	}

	@Override
	public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		Vec3 vec3d = getCollisionHorizontalEscapeVector(
				this.getBbWidth(), passenger.getBbWidth(), this.getYRot() + (passenger.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F)
		);
		Vec3 vec3d2 = this.rideableRavagers$locateSafeDismountingPos(vec3d, passenger);
		if (vec3d2 != null) {
			return vec3d2;
		} else {
			Vec3 vec3d3 = getCollisionHorizontalEscapeVector(
					this.getBbWidth(), passenger.getBbWidth(), this.getYRot() + (passenger.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F)
			);
			Vec3 vec3d4 = this.rideableRavagers$locateSafeDismountingPos(vec3d3, passenger);
			return vec3d4 != null ? vec3d4 : this.position();
		}
	}

	@Unique
	protected void rideableRavagers$putPlayerOnBack(Player player) {
		if (!this.level().isClientSide) {
			player.setYRot(this.getYRot());
			player.setXRot(this.getXRot());
			player.startRiding(this);
		}
	}

	@Inject(
			method = "aiStep",
			at = @At("HEAD")
	)
	private void rideableRavagers$tickMovement(CallbackInfo ci) {
		if (!this.level().isClientSide && this.isAlive()) {
			int i = this.rideableRavagers$getBreedingAge();
			if (i < 0) {
				this.rideableRavagers$setBreedingAge(++i);
			} else if (i > 0) {
				this.rideableRavagers$setBreedingAge(--i);
			}
		}
	}

	@Inject(
			method = "addAdditionalSaveData",
			at = @At("HEAD")
	)
	private void rideableRavagers$writeCustomDataToNbt(ValueOutput valueOutput, CallbackInfo ci) {
		valueOutput.putInt("Age", this.rideableRavagers$getBreedingAge());
		valueOutput.putInt("ForcedAge", this.rideableRavagers$forcedAge);
		valueOutput.putBoolean("Bred", this.rideableRavagers$isBred());
		EntityReference<LivingEntity> entityReference = this.getOwnerReference();
		EntityReference.store(entityReference, valueOutput, "Owner");

		valueOutput.putInt("InLove", this.rideableRavagers$loveTicks);
		EntityReference.store(this.rideableRavagers$loveCause, valueOutput, "LoveCause");
	}

	@Inject(
			method = "readAdditionalSaveData",
			at = @At("HEAD")
	)
	private void rideableRavagers$readCustomDataToNbt(ValueInput valueInput, CallbackInfo ci) {
		this.rideableRavagers$setBreedingAge(valueInput.getIntOr("Age", 0));
		this.rideableRavagers$forcedAge = valueInput.getIntOr("ForcedAge", 0);
		this.rideableRavagers$setBred(valueInput.getBooleanOr("Bred", false));
		EntityReference<LivingEntity> entityReference = EntityReference.readWithOldOwnerConversion(valueInput, "Owner", this.level());
		if (entityReference != null) {
			EntityAttachment.INSTANCE.setData(this, Attachments.RAVAGER_OWNER, Optional.of(entityReference));
			this.rideableRavagers$setTamed(true);
		} else {
			EntityAttachment.INSTANCE.setData(this, Attachments.RAVAGER_OWNER, Optional.empty());
			this.rideableRavagers$setTamed(false);
		}

		this.rideableRavagers$loveTicks = valueInput.getIntOr("InLove", 0);
		this.rideableRavagers$loveCause = EntityReference.read(valueInput, "LoveCause");
	}

	@Inject(
			method = "handleEntityEvent",
			at = @At("HEAD"),
			cancellable = true
	)
	private void rideableRavagers$handleStatus(byte status, CallbackInfo ci) {
		if (status == EntityEvent.IN_LOVE_HEARTS) {
			for(int i = 0; i < 7; ++i) {
				double d = this.random.nextGaussian() * 0.02;
				double e = this.random.nextGaussian() * 0.02;
				double f = this.random.nextGaussian() * 0.02;
				this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
			}
			ci.cancel();
		}
	}
}
