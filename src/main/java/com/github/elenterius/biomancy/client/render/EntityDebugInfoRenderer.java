package com.github.elenterius.biomancy.client.render;

import com.github.elenterius.biomancy.util.debug.DebugInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EntityDebugInfoRenderer implements DebugRenderer.SimpleDebugRenderer {

	private final Minecraft minecraft;

	public EntityDebugInfoRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
		if (minecraft.level == null || !minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) return;

		for (Entity entity : minecraft.level.entitiesForRendering()) {
			if (entity.isInvisible() || !frustum.isVisible(entity.getBoundingBox())) continue;

			float entityPartialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(!minecraft.level.tickRateManager().isEntityFrozen(entity));

			if (entity instanceof DebugInfo.EntityContext context) {
				context.emitDebugGizmos(entity.distanceToSqr(camX, camY, camZ), entityPartialTicks);
			}

			if (entity instanceof LivingEntity living) {
				ItemStack itemStack = living.getMainHandItem();
				if (itemStack.getItem() instanceof DebugInfo.ItemContext context) {
					context.emitDebugGizmos(living, itemStack, entityPartialTicks);
				}
			}
		}
	}

}
