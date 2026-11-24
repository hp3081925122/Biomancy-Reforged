package com.github.elenterius.biomancy.mixin.client;

import com.github.elenterius.biomancy.client.render.GasCloudDebugRenderer;
import com.github.elenterius.biomancy.entity.misc.GasCloud;
import com.github.elenterius.biomancy.util.debug.DebugInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

	@Shadow
	public Entity crosshairPickEntity;

	@Shadow
	public Camera camera;

	// 必须在原版 renderHitbox 之后注入，避免调试标签绘制切换 RenderType 提前 flush 原版已获取的 lines BufferBuilder
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;FFFF)V", shift = At.Shift.AFTER))
	private void render(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBuffer, int packedLight, CallbackInfo ci) {
		if (entity instanceof GasCloud gasCloud) {
			GasCloudDebugRenderer.renderDebugInfo(gasCloud, (EntityRenderDispatcher) (Object) this, poseStack, multiBuffer, packedLight, partialTicks);
		}
		else if (entity instanceof DebugInfo.EntityContext context) {
			context.renderDebugInfo((EntityRenderDispatcher) (Object) this, poseStack, multiBuffer, packedLight, partialTicks);
		}

		if (entity instanceof LivingEntity living) {
			ItemStack itemStack = living.getMainHandItem();
			if (itemStack.getItem() instanceof DebugInfo.ItemContext context) {
				context.renderDebugInfo(living, itemStack, (EntityRenderDispatcher) (Object) this, poseStack, multiBuffer, packedLight, partialTicks);
			}
		}
	}

}