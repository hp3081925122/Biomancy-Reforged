package com.github.elenterius.biomancy.datagen.models;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.item.weapon.DespoilingSwordItem;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.ItemLayerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Objects;

public class ModItemModelProvider extends ItemModelProvider {

	protected static final String LAYER_0_TEXTURE = "layer0";
	protected static final String LAYER_1_TEXTURE = "layer1";

	public ModItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
		super(packOutput, BiomancyMod.MOD_ID, existingFileHelper);
	}

	private static Identifier registryKey(Item item) {
		return Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
	}

	@Override
	protected void registerModels() {
		basicItem(ModItems.TAB_ICON);

		lootItem(ModItems.MOB_FANG);
		lootItem(ModItems.MOB_CLAW);
		lootItem(ModItems.MOB_SINEW);
		lootItem(ModItems.MOB_MARROW);
		lootItem(ModItems.WITHERED_MOB_MARROW);
		lootItem(ModItems.GENERIC_MOB_GLAND);
		lootItem(ModItems.TOXIN_GLAND);
		lootItem(ModItems.VOLATILE_GLAND);
		lootItem(ModItems.LIVING_FLESH);
		lootItem(ModItems.ACIDIC_EGG);

		componentItem(ModItems.FLESH_BITS);
		componentItem(ModItems.BONE_FRAGMENTS);
		componentItem(ModItems.ELASTIC_FIBERS);
		componentItem(ModItems.MINERAL_FRAGMENT);
		componentItem(ModItems.TOUGH_FIBERS);
		componentItem(ModItems.ORGANIC_MATTER);
		componentItem(ModItems.EXOTIC_DUST);
		componentItem(ModItems.BIO_LUMENS);
		componentItem(ModItems.NUTRIENTS);
		componentItem(ModItems.GEM_FRAGMENTS);
		componentItem(ModItems.STONE_POWDER);

		componentItem(ModItems.REGENERATIVE_FLUID);
		componentItem(ModItems.WITHERING_OOZE);
		componentItem(ModItems.HORMONE_SECRETION);
		componentItem(ModItems.TOXIN_EXTRACT);
		componentItem(ModItems.ACID_EXTRACT);
		componentItem(ModItems.VOLATILE_FLUID);
		componentItem(ModItems.BILE);

		serumItem(ModItems.REJUVENATION_SERUM);
		serumItem(ModItems.AGEING_SERUM);
		serumItem(ModItems.ENLARGEMENT_SERUM);
		serumItem(ModItems.SHRINKING_SERUM);
		serumItem(ModItems.BREEDING_STIMULANT);
		serumItem(ModItems.ABSORPTION_BOOST);
		serumItem(ModItems.CLEANSING_SERUM);
		serumItem(ModItems.INSOMNIA_CURE);
		serumItem(ModItems.FRENZY_SERUM);
		serumOverlayItem(ModItems.POTION_SERUM);

		serumItem(ModItems.ORGANIC_COMPOUND);
		serumItem(ModItems.UNSTABLE_COMPOUND);
		serumItem(ModItems.GENETIC_COMPOUND);
		serumItem(ModItems.EXOTIC_COMPOUND);
		serumItem(ModItems.HEALING_ADDITIVE);
		serumItem(ModItems.DECAYING_ADDITIVE);

		emissiveItem(ModItems.PRIMORDIAL_CORE);
		fleshPlunderer(ModItems.DESPOIL_SICKLE);
		basicItem(ModItems.CREATOR_MIX);
		basicItem(ModItems.NUTRIENT_PASTE);
		basicItem(ModItems.NUTRIENT_BAR);
		basicItem(ModItems.BLOOMBERRY);
		serumItem(ModItems.VIAL);
		basicItem(ModItems.FERTILIZER);
		overlayItem(ModItems.ESSENCE);

		armorItem(ModItems.ACOLYTE_ARMOR_HELMET);
		armorItem(ModItems.ACOLYTE_ARMOR_CHESTPLATE);
		armorItem(ModItems.ACOLYTE_ARMOR_LEGGINGS);
		armorItem(ModItems.ACOLYTE_ARMOR_BOOTS);
		armorItem(ModItems.WARRIOR_ARMOR_HELMET);
		armorItem(ModItems.WARRIOR_ARMOR_CHESTPLATE);
		armorItem(ModItems.WARRIOR_ARMOR_LEGGINGS);
		armorItem(ModItems.WARRIOR_ARMOR_BOOTS);

		basicItem(ModItems.FLESH_DOOR);
		basicItem(ModItems.FULL_FLESH_DOOR);
		flatBlockItem(ModItems.FLESH_LADDER);
		flatBlockItem(ModItems.MALIGNANT_FLESH_VEINS);

		dynamicBucket(ModItems.ACID_BUCKET.get());
		flatBlockItem(ModItems.ACID_SPLATTER, "_0");
		flatBlockItem(ModItems.VOLATILE_SPLATTER, "_0");

		basicItem(ModItems.GELLING_AGENT);

		weaponItem(ModItems.GRENADE_CASING);
		weaponItem(ModItems.TOXIN_GRENADE);
		weaponItem(ModItems.ACID_GRENADE);
		weaponItem(ModItems.DECAY_GRENADE);
		weaponItem(ModItems.VOLATILE_GRENADE);

		ModItems.ITEMS.getEntries().stream().map(DeferredHolder::get).filter(SpawnEggItem.class::isInstance).forEach(this::spawnEggItem);
	}

	public <T extends Item> void emissiveItem(DeferredItem<T> registryObject) {
		emissiveItem(registryObject.getId());
	}

	public void emissiveItem(Identifier registryKey) {
		getBuilder(registryKey.toString())
				.parent(new ModelFile.UncheckedModelFile("item/generated"))
				.customLoader(ItemLayerModelBuilder::begin).emissive(0xf, 0xf, 1).end()
				.texture(LAYER_0_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), ITEM_FOLDER + "/" + registryKey.getPath()))
				.texture(LAYER_1_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), ITEM_FOLDER + "/" + registryKey.getPath() + "_emissive"));
	}

	public <T extends DespoilingSwordItem> void fleshPlunderer(DeferredItem<T> registryObject) {
		Identifier registryKey = registryObject.getId();
		getBuilder(registryKey.toString())
				.parent(new ModelFile.UncheckedModelFile("item/handheld"))
				.customLoader(ItemLayerModelBuilder::begin).emissive(0xf, 0xf, 1).end()
				.texture(LAYER_0_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), ITEM_FOLDER + "/weapon/" + registryKey.getPath()))
				.texture(LAYER_1_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), ITEM_FOLDER + "/weapon/" + registryKey.getPath() + "_emissive"));
	}

	public <T extends Item> ItemModelBuilder basicItem(DeferredItem<T> registryObject) {
		return basicItem(registryObject.getId());
	}

	public ItemModelBuilder spawnEggItem(Item item) {
		return spawnEggItem(registryKey(item));
	}

	public ItemModelBuilder spawnEggItem(Identifier registryKey) {
		return getBuilder(registryKey.toString()).parent(new ModelFile.UncheckedModelFile("item/template_spawn_egg"));
	}

	public <T extends Item> ItemModelBuilder lootItem(DeferredItem<T> registryObject) {
		return basicItem(registryObject.getId(), "loot");
	}

	public ItemModelBuilder lootItem(Item item) {
		return basicItem(registryKey(item), "loot");
	}

	public <T extends Item> ItemModelBuilder componentItem(DeferredItem<T> registryObject) {
		return basicItem(registryObject.getId(), "component");
	}

	public ItemModelBuilder componentItem(Item item) {
		return basicItem(registryKey(item), "component");
	}

	public <T extends Item> ItemModelBuilder serumItem(DeferredItem<T> registryObject) {
		return basicItem(registryObject.getId(), "serum");
	}

	public ItemModelBuilder serumItem(Item item) {
		return basicItem(registryKey(item), "serum");
	}

	public <T extends Item> ItemModelBuilder serumOverlayItem(DeferredItem<T> registryObject) {
		return overlayItem(registryObject.getId(), "serum");
	}

	public <T extends Item> ItemModelBuilder armorItem(DeferredItem<T> registryObject) {
		return basicItem(registryObject.getId(), "armor");
	}

	public <T extends Item> ItemModelBuilder weaponItem(DeferredItem<T> registryObject) {
		return basicItem(registryObject.getId(), "weapon");
	}

	public ItemModelBuilder weaponItem(Item item) {
		return basicItem(registryKey(item), "weapon");
	}

	public ItemModelBuilder handheldWeaponItem(Item item) {
		return handheldItem(registryKey(item), "weapon");
	}

	public <T extends Item> ItemModelBuilder handheldWeaponItem(DeferredItem<T> registryObject) {
		return handheldItem(registryObject.getId(), "weapon");
	}

	public ItemModelBuilder miscItem(Item item) {
		return basicItem(registryKey(item), "misc");
	}

	public ItemModelBuilder basicItem(Identifier registryKey, String subfolder) {
		return getBuilder(registryKey.toString())
				.parent(new ModelFile.UncheckedModelFile("item/generated"))
				.texture(LAYER_0_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), ITEM_FOLDER + "/" + subfolder + "/" + registryKey.getPath()));
	}

	public ItemModelBuilder overlayItem(Item item) {
		return overlayItem(registryKey(item));
	}

	public <T extends Item> ItemModelBuilder overlayItem(DeferredItem<T> registryObject) {
		return overlayItem(registryObject.getId());
	}

	public ItemModelBuilder overlayItem(Identifier registryKey) {
		String texturePath = ITEM_FOLDER + "/" + registryKey.getPath() + "_overlay";
		return basicItem(registryKey).texture(LAYER_1_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), texturePath));
	}

	public ItemModelBuilder overlayItem(Identifier registryKey, String subfolder) {
		String texturePath = ITEM_FOLDER + "/" + subfolder + "/" + registryKey.getPath() + "_overlay";
		return basicItem(registryKey, subfolder).texture(LAYER_1_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), texturePath));
	}

	public ItemModelBuilder handheldItem(Item item) {
		return handheldItem(registryKey(item));
	}

	public <T extends Item> ItemModelBuilder handheldItem(DeferredItem<T> registryObject) {
		return handheldItem(registryObject.getId());
	}

	public ItemModelBuilder handheldItem(Identifier registryKey) {
		return getBuilder(registryKey.toString())
				.parent(new ModelFile.UncheckedModelFile("item/handheld"))
				.texture(LAYER_0_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), ITEM_FOLDER + "/" + registryKey.getPath()));
	}

	public ItemModelBuilder handheldItem(Identifier registryKey, String subfolder) {
		return getBuilder(registryKey.toString())
				.parent(new ModelFile.UncheckedModelFile("item/handheld"))
				.texture(LAYER_0_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), ITEM_FOLDER + "/" + subfolder + "/" + registryKey.getPath()));
	}

	public <T extends BlockItem> ItemModelBuilder flatBlockItem(DeferredItem<T> registryObject) {
		return flatBlockItem(registryObject.getId());
	}

	public ItemModelBuilder flatBlockItem(BlockItem blockItem) {
		return flatBlockItem(registryKey(blockItem));
	}

	public ItemModelBuilder flatBlockItem(Identifier registryKey) {
		return getBuilder(registryKey.toString())
				.parent(new ModelFile.UncheckedModelFile("item/generated"))
				.texture(LAYER_0_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), BLOCK_FOLDER + "/" + registryKey.getPath()));
	}

	public <T extends BlockItem> ItemModelBuilder flatBlockItem(DeferredItem<T> registryObject, String suffix) {
		return flatBlockItem(registryObject.getId(), suffix);
	}

	public ItemModelBuilder flatBlockItem(BlockItem blockItem, String suffix) {
		return flatBlockItem(registryKey(blockItem), suffix);
	}

	public ItemModelBuilder flatBlockItem(Identifier registryKey, String suffix) {
		return getBuilder(registryKey.toString())
				.parent(new ModelFile.UncheckedModelFile("item/generated"))
				.texture(LAYER_0_TEXTURE, Identifier.fromNamespaceAndPath(registryKey.getNamespace(), BLOCK_FOLDER + "/" + registryKey.getPath() + suffix));
	}

	public ItemModelBuilder dynamicBucket(BucketItem item) {
		Identifier itemKey = registryKey(item);
		Identifier fluidKey = Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(item.content));
		Identifier loaderKey = Identifier.fromNamespaceAndPath("neoforge", "fluid_container");
		Identifier bucketModelKey = Identifier.fromNamespaceAndPath("neoforge", "item/bucket");

		return getBuilder(itemKey.toString())
				.parent(getExistingFile(bucketModelKey))
				.customLoader((builder, existingFileHelper) -> new CustomLoaderBuilder<ItemModelBuilder>(loaderKey, builder, existingFileHelper, false) {
					@Override
					public JsonObject toJson(JsonObject json) {
						JsonObject json1 = super.toJson(json);
						json1.addProperty("fluid", fluidKey.toString());
						return json1;
					}
				}).end();
	}
}
