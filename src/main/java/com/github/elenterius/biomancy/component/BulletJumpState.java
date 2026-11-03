package com.github.elenterius.biomancy.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BulletJumpState(int costIndex, long timestamp) {

	public static final BulletJumpState EMPTY = new BulletJumpState(0, 0);

	public static final Codec<BulletJumpState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("bullet_jump_cost_index", 0).forGetter(BulletJumpState::costIndex),
			Codec.LONG.optionalFieldOf("bullet_jump_timestamp", 0L).forGetter(BulletJumpState::timestamp)
	).apply(instance, BulletJumpState::new));

	public static final StreamCodec<ByteBuf, BulletJumpState> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, BulletJumpState::costIndex,
			ByteBufCodecs.VAR_LONG, BulletJumpState::timestamp,
			BulletJumpState::new
	);

}
