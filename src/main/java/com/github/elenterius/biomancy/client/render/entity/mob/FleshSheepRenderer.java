package com.github.elenterius.biomancy.client.render.entity.mob;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.RenderPassInfo;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.FleshSheep;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

public class FleshSheepRenderer<T extends FleshSheep> extends FleshAnimalRenderer<T> {

	private static final DataTicket<Boolean> IS_GRAZING = DataTicket.create(BiomancyMod.MOD_ID + ":is_grazing", Boolean.class);

	public FleshSheepRenderer(EntityRendererProvider.Context context) {
		super(context, new FleshSheepModel<>(), 0.7f);
	}

	@Override
	public void addRenderData(T sheep, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
		renderState.addGeckolibData(IS_GRAZING, FleshSheep.Animations.getEatAnimationTick(sheep) > 0);
	}

	@Override
	protected boolean shouldTurnHead(RenderPassInfo<LivingEntityRenderState> renderPassInfo) {
		return !renderPassInfo.getOrDefaultGeckolibData(IS_GRAZING, false);
	}

}
