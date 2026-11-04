package com.github.elenterius.biomancy.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record EssenceData(Identifier entityType, String name, Optional<UUID> entityUUID, List<Integer> colors, int tier, Optional<CompoundTag> sounds, Optional<String> playerName) {

	public static final Codec<EssenceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("entity_type").forGetter(EssenceData::entityType),
			Codec.STRING.fieldOf("name").forGetter(EssenceData::name),
			UUIDUtil.CODEC.optionalFieldOf("entity_uuid").forGetter(EssenceData::entityUUID),
			Codec.INT.listOf().fieldOf("colors").forGetter(EssenceData::colors),
			Codec.INT.optionalFieldOf("essence_tier", 0).forGetter(EssenceData::tier),
			CompoundTag.CODEC.optionalFieldOf("sounds").forGetter(EssenceData::sounds),
			Codec.STRING.optionalFieldOf("player_name").forGetter(EssenceData::playerName)
	).apply(instance, EssenceData::new));

	public static final StreamCodec<ByteBuf, EssenceData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

	public EssenceData {
		colors = List.copyOf(colors);
		sounds = sounds.map(CompoundTag::copy);
	}

	public Optional<EntityType<?>> getEntityType() {
		return EntityType.byString(entityType.toString());
	}

	public int[] colorArray() {
		return colors.stream().mapToInt(Integer::intValue).toArray();
	}

	public EssenceData withPlayerName(String playerName) {
		return new EssenceData(entityType, name, entityUUID, colors, tier, sounds, Optional.of(playerName));
	}

}
