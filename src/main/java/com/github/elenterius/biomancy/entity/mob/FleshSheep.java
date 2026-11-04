package com.github.elenterius.biomancy.entity.mob;

import java.util.Optional;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import com.github.elenterius.biomancy.init.ModEntityTypes;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModLoot;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.mixin.accessor.SheepAccessor;
import com.github.elenterius.biomancy.util.animation.MobAnimations;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class FleshSheep extends Sheep implements GeoEntity {

	protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public FleshSheep(EntityType<? extends Sheep> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public void setColor(DyeColor dyeColor) {
		super.setColor(DyeColor.GRAY);
	}

	@Override
	public DyeColor getColor() {
		return DyeColor.GRAY;
	}

	@Override
	public void shear(ServerLevel level, SoundSource soundSource, ItemStack tool) {
		level().playSound(null, this, SoundEvents.SHEEP_SHEAR, soundSource, 1f, 1f);

		setSheared(true);

		int count = random.nextIntBetweenInclusive(1, 4);
		for (int i = 0; i < count; i++) {
			ItemEntity itemEntity = spawnAtLocation(level, new ItemStack(ModItems.FLESH_BITS.get()), 1f);
			if (itemEntity != null) {
				itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(
						(random.nextFloat() - random.nextFloat()) * 0.1f,
						random.nextFloat() * 0.05f,
						(random.nextFloat() - random.nextFloat()) * 0.1f)
				);
			}
		}

		count = random.nextIntBetweenInclusive(2, 4);
		for (int i = 0; i < count; i++) {
			ItemEntity itemEntity = spawnAtLocation(level, new ItemStack(Items.STRING), 1f);
			if (itemEntity != null) {
				itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(
						(random.nextFloat() - random.nextFloat()) * 0.1f,
						random.nextFloat() * 0.05f,
						(random.nextFloat() - random.nextFloat()) * 0.1f)
				);
			}
		}
	}

	@Override
	public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level world, BlockPos pos) {
		world.playSound(null, this, SoundEvents.SHEEP_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 1f, 1f);
		gameEvent(GameEvent.SHEAR, player);

		if (!world.isClientSide()) {
			setSheared(true);

			List<ItemStack> list = new ArrayList<>();

			int count = random.nextIntBetweenInclusive(1, 4);
			for (int i = 0; i < count; i++) {
				list.add(new ItemStack(ModItems.FLESH_BITS.get()));
			}

			count = random.nextIntBetweenInclusive(2, 4);
			for (int i = 0; i < count; i++) {
				list.add(new ItemStack(Items.STRING));
			}

			return list;
		}

		return List.of();
	}

	protected Optional<ResourceKey<LootTable>> getDefaultLootTable() {
		return isSheared() ? getType().getDefaultLootTable() : Optional.of(ModLoot.Entity.FLESH_SHEEP_UNSHORN);
	}

	@Override
	protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
		Optional<ResourceKey<LootTable>> lootTable = getLootTable();
		if (lootTable.equals(getType().getDefaultLootTable())) {
			lootTable = getDefaultLootTable();
		}
		lootTable.ifPresent(key -> dropFromLootTable(level, source, playerKilled, key));
	}

	@Nullable
	@Override
	public Sheep getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
		if (otherParent.getClass() != getClass() && random.nextFloat() < 0.15f) {
			return (Sheep) otherParent.getBreedOffspring(level, this);
		}

		return ModEntityTypes.FLESH_SHEEP.get().create(level, EntitySpawnReason.BREEDING);
	}

	@Override
	public boolean canMate(Animal otherAnimal) {
		return super.canMate(otherAnimal);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSoundEvents.FLESH_SHEEP_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return ModSoundEvents.FLESH_SHEEP_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSoundEvents.FLESH_SHEEP_DEATH.get();
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(MobAnimations.walkController(this));
		controllers.add(MobAnimations.babyTransformController(this));
		controllers.add(Animations.shearedController(this));
		controllers.add(Animations.grazingController(this));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	public static final class Animations {

		private Animations() {}

		public static final RawAnimation GRAZING_ANIMATION = RawAnimation.begin().thenPlay("grazing");
		public static final RawAnimation SHEARED_ANIMATION = RawAnimation.begin().thenPlay("sheared");

		public static <T extends Sheep & GeoEntity> AnimationController<T> grazingController(T geoEntity) {
			return new AnimationController<>("grazing", state -> {
				if (getEatAnimationTick(state.animatable()) > 0) {
					return state.setAndContinue(GRAZING_ANIMATION);
				}

				state.controller().reset();
				return PlayState.STOP;
			});
		}

		public static <T extends Sheep & GeoEntity> AnimationController<T> shearedController(T geoEntity) {
			return new AnimationController<>("sheared", state -> {
				if (geoEntity.isSheared()) {
					return state.setAndContinue(SHEARED_ANIMATION);
				}

				return PlayState.STOP;
			});
		}

		public static int getEatAnimationTick(Sheep sheep) {
			return ((SheepAccessor) sheep).biomancy$getEatAnimationTick();
		}

	}

}
