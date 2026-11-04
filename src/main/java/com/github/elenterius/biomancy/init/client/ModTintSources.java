package com.github.elenterius.biomancy.init.client;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.membrane.BiometricMembraneBlock;
import com.github.elenterius.biomancy.block.vialholder.VialHolderBlock;
import com.github.elenterius.biomancy.init.ModBlocks;
import com.github.elenterius.biomancy.init.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ModTintSources {

	private ModTintSources() {}

	static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
		event.register(BiomancyMod.rl("essence"), Essence.MAP_CODEC);
		event.register(BiomancyMod.rl("potion_serum"), PotionSerum.MAP_CODEC);
		event.register(BiomancyMod.rl("biometric_membrane"), BiometricMembrane.MAP_CODEC);
	}

	static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
		event.register(blockTintLayers(5, VialHolderBlock::getTintColor), ModBlocks.VIAL_HOLDER.get());
		event.register(blockTintLayers(2, BiometricMembraneBlock::getTintColor), ModBlocks.BIOMETRIC_MEMBRANE.get());
	}

	private static List<BlockTintSource> blockTintLayers(int layers, BlockTintFunction function) {
		List<BlockTintSource> sources = new ArrayList<>(layers);
		for (int i = 0; i < layers; i++) {
			int tintIndex = i;
			sources.add(new BlockTintSource() {
				@Override
				public int color(BlockState state) {
					return ARGB.opaque(function.getTintColor(state, null, null, tintIndex));
				}

				@Override
				public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
					return ARGB.opaque(function.getTintColor(state, level, pos, tintIndex));
				}
			});
		}
		return List.copyOf(sources);
	}

	private static <T extends ItemTintSource> MapCodec<T> layerCodec(Function<Integer, T> factory, Function<T, Integer> getter) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.optionalFieldOf("layer", 0).forGetter(getter)
		).apply(instance, factory));
	}

	@FunctionalInterface
	private interface BlockTintFunction {
		int getTintColor(BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int tintIndex);
	}

	public record Essence(int layer) implements ItemTintSource {
		public static final MapCodec<Essence> MAP_CODEC = layerCodec(Essence::new, Essence::layer);

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(ModItems.ESSENCE.get().getColor(stack, layer));
		}

		@Override
		public MapCodec<Essence> type() {
			return MAP_CODEC;
		}
	}

	public record PotionSerum(int layer) implements ItemTintSource {
		public static final MapCodec<PotionSerum> MAP_CODEC = layerCodec(PotionSerum::new, PotionSerum::layer);

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(ModItems.POTION_SERUM.get().getTintColor(stack, layer));
		}

		@Override
		public MapCodec<PotionSerum> type() {
			return MAP_CODEC;
		}
	}

	public record BiometricMembrane(int layer) implements ItemTintSource {
		public static final MapCodec<BiometricMembrane> MAP_CODEC = layerCodec(BiometricMembrane::new, BiometricMembrane::layer);

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(BiometricMembraneBlock.getTintColor(stack, layer));
		}

		@Override
		public MapCodec<BiometricMembrane> type() {
			return MAP_CODEC;
		}
	}

}
