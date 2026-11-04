package com.github.elenterius.biomancy.menu;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModBioForgeTabs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class BioForgeTab {
	public static final String JSON_KEY = "bio_forge_tab";

	private static final Identifier DEPRECATED_WEAPONS_TAB = BiomancyMod.rl("weapons");

	public static final Codec<BioForgeTab> CODEC = Identifier.CODEC.comapFlatMap(BioForgeTab::byId, tab -> ModBioForgeTabs.REGISTRY.getKey(tab));

	public static final StreamCodec<RegistryFriendlyByteBuf, BioForgeTab> STREAM_CODEC = StreamCodec.of(
			(buffer, tab) -> buffer.writeIdentifier(Objects.requireNonNull(ModBioForgeTabs.REGISTRY.getKey(tab))),
			buffer -> {
				BioForgeTab value = ModBioForgeTabs.REGISTRY.getValue(buffer.readIdentifier());
				return value != null ? value : ModBioForgeTabs.MISC.get();
			}
	);

	private final int sortPriority;
	private final Item iconItem;

	public BioForgeTab(int sortPriority, Item iconItem) {
		this.sortPriority = sortPriority;
		this.iconItem = iconItem;
	}

	public BioForgeTab(Item itemSupplier) {
		this(0, itemSupplier);
	}

	private static DataResult<BioForgeTab> byId(Identifier id) {
		BioForgeTab tab = ModBioForgeTabs.REGISTRY.getValue(id);
		if (tab != null) return DataResult.success(tab);

		if (id.equals(DEPRECATED_WEAPONS_TAB)) {
			BiomancyMod.LOGGER.warn("A recipe uses the deprecated \"biomancy:weapons\" bio-forge tab instead of \"biomancy:tools\". Using \"biomancy:tools\" fallback, please update your recipe.");
			return DataResult.success(ModBioForgeTabs.TOOLS.get());
		}

		return DataResult.error(() -> "Unknown Bio-Forge tab '%s'".formatted(id));
	}

	public ItemStack getIcon() {
		return new ItemStack(iconItem);
	}

	public String enumId() {
		return enumIdFrom(ModBioForgeTabs.REGISTRY.getKey(this));
	}

	public static String enumIdFrom(Identifier key) {
		return JSON_KEY + "_" + key.getNamespace() + "_" + key.getPath();
	}

	public String translationKey() {
		return ModBioForgeTabs.REGISTRY.getKey(this).toLanguageKey(JSON_KEY);
	}

	public int sortPriority() {
		return sortPriority;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (BioForgeTab) obj;
		return ModBioForgeTabs.REGISTRY.getKey(this).equals(ModBioForgeTabs.REGISTRY.getKey(that)) && this.sortPriority == that.sortPriority && Objects.equals(this.iconItem, that.iconItem);
	}

	@Override
	public String toString() {
		return "BioForgeTab[" + "id=" + ModBioForgeTabs.REGISTRY.getKey(this) + ", " + "sortPriority=" + sortPriority + ", " + "iconSupplier=" + iconItem + ']';
	}

}
