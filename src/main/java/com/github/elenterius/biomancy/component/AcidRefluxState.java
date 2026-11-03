package com.github.elenterius.biomancy.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AcidRefluxState(byte acidLevel, boolean hasReflux, long delayStartTime) {

	public static final AcidRefluxState EMPTY = new AcidRefluxState((byte) 0, false, 0);

	public static final Codec<AcidRefluxState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BYTE.optionalFieldOf("acid_level", (byte) 0).forGetter(AcidRefluxState::acidLevel),
			Codec.BOOL.optionalFieldOf("has_reflux", false).forGetter(AcidRefluxState::hasReflux),
			Codec.LONG.optionalFieldOf("delay_start_time", 0L).forGetter(AcidRefluxState::delayStartTime)
	).apply(instance, AcidRefluxState::new));

	public static final StreamCodec<ByteBuf, AcidRefluxState> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BYTE, AcidRefluxState::acidLevel,
			ByteBufCodecs.BOOL, AcidRefluxState::hasReflux,
			ByteBufCodecs.VAR_LONG, AcidRefluxState::delayStartTime,
			AcidRefluxState::new
	);

	public AcidRefluxState withAcidLevel(byte acidLevel) {
		return new AcidRefluxState(acidLevel, hasReflux, delayStartTime);
	}

	public AcidRefluxState withReflux(boolean hasReflux) {
		return new AcidRefluxState(acidLevel, hasReflux, delayStartTime);
	}

	public AcidRefluxState withDelayStartTime(long delayStartTime) {
		return new AcidRefluxState(acidLevel, hasReflux, delayStartTime);
	}

}
