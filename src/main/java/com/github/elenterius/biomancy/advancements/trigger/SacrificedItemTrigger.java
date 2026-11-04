package com.github.elenterius.biomancy.advancements.trigger;

import com.github.elenterius.biomancy.util.ItemHolders;
import com.github.elenterius.biomancy.init.ModTriggers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

public class SacrificedItemTrigger extends SimpleCriterionTrigger<SacrificedItemTrigger.TriggerInstance> {

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack stack) {
		trigger(player, triggerInstance -> triggerInstance.matches(stack));
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {

		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> sacrificedItems(ItemLike... items) {
			return create(Optional.of(ItemPredicate.Builder.item().of(ItemHolders.LOOKUP, items).build()));
		}

		public static Criterion<TriggerInstance> sacrificedItem(ItemLike item) {
			return create(Optional.of(ItemPredicate.Builder.item().of(ItemHolders.LOOKUP, item).build()));
		}

		public static Criterion<TriggerInstance> sacrificedItem(TagKey<Item> tag) {
			return create(Optional.of(ItemPredicate.Builder.item().of(ItemHolders.LOOKUP, tag).build()));
		}

		public static Criterion<TriggerInstance> sacrificedItem() {
			return create(Optional.empty());
		}

		private static Criterion<TriggerInstance> create(Optional<ItemPredicate> predicate) {
			return ModTriggers.SACRIFICED_ITEM_TRIGGER.get().createCriterion(new TriggerInstance(Optional.empty(), predicate));
		}

		public boolean matches(ItemStack stack) {
			return item.isEmpty() || item.get().test(stack);
		}

	}

}
