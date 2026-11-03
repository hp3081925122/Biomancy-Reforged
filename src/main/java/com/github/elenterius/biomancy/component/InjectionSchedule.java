package com.github.elenterius.biomancy.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record InjectionSchedule(int delayInTicks, long timestamp) {

	public static final Codec<InjectionSchedule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("delay_in_ticks").forGetter(InjectionSchedule::delayInTicks),
			Codec.LONG.fieldOf("schedule_timestamp").forGetter(InjectionSchedule::timestamp)
	).apply(instance, InjectionSchedule::new));

	public static final StreamCodec<ByteBuf, InjectionSchedule> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, InjectionSchedule::delayInTicks,
			ByteBufCodecs.VAR_LONG, InjectionSchedule::timestamp,
			InjectionSchedule::new
	);

}
