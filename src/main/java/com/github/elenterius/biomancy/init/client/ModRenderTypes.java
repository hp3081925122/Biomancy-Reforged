package com.github.elenterius.biomancy.init.client;

import com.github.elenterius.biomancy.BiomancyMod;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.function.Function;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID, value = Dist.CLIENT)
public final class ModRenderTypes {

	public static final RenderPipeline ENTITY_CUTOUT_PARTY_TIME_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
			.withLocation(BiomancyMod.rl("pipeline/entity_cutout_party_time"))
			.withVertexShader(BiomancyMod.rl("core/entity_cutout_party_time"))
			.withFragmentShader(BiomancyMod.rl("core/entity_cutout_party_time"))
			.withShaderDefine("ALPHA_CUTOUT", 0.1f)
			.withSampler("Sampler1")
			.build();

	public static final RenderPipeline PROJECTILE_TRAIL_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
			.withLocation(BiomancyMod.rl("pipeline/projectile_trail"))
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
			.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
			.build();

	private static final Function<Identifier, RenderType> ENTITY_CUTOUT_PARTY_TIME = Util.memoize(texture -> RenderType.create(
			"biomancy_entity_cutout_party_time",
			RenderSetup.builder(ENTITY_CUTOUT_PARTY_TIME_PIPELINE)
					.withTexture("Sampler0", texture)
					.useLightmap()
					.useOverlay()
					.affectsCrumbling()
					.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
					.createRenderSetup()
	));

	public static final RenderPipeline POSITION_TEX_COLOR_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
			.withLocation(BiomancyMod.rl("pipeline/position_tex_color"))
			.withVertexShader("core/position_tex_color")
			.withFragmentShader("core/position_tex_color")
			.withSampler("Sampler0")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.build();

	private static final Function<Identifier, RenderType> POSITION_TEX_COLOR = Util.memoize(texture -> RenderType.create(
			"biomancy_position_tex_color",
			RenderSetup.builder(POSITION_TEX_COLOR_PIPELINE).withTexture("Sampler0", texture).createRenderSetup()
	));

	private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(texture -> RenderType.create(
			"biomancy_entity_translucent_cull",
			RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
					.withTexture("Sampler0", texture)
					.useLightmap()
					.useOverlay()
					.affectsCrumbling()
					.sortOnUpload()
					.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
					.createRenderSetup()
	));

	public static final RenderPipeline BLOCK_PREVIEW_PIPELINE = RenderPipeline.builder(RenderPipelines.ITEM_SNIPPET)
			.withLocation(BiomancyMod.rl("pipeline/block_preview"))
			.withShaderDefine("ALPHA_CUTOUT", 0.1f)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
			.build();

	private static final RenderType BLOCK_PREVIEW = RenderType.create(
			"biomancy_block_preview",
			RenderSetup.builder(BLOCK_PREVIEW_PIPELINE)
					.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
					.useLightmap()
					.sortOnUpload()
					.createRenderSetup()
	);

	private static final RenderType PROJECTILE_TRAIL = RenderType.create(
			"biomancy_projectile_trail",
			RenderSetup.builder(PROJECTILE_TRAIL_PIPELINE).sortOnUpload().createRenderSetup()
	);

	private ModRenderTypes() {}

	@SubscribeEvent
	public static void registerPipelines(final RegisterRenderPipelinesEvent event) {
		event.registerPipeline(ENTITY_CUTOUT_PARTY_TIME_PIPELINE);
		event.registerPipeline(PROJECTILE_TRAIL_PIPELINE);
		event.registerPipeline(POSITION_TEX_COLOR_PIPELINE);
		event.registerPipeline(BLOCK_PREVIEW_PIPELINE);
	}

	public static RenderType blockPreview() {
		return BLOCK_PREVIEW;
	}

	public static RenderType entityTranslucentCull(Identifier texture) {
		return ENTITY_TRANSLUCENT_CULL.apply(texture);
	}

	public static RenderType positionTexColor(Identifier texture) {
		return POSITION_TEX_COLOR.apply(texture);
	}

	public static RenderType getCutoutPartyTime(Identifier textureLocation) {
		return ENTITY_CUTOUT_PARTY_TIME.apply(textureLocation);
	}

	public static RenderType trail() {
		return PROJECTILE_TRAIL;
	}

}
