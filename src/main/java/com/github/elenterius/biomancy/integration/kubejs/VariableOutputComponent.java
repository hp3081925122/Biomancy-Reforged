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
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record VariableOutputComponent(RecipeComponentType<?> type) implements RecipeComponent<VariableOutput> {

	public static final String COUNT_RANGE_KEY = "countRange";

	public static final RecipeComponentType<VariableOutput> TYPE = RecipeComponentType.unit(BiomancyMod.rl("variable_output"), VariableOutputComponent::new);

	private static RecipeComponent<ItemStack> itemStackComponent() {
		return ItemStackComponent.ITEM_STACK.instance();
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
		return type.toString();
	}

}
