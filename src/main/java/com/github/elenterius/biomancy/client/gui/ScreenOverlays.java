package com.github.elenterius.biomancy.client.gui;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.serum.SerumContainer;
import com.github.elenterius.biomancy.block.WaterGelBlock;
import com.github.elenterius.biomancy.block.cradle.PrimordialCradleBlock;
import com.github.elenterius.biomancy.block.cradle.PrimordialCradleBlockEntity;
import com.github.elenterius.biomancy.block.membrane.Membrane;
import com.github.elenterius.biomancy.client.util.GuiRenderUtil;
import com.github.elenterius.biomancy.client.util.GuiUtil;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.item.ItemCharge;
import com.github.elenterius.biomancy.item.KnowledgeReader;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import com.github.elenterius.biomancy.item.weapon.gun.CausticGunbladeItem;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.shooting.Gun;
import com.github.elenterius.biomancy.util.shooting.GunState;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.client.gui.LayeredDraw;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class ScreenOverlays {
	public static final ResourceLocation INJECTOR_COOL_DOWN = BiomancyMod.rl("textures/gui/indicator_injector_cooldown.png");
	public static final ResourceLocation BLOOD_CHARGE_METER = BiomancyMod.rl("textures/gui/blood_charge_meter.png");
	public static final int BLOOD_CHARGE_METER_FRAMES = 5;

	public static final ResourceLocation VEINS = BiomancyMod.rl("textures/gui/overlay/veins.png");
	public static final ResourceLocation VIGNETTE = BiomancyMod.rl("textures/gui/overlay/vignette.png");
	public static final ResourceLocation MEMBRANE = BiomancyMod.rl("textures/gui/overlay/membrane.png");
	public static final ResourceLocation WATER_GEL = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");

	public static final LayeredDraw.Layer INSIDE_BLOCK_OVERLAY = layer((guiGraphics, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null) return;
		if (minecraft.player.isSpectator()) return;
		if (minecraft.options.hideGui) return;

		Camera camera = minecraft.gameRenderer.getMainCamera();
		Block block = camera.getBlockAtCamera().getBlock();

		if (block instanceof WaterGelBlock) {
			float brightness = LightTexture.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(camera.getBlockPosition()));
			renderInsideBlockOverlay(guiGraphics, -90, screenWidth, screenHeight, brightness, WATER_GEL);
		}
		else if (block instanceof Membrane) {
			float brightness = LightTexture.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(camera.getBlockPosition()));
			renderInsideBlockOverlay(guiGraphics, -90, screenWidth, screenHeight, brightness, MEMBRANE);
		}
	});

	public static final LayeredDraw.Layer FRENZY_OVERLAY = layer((guiGraphics, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null || minecraft.level == null) return;
		if (!minecraft.player.hasEffect(ModMobEffects.FRENZY)) return;
		if (minecraft.options.hideGui) return;

		guiGraphics.pose().pushPose();

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();

		float intensityPct = Mth.clamp(minecraft.player.getEffect(ModMobEffects.FRENZY).getAmplifier() / 2f, 0f, 1f);
		float colorOffset = Mth.lerp(intensityPct, 0.3f, 0f);

		float scale = Mth.lerp(intensityPct, 1.6f, 1f);
		guiGraphics.pose().translate(screenWidth / 2f, screenHeight / 2f, 0);
		guiGraphics.pose().scale(scale, scale, scale);
		guiGraphics.pose().translate(-screenWidth / 2f, -screenHeight / 2f, 0);

		guiGraphics.setColor(1f, 1f, 1f, 1f);
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(VEINS, 0, 0, -90, 0f, 0f, screenWidth, screenHeight, screenWidth, screenHeight);

		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		guiGraphics.setColor(colorOffset, 1f, 1f, 1f);

		guiGraphics.blit(VIGNETTE, 0, 0, -90, 0f, 0f, screenWidth, screenHeight, screenWidth, screenHeight);

		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();

		guiGraphics.pose().popPose();
	});

	public static final LayeredDraw.Layer GUN_OVERLAY = layer((poseStack, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.options.hideGui && minecraft.player != null) {
			ItemStack itemStack = minecraft.player.getMainHandItem();
			if (itemStack.isEmpty() || !(itemStack.getItem() instanceof Gun<?> gun)) return;

			setupOverlayRenderState(true, false);
			renderGunOverlay(minecraft.font, poseStack, screenWidth, screenHeight, -90, minecraft.player, itemStack, gun);
		}
	});

	public static final LayeredDraw.Layer INJECTOR_OVERLAY = layer((guiGraphics, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.options.hideGui && minecraft.player != null) {
			ItemStack itemStack = minecraft.player.getMainHandItem();
			if (itemStack.isEmpty() || !(itemStack.getItem() instanceof InjectorItem injector)) return;

			setupOverlayRenderState(true, false);
			renderInjectorOverlay(guiGraphics, minecraft.font, partialTicks, screenWidth, screenHeight, -90, minecraft.player, itemStack, injector);
		}
	});

	public static final LayeredDraw.Layer CHARGE_BAR_OVERLAY = layer((guiGraphics, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.options.hideGui && minecraft.player != null) {
			ItemStack stack = minecraft.player.getMainHandItem();
			if (stack.isEmpty()) return;

			if (stack.getItem() instanceof ItemCharge abilityCharge) {
				if (GuiUtil.isFirstPersonView()) {
					setupOverlayRenderState(true, false);
					renderBloodChargeMeter(guiGraphics, minecraft.font, screenWidth, screenHeight, -90, abilityCharge.getCharge(stack), abilityCharge.getChargePct(stack));
				}
			}

			if (stack.getItem() instanceof CausticGunbladeItem) {
				if (GuiUtil.isFirstPersonView()) {
					setupOverlayRenderState(true, false);
					CausticGunbladeItem.Abilities.ACID_REFLUX.render(minecraft.font, guiGraphics, partialTicks, screenWidth, screenHeight, stack);
				}
			}
		}
	});

	public static final LayeredDraw.Layer KNOWLEDGE_OVERLAY = layer((guiGraphics, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null || minecraft.level == null) return;
		if (minecraft.options.hideGui) return;
		if (!GuiUtil.isFirstPersonView()) return;

		BlockHitResult blockHitResult = getBlockHitResult(minecraft);
		if (blockHitResult == null) return;

		if (KnowledgeReader.canShowKnowledgeOverlay(minecraft.player, EquipmentSlot.HEAD)) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			BlockState blockState = minecraft.level.getBlockState(blockPos);
			if (blockState.getBlock() instanceof PrimordialCradleBlock && minecraft.level.getBlockEntity(blockPos) instanceof PrimordialCradleBlockEntity cradle) {
				setupOverlayRenderState(true, false);
				renderKnowledgeOverlay(minecraft.font, guiGraphics, screenWidth, screenHeight, cradle);
			}
		}
	});

	private ScreenOverlays() {}

	private static LayeredDraw.Layer layer(Overlay overlay) {
		return (guiGraphics, deltaTracker) -> overlay.render(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true), guiGraphics.guiWidth(), guiGraphics.guiHeight());
	}

	static void setupOverlayRenderState(boolean blend, boolean depthTest) {
		if (blend) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
		}
		else {
			RenderSystem.disableBlend();
		}

		if (depthTest) {
			RenderSystem.enableDepthTest();
		}
		else {
			RenderSystem.disableDepthTest();
		}

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
	}

	@FunctionalInterface
	private interface Overlay {
		void render(GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight);
	}

	static void renderInsideBlockOverlay(GuiGraphics guiGraphics, int blitOffset, int screenWidth, int screenHeight, float brightness, ResourceLocation texture) {
		int x1 = 0, y1 = 0;
		float minU = 0f, maxU = 1f;
		float minV = 0f, maxV = 1f;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.setColor(brightness, brightness, brightness, 0.5f);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);

		Matrix4f matrix4f = guiGraphics.pose().last().pose();
		BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferbuilder.addVertex(matrix4f, x1, y1, blitOffset).setUv(minU, minV);
		bufferbuilder.addVertex(matrix4f, x1, screenHeight, blitOffset).setUv(minU, maxV);
		bufferbuilder.addVertex(matrix4f, screenWidth, screenHeight, blitOffset).setUv(maxU, maxV);
		bufferbuilder.addVertex(matrix4f, screenWidth, y1, blitOffset).setUv(maxU, minV);
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
	}

	static void renderKnowledgeOverlay(Font font, GuiGraphics guiGraphics, int screenWidth, int screenHeight, PrimordialCradleBlockEntity cradle) {

		int x = screenWidth / 2 + 64;
		int y = screenHeight / 2 - font.lineHeight - 2;

		y += font.lineHeight;
		drawValueWithLabel(guiGraphics, font, cradle.getBiomassPct(), "Biomass", x, y += font.lineHeight + 2);
		drawValueWithLabel(guiGraphics, font, cradle.getLifeEnergyPct(), "Life", x, y += font.lineHeight + 2);
		drawValueWithLabel(guiGraphics, font, cradle.getDiseaseChance(), "Disease", x, y += font.lineHeight + 2);
		drawValueWithLabel(guiGraphics, font, cradle.getHostileChance(), "Hostile", x, y += font.lineHeight + 2);
		drawValueWithLabel(guiGraphics, font, cradle.getAnomalyChance(), "Anomaly", x, y + (font.lineHeight + 2));
	}

	static void drawValueWithLabel(GuiGraphics guiGraphics, Font font, String valueString, String label, int x, int y, Style numberStyle, Style labelStyle) {
		MutableComponent valueText = ComponentUtil.literal(valueString).withStyle(numberStyle);
		MutableComponent labelText = ComponentUtil.literal(label).withStyle(labelStyle);
		guiGraphics.drawString(font, valueText, x - font.width(valueText) - 4, y, 0xff_ffffff);
		guiGraphics.drawString(font, labelText, x, y, 0xff_ffffff);
	}

	private static void drawValueWithLabel(GuiGraphics guiGraphics, Font font, float value, String label, int x, int y) {
		drawValueWithLabel(guiGraphics, font, Math.round(value * 100f) + "%", label, x, y, TextStyles.LIGHT_GRAY, TextStyles.GRAY);
	}

	static void renderGunOverlay(Font font, GuiGraphics guiGraphics, int screenWidth, int screenHeight, int zDepth, LocalPlayer player, ItemStack stack, Gun<?> gun) {
		renderAmmoOverlay(guiGraphics, font, screenWidth, screenHeight, zDepth, stack, gun);

		if (GuiUtil.isFirstPersonView()) {
			renderReloadIndicator(guiGraphics, screenWidth, screenHeight, zDepth, player, stack, gun);
		}
	}

	static void renderInjectorOverlay(GuiGraphics guiGraphics, Font font, float partialTicks, int screenWidth, int screenHeight, int zDepth, LocalPlayer player, ItemStack stack, InjectorItem injector) {
		if (GuiUtil.isFirstPersonView()) {
			float progress = 1f - player.getCooldowns().getCooldownPercent(injector, partialTicks);
			if (progress < 1f) {
				int x = screenWidth / 2 - 8;
				int y = screenHeight / 2 - 7 - 8;
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				guiGraphics.blit(INJECTOR_COOL_DOWN, x, y, zDepth, 0, 0, 16, 7, 16, 16);
				guiGraphics.blit(INJECTOR_COOL_DOWN, x, y, zDepth, 0, 7, (int) (progress * 16f), 7, 16, 16);
				RenderSystem.defaultBlendFunc();
			}

			ItemStack storedStack = injector.getStoredItemStack(stack);
			if (storedStack.getItem() instanceof SerumContainer container && !container.getSerum(storedStack).isEmpty()) {
				short maxAmmo = InjectorItem.MAX_SLOT_SIZE;
				renderAmmoCount(guiGraphics, font, screenWidth, screenHeight, zDepth, maxAmmo, storedStack.getCount(), 0xFFFEFEFE, 0xFF9E9E9E);
			}
		}
	}

	public static void renderBloodChargeMeter(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight, int zDepth, int charge, float chargePct) {
		int x = (screenWidth - 31) / 2 + screenWidth % 2;
		int y = (screenHeight - 31) / 2;

		guiGraphics.blit(BLOOD_CHARGE_METER, x, y, zDepth, 0, (int) (chargePct * (BLOOD_CHARGE_METER_FRAMES - 1)) * 31, 31, 31, 31, 31 * 5);
	}

	static void renderReloadIndicator(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int zDepth, LocalPlayer player, ItemStack stack, Gun<?> gun) {
		int x = screenWidth / 2 + screenWidth % 2;
		int y = screenHeight / 2;

		GunState gunState = gun.getGunState(stack);
		if (gunState == GunState.RELOADING) {
			long elapsedTime = player.clientLevel.getGameTime() - gun.getReloadStartTime(stack);
			float reloadProgress = gun.getReloadProgress(elapsedTime, gun.getReloadDurationTicks(stack));
			GuiRenderUtil.drawSquareProgressBar(guiGraphics, x, y, zDepth, 10, reloadProgress);
		}
		else {
			long elapsedTimeFromShot = player.clientLevel.getGameTime() - gun.getShootTimestamp(stack);

			switch (gun.getShootBehavior()) {
				case ON_FULL_CHARGE -> {
					int delayBetweenShots = Math.max(gun.getDelayBetweenShots(stack), 1);
					int ticksUsingItem = player.getTicksUsingItem();
					if (ticksUsingItem >= 0) {
						float pct = ticksUsingItem / (float) delayBetweenShots;
						float chargeProgress = Mth.clamp(pct - Mth.floor(pct), 0f, 1f);
						GuiRenderUtil.drawSquareProgressBar(guiGraphics, x, y, zDepth, 10, chargeProgress);
					}
				}
				case ON_RELEASE_INSTANT, ON_RELEASE_WITH_FULL_CHARGE -> {
					if (player.isUsingItem()) {
						int delayBetweenShots = Math.max(gun.getDelayBetweenShots(stack), 1);
						float elapsedDuration = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks());
						float chargePercentage = Mth.clamp(elapsedDuration / delayBetweenShots, 0f, 1f);
						GuiRenderUtil.drawSquareProgressBar(guiGraphics, x, y, zDepth, 10, chargePercentage);
					}
				}
				default -> {}
			}

			renderAttackIndicator(guiGraphics, screenWidth, screenHeight, zDepth, player, elapsedTimeFromShot, gun.getDelayBetweenShots(stack));
		}
	}

	static void renderAmmoOverlay(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight, int zDepth, ItemStack stack, Gun<?> gun) {
		int maxAmmo = gun.getMaxAmmo(stack);
		int ammo = gun.getAmmo(stack);
		renderAmmoCount(guiGraphics, font, screenWidth, screenHeight, zDepth, maxAmmo, ammo, 0xFFFEFEFE, 0xFF9E9E9E);
	}

	static void renderAmmoCount(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight, int zDepth, int maxAmmoIn, int ammoIn, int primaryColor, int secondaryColor) {
		String maxAmmo = "/" + maxAmmoIn;
		String ammo = String.valueOf(ammoIn);
		int x = screenWidth - font.width(maxAmmo) - 4;
		int y = screenHeight - font.lineHeight - 4;
		guiGraphics.drawString(font, maxAmmo, x, y, secondaryColor);
		guiGraphics.pose().pushPose();
		float scale = 1.5f;
		guiGraphics.pose().translate(x - font.width(ammo) * scale, y - font.lineHeight * scale * 0.5f, zDepth);
		guiGraphics.pose().scale(scale, scale, 0);
		guiGraphics.drawString(font, ammo, 0, 0, primaryColor);
		guiGraphics.pose().popPose();
	}

	public static void renderAttackIndicator(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int zDepth, LocalPlayer player, long elapsedTime, int shootDelay) {
		if (shootDelay >= SharedConstants.TICKS_PER_SECOND / 2 && elapsedTime < shootDelay && GuiUtil.canDrawAttackIndicator(player)) {
			float progress = (float) elapsedTime / shootDelay;
			if (progress < 1f) {
				int x = screenWidth / 2 - 8;
				int y = screenHeight / 2 - 7 + 16;
				GuiRenderUtil.drawAttackIndicator(guiGraphics, x, y, progress);
			}
		}
	}

	static @Nullable BlockHitResult getBlockHitResult(Minecraft minecraft) {
		if (minecraft.hitResult == null) return null;
		if (minecraft.hitResult.getType() != HitResult.Type.BLOCK) return null;
		if (minecraft.hitResult instanceof BlockHitResult blockHitResult) return blockHitResult;
		return null;
	}

}
