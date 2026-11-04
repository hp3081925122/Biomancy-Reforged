package com.github.elenterius.biomancy.tooltip;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record TooltipContents(TooltipComponent component) implements ComponentContents {

	public static final MapCodec<TooltipContents> CODEC = MapCodec.of(
			Encoder.<TooltipContents>error("TooltipContents is a client-side placeholder and can't be serialized").fieldOf("tooltip"),
			Decoder.<TooltipContents>error("TooltipContents is a client-side placeholder and can't be deserialized").fieldOf("tooltip")
	);

	@Override
	public MapCodec<TooltipContents> codec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "tooltip{" + component + "}";
	}

}
