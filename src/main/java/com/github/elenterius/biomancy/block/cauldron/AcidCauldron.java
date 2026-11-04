package com.github.elenterius.biomancy.block.cauldron;

import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.InteractionResult;
import com.github.elenterius.biomancy.init.AcidInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;

public class AcidCauldron extends LayeredCauldronBlock {

	public AcidCauldron(Properties properties) {
		super(Biome.Precipitation.NONE, AcidInteractions.ACID_CAULDRON, properties);
	}

	@Override
	protected boolean canReceiveStalactiteDrip(Fluid fluid) {
		return false;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (entity instanceof LivingEntity livingEntity) {
			if (livingEntity.tickCount % 5 != 0) return;
			AcidInteractions.handleEntityInsideAcid(livingEntity);
		}
	}

	@Override
	public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			Block convertedBlock = AcidInteractions.convertBlock(block);
			if (convertedBlock != null) {
				if (!level.isClientSide()) {
					player.setItemInHand(hand, new ItemStack(convertedBlock.asItem(), stack.getCount()));
					player.awardStat(Stats.USE_CAULDRON);

					SoundType soundType = block.getSoundType(block.defaultBlockState(), level, pos, null);
					level.playSound(null, pos, soundType.getHitSound(), SoundSource.BLOCKS, soundType.volume, soundType.pitch);
					level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
				}

				return InteractionResult.SUCCESS;
			}
		}

		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}
