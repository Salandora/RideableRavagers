package com.github.salandora.rideableravagers.mixins;

import com.github.salandora.rideableravagers.entity.BreedableEntity;
import com.github.salandora.rideableravagers.entity.Tamed;
import com.github.salandora.rideableravagers.entity.ai.goal.BreedableMateGoal;
import com.github.salandora.rideableravagers.entity.ai.goal.RavagerAttackWithOwnerGoal;
import com.github.salandora.rideableravagers.entity.ai.goal.RavagerTemptGoal;
import com.github.salandora.rideableravagers.entity.ai.goal.RavagerTrackOwnerAttackerGoal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.entity.AgeableMob.getSpeedUpSecondsWhenFeeding;

@SuppressWarnings({"WrongEntityDataParameterClass", "AddedMixinMembersNamePattern", "DataFlowIssue"})
@Mixin(Ravager.class)
public abstract class RavagerEntityMixin extends Raider implements BreedableEntity, Tamed, Saddleable {
	@Unique
	private static final EntityDataAccessor<Boolean> ridableravagers$SADDLED = SynchedEntityData.defineId(Ravager.class, EntityDataSerializers.BOOLEAN);
	@Unique
	private static final EntityDataAccessor<Integer> ridableravagers$BOOST_TIME = SynchedEntityData.defineId(Ravager.class, EntityDataSerializers.INT);
	@Unique
	private static final EntityDataAccessor<Byte> ridableravagers$FLAGS = SynchedEntityData.defineId(Ravager.class, EntityDataSerializers.BYTE);
	@Unique
	private static final int ridableravagers$TAMED_FLAG = 2;
	@Unique
	private static final int ridableravagers$BABY_FLAG = 4;
	@Unique
	private static final int ridableravagers$BRED_FLAG = 8;

	@Unique
	private int ridableravagers$breedingAge;
	@Unique
	protected int ridableravagers$forcedAge;
	@Unique
	private int ridableravagers$loveTicks;

	@Nullable
	@Unique
	private UUID ridableravagers$lovingPlayer;

	@Nullable
	@Unique
	private UUID ridableravagers$ownerUuid;

	@Unique
	private ItemBasedSteering ridableravagers$saddledComponent;

	protected RavagerEntityMixin(EntityType<? extends Raider> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void ridableravagers$constructor(EntityType<? extends Ravager> entityType, Level world, CallbackInfo ci) {
		this.ridableravagers$saddledComponent = new ItemBasedSteering(this.entityData, ridableravagers$BOOST_TIME, ridableravagers$SADDLED);
	}

	@Unique
	protected boolean ridableravagers$getFlag(int bitmask) {
		return (this.entityData.get(ridableravagers$FLAGS) & bitmask) != 0;
	}
	@Unique
	protected void ridableravagers$setFlag(int bitmask, boolean flag) {
		byte b = this.entityData.get(ridableravagers$FLAGS);
		if (flag) {
			this.entityData.set(ridableravagers$FLAGS, (byte)(b | bitmask));
		} else {
			this.entityData.set(ridableravagers$FLAGS, (byte)(b & ~bitmask));
		}
	}

	@Unique
	public boolean isBred() {
		return this.ridableravagers$getFlag(ridableravagers$BRED_FLAG);
	}
	@Unique
	public void setBred(boolean bred) {
		this.ridableravagers$setFlag(ridableravagers$BRED_FLAG, bred);
	}

	@Unique
	public boolean isTamed() {
		return this.ridableravagers$getFlag(ridableravagers$TAMED_FLAG);
	}
	@Unique
	public void ridableravagers$setTamed(boolean tame) {
		this.ridableravagers$setFlag(ridableravagers$TAMED_FLAG, tame);
	}

	@Override
	public boolean requiresCustomPersistence() {
		return super.requiresCustomPersistence() || this.isBred();
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return super.shouldDespawnInPeaceful() && !this.isBred();
	}

	@Override
	public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
		if (target instanceof Creeper || target instanceof Ghast) {
			return false;
		} else if (target instanceof Tamed tamedEntity) {
			return !tamedEntity.isTamed() || tamedEntity.getOwner() != owner;
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

	@Nullable
	@Unique
	public UUID getOwnerUUID() {
		return this.ridableravagers$ownerUuid;
	}
	@Unique
	public void ridableravagers$setOwnerUuid(@Nullable UUID ownerUuid) {
		this.ridableravagers$ownerUuid = ownerUuid;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ridableravagers$FLAGS, (byte) 0);
		this.entityData.define(ridableravagers$BOOST_TIME, 0);
		this.entityData.define(ridableravagers$SADDLED, true);
	}

	@Override
	public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> data) {
		if (ridableravagers$FLAGS.equals(data)) {
			this.refreshDimensions();
		}

		if (ridableravagers$BOOST_TIME.equals(data) && this.level().isClientSide) {
			this.ridableravagers$saddledComponent.onSynced();
		}

		super.onSyncedDataUpdated(data);
	}

