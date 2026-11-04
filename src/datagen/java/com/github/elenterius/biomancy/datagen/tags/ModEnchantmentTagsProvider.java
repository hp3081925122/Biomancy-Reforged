package com.github.elenterius.biomancy.datagen.tags;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModEnchantmentTagsProvider extends EnchantmentTagsProvider {

	private static final List<ResourceKey<Enchantment>> ENCHANTMENTS = List.of(
			ModEnchantments.DESPOIL,
			ModEnchantments.ANESTHETIC,
			ModEnchantments.SURGICAL_PRECISION,
			ModEnchantments.PARASITIC_METABOLISM,
			ModEnchantments.SELF_FEEDING
	);

	public ModEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, BiomancyMod.MOD_ID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		for (TagKey<Enchantment> tag : List.of(
				EnchantmentTags.NON_TREASURE,
				EnchantmentTags.IN_ENCHANTING_TABLE,
				EnchantmentTags.TRADEABLE,
				EnchantmentTags.ON_RANDOM_LOOT,
				EnchantmentTags.ON_TRADED_EQUIPMENT,
				EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT,
				EnchantmentTags.TOOLTIP_ORDER
		)) {
			tag(tag).addAll(ENCHANTMENTS);
		}

	}

}
