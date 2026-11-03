package com.github.elenterius.biomancy.integration.tetra;

import com.github.elenterius.biomancy.api.tribute.SimpleTribute;
import com.github.elenterius.biomancy.api.tribute.Tributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public final class TetraCompat {

	private static final ResourceLocation DRAGON_SINEW_ID = ResourceLocation.fromNamespaceAndPath("tetra", "dragon_sinew");

	private TetraCompat() {}

	public static void init(Consumer<TetraHelper> helperSetter) {
		helperSetter.accept(new TetraHelperImpl(findClass("se.mickelus.tetra.items.modular.IModularItem"), findClass("se.mickelus.tetra.properties.IToolProvider")));
	}

	public static void onPostSetup() {
		registerDragonSinewAsTribute();
	}

	private static void registerDragonSinewAsTribute() {
		BuiltInRegistries.ITEM.getOptional(DRAGON_SINEW_ID)
				.ifPresent(item -> Tributes.register(item, SimpleTribute.builder().biomass(20).lifeEnergy(25).successModifier(20).hostileModifier(20).anomalyModifier(30).build()));
	}

	private static @Nullable Class<?> findClass(String name) {
		try {
			return Class.forName(name, false, TetraCompat.class.getClassLoader());
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}

	static final class TetraHelperImpl implements TetraHelper {

		private final @Nullable Class<?> modularItemClass;
		private final @Nullable Class<?> toolProviderClass;

		TetraHelperImpl(@Nullable Class<?> modularItemClass, @Nullable Class<?> toolProviderClass) {
			this.modularItemClass = modularItemClass;
			this.toolProviderClass = toolProviderClass;
		}

		@Override
		public boolean isToolOrModularItem(Item item) {
			return (modularItemClass != null && modularItemClass.isInstance(item)) || (toolProviderClass != null && toolProviderClass.isInstance(item));
		}

	}

}
