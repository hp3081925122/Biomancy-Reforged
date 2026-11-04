package com.github.elenterius.biomancy.client.render.entity.projectile;

import com.github.elenterius.biomancy.entity.projectile.AcidSprayProjectile;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.init.client.ModRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class AcidSprayRenderer extends EntityRenderer<AcidSprayProjectile, AcidSprayRenderer.AcidSprayRenderState> {

	private static final Vector3f[] QUAD = new Vector3f[]{
			new Vector3f(-1f, -1f, 0f),
			new Vector3f(-1f, 1f, 0f),
			new Vector3f(1f, 1f, 0f),
			new Vector3f(1f, -1f, 0f)
	};

	private static final float ALMOST_ZERO = 1e-6f;

	private static final float Y_OFFSET = 0.125f;
	private static final float TRAIL_WIDTH = 0.25f;
	private static final float MAX_SEGMENT_LENGTH_SQUARED = 5f;

	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/particle/drip_hang.png");

	public AcidSprayRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public AcidSprayRenderState createRenderState() {
		return new AcidSprayRenderState();
	}

	@Override
	public void extractRenderState(AcidSprayProjectile entity, AcidSprayRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.color = ModMobEffects.CORROSIVE.get().getColor();
		state.trailPoints = null;

		Vec3 motion = entity.getDeltaMovement();
		if (motion.lengthSqr() < 1e-6d) return;

		Vec3[] trail = entity.getTrailPositions();
		if (trail.length == 0) return;

		Vec3 currentPos = entity.getPosition(partialTick);
		Vec3 direction = motion.normalize();
		double offsetDistance = TRAIL_WIDTH * 1.75f;

		Vec3[] points = new Vec3[trail.length + 1];
		points[0] = currentPos.subtract(direction.scale(offsetDistance));
		System.arraycopy(trail, 0, points, 1, trail.length);

		state.trailPoints = points;
		state.trailWidth = TRAIL_WIDTH + (float) Math.sin(entity.getId() * 13.37d) * 0.005f;
	}

	@Override
	public void submit(AcidSprayRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		float r = (state.color >> 16 & 255) / 255f;
		float g = (state.color >> 8 & 255) / 255f;
		float b = (state.color & 255) / 255f;

		poseStack.pushPose();
		poseStack.mulPose(camera.orientation);
		poseStack.scale(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -(0.5f + Y_OFFSET), 0f);
		submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.positionTexColor(TEXTURE), (pose, consumer) -> {
			consumer.addVertex(pose, QUAD[0].x(), QUAD[0].y(), QUAD[0].z()).setUv(1f, 1f).setColor(r, g, b, 1f);
			consumer.addVertex(pose, QUAD[1].x(), QUAD[1].y(), QUAD[1].z()).setUv(1f, 0f).setColor(r, g, b, 1f);
			consumer.addVertex(pose, QUAD[2].x(), QUAD[2].y(), QUAD[2].z()).setUv(0f, 0f).setColor(r, g, b, 1f);
			consumer.addVertex(pose, QUAD[3].x(), QUAD[3].y(), QUAD[3].z()).setUv(0f, 1f).setColor(r, g, b, 1f);
		});
		poseStack.popPose();

		Vec3[] points = state.trailPoints;
		if (points != null) {
			Vector3f up = new Vector3f(0f, 1f, 0f).rotate(camera.orientation);
			Vector3f left = new Vector3f(-1f, 0f, 0f).rotate(camera.orientation);

			poseStack.pushPose();
			poseStack.translate(-state.x, -state.y + Y_OFFSET, -state.z);
			submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.trail(), (pose, consumer) -> {
				renderSegments(points, state.trailWidth, pose, consumer, up, left, r, g, b, false);
				renderSegments(points, state.trailWidth, pose, consumer, up, left, r, g, b, true);
			});
			poseStack.popPose();
		}

		super.submit(state, poseStack, submitNodeCollector, camera);
	}

	private static void renderSegments(Vec3[] points, float baseWidth, PoseStack.Pose pose, VertexConsumer consumer, Vector3f up, Vector3f left, float r, float g, float b, boolean useAxis2) {
		Vector3f axis1 = new Vector3f();
		Vector3f axis2 = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();

		int segments = points.length - 1;

		for (int i = 0; i < segments; i++) {
			Vec3 from = points[i];
			Vec3 to = points[i + 1];

			Vector3f dir = to.subtract(from).toVector3f();
			float lengthSqr = dir.lengthSquared();
			if (lengthSqr < ALMOST_ZERO || lengthSqr > MAX_SEGMENT_LENGTH_SQUARED) continue;

			dir.normalize();

			float t = i / (float) segments;
			float width = Mth.lerp(t, baseWidth, baseWidth * 0.25f);
			float alpha = Mth.lerp(t, 0.8f, 0.1f);

			dir.cross(up, axis1).normalize();
			if (axis1.lengthSquared() < ALMOST_ZERO) {
				dir.cross(left, axis1).normalize();
			}

			if (useAxis2) {
				dir.cross(axis1, axis2).normalize();
				axis2.mul(width);
				v1.set(from.x, from.y, from.z).add(axis2);
				v2.set(to.x, to.y, to.z).sub(axis2);
			}
			else {
				axis1.mul(width);
				v1.set(from.x, from.y, from.z).add(axis1);
				v2.set(to.x, to.y, to.z).sub(axis1);
			}

			consumer.addVertex(pose, v1.x, v1.y, v1.z).setColor(r, g, b, alpha);
			consumer.addVertex(pose, v2.x, v2.y, v2.z).setColor(r, g, b, alpha);
		}
	}

	public static class AcidSprayRenderState extends EntityRenderState {
		public int color;
		public Vec3 @Nullable [] trailPoints;
		public float trailWidth;
	}

}
