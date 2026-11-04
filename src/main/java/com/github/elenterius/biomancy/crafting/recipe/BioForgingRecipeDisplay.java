package com.github.elenterius.biomancy.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public record BioForgingRecipeDisplay(List<SlotDisplay> ingredients, List<Integer> counts, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

	public static final MapCodec<BioForgingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(BioForgingRecipeDisplay::ingredients),
			Codec.INT.listOf().fieldOf("counts").forGetter(BioForgingRecipeDisplay::counts),
			SlotDisplay.CODEC.fieldOf("result").forGetter(BioForgingRecipeDisplay::result),
			SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(BioForgingRecipeDisplay::craftingStation)
	).apply(instance, BioForgingRecipeDisplay::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BioForgingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
			SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), BioForgingRecipeDisplay::ingredients,
			ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), BioForgingRecipeDisplay::counts,
			SlotDisplay.STREAM_CODEC, BioForgingRecipeDisplay::result,
			SlotDisplay.STREAM_CODEC, BioForgingRecipeDisplay::craftingStation,
			BioForgingRecipeDisplay::new
	);

	public static final RecipeDisplay.Type<BioForgingRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public RecipeDisplay.Type<BioForgingRecipeDisplay> type() {
		return TYPE;
	}

	@Override
	public boolean isEnabled(FeatureFlagSet enabledFeatures) {
		return ingredients.stream().allMatch(display -> display.isEnabled(enabledFeatures)) && RecipeDisplay.super.isEnabled(enabledFeatures);
	}

}
