package com.github.elenterius.biomancy.client.render.entity.projectile;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.github.elenterius.biomancy.BiomancyMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class OrientedGeoProjectileRenderer<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T, EntityRenderState> {

	private static final DataTicket<Float> Y_ROT = DataTicket.create(BiomancyMod.MOD_ID + ":projectile_y_rot", Float.class);
	private static final DataTicket<Float> X_ROT = DataTicket.create(BiomancyMod.MOD_ID + ":projectile_x_rot", Float.class);

	public OrientedGeoProjectileRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
		super(context, model);
	}

	@Override
	public void addRenderData(T projectile, @Nullable Void relatedObject, EntityRenderState renderState, float partialTick) {
		renderState.addGeckolibData(Y_ROT, projectile.getYRot(partialTick));
		renderState.addGeckolibData(X_ROT, projectile.getXRot(partialTick));
	}

	@Override
	protected void applyRotations(RenderPassInfo<EntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
		poseStack.mulPose(Axis.YP.rotationDegrees(renderPassInfo.getOrDefaultGeckolibData(Y_ROT, 0f)));
		poseStack.mulPose(Axis.XP.rotationDegrees(-renderPassInfo.getOrDefaultGeckolibData(X_ROT, 0f)));
		poseStack.mulPose(Axis.YP.rotationDegrees(180f));
	}

}
