package com.github.elenterius.biomancy.loot;

import com.github.elenterius.biomancy.BiomancyConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record TradeConfigCondition(Option option) implements LootItemCondition {

	public static final MapCodec<TradeConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Option.CODEC.fieldOf("option").forGetter(TradeConfigCondition::option)
	).apply(instance, TradeConfigCondition::new));

	@Override
	public MapCodec<TradeConfigCondition> codec() {
		return CODEC;
	}

	@Override
	public boolean test(LootContext context) {
		return switch (option) {
			case VILLAGERS -> !Boolean.FALSE.equals(BiomancyConfig.SERVER_SYNCED.addTradesToVillagers.get());
			case WANDERING_TRADER -> !Boolean.FALSE.equals(BiomancyConfig.SERVER_SYNCED.addTradesToWanderingTrader.get());
		};
	}

	public enum Option implements StringRepresentable {
		VILLAGERS("villagers"),
		WANDERING_TRADER("wandering_trader");

		public static final Codec<Option> CODEC = StringRepresentable.fromEnum(Option::values);

		private final String name;

		Option(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

}
