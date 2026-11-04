package com.github.elenterius.biomancy.block.cradle;

import com.github.elenterius.biomancy.block.base.BlockTooltipProvider;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import com.github.elenterius.biomancy.util.ItemCategoryUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import com.github.elenterius.biomancy.item.weapon.gun.GunbladeItem;
import com.github.elenterius.biomancy.item.weapon.ClawsItem;
import com.mojang.serialization.MapCodec;
import com.github.elenterius.biomancy.util.BlockItemUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.github.elenterius.biomancy.util.InteractionUtil;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.github.elenterius.biomancy.init.*;
import com.github.elenterius.biomancy.init.tags.ModItemTags;
import com.github.elenterius.biomancy.sounds.SoundUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.FormatUtil;
import com.github.elenterius.biomancy.world.mound.MoundShape;
import com.github.elenterius.spatialdb.SpatialDBManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PrimordialCradleBlock extends HorizontalDirectionalBlock implements BlockTooltipProvider, EntityBlock {

	public static final MapCodec<PrimordialCradleBlock> CODEC = simpleCodec(PrimordialCradleBlock::new);

	public static final Predicate<ItemStack> CANNOT_BE_SACRIFICED = stack -> {
		if (stack.is(ModItemTags.CANNOT_BE_EATEN_BY_CRADLE)) return true;

		Item item = stack.getItem();

		if (ItemCategoryUtil.isTieredTool(stack) || item instanceof BowItem || item instanceof CompassItem || item instanceof CrossbowItem || item instanceof FishingRodItem || item instanceof TridentItem || item instanceof ClawsItem || item instanceof GunbladeItem) return true;

		if (item instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();

			if (block instanceof EntityBlock) return true;
		}

		if (stack.isEnchanted()) return true;
		if (!item.canFitInsideContainerItems()) return true;
		return stack.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(stack)) != null;
	};

	protected static final VoxelShape INSIDE_AABB = box(3, 4, 3, 13, 16, 13);
	protected static final VoxelShape OUTSIDE_AABB = Stream.of(
			box(1, 0, 1, 15, 5, 15),
			box(0, 5, 0, 16, 12, 16),
			box(1, 12, 1, 15, 16, 15)
	).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	protected static final VoxelShape AABB = Shapes.join(OUTSIDE_AABB, INSIDE_AABB, BooleanOp.ONLY_FIRST);

	public PrimordialCradleBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends PrimordialCradleBlock> codec() {
		return CODEC;
	}

	public static float getYRotation(BlockState state) {
		return state.getValue(FACING).toYRot();
	}

	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityType, BlockEntityType<E> targetType, BlockEntityTicker<? super E> entityTicker) {
		return targetType == blockEntityType ? (BlockEntityTicker<A>) entityTicker : null;
	}

	public static int getPrimalEnergy(CompoundTag tag) {
		return tag.contains(PrimordialCradleBlockEntity.PRIMAL_ENERGY_KEY) ? tag.getIntOr(PrimordialCradleBlockEntity.PRIMAL_ENERGY_KEY, 0) : 0;
	}

	public static MutableComponent createValueComponent(DecimalFormat df, int value, String name) {
		return ComponentUtil.literal(df.format(value))
				.withStyle(TextStyles.PRIMORDIAL_RUNES_LIGHT_GRAY)
				.append(ComponentUtil.SPACE)
				.append(ComponentUtil.literal(name).withStyle(TextStyles.GRAY));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return ModBlockEntities.PRIMORDIAL_CRADLE.get().create(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return level.isClientSide() ? null : createTickerHelper(blockEntityType, ModBlockEntities.PRIMORDIAL_CRADLE.get(), PrimordialCradleBlockEntity::serverTick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return InteractionUtil.toItemInteractionResult(use(state, level, pos, player, hand, hit));
	}

	protected InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);

		if (increaseFillLevel(player, level, pos, stack.copyWithCount(1))) {
			if (!level.isClientSide()) {
				boolean isPotion = stack.getItem() instanceof PotionItem;

				if (!player.isCreative()) {
					stack.shrink(1);
				}

				if (stack.getCraftingRemainder() != null) {
					player.getInventory().add(stack.getCraftingRemainder().create());
				}
				else if (isPotion && stack.isEmpty()) {
					player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
				}

			}
			return InteractionResult.SUCCESS;
		}
		if (!level.isClientSide()) {
			SoundUtil.Server.playBlockSound((ServerLevel) level, pos, ModSoundEvents.CRADLE_NO);
		}

		return InteractionResult.CONSUME;
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (!level.isClientSide() && entity instanceof ItemEntity itemEntity) {
			ItemStack stack = itemEntity.getItem();
			Entity thrower = itemEntity.getOwner();

			if (stack.is(ModItems.LIVING_FLESH.get()) && thrower == null && itemEntity.getAge() < 80) {
				return;
			}

			if (increaseFillLevel(thrower, level, pos, stack)) {
				if (stack.getCraftingRemainder() != null) {
					entity.spawnAtLocation((ServerLevel) level, stack.getCraftingRemainder().create());
				}
				else if (stack.getItem() instanceof PotionItem) {
					entity.spawnAtLocation((ServerLevel) level, new ItemStack(Items.GLASS_BOTTLE));
				}
			}
		}
	}

	private boolean increaseFillLevel(@Nullable Entity player, Level level, BlockPos pos, ItemStack stack) {
		if (level.isClientSide()) return false;
		if (stack.isEmpty()) return false;
		if (CANNOT_BE_SACRIFICED.test(stack)) return false;

		ItemStack copyOfStack = stack.copyWithCount(1);
		if (level.getBlockEntity(pos) instanceof PrimordialCradleBlockEntity cradle && !cradle.isFull() && cradle.insertItem(stack)) {
			if (player instanceof ServerPlayer serverPlayer) {
				ModTriggers.SACRIFICED_ITEM_TRIGGER.get().trigger(serverPlayer, copyOfStack);
			}
			SoundEvent soundEvent = cradle.isFull() ? ModSoundEvents.CRADLE_BECAME_FULL.get() : ModSoundEvents.CRADLE_EAT.get();
			SoundUtil.Server.playBlockSound((ServerLevel) level, pos, soundEvent);
			return true;
		}

		return false;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return AABB;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(4) == 0 && level.getBlockEntity(pos) instanceof PrimordialCradleBlockEntity creator && creator.isFull()) {
			int particleAmount = random.nextInt(2, 8);
			int color = 0x9f4576;
			double r = (color >> 16 & 255) / 255d;
			double g = (color >> 8 & 255) / 255d;
			double b = (color & 255) / 255d;
			for (int i = 0; i < particleAmount; i++) {
				level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) r, (float) g, (float) b), pos.getX() + ((random.nextFloat() * 0.60625) + 0.13125f), pos.getY() + 0.5f, pos.getZ() + ((random.nextFloat() * 0.60625) + 0.13125f), r, g, b);
			}
			if (random.nextInt(3) == 0) {
				SoundUtil.Client.playBlockSound(level, pos, ModSoundEvents.CRADLE_CRAFTING_RANDOM, 0.85f);
			}
		}
	}

	
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		CompoundTag tag = BlockItemUtil.getBlockEntityData(stack);
		if (tag == null) return;

		DecimalFormat df = FormatUtil.getIntegerFormatter();

		int primalEnergy = getPrimalEnergy(tag);
		boolean hasPrimalEnergy = primalEnergy > 0;
		boolean hasTributes = tag.contains(PrimordialCradleBlockEntity.SACRIFICE_KEY);
		boolean hasProcGenValues = tag.contains(PrimordialCradleBlockEntity.PROC_GEN_VALUES_KEY);

		if (hasProcGenValues) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);
			tooltip.accept(ComponentUtil.literal("Seeded with:").withStyle(TextStyles.PRIMORDIAL_RUNES_MUTED_PURPLE));

			MoundShape.ProcGenValues procGenValues = MoundShape.ProcGenValues.readFrom(tag.getCompoundOrEmpty(PrimordialCradleBlockEntity.PROC_GEN_VALUES_KEY));

			tooltip.accept(
					ComponentUtil.literal(df.format(procGenValues.biomeTemperature()))
							.withStyle(TextStyles.PRIMORDIAL_RUNES_PURPLE)
							.append(ComponentUtil.literal(" Temperature").withStyle(TextStyles.PRIMORDIAL_RUNES_MUTED_PURPLE))
			);
			tooltip.accept(
					ComponentUtil.literal(df.format(procGenValues.biomeHumidity()))
							.withStyle(TextStyles.PRIMORDIAL_RUNES_PURPLE)
							.append(ComponentUtil.literal(" Humidity").withStyle(TextStyles.PRIMORDIAL_RUNES_MUTED_PURPLE))
			);

			if (!hasPrimalEnergy) tooltip.accept(ComponentUtil.EMPTY_LINE);
		}

		if (hasPrimalEnergy) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);
			tooltip.accept(
					ComponentUtil.literal(df.format(primalEnergy)).withStyle(TextStyles.PRIMORDIAL_RUNES_PURPLE)
							.append(ComponentUtil.literal(" Primal Energy").withStyle(TextStyles.PRIMORDIAL_RUNES_MUTED_PURPLE))
			);

			if (!hasTributes) tooltip.accept(ComponentUtil.EMPTY_LINE);
		}

		if (hasTributes) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);

			CompoundTag sacrificeTag = tag.getCompoundOrEmpty(PrimordialCradleBlockEntity.SACRIFICE_KEY);
			byte biomass = sacrificeTag.getByteOr("Biomass", (byte) 0);
			int lifeEnergy = sacrificeTag.getIntOr("LifeEnergy", 0);
			int success = sacrificeTag.getIntOr("Success", 0);
			int disease = sacrificeTag.getIntOr("Disease", 0);
			int hostile = sacrificeTag.getIntOr("Hostile", 0);
			int anomaly = sacrificeTag.getIntOr("Anomaly", 0);

			if (biomass > 0) tooltip.accept(createValueComponent(df, biomass, "Biomass"));
			if (lifeEnergy > 0) tooltip.accept(createValueComponent(df, lifeEnergy, "Life Energy"));
			if (success > 0) tooltip.accept(createValueComponent(df, success, "Success"));
			if (disease > 0) tooltip.accept(createValueComponent(df, disease, "Disease"));
			if (hostile != 0) tooltip.accept(createValueComponent(df, hostile, "Hostile"));
			if (anomaly > 0) tooltip.accept(createValueComponent(df, anomaly, "Anomaly"));
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		SpatialDBManager.getInstance(level).removeShape(level, pos);
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
	}
}
