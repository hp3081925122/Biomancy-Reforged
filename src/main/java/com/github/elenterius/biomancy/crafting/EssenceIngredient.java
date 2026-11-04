package com.github.elenterius.biomancy.crafting;

import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.component.EssenceData;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.item.EssenceItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

public final class EssenceIngredient implements ICustomIngredient {

	public static final MapCodec<EssenceIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.fieldOf("entity_type").forGetter(EssenceIngredient::entityTypeId),
			Codec.intRange(0, 3).optionalFieldOf("tier", 0).forGetter(EssenceIngredient::tier)
	).apply(instance, EssenceIngredient::new));

	public static final StreamCodec<ByteBuf, EssenceIngredient> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, EssenceIngredient::entityTypeId,
			ByteBufCodecs.VAR_INT, EssenceIngredient::tier,
			EssenceIngredient::new
	);

	private final Identifier entityTypeId;
	private final int tier;
	private ItemStack displayStack;

	private EssenceIngredient(Identifier entityTypeId, int tier) {
		this.entityTypeId = entityTypeId;
		this.tier = tier;
	}

	private ItemStack displayStack() {
		if (displayStack == null) displayStack = EssenceItem.fromEntityType(BuiltInRegistries.ENTITY_TYPE.getValue(entityTypeId), tier);
		return displayStack;
	}

	public static Ingredient of(EntityType<?> entityType) {
		return of(entityType, 0);
	}

	public static Ingredient of(EntityType<?> entityType, int tier) {
		if (tier < 0 || tier > 3) throw new IllegalArgumentException("Cannot create a EssenceIngredient with invalid tier");
		return new EssenceIngredient(EntityType.getKey(entityType), tier).toVanilla();
	}

	public Identifier entityTypeId() {
		return entityTypeId;
	}

	public int tier() {
		return tier;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (!stack.is(ModItems.ESSENCE.get())) return false;

		EssenceData data = stack.get(ModDataComponents.ESSENCE);
		if (data == null || !data.entityType().equals(entityTypeId)) return false;

		return tier <= 0 || data.tier() == tier;
	}

	public ItemStack getDisplayStack() {
		return displayStack().copy();
	}

	@Override
	public Stream<Holder<Item>> items() {
		return Stream.of(ModItems.ESSENCE.get().builtInRegistryHolder());
	}

	@Override
	public SlotDisplay display() {
		ItemStack stack = displayStack();
		if (stack.isEmpty()) return new SlotDisplay.ItemSlotDisplay(ModItems.ESSENCE.get().builtInRegistryHolder());
		return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IngredientType<?> getType() {
		return ModRecipes.ESSENCE_INGREDIENT.get();
	}

}
