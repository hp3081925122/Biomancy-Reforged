package com.github.elenterius.biomancy.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public interface ItemCountRange {

	enum RangeSerializerType implements StringRepresentable {

		CONSTANT("constant", ConstantValue.CODEC, ConstantValue.STREAM_CODEC),
		UNIFORM("uniform", UniformRange.CODEC, UniformRange.STREAM_CODEC),
		BINOMIAL("binomial", BinomialRange.CODEC, BinomialRange.STREAM_CODEC);

		public static final Codec<RangeSerializerType> CODEC = StringRepresentable.fromEnum(RangeSerializerType::values);
		public static final StreamCodec<ByteBuf, RangeSerializerType> STREAM_CODEC = ByteBufCodecs.BYTE.map(b -> values()[b], t -> (byte) t.ordinal());

		final String type;
		final MapCodec<? extends ItemCountRange> codec;
		final StreamCodec<ByteBuf, ? extends ItemCountRange> streamCodec;

		RangeSerializerType(String type, MapCodec<? extends ItemCountRange> codec, StreamCodec<ByteBuf, ? extends ItemCountRange> streamCodec) {
			this.type = type;
			this.codec = codec;
			this.streamCodec = streamCodec;
		}

		public String getType() {
			return type;
		}

		@Override
		public String getSerializedName() {
			return type;
		}

	}

	Codec<ItemCountRange> CODEC = RangeSerializerType.CODEC.dispatch("type", ItemCountRange::getSerializerType, t -> t.codec);

	@SuppressWarnings("unchecked")
	StreamCodec<ByteBuf, ItemCountRange> STREAM_CODEC = RangeSerializerType.STREAM_CODEC.dispatch(ItemCountRange::getSerializerType, t -> (StreamCodec<ByteBuf, ItemCountRange>) t.streamCodec);

	int getCount(RandomSource rng);

	int getMaxCount();

	RangeSerializerType getSerializerType();

	record UniformRange(int min, int max) implements ItemCountRange {

		public static final MapCodec<UniformRange> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.fieldOf("min").forGetter(UniformRange::min),
				Codec.INT.fieldOf("max").forGetter(UniformRange::max)
		).apply(instance, UniformRange::new));

		public static final StreamCodec<ByteBuf, UniformRange> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, UniformRange::min,
				ByteBufCodecs.VAR_INT, UniformRange::max,
				UniformRange::new
		);

		@Override
		public int getCount(RandomSource rng) {
			return Mth.nextInt(rng, min, max);
		}

		@Override
		public int getMaxCount() {
			return max;
		}

		@Override
		public RangeSerializerType getSerializerType() {
			return RangeSerializerType.UNIFORM;
		}

	}

	record BinomialRange(int n, float p) implements ItemCountRange {

		public static final MapCodec<BinomialRange> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.fieldOf("n").forGetter(BinomialRange::n),
				Codec.FLOAT.fieldOf("p").forGetter(BinomialRange::p)
		).apply(instance, BinomialRange::new));

		public static final StreamCodec<ByteBuf, BinomialRange> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, BinomialRange::n,
				ByteBufCodecs.FLOAT, BinomialRange::p,
				BinomialRange::new
		);

		@Override
		public int getCount(RandomSource rng) {
			int v = 0;
			for (int i = 0; i < n; i++) {
				if (rng.nextFloat() < p) v++;
			}
			return v;
		}

		@Override
		public int getMaxCount() {
			return n;
		}

		@Override
		public RangeSerializerType getSerializerType() {
			return RangeSerializerType.BINOMIAL;
		}

	}

	record ConstantValue(int value) implements ItemCountRange {

		public static final MapCodec<ConstantValue> CODEC = Codec.INT.fieldOf("value").xmap(ConstantValue::new, ConstantValue::value);

		public static final StreamCodec<ByteBuf, ConstantValue> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(ConstantValue::new, ConstantValue::value);

		@Override
		public int getCount(RandomSource rng) {
			return value;
		}

		@Override
		public int getMaxCount() {
			return value;
		}

		@Override
		public RangeSerializerType getSerializerType() {
			return RangeSerializerType.CONSTANT;
		}

	}

}
