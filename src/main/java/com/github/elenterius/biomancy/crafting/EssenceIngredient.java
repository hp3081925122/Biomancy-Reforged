package com.github.elenterius.biomancy.crafting;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

public final class EssenceIngredient implements ICustomIngredient {

	public static final MapCodec<EssenceIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("entity_type").forGetter(EssenceIngredient::entityTypeId),
			Codec.intRange(0, 3).optionalFieldOf("tier", 0).forGetter(EssenceIngredient::tier)
	).apply(instance, EssenceIngredient::new));

	public static final StreamCodec<ByteBuf, EssenceIngredient> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, EssenceIngredient::entityTypeId,
			ByteBufCodecs.VAR_INT, EssenceIngredient::tier,
			EssenceIngredient::new
	);

	private final ResourceLocation entityTypeId;
	private final int tier;
	private final ItemStack displayStack;

	private EssenceIngredient(ResourceLocation entityTypeId, int tier) {
		this.entityTypeId = entityTypeId;
		this.tier = tier;
		displayStack = EssenceItem.fromEntityType(BuiltInRegistries.ENTITY_TYPE.get(entityTypeId), tier);
	}

	public static Ingredient of(EntityType<?> entityType) {
		return of(entityType, 0);
	}

	public static Ingredient of(EntityType<?> entityType, int tier) {
		if (tier < 0 || tier > 3) throw new IllegalArgumentException("Cannot create a EssenceIngredient with invalid tier");
		return new EssenceIngredient(EntityType.getKey(entityType), tier).toVanilla();
	}

	public ResourceLocation entityTypeId() {
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

	@Override
	public Stream<ItemStack> getItems() {
		return Stream.of(displayStack.copy());
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