	@Unique
	public void ridableravagers$growUp(int age, boolean overGrow) {
		int newAge = this.getBreedingAge();
		int oldAge = newAge;

		newAge += age * 20;
		if (newAge > 0) {
			newAge = 0;
		}

		int difference = newAge - oldAge;
		this.setBreedingAge(newAge);
		if (overGrow) {
			this.ridableravagers$forcedAge += difference;
		}

		if (this.getBreedingAge() == 0) {
			this.setBreedingAge(this.ridableravagers$forcedAge);
		}
	}

	@Override
	public int getBreedingAge() {
		if (this.level().isClientSide) {
			return this.ridableravagers$getFlag(ridableravagers$BABY_FLAG) ? -1 : 1;
		} else {
			return this.ridableravagers$breedingAge;
		}
	}

	@Override
	public void setBreedingAge(int age) {
		int i = this.getBreedingAge();
		this.ridableravagers$breedingAge = age;
		if (i < 0 && age >= 0 || i >= 0 && age < 0) {
			this.ridableravagers$setFlag(ridableravagers$BABY_FLAG, age < 0);
			this.ridableravagers$onGrowUp();
		}
	}

	@SuppressWarnings("DataFlowIssue")
	@Unique
	protected void ridableravagers$onGrowUp() {
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
		return this.getBreedingAge() < 0;
	}

	@Override
	public void setBaby(boolean baby) {
		this.setBreedingAge(baby ? -24000 : 0);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.is(Items.COOKED_BEEF);
	}

	@Override
	public void lovePlayer(@Nullable Player player) {
		this.ridableravagers$loveTicks = 600;
		if (player != null) {
			this.ridableravagers$lovingPlayer = player.getUUID();
		}

		this.level().broadcastEntityEvent(this, EntityEvent.IN_LOVE_HEARTS);

	}

	@Override
	public void setLoveTicks(int loveTicks) {
		this.ridableravagers$loveTicks = loveTicks;
	}

	@Override
	public int getLoveTicks() {
		return this.ridableravagers$loveTicks;
	}

	@Override
	public @Nullable ServerPlayer getLovingPlayer() {
		if (this.ridableravagers$lovingPlayer == null) {
			return null;
		} else {
			Player playerEntity = this.level().getPlayerByUUID(this.ridableravagers$lovingPlayer);
			return playerEntity instanceof ServerPlayer ? (ServerPlayer)playerEntity : null;
		}
	}

	@Inject(method = "registerGoals", at = @At("TAIL"))
	private void ridableravagers$initGoals(CallbackInfo ci) {
		Ravager ravager = (Ravager)(Object)this;
		this.goalSelector.addGoal(2, new BreedableMateGoal(this, 1.0, Ravager.class));
		this.goalSelector.addGoal(3, new RavagerTemptGoal(ravager, 1.25, Ingredient.of(Items.COOKED_BEEF), false));

		this.targetSelector.getAvailableGoals().stream()
				.filter(goal -> goal.getGoal() instanceof net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<?>)
				.map(goal -> (net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<?>)goal.getGoal())
				.forEach(goal -> {
					NearestAttackableTargetGoalAccessor accessor = (NearestAttackableTargetGoalAccessor)goal;
					Class<?> targetClazz = accessor.getTargetType();
					if (targetClazz == Player.class) {
						accessor.getTargetConditions().selector(entity -> !this.isTamed());
					} else if (targetClazz == AbstractVillager.class) {
						accessor.getTargetConditions().selector(entity -> !this.isTamed() && !entity.isBaby());
					} else if (targetClazz == IronGolem.class) {
						accessor.getTargetConditions().selector(entity -> !this.isTamed());
					}
				});

		this.targetSelector.addGoal(1, new RavagerTrackOwnerAttackerGoal(ravager));
		this.targetSelector.addGoal(2, new RavagerAttackWithOwnerGoal(ravager));
	}
	@Override
	public Mob createChild(ServerLevel world, BreedableEntity other) {
		Ravager entity = EntityType.RAVAGER.create(world);
		((BreedableEntity) entity).setBred(true);
		((RavagerEntityMixin) (Object) entity).ridableravagers$saddledComponent.setSaddle(false);
		return entity;
	}

