package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MigrationHandler {

	private MigrationHandler() {}

	public static void registerAliases() {
		addAlias(ModSerums.SERUMS, "growth_serum", ModSerums.AGEING_SERUM);

		addAlias(ModBlocks.BLOCKS, "bio_lantern", ModBlocks.YELLOW_BIO_LANTERN);
		addAlias(ModBlocks.BLOCKS, "bone_spike", ModBlocks.FLESH_SPIKE);
		addAlias(ModBlocks.BLOCKS, "creator", ModBlocks.PRIMORDIAL_CRADLE);
		addAlias(ModBlocks.BLOCKS, "flesh_block", ModBlocks.FLESH);
		addAlias(ModBlocks.BLOCKS, "flesh_block_slab", ModBlocks.FLESH_SLAB);
		addAlias(ModBlocks.BLOCKS, "flesh_block_stairs", ModBlocks.FLESH_STAIRS);
		addAlias(ModBlocks.BLOCKS, "flesh_irisdoor", ModBlocks.FLESH_IRIS_DOOR);
		addAlias(ModBlocks.BLOCKS, "necrotic_flesh_block", ModBlocks.MALIGNANT_FLESH);
		addAlias(ModBlocks.BLOCKS, "flesh_tentacle", ModBlocks.MALIGNANT_FLESH_VEINS);
		addAlias(ModBlocks.BLOCKS, "corrupted_primal_flesh", ModBlocks.PRIMAL_FLESH);

		addAlias(ModEntityTypes.ENTITIES, "malignant_flesh_blob", ModEntityTypes.PRIMORDIAL_HUNGRY_FLESH_BLOB);

		addAlias(ModBlockEntities.BLOCK_ENTITIES, "creator", ModBlockEntities.PRIMORDIAL_CRADLE);

		addAlias(ModItems.ITEMS, "long_claws", ModItems.RAVENOUS_CLAWS);
		addAlias(ModItems.ITEMS, "mascot_pattern", ModItems.MASCOT_BANNER_PATTERNS);
		addAlias(ModItems.ITEMS, "mascot_outline_pattern", ModItems.MASCOT_BANNER_PATTERNS);
		addAlias(ModItems.ITEMS, "mascot_accent_pattern", ModItems.MASCOT_BANNER_PATTERNS);
		addAlias(ModItems.ITEMS, "bio_lantern", ModItems.YELLOW_BIO_LANTERN);
		addAlias(ModItems.ITEMS, "glass_vial", ModItems.VIAL);
		addAlias(ModItems.ITEMS, "creator", ModItems.PRIMORDIAL_CRADLE);
		addAlias(ModItems.ITEMS, "flesh_block", ModItems.FLESH_BLOCK);
		addAlias(ModItems.ITEMS, "flesh_block_slab", ModItems.FLESH_SLAB);
		addAlias(ModItems.ITEMS, "flesh_block_stairs", ModItems.FLESH_STAIRS);
		addAlias(ModItems.ITEMS, "flesh_irisdoor", ModItems.FLESH_IRIS_DOOR);
		addAlias(ModItems.ITEMS, "necrotic_flesh_block", ModItems.MALIGNANT_FLESH_BLOCK);
		addAlias(ModItems.ITEMS, "flesh_tentacle", ModItems.MALIGNANT_FLESH_VEINS);
		addAlias(ModItems.ITEMS, "corrupted_primal_flesh", ModItems.PRIMAL_FLESH_BLOCK);
		addAlias(ModItems.ITEMS, "biometal", ModItems.LIVING_FLESH);
		ModItems.ITEMS.addAlias(BiomancyMod.rl("bone_gear"), BuiltInRegistries.ITEM.getKey(Items.BONE));
		addAlias(ModItems.ITEMS, "lens", ModItems.GEM_FRAGMENTS);
		addAlias(ModItems.ITEMS, "skin_chunk", ModItems.FLESH_BITS);
		addAlias(ModItems.ITEMS, "flesh_lump", ModItems.FLESH_BITS);
		addAlias(ModItems.ITEMS, "mended_skin", ModItems.FLESH_BITS);
		addAlias(ModItems.ITEMS, "stomach", ModItems.GENERIC_MOB_GLAND);
		addAlias(ModItems.ITEMS, "artificial_stomach", ModItems.GENERIC_MOB_GLAND);
		addAlias(ModItems.ITEMS, "bolus", ModItems.NUTRIENTS);
		addAlias(ModItems.ITEMS, "keratin_filaments", ModItems.TOUGH_FIBERS);
		addAlias(ModItems.ITEMS, "digestate", ModItems.ORGANIC_MATTER);
		addAlias(ModItems.ITEMS, "oxide_powder", ModItems.MINERAL_FRAGMENT);
		addAlias(ModItems.ITEMS, "silicate_paste", ModItems.MINERAL_FRAGMENT);
		addAlias(ModItems.ITEMS, "bio_minerals", ModItems.MINERAL_FRAGMENT);
		addAlias(ModItems.ITEMS, "hormone_bile", ModItems.HORMONE_SECRETION);
		addAlias(ModItems.ITEMS, "corrosive_additive", ModItems.DECAYING_ADDITIVE);
	}

	private static <T> void addAlias(DeferredRegister<T> register, String from, DeferredHolder<T, ? extends T> to) {
		register.addAlias(BiomancyMod.rl(from), to.getId());
	}

}
