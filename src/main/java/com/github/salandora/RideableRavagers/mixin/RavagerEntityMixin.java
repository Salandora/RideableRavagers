package com.github.salandora.RideableRavagers.mixin;

import com.github.salandora.RideableRavagers.entity.BreedableEntity;
import com.github.salandora.RideableRavagers.entity.Tamed;
import com.github.salandora.RideableRavagers.entity.ai.goal.ActiveTargetAccessor;
import com.github.salandora.RideableRavagers.entity.ai.goal.BreedableMateGoal;
import com.github.salandora.RideableRavagers.entity.ai.goal.RavagerAttackWithOwnerGoal;
import com.github.salandora.RideableRavagers.entity.ai.goal.RavagerTemptGoal;
import com.github.salandora.RideableRavagers.entity.ai.goal.RavagerTrackOwnerAttackerGoal;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static net.minecraft.entity.passive.PassiveEntity.toGrowUpAge;

@Mixin(RavagerEntity.class)
public abstract class RavagerEntityMixin extends RaiderEntity implements BreedableEntity, Tamed {
	private static final TrackedData<Boolean> ridableravagers$BABY = DataTracker.registerData(RavagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Byte> ridableravagers$FLAGS = DataTracker.registerData(RavagerEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final int ridableravagers$TAMED_FLAG = 2;
	//private static final int ridableravagers$SADDLED_FLAG = 4;
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

	protected RavagerEntityMixin(EntityType<? extends RaiderEntity> entityType, World world) {
		super(entityType, world);
	}

	@Unique
	protected boolean ridableravagers$getFlag(int bitmask) {
		return (this.dataTracker.get(ridableravagers$FLAGS) & bitmask) != 0;
	}
	@Unique
	protected void ridableravagers$setFlag(int bitmask, boolean flag) {
		byte b = this.dataTracker.get(ridableravagers$FLAGS);
		if (flag) {
			this.dataTracker.set(ridableravagers$FLAGS, (byte)(b | bitmask));
		} else {
			this.dataTracker.set(ridableravagers$FLAGS, (byte)(b & ~bitmask));
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
	public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
		if (target instanceof CreeperEntity || target instanceof GhastEntity) {
			return false;
		} else if (target instanceof Tamed tamedEntity) {
			return !tamedEntity.isTamed() || tamedEntity.getOwner() != owner;
		} else if (target instanceof WolfEntity wolfEntity) {
			return !wolfEntity.isTamed() || wolfEntity.getOwner() != owner;
		} else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).shouldDamagePlayer((PlayerEntity)target)) {
			return false;
		} else if (target instanceof AbstractHorseEntity && ((AbstractHorseEntity)target).isTame()) {
			return false;
		} else {
			return !(target instanceof TameableEntity) || !((TameableEntity)target).isTamed();
		}
	}

	@Nullable
	@Unique
	public UUID getOwnerUuid() {
		return this.ridableravagers$ownerUuid;
	}
	@Unique
	public void ridableravagers$setOwnerUuid(@Nullable UUID ownerUuid) {
		this.ridableravagers$ownerUuid = ownerUuid;
	}

	@Override
	public EntityView method_48926() {
		return this.getWorld();
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(ridableravagers$BABY, false);
		this.dataTracker.startTracking(ridableravagers$FLAGS, (byte) 0);
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		if (ridableravagers$BABY.equals(data)) {
			this.calculateDimensions();
		}

		super.onTrackedDataSet(data);
	}

	@Unique
	public void ridableravagers$growUp(int age, boolean overGrow) {
		int i = this.getBreedingAge();
		i += age * 20;
		if (i > 0) {
			i = 0;
		}

		int k = i - i;
		this.setBreedingAge(i);
		if (overGrow) {
			this.ridableravagers$forcedAge += k;
		}

		if (this.getBreedingAge() == 0) {
			this.setBreedingAge(this.ridableravagers$forcedAge);
		}
	}

	@Override
	public int getBreedingAge() {
		if (this.getWorld().isClient) {
			return this.dataTracker.get(ridableravagers$BABY) ? -1 : 1;
		} else {
			return this.ridableravagers$breedingAge;
		}
	}

	@Override
	public void setBreedingAge(int age) {
		int i = this.getBreedingAge();
		this.ridableravagers$breedingAge = age;
		if (i < 0 && age >= 0 || i >= 0 && age < 0) {
			this.dataTracker.set(ridableravagers$BABY, age < 0);
			this.ridableravagers$onGrowUp();
		}
	}

	@Unique
	protected void ridableravagers$onGrowUp() {
		if (this.isBaby()) {
			this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(3);
		} else if (this.hasVehicle()) {
			this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(12);

			Entity var2 = this.getVehicle();
			if (var2 instanceof BoatEntity boatEntity && !boatEntity.isSmallerThanBoat(this)) {
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
		return stack.isOf(Items.COOKED_BEEF);
	}

	@Override
	public void lovePlayer(@Nullable PlayerEntity player) {
		this.ridableravagers$loveTicks = 600;
		if (player != null) {
			this.ridableravagers$lovingPlayer = player.getUuid();
		}

		this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_BREEDING_PARTICLES);

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
	public @Nullable ServerPlayerEntity getLovingPlayer() {
		if (this.ridableravagers$lovingPlayer == null) {
			return null;
		} else {
			PlayerEntity playerEntity = this.getWorld().getPlayerByUuid(this.ridableravagers$lovingPlayer);
			return playerEntity instanceof ServerPlayerEntity ? (ServerPlayerEntity)playerEntity : null;
		}
	}

	@Inject(method = "initGoals", at = @At("TAIL"))
	private void ridableravagers$initGoals(CallbackInfo ci) {
		RavagerEntity ravager = (RavagerEntity)(Object)this;
		this.goalSelector.add(2, new BreedableMateGoal(this, 1.0, RavagerEntity.class));
		this.goalSelector.add(3, new RavagerTemptGoal(ravager, 1.25, Ingredient.ofItems(Items.COOKED_BEEF), false));

		this.targetSelector.getGoals().stream()
				.filter(goal -> goal.getGoal() instanceof ActiveTargetGoal<?>)
				.map(goal -> (ActiveTargetGoal<?>)goal.getGoal())
				.forEach(goal -> {
					ActiveTargetAccessor accessor = (ActiveTargetAccessor)goal;
					Class<?> targetClazz = accessor.getTargetClass();
					if (targetClazz == PlayerEntity.class) {
						accessor.setPredicate(entity -> !this.isTamed());
					} else if (targetClazz == MerchantEntity.class) {
						accessor.setPredicate(entity -> !this.isTamed() && !entity.isBaby());
					} else if (targetClazz == IronGolemEntity.class) {
						accessor.setPredicate(entity -> !this.isTamed());
					}
				});

		this.targetSelector.add(1, new RavagerTrackOwnerAttackerGoal(ravager));
		this.targetSelector.add(2, new RavagerAttackWithOwnerGoal(ravager));
	}
	@Override
	public MobEntity createChild(ServerWorld world, BreedableEntity other) {
		RavagerEntity entity = EntityType.RAVAGER.create(world);
		((BreedableEntity) entity).setBred(true);
		return entity;
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (this.isBreedingItem(itemStack)) {
			int i = this.getBreedingAge();
			if (!this.getWorld().isClient && i == 0 && this.canEat()) {
				this.eat(player, hand, itemStack);
				this.lovePlayer(player);
				return ActionResult.SUCCESS;
			}

			if (this.isBaby()) {
				// TODO: change to "Feed the baby Steaks to gain hearts as it grows to "gain it's trust". (Could use angry hearts from villagers with some random number eventually being the hearts when the Ravager grows.)"
				this.eat(player, hand, itemStack);
				if (!this.isTamed()) {
					this.ridableravagers$setTamed(true);
					this.ridableravagers$setOwnerUuid(player.getUuid());
					this.setTarget(null);
				}

				this.ridableravagers$growUp(toGrowUpAge(-i), true);
				return ActionResult.success(this.getWorld().isClient);
			}

			if (this.getWorld().isClient) {
				return ActionResult.CONSUME;
			}
		}

		if (this.hasPassengers() || this.isBaby()) {
			return super.interactMob(player, hand);
		}

		this.ridableravagers$putPlayerOnBack(player);
		return ActionResult.success(this.getWorld().isClient);
	}

	@Override
	public boolean canBeLeashedBy(PlayerEntity player) {
		return !this.isLeashed() && isBred();
	}

	@Override
	protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
		super.tickControlled(controllingPlayer, movementInput);
		Vec2f vec2f = this.ridableravagers$getControlledRotation(controllingPlayer);
		this.setRotation(vec2f.y, vec2f.x);
		this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
	}

	protected Vec2f ridableravagers$getControlledRotation(LivingEntity controllingPassenger) {
		return new Vec2f(controllingPassenger.getPitch() * 0.5F, controllingPassenger.getYaw());
	}

	@Override
	protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
		float f = controllingPlayer.sidewaysSpeed * 0.5F;
		float g = controllingPlayer.forwardSpeed;
		if (g <= 0.0F) {
			g *= 0.25F;
		}

		return new Vec3d((double)f, 0.0, (double)g);
	}

	@Override
	protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
		return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
	}

	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		Entity var3 = this.getFirstPassenger();
		if (var3 instanceof MobEntity) {
			return (MobEntity)var3;
		} else {
			//if (this.isSaddled()) {
				var3 = this.getFirstPassenger();
				if (var3 instanceof PlayerEntity) {
					return (PlayerEntity)var3;
				}
			//}

			return null;
		}
	}

	@Nullable
	private Vec3d ridableravagers$locateSafeDismountingPos(Vec3d offset, LivingEntity passenger) {
		double d = this.getX() + offset.x;
		double e = this.getBoundingBox().minY;
		double f = this.getZ() + offset.z;
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for(EntityPose entityPose : passenger.getPoses()) {
			mutable.set(d, e, f);
			double g = this.getBoundingBox().maxY + 0.75;

			do {
				double h = this.getWorld().getDismountHeight(mutable);
				if ((double)mutable.getY() + h > g) {
					break;
				}

				if (Dismounting.canDismountInBlock(h)) {
					Box box = passenger.getBoundingBox(entityPose);
					Vec3d vec3d = new Vec3d(d, (double)mutable.getY() + h, f);
					if (Dismounting.canPlaceEntityAt(this.getWorld(), passenger, box.offset(vec3d))) {
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
	public Vec3d updatePassengerForDismount(LivingEntity passenger) {
		Vec3d vec3d = getPassengerDismountOffset(
				(double)this.getWidth(), (double)passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.RIGHT ? 90.0F : -90.0F)
		);
		Vec3d vec3d2 = this.ridableravagers$locateSafeDismountingPos(vec3d, passenger);
		if (vec3d2 != null) {
			return vec3d2;
		} else {
			Vec3d vec3d3 = getPassengerDismountOffset(
					(double)this.getWidth(), (double)passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.LEFT ? 90.0F : -90.0F)
			);
			Vec3d vec3d4 = this.ridableravagers$locateSafeDismountingPos(vec3d3, passenger);
			return vec3d4 != null ? vec3d4 : this.getPos();
		}
	}

	@Unique
	protected void ridableravagers$putPlayerOnBack(PlayerEntity player) {
		if (!this.getWorld().isClient) {
			player.setYaw(this.getYaw());
			player.setPitch(this.getPitch());
			player.startRiding(this);
		}
	}

	@Inject(method = "tickMovement", at = @At("HEAD"))
	private void ridableravagers$tickMovement(CallbackInfo ci) {
		if (!this.getWorld().isClient && this.isAlive()) {
			int i = this.getBreedingAge();
			if (i < 0) {
				this.setBreedingAge(++i);
			} else if (i > 0) {
				this.setBreedingAge(--i);
			}
		}
	}

	@Inject(method = "writeCustomDataToNbt", at=@At("HEAD"))
	private void ridableravagers$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putInt("Age", this.getBreedingAge());
		nbt.putInt("ForcedAge", this.ridableravagers$forcedAge);
		nbt.putBoolean("Bred", this.isBred());
		nbt.putBoolean("Tame", this.isTamed());
		if (this.getOwnerUuid() != null) {
			nbt.putUuid("Owner", this.getOwnerUuid());
		}

		nbt.putInt("InLove", this.ridableravagers$loveTicks);
		if (this.ridableravagers$lovingPlayer != null) {
			nbt.putUuid("LoveCause", this.ridableravagers$lovingPlayer);
		}
	}

	@Inject(method = "readCustomDataFromNbt", at=@At("HEAD"))
	private void ridableravagers$readCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		this.setBreedingAge(nbt.getInt("Age"));
		this.ridableravagers$forcedAge = nbt.getInt("ForcedAge");
		this.setBred(nbt.getBoolean("Bred"));
		this.ridableravagers$setTamed(nbt.getBoolean("Tame"));
		UUID uUID;
		if (nbt.containsUuid("Owner")) {
			uUID = nbt.getUuid("Owner");
		} else {
			String string = nbt.getString("Owner");
			uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
		}

		if (uUID != null) {
			this.ridableravagers$setOwnerUuid(uUID);
		}

		this.ridableravagers$loveTicks = nbt.getInt("InLove");
		this.ridableravagers$lovingPlayer = nbt.containsUuid("LoveCause") ? nbt.getUuid("LoveCause") : null;
	}

	@Inject(method = "handleStatus", at = @At("HEAD"), cancellable = true)
	private void ridableravagers$handleStatus(byte status, CallbackInfo ci) {
		if (status == EntityStatuses.ADD_BREEDING_PARTICLES) {
			for(int i = 0; i < 7; ++i) {
				double d = this.random.nextGaussian() * 0.02;
				double e = this.random.nextGaussian() * 0.02;
				double f = this.random.nextGaussian() * 0.02;
				this.getWorld().addParticle(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
			}
			ci.cancel();
		}
	}
}