	@Override
	protected @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
		ItemStack itemStack = player.getItemInHand(hand);
		boolean breedingItem = this.isBreedingItem(itemStack);
		if (!breedingItem && this.isTamed() && this.isSaddled() && !this.isVehicle() && !this.isBaby() && !player.isSecondaryUseActive()) {
			if (!this.level().isClientSide) {
				this.ridableravagers$putPlayerOnBack(player);
			}
			return InteractionResult.sidedSuccess(this.level().isClientSide);
		} else if (breedingItem) {
			int i = this.getBreedingAge();
			if (!this.level().isClientSide && i == 0 && this.canEat()) {
				this.eat(player, hand, itemStack);
				this.lovePlayer(player);
				return InteractionResult.SUCCESS;
			}

			if (this.isBaby()) {
				// TODO: change to "Feed the baby Steaks to gain hearts as it grows to "gain it's trust". (Could use angry hearts from villagers with some random number eventually being the hearts when the Ravager grows.)"
				this.eat(player, hand, itemStack);
				if (!this.isTamed()) {
					this.level().broadcastEntityEvent(this, EntityEvent.IN_LOVE_HEARTS);
					this.ridableravagers$setTamed(true);
					this.ridableravagers$setOwnerUuid(player.getUUID());
					this.setTarget(null);
				}

				this.ridableravagers$growUp(getSpeedUpSecondsWhenFeeding(-i), true);
				return InteractionResult.sidedSuccess(this.level().isClientSide);
			}

			if (this.level().isClientSide) {
				return InteractionResult.CONSUME;
			}
		}

