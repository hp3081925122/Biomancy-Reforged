package com.github.elenterius.biomancy.block;

import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.LevelReader;
import com.github.elenterius.biomancy.util.InteractionUtil;
import net.minecraft.world.InteractionResult;
import com.github.elenterius.biomancy.block.base.SimpleMultiFaceBlock;
import com.github.elenterius.biomancy.entity.misc.LivingEntityData;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.util.VoxelShapeUtil;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class JumpPadBlock extends SimpleMultiFaceBlock {

	public static final double EPSILON = 1.0e-7d;

	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

	protected static final Map<Direction, VoxelShape> SHAPE_BY_FACE = Util.make(new EnumMap<>(Direction.class), map -> {
		map.put(Direction.UP, createFaceShape(Direction.UP));
		map.put(Direction.DOWN, createFaceShape(Direction.DOWN));
		map.put(Direction.NORTH, createFaceShape(Direction.NORTH));
		map.put(Direction.SOUTH, createFaceShape(Direction.SOUTH));
		map.put(Direction.EAST, createFaceShape(Direction.EAST));
		map.put(Direction.WEST, createFaceShape(Direction.WEST));
	});

	public JumpPadBlock(Properties properties) {
		super(properties.noCollision().forceSolidOn());
		registerDefaultState(defaultBlockState().setValue(ENABLED, Boolean.TRUE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(ENABLED);
	}

	private static VoxelShape createFaceShape(Direction face) {
		return VoxelShapeUtil.createXZRotatedTowards(face, 2, 14, 2, 14, 16, 14);
	}

	@Override
	protected VoxelShape calculateMultifaceShape(BlockState state) {
		VoxelShape voxelshape = Shapes.empty();

		for (Direction face : DIRECTIONS) {
			if (hasFace(state, face)) {
				voxelshape = Shapes.or(voxelshape, SHAPE_BY_FACE.get(face));
			}
		}

		return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		return InteractionUtil.toItemInteractionResult(use(state, level, pos, player, hand, hitResult));
	}

	protected InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!player.mayBuild()) return InteractionResult.PASS;
		if (!player.isSecondaryUseActive()) return InteractionResult.PASS;
		if (level.isClientSide()) return InteractionResult.SUCCESS;

		BlockState newState = state.cycle(ENABLED);
		level.setBlock(pos, newState, UPDATE_CLIENTS);
		level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));

		float pitch = newState.getValue(ENABLED) ? 1f : 0.8f;
		level.playSound(null, pos, ModSoundEvents.FLESH_BLOCK_HIT.get(), SoundSource.BLOCKS, 0.6f, pitch + level.getRandom().nextFloat() * 0.2f);

		return InteractionResult.CONSUME;
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
		if (level.hasNeighborSignal(pos)) {
			level.setBlock(pos, state.cycle(ENABLED), UPDATE_ALL);
		}
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return context.getItemInHand().is(asItem()) && super.canBeReplaced(state, context);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		return getStateForPlacement(state, level, pos, context.getClickedFace().getOpposite());
	}

	@Override
	public @Nullable PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.DESTROY;
	}

	@Override
	public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
		if (oldState.is(newState.getBlock()) && levelReader instanceof ServerLevel serverLevel) {
			Level level = serverLevel;
			List<Direction> missingFaces = new ArrayList<>();
			for (Direction face : Direction.values()) {
				if (hasFace(oldState, face) && !hasFace(newState, face)) {
					missingFaces.add(face);
				}
			}

			if (!missingFaces.isEmpty()) {
				for (Direction face : missingFaces) {
					BlockState temp = defaultBlockState().setValue(getFaceProperty(face), Boolean.TRUE);
					List<ItemStack> drops = getDrops(temp, serverLevel, pos, null);
					drops.forEach(itemStack -> Block.popResourceFromFace(level, pos, face, itemStack));
				}
				SoundType soundType = oldState.getSoundType(level, pos, null);
				level.playSound(null, pos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1f) / 2f, soundType.getPitch() * 0.8f);
			}
		}
	}

	public boolean checkIfAABBIntersects(BlockPos pos, BlockState blockState, Direction face, AABB aabb) {
		return hasFace(blockState, face) && SHAPE_BY_FACE.get(face).bounds().move(pos).inflate(EPSILON).intersects(aabb);
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (level.isClientSide()) return;

		List<Direction> availableFaces = new ArrayList<>();
		List<AABB> collisionAABBs = new ArrayList<>();
		for (Direction face : Direction.values()) {
			if (hasFace(state, face)) {
				availableFaces.add(face);
				collisionAABBs.add(SHAPE_BY_FACE.get(face).bounds().move(pos));
			}
		}

		CollisionPrediction collisionPrediction = sweptAABB(entity, collisionAABBs);

		boolean skipCollision = switch (collisionPrediction) {
			case PREVIOUS_TICK, NEXT_TICK, NONE -> true;
			case TUNNELED_PREVIOUS_TICK, CURRENT_TICK, TUNNELS_NEXT_TICK -> false;
		};

		if (collisionPrediction != CollisionPrediction.NONE) {
			entity.fallDistance = 0;

			if (entity instanceof LivingEntityData.TransientDataProvider provider) {
				if (!entity.isSteppingCarefully()) provider.biomancy$getData().setDiscardFriction(true);
			}
		}

		if (skipCollision) return;
		if (!state.getValue(ENABLED)) return;
		if (entity.isSteppingCarefully() || entity.isSuppressingBounce()) return;

		Vector3d mutableImpulse = new Vector3d();
		for (Direction face : availableFaces) {
			Vec3i normal = face.getOpposite().getUnitVec3i();
			mutableImpulse.add(normal.getX() * 2d, normal.getY(), normal.getZ() * 2d);
		}

		final Vec3 velocity = entity.getDeltaMovement();

		double length = (entity instanceof LivingEntity ? 1d : 0.8d);
		mutableImpulse.normalize(length + velocity.length() * 0.8d);

		if (mutableImpulse.length() > 0d) {
			if (debounce(entity, pos)) return;

			entity.setDeltaMovement(mutableImpulse.x, mutableImpulse.y, mutableImpulse.z);
			entity.needsSync = true;
			entity.hurtMarked = true;

			level.playSound(null, pos, ModSoundEvents.FLESH_BLOB_JUMP.get(), SoundSource.BLOCKS, 0.5f, 1.2f);
		}
	}

	protected static CollisionPrediction sweptAABB(Entity entity, List<AABB> collisionAABBs) {
		AABB entityAABB = entity.getBoundingBox();
		Vec3 currentPos = entityAABB.getCenter();
		Vec3 offset = currentPos.subtract(entity.position());
		Vec3 prevPos = new Vec3(entity.xOld + offset.x, entity.yOld + offset.y, entity.zOld + offset.z);

		Vec3 nextPos = currentPos.add(entity.getDeltaMovement());

		double halfX = entityAABB.getXsize() / 2d + EPSILON;
		double halfY = entityAABB.getYsize() / 2d + EPSILON;
		double halfZ = entityAABB.getZsize() / 2d + EPSILON;

		for (AABB collisionAABB : collisionAABBs) {
			AABB inflatedAABB = collisionAABB.inflate(halfX, halfY, halfZ);

			if (inflatedAABB.contains(currentPos)) {
				return CollisionPrediction.CURRENT_TICK;
			}

			if (inflatedAABB.intersects(prevPos, currentPos)) {
				if (inflatedAABB.contains(prevPos)) return CollisionPrediction.PREVIOUS_TICK;
				return CollisionPrediction.TUNNELED_PREVIOUS_TICK;
			}

			if (inflatedAABB.intersects(currentPos, nextPos)) {
				if (inflatedAABB.contains(nextPos)) return CollisionPrediction.NEXT_TICK;

				inflatedAABB.clip(currentPos, nextPos).ifPresent(clipPos -> {
					entity.setPos(clipPos.x - offset.x, clipPos.y - offset.y, clipPos.z - offset.z);
				});

				return CollisionPrediction.TUNNELS_NEXT_TICK;
			}
		}

		return CollisionPrediction.NONE;
	}

	protected static boolean debounce(Entity entity, BlockPos blockPos) {
		CompoundTag tag = entity.getPersistentData().getCompoundOrEmpty("biomancy:jump_pad");
		if (entity.tickCount != tag.getIntOr("tick", 0)) {
			tag.putInt("tick", entity.tickCount);
			tag.remove("visitedPositions");
		}

		long visitingPos = blockPos.asLong();
		ListTag visitedPositions = tag.getListOrEmpty("visitedPositions");
		for (Tag visitedPos : visitedPositions) {
			if (visitedPos instanceof LongTag longTag) {
				if (longTag.longValue() == visitingPos) {
					return true;
				}
			}
		}
		visitedPositions.add(LongTag.valueOf(visitingPos));
		tag.put("visitedPositions", visitedPositions);
		entity.getPersistentData().put("biomancy:jump_pad", tag);

		return false;
	}

	protected enum CollisionPrediction {
		PREVIOUS_TICK,
		TUNNELED_PREVIOUS_TICK,
		CURRENT_TICK,
		NEXT_TICK,
		TUNNELS_NEXT_TICK,
		NONE
	}

}
