package com.github.elenterius.geckolibextras;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.keyframe.event.KeyFrameEvent;
import software.bernie.geckolib.animation.keyframe.event.data.KeyFrameData;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public final class GLibExtras {

	public static final String ID = "geckolib_extras";

	public static final DataTicket<@Nullable Object> ITEM_HOST_TICKET = new DataTicket<>(ID + ":item_host_object", Object.class);

	private GLibExtras() {}

	public static void setItemHostObject(ItemStack stack, Object host) {
		if (GeoRenderProvider.of(stack).getGeoItemRenderer() instanceof GeoItemRendererExtension extension) {
			extension.GLibExtras$setItemHostObject(host);
		}
	}

	public interface GeoItemRendererExtension {

		void GLibExtras$setItemHostObject(@Nullable Object host);

		@Nullable Object GLibExtras$getItemHostObject();

	}

	public static <T extends Item & GeoItem, E extends KeyFrameData> @Nullable ItemStack getCurrentItemStack(KeyFrameEvent<T, E> event) {
		if (GeoRenderProvider.of(event.getAnimatable()).getGeoItemRenderer() instanceof GeoItemRenderer<?> renderer) {
			return renderer.getCurrentItemStack();
		}
		return null;
	}

	public static <T extends Item & GeoItem, E extends KeyFrameData> @Nullable Object getCurrentItemHost(KeyFrameEvent<T, E> event) {
		if (GeoRenderProvider.of(event.getAnimatable()).getGeoItemRenderer() instanceof GeoItemRendererExtension extension) {
			return extension.GLibExtras$getItemHostObject();
		}
		return null;
	}

}
