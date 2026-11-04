package com.github.elenterius.biomancy.client.render.entity.mob;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.FleshChicken;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class FleshChickenRenderer<T extends FleshChicken> extends FleshAnimalRenderer<T> {

	private static final DataTicket<Float> WING_ROTATION = DataTicket.create(BiomancyMod.MOD_ID + ":wing_rotation", Float.class);

	public FleshChickenRenderer(EntityRendererProvider.Context context) {
		super(context, new FleshChickenModel<>(), 0.3f);
	}

	@Override
	public void addRenderData(T chicken, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
		float flapProgress = Mth.lerp(partialTick, chicken.oFlap, chicken.flap);
		float flapSpeed = Mth.lerp(partialTick, chicken.oFlapSpeed, chicken.flapSpeed);
		renderState.addGeckolibData(WING_ROTATION, (Mth.sin(flapProgress) + 1f) * flapSpeed);
	}

	@Override
	public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
		super.adjustModelBonesForRender(renderPassInfo, snapshots);

		float wingRotation = renderPassInfo.getOrDefaultGeckolibData(WING_ROTATION, 0f);
		snapshots.ifPresent("right_wing", bone -> setRotZ(bone, wingRotation));
		snapshots.ifPresent("left_wing", bone -> setRotZ(bone, -wingRotation));
	}

}
