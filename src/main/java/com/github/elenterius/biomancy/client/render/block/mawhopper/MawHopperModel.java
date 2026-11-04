package com.github.elenterius.biomancy.client.render.block.mawhopper;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.mawhopper.MawHopperBlock;
import com.github.elenterius.biomancy.block.mawhopper.MawHopperBlockEntity;
import com.github.elenterius.biomancy.block.property.VertexType;
import com.github.elenterius.biomancy.client.render.BiomancyGeoModel;
import net.minecraft.resources.Identifier;

public class MawHopperModel extends BiomancyGeoModel<MawHopperBlockEntity> {

	protected static final Identifier SOURCE_MODEL = BiomancyMod.rl("block/maw_hopper");
	protected static final Identifier STRAIGHT_MODEL = BiomancyMod.rl("block/maw_hopper_connected_straight");
	protected static final Identifier CORNER_MODEL = BiomancyMod.rl("block/maw_hopper_connected_corner");
	protected static final Identifier TEXTURE = BiomancyMod.rl("textures/block/maw_hopper.png");
	protected static final Identifier INPUT_ANIMATION = BiomancyMod.rl("block/maw_hopper");
	protected static final Identifier CONNECTED_ANIMATION = BiomancyMod.rl("block/maw_hopper_connected");

	@Override
	public Identifier getModelResource(MawHopperBlockEntity blockEntity) {
		VertexType vertexType = MawHopperBlock.getVertexType(blockEntity.getBlockState());
		if (vertexType == VertexType.SOURCE) return SOURCE_MODEL;
		return MawHopperBlock.getConnection(blockEntity.getBlockState()).isCorner() ? CORNER_MODEL : STRAIGHT_MODEL;
	}

	@Override
	public Identifier getTextureResource(MawHopperBlockEntity blockEntity) {
		return TEXTURE;
	}

	@Override
	public Identifier getAnimationResource(MawHopperBlockEntity blockEntity) {
		VertexType vertexType = MawHopperBlock.getVertexType(blockEntity.getBlockState());
		if (vertexType == VertexType.SOURCE) return INPUT_ANIMATION;
		return CONNECTED_ANIMATION;
	}

}
