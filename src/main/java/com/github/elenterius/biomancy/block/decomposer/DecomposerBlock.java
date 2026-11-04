package com.github.elenterius.biomancy.block.decomposer;

import com.github.elenterius.biomancy.block.base.BlockTooltipProvider;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import com.mojang.serialization.MapCodec;
import com.github.elenterius.biomancy.util.BlockItemUtil;
import net.minecraft.world.item.Item;
import com.github.elenterius.biomancy.util.InteractionUtil;
import net.minecraft.world.InteractionResult;
import com.github.elenterius.biomancy.block.base.HorizontalFacingMachineBlock;
import com.github.elenterius.biomancy.block.base.MachineBlockEntity;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.sounds.SoundUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.FormatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Stream;

public class DecomposerBlock extends HorizontalFacingMachineBlock implements BlockTooltipProvider {

	public static final MapCodec<DecomposerBlock> CODEC = simpleCodec(DecomposerBlock::new);

	protected static final VoxelShape AABB = Stream.of(Block.box(0, 0, 0, 16, 14, 16), Block.box(1, 14, 1, 15, 17, 15), Block.box(2, 17, 2, 14, 18, 14)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public DecomposerBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends DecomposerBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return ModBlockEntities.DECOMPOSER.get().create(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return level.isClientSide() ? null : createTickerHelper(blockEntityType, ModBlockEntities.DECOMPOSER.get(), MachineBlockEntity::serverTick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return InteractionUtil.toItemInteractionResult(use(state, level, pos, player, hand, hit));
	}

	protected InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof DecomposerBlockEntity decomposer && decomposer.canPlayerInteract(player)) {
			if (!level.isClientSide()) {
				player.openMenu(decomposer, buffer -> buffer.writeBlockPos(pos));
				SoundUtil.Server.playBlockSound((ServerLevel) level, pos, ModSoundEvents.UI_DECOMPOSER_OPEN);
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.CONSUME;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return AABB;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(5) == 0 && Boolean.TRUE.equals(state.getValue(CRAFTING))) {
			int particleAmount = random.nextInt(1, 5);
			int color = 0xc7b15d;
			double r = (color >> 16 & 255) / 255d;
			double g = (color >> 8 & 255) / 255d;
			double b = (color & 255) / 255d;
			for (int i = 0; i < particleAmount; i++) {
				level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) r, (float) g, (float) b), pos.getX() + 0.5d + ((random.nextFloat() - random.nextFloat()) * 0.3F), pos.getY() + 0.75d, pos.getZ() + 0.5d + ((random.nextFloat() - random.nextFloat()) * 0.3F), r, g, b);
			}

			if (random.nextInt(3) == 0) {
				SoundUtil.Client.playBlockSound(level, pos, ModSoundEvents.DECOMPOSER_CRAFTING_RANDOM, 0.65f);
			}
		}
	}

	
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		int fuelAmount = getFuelAmount(stack);
		if (fuelAmount > 0) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);
			DecimalFormat df = FormatUtil.getIntegerFormatter();
			tooltip.accept(ComponentUtil.translatable("tooltip.biomancy.nutrients_fuel").withStyle(ChatFormatting.GRAY));
			tooltip.accept(ComponentUtil.literal("%s/%s u".formatted(df.format(fuelAmount), df.format(DecomposerBlockEntity.MAX_FUEL))).withStyle(TextStyles.NUTRIENTS));
		}
	}

	public static int getFuelAmount(ItemStack stack) {
		CompoundTag tag = BlockItemUtil.getBlockEntityData(stack);
		return tag != null && tag.contains("Fuel") ? tag.getCompoundOrEmpty("Fuel").getIntOr("Amount", 0) : 0;
	}
}
