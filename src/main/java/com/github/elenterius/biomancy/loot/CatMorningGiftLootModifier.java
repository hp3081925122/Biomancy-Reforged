package com.github.elenterius.biomancy.loot;

import com.github.elenterius.biomancy.init.ModItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

public class CatMorningGiftLootModifier extends LootModifier {

	public static final MapCodec<CatMorningGiftLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
			codecStart(inst).apply(inst, CatMorningGiftLootModifier::new));

	public CatMorningGiftLootModifier() {
		this(
				new LootItemCondition[]{
						LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS).build(),
						LootTableIdCondition.builder(BuiltInLootTables.CAT_MORNING_GIFT.identifier()).build()
				}
		);
	}

	public CatMorningGiftLootModifier(LootItemCondition[] conditions) {
		this(conditions, IGlobalLootModifier.DEFAULT_PRIORITY);
	}

	public CatMorningGiftLootModifier(LootItemCondition[] conditions, int priority) {
		super(conditions, priority);
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		if (!(context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Cat cat)) return generatedLoot;

		RandomSource random = context.getRandom();

		boolean isBlackCat = cat.getVariant().unwrapKey()
				.filter(variant -> variant.equals(CatVariants.ALL_BLACK) || variant.equals(CatVariants.BLACK))
				.isPresent();

		for (int i = 0; i < generatedLoot.size(); i++) {
			ItemStack stack = generatedLoot.get(i);

			if (stack.is(Items.PHANTOM_MEMBRANE)) continue;

			if (isBlackCat) {
				if (random.nextFloat() < 0.25f) {
					generatedLoot.set(i, new ItemStack(random.nextBoolean() ? ModItems.MOB_CLAW.get() : ModItems.MOB_FANG.get(), random.nextInt(1, 3)));
				}
			}
			else if (random.nextFloat() < 0.10f) {
				Item[] items = {ModItems.FLESH_BITS.get(), ModItems.MOB_CLAW.get(), ModItems.MOB_FANG.get()};
				generatedLoot.set(i, new ItemStack(items[random.nextInt(items.length)]));
			}

			if (stack.is(Items.STRING) && random.nextFloat() < 0.2f) {
				generatedLoot.add(new ItemStack(ModItems.ORGANIC_MATTER.get()));
			}
		}

		return generatedLoot;
	}

}
