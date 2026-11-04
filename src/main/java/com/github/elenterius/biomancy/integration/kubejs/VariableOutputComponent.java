package com.github.elenterius.biomancy.integration.kubejs;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.crafting.VariableOutput;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record VariableOutputComponent() implements RecipeComponent<VariableOutput> {

	public static final String COUNT_RANGE_KEY = "countRange";

	public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(BiomancyMod.rl("variable_output"));
	public static final VariableOutputComponent INSTANCE = new VariableOutputComponent();

	private static RecipeComponent<ItemStack> itemStackComponent() {
		return ItemStackComponent.ITEM_STACK;
	}

	@Override
	public ResourceKey<RecipeComponentType<?>> type() {
		return TYPE;
	}

	@Override
	public Codec<VariableOutput> codec() {
		return VariableOutput.CODEC;
	}

	@Override
	public TypeInfo typeInfo() {
		return TypeInfo.of(VariableOutput.class);
	}

	@Override
	public VariableOutput wrap(RecipeScriptContext cx, Object from) {
		if (from instanceof VariableOutput output) return output;

		if (from instanceof Map<?, ?> map && map.containsKey(COUNT_RANGE_KEY)) {
			return cx.ops().decode(cx.cx(), VariableOutput.CODEC, from);
		}

		return new VariableOutput(itemStackComponent().wrap(cx, from));
	}

	@Override
	public boolean matches(RecipeMatchContext cx, VariableOutput value, ReplacementMatchInfo match) {
		return itemStackComponent().matches(cx, value.getItemStack(), match);
	}

	@Override
	public boolean isEmpty(VariableOutput value) {
		return value.getItemStack().isEmpty();
	}

	@Override
	public String toString() {
		return TYPE.identifier().toString();
	}

}