		InteractionResult actionResult = super.mobInteract(player, hand);
		if (!actionResult.consumesAction()) {
			return itemStack.is(Items.SADDLE) ? itemStack.interactLivingEntity(player, this, hand) : InteractionResult.PASS;
		} else {
			return actionResult;
		}
	}

	@Override
	public boolean canBeLeashed(@NotNull Player player) {
		return !this.isLeashed() && isBred();
	}

	@Override
	protected void tickRidden(@NotNull Player controllingPlayer, @NotNull Vec3 movementInput) {
		Vec2 vec2f = this.ridableravagers$getControlledRotation(controllingPlayer);
		this.setRot(vec2f.y, vec2f.x);
		this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
		this.ridableravagers$saddledComponent.tickBoost();
		super.tickRidden(controllingPlayer, movementInput);
	}

	@Unique
	protected Vec2 ridableravagers$getControlledRotation(LivingEntity controllingPassenger) {
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

	public boolean isSaddled() {
		return this.ridableravagers$saddledComponent.hasSaddle();
	}

	public boolean isSaddleable() {
		return this.isAlive() && !this.isBaby();
	}

	public void equipSaddle(@Nullable SoundSource sound) {
		this.ridableravagers$saddledComponent.setSaddle(true);
		if (sound != null) {
			this.level().playSound(null, this, SoundEvents.RAVAGER_ROAR, sound, 0.5F, 1.0F);
		}
	}

	@Override
	protected float getRiddenSpeed(@NotNull Player controllingPlayer) {
		return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)this.ridableravagers$saddledComponent.boostFactor());
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (this.isSaddled()) {
			this.spawnAtLocation(Items.SADDLE);
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
	private Vec3 ridableravagers$locateSafeDismountingPos(Vec3 offset, LivingEntity passenger) {
		double d = this.getX() + offset.x;
		double e = this.getBoundingBox().minY;
		double f = this.getZ() + offset.z;
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for(Pose entityPose : passenger.getDismountPoses()) {
			mutable.set(d, e, f);
			double g = this.getBoundingBox().maxY + 0.75;

			do {
				double h = this.level().getBlockFloorHeight(mutable);
				if ((double)mutable.getY() + h > g) {
					break;
				}

				if (DismountHelper.isBlockFloorValid(h)) {
					AABB box = passenger.getLocalBoundsForPose(entityPose);
					Vec3 vec3d = new Vec3(d, (double)mutable.getY() + h, f);
					if (DismountHelper.canDismountTo(this.level(), passenger, box.move(vec3d))) {
						passenger.setPose(entityPose);
						return vec3d;
					}
				}

				mutable.move(Direction.UP);
			} while(!((double)mutable.getY() < g));
		}

		return null;
	}

	@Override
	public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		Vec3 vec3d = getCollisionHorizontalEscapeVector(
				this.getBbWidth(), passenger.getBbWidth(), this.getYRot() + (passenger.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F)
		);
		Vec3 vec3d2 = this.ridableravagers$locateSafeDismountingPos(vec3d, passenger);
		if (vec3d2 != null) {
			return vec3d2;
		} else {
			Vec3 vec3d3 = getCollisionHorizontalEscapeVector(
					this.getBbWidth(), passenger.getBbWidth(), this.getYRot() + (passenger.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F)
			);
			Vec3 vec3d4 = this.ridableravagers$locateSafeDismountingPos(vec3d3, passenger);
			return vec3d4 != null ? vec3d4 : this.position();
		}
	}

	@Unique
	protected void ridableravagers$putPlayerOnBack(Player player) {
		if (!this.level().isClientSide) {
			player.setYRot(this.getYRot());
			player.setXRot(this.getXRot());
			player.startRiding(this);
		}
	}

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void ridableravagers$tickMovement(CallbackInfo ci) {
		if (!this.level().isClientSide && this.isAlive()) {
			int i = this.getBreedingAge();
			if (i < 0) {
				this.setBreedingAge(++i);
			} else if (i > 0) {
				this.setBreedingAge(--i);
			}
		}
	}

	@Inject(method = "addAdditionalSaveData", at=@At("HEAD"))
	private void ridableravagers$writeCustomDataToNbt(CompoundTag nbt, CallbackInfo ci) {
		nbt.putInt("Age", this.getBreedingAge());
		nbt.putInt("ForcedAge", this.ridableravagers$forcedAge);
		nbt.putBoolean("Bred", this.isBred());
		nbt.putBoolean("Tame", this.isTamed());
		if (this.getOwnerUUID() != null) {
			nbt.putUUID("Owner", this.getOwnerUUID());
		}

		nbt.putInt("InLove", this.ridableravagers$loveTicks);
		if (this.ridableravagers$lovingPlayer != null) {
			nbt.putUUID("LoveCause", this.ridableravagers$lovingPlayer);
		}

		this.ridableravagers$saddledComponent.addAdditionalSaveData(nbt);
	}

	@Inject(method = "readAdditionalSaveData", at=@At("HEAD"))
	private void ridableravagers$readCustomDataToNbt(CompoundTag nbt, CallbackInfo ci) {
		this.setBreedingAge(nbt.getInt("Age"));
		this.ridableravagers$forcedAge = nbt.getInt("ForcedAge");
		this.setBred(nbt.getBoolean("Bred"));
		this.ridableravagers$setTamed(nbt.getBoolean("Tame"));
		UUID uUID;
		if (nbt.hasUUID("Owner")) {
			uUID = nbt.getUUID("Owner");
		} else {
			String string = nbt.getString("Owner");
			uUID = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), string);
		}

		if (uUID != null) {
			this.ridableravagers$setOwnerUuid(uUID);
		}

		this.ridableravagers$loveTicks = nbt.getInt("InLove");
		this.ridableravagers$lovingPlayer = nbt.hasUUID("LoveCause") ? nbt.getUUID("LoveCause") : null;

		this.ridableravagers$saddledComponent.readAdditionalSaveData(nbt);
	}

	@Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
	private void ridableravagers$handleStatus(byte status, CallbackInfo ci) {
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
