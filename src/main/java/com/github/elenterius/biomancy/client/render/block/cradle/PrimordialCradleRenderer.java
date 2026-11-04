package com.github.elenterius.biomancy.client.render.block.cradle;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.cradle.PrimordialCradleBlockEntity;
import com.github.elenterius.biomancy.client.render.block.CustomGeoBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public class PrimordialCradleRenderer extends CustomGeoBlockRenderer<PrimordialCradleBlockEntity> {

	private static final DataTicket<Float> BIOMASS_PCT = DataTicket.create(BiomancyMod.MOD_ID + ":cradle_biomass_pct", Float.class);
	private static final DataTicket<Boolean> HAS_MODIFIERS = DataTicket.create(BiomancyMod.MOD_ID + ":cradle_has_modifiers", Boolean.class);
	private static final DataTicket<Float> LIFE_ENERGY_PCT = DataTicket.create(BiomancyMod.MOD_ID + ":cradle_life_energy_pct", Float.class);

	private static final String EYE_OVERLAY_BONE = "_eye_overlay";

	public PrimordialCradleRenderer(BlockEntityRendererProvider.Context context) {
		super(context, new PrimordialCradleModel(), RenderTypes::entityCutoutCull);
	}

	@Override
	public void addRenderData(PrimordialCradleBlockEntity cradle, @Nullable Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
		renderState.addGeckolibData(BIOMASS_PCT, cradle.getBiomassPct());
		renderState.addGeckolibData(HAS_MODIFIERS, cradle.hasModifiers());
		renderState.addGeckolibData(LIFE_ENERGY_PCT, Math.min(cradle.getLifeEnergyPct(), 1f));
	}

	@Override
	public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
		float biomassPct = renderPassInfo.getOrDefaultGeckolibData(BIOMASS_PCT, 0f);
		boolean hasModifiers = renderPassInfo.getOrDefaultGeckolibData(HAS_MODIFIERS, false);

		snapshots.ifPresent("_fill_level", bone -> {
			if (biomassPct > 0) {
				bone.setTranslateY(Mth.floor(biomassPct * 8f) + 2f);
			}
			else {
				bone.skipRender(true).skipChildrenRender(true);
			}
		});

		if (biomassPct <= 0 || !hasModifiers) {
			snapshots.ifPresent("_toppings", bone -> bone.skipRender(true).skipChildrenRender(true));
		}

		snapshots.ifPresent(EYE_OVERLAY_BONE, bone -> bone.skipRender(true));
	}

	@Override
	public void preRenderPass(RenderPassInfo<BlockEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
		super.preRenderPass(renderPassInfo, renderTasks);

		float lifeEnergyPct = renderPassInfo.getOrDefaultGeckolibData(LIFE_ENERGY_PCT, 0f);
		if (lifeEnergyPct <= 0) return;

		renderPassInfo.model().getBone(EYE_OVERLAY_BONE).ifPresent(bone -> renderPassInfo.addPerBoneRender(bone, (passInfo, eyeBone, collector) -> {
			if (!(eyeBone instanceof CuboidGeoBone cuboidBone)) return;

			Identifier texture = getGeoModel().getTextureResource(passInfo.renderState());
			int color = passInfo.renderColor();
			PoseStack poseStack = passInfo.poseStack();
			eyeBone.translateAwayFromPivotPoint(poseStack);

			collector.submitCustomGeometry(poseStack, RenderTypes.eyes(texture), (pose, consumer) -> {
				PoseStack cubePoseStack = new PoseStack();
				cubePoseStack.last().set(pose);
				for (GeoCube cube : cuboidBone.cubes) {
					cubePoseStack.pushPose();
					renderEyeCube(cubePoseStack, cube, consumer, lifeEnergyPct, color);
					cubePoseStack.popPose();
				}
			});
		}));
	}

	private static void renderEyeCube(PoseStack poseStack, GeoCube cube, VertexConsumer consumer, float lifeEnergyPct, int color) {
		cube.translateToPivotPoint(poseStack);
		cube.rotate(poseStack);
		cube.translateAwayFromPivotPoint(poseStack);

		Matrix3f normalPose = poseStack.last().normal();
		Matrix4f pose = new Matrix4f(poseStack.last().pose());

		for (GeoQuad quad : cube.quads()) {
			if (quad == null) continue;

			Vector3f normal = normalPose.transform(quad.normalVec());
			RenderUtil.fixInvertedFlatCube(cube, normal);

			GeoVertex[] vertices = quad.vertices();
			if (quad.direction() == Direction.NORTH) {
				float steps = (vertices[0].posY() - vertices[2].posY()) * 16f;
				float delta = (Mth.floor(lifeEnergyPct * (steps - 1f)) + 1f) / steps;
				float textureV = Mth.lerp(delta, vertices[2].texV(), vertices[0].texV());
				float positionY = Mth.lerp(delta, vertices[2].posY(), vertices[0].posY());

				for (GeoVertex vertex : vertices) {
					boolean isTopVertex = vertex == vertices[0] || vertex == vertices[1];
					addVertex(consumer, pose, normal, color, vertex.posX(), isTopVertex ? positionY : vertex.posY(), vertex.posZ(), vertex.texU(), isTopVertex ? textureV : vertex.texV());
				}
			}
			else {
				for (GeoVertex vertex : vertices) {
					addVertex(consumer, pose, normal, color, vertex.posX(), vertex.posY(), vertex.posZ(), vertex.texU(), vertex.texV());
				}
			}
		}
	}

	private static void addVertex(VertexConsumer consumer, Matrix4f pose, Vector3f normal, int color, float x, float y, float z, float u, float v) {
		Vector4f position = pose.transform(new Vector4f(x, y, z, 1f));
		consumer.addVertex(position.x(), position.y(), position.z(), color, u, v, OverlayTexture.NO_OVERLAY, 0xf000f0, normal.x(), normal.y(), normal.z());
	}

}
