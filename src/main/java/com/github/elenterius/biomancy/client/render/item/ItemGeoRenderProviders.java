package com.github.elenterius.biomancy.client.render.item;

import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.GeoItemRenderer;
import com.github.elenterius.biomancy.client.render.item.armor.AcolyteArmorRenderer;
import com.github.elenterius.biomancy.client.render.item.armor.WarriorArmorRenderer;
import com.github.elenterius.biomancy.client.render.item.caustic_gunblade.CausticGunbladeRenderer;
import com.github.elenterius.biomancy.client.render.item.extractor.ExtractorRenderer;
import com.github.elenterius.biomancy.client.render.item.guidebook.GuideBookRenderer;
import com.github.elenterius.biomancy.client.render.item.impaler.ImpalerRenderer;
import com.github.elenterius.biomancy.client.render.item.injector.InjectorRenderer;
import com.github.elenterius.biomancy.client.render.item.ravenousclaws.RavenousClawsRenderer;
import com.github.elenterius.biomancy.client.render.item.shield.ThornShieldRenderer;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import com.google.common.base.Suppliers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public final class ItemGeoRenderProviders {

	private ItemGeoRenderProviders() {}

	public static void register() {
		GeoRenderProviders.register(item(GuideBookRenderer::new), ModItems.DEV_GUIDE_BOOK.get());
		GeoRenderProviders.register(item(ExtractorRenderer::new), ModItems.ESSENCE_EXTRACTOR.get());
		GeoRenderProviders.register(item(InjectorRenderer::new), ModItems.INJECTOR.get());
		GeoRenderProviders.register(item(RavenousClawsRenderer::new), ModItems.RAVENOUS_CLAWS.get());
		GeoRenderProviders.register(item(CausticGunbladeRenderer::new), ModItems.CAUSTIC_GUNBLADE.get());
		GeoRenderProviders.register(item(ImpalerRenderer::new), ModItems.IMPALER.get());
		GeoRenderProviders.register(item(ThornShieldRenderer::new), ModItems.THORN_SHIELD.get());
		GeoRenderProviders.register(armor(AcolyteArmorRenderer::new),
				ModItems.ACOLYTE_ARMOR_HELMET.get(), ModItems.ACOLYTE_ARMOR_CHESTPLATE.get(), ModItems.ACOLYTE_ARMOR_LEGGINGS.get(), ModItems.ACOLYTE_ARMOR_BOOTS.get());
		GeoRenderProviders.register(armor(WarriorArmorRenderer::new),
				ModItems.WARRIOR_ARMOR_HELMET.get(), ModItems.WARRIOR_ARMOR_CHESTPLATE.get(), ModItems.WARRIOR_ARMOR_LEGGINGS.get(), ModItems.WARRIOR_ARMOR_BOOTS.get());
	}

	private static GeoRenderProvider item(Supplier<? extends GeoItemRenderer<?>> factory) {
		Supplier<? extends GeoItemRenderer<?>> renderer = Suppliers.memoize(factory::get);
		return new GeoRenderProvider() {
			@Override
			public @Nullable GeoItemRenderer<?> getGeoItemRenderer() {
				return renderer.get();
			}
		};
	}

	private static GeoRenderProvider armor(Supplier<? extends GeoArmorRenderer<?, ?>> factory) {
		Supplier<? extends GeoArmorRenderer<?, ?>> renderer = Suppliers.memoize(factory::get);
		return new GeoRenderProvider() {
			@Override
			public @Nullable GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
				return renderer.get();
			}
		};
	}

}
