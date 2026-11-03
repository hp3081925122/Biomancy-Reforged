package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.serum.Serum;
import com.github.elenterius.biomancy.item.*;
import com.github.elenterius.biomancy.item.armor.AcolyteArmorItem;
import com.github.elenterius.biomancy.item.armor.WarriorArmorItem;
import com.github.elenterius.biomancy.item.extractor.ExtractorItem;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import com.github.elenterius.biomancy.item.shield.ThornShieldItem;
import com.github.elenterius.biomancy.item.weapon.DespoilingSwordItem;
import com.github.elenterius.biomancy.item.weapon.GrenadeItem;
import com.github.elenterius.biomancy.item.weapon.RavenousClawsItem;
import com.github.elenterius.biomancy.item.weapon.gun.CausticGunbladeItem;
import com.github.elenterius.biomancy.item.weapon.gun.ImpalerItem;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ModItems {

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BiomancyMod.MOD_ID);
	public static final DeferredRegister.Items DEV_ITEMS = DeferredRegister.createItems(BiomancyMod.MOD_ID);
	public static final DeferredItem<SimpleItem> MOB_FANG = registerSimpleItem("mob_fang");
	public static final DeferredItem<SimpleItem> MOB_CLAW = registerSimpleItem("mob_claw");
	public static final DeferredItem<SimpleItem> MOB_SINEW = registerSimpleItem("mob_sinew", ModRarities.UNCOMMON);
	public static final DeferredItem<BoneMarrowItem> MOB_MARROW = registerItem("mob_marrow", props -> new BoneMarrowItem(props.food(ModFoods.MARROW_FLUID).rarity(ModRarities.RARE)));
	public static final DeferredItem<BoneMarrowItem> WITHERED_MOB_MARROW = registerItem("withered_mob_marrow", props -> new BoneMarrowItem(props.food(ModFoods.CORROSIVE_FLUID).rarity(ModRarities.VERY_RARE)));
	public static final DeferredItem<SimpleItem> GENERIC_MOB_GLAND = registerItem("mob_gland", props -> new SimpleItem(props.food(ModFoods.POOR_FLESH).rarity(ModRarities.UNCOMMON)));
	public static final DeferredItem<SimpleItem> TOXIN_GLAND = registerItem("toxin_gland", props -> new SimpleItem(props.food(ModFoods.TOXIN_GLAND).rarity(ModRarities.RARE)));
	public static final DeferredItem<VolatileGlandItem> VOLATILE_GLAND = registerItem("volatile_gland", props -> new VolatileGlandItem(props.food(ModFoods.VOLATILE_GLAND).rarity(ModRarities.RARE)));
	public static final DeferredItem<AcidicEggItem> ACIDIC_EGG = registerItem("acidic_egg", AcidicEggItem::new);
	public static final DeferredItem<SimpleItem> FLESH_BITS = registerSimpleItem("flesh_bits");
	public static final DeferredItem<SimpleItem> BONE_FRAGMENTS = registerSimpleItem("bone_fragments");
	public static final DeferredItem<SimpleItem> TOUGH_FIBERS = registerSimpleItem("tough_fibers");
	public static final DeferredItem<SimpleItem> ELASTIC_FIBERS = registerSimpleItem("elastic_fibers");
	public static final DeferredItem<SimpleItem> MINERAL_FRAGMENT = registerSimpleItem("mineral_fragment");
	public static final DeferredItem<SimpleItem> GEM_FRAGMENTS = registerSimpleItem("gem_fragments");
	public static final DeferredItem<SimpleItem> NUTRIENTS = registerSimpleItem("nutrients");
	public static final DeferredItem<SimpleItem> ORGANIC_MATTER = registerSimpleItem("organic_matter");
	public static final DeferredItem<BioluminescentGooItem> BIO_LUMENS = registerItem("bio_lumens", BioluminescentGooItem::new);
	public static final DeferredItem<SimpleItem> EXOTIC_DUST = registerSimpleItem("exotic_dust");
	public static final DeferredItem<SimpleItem> STONE_POWDER = registerSimpleItem("stone_powder");
	public static final DeferredItem<SimpleItem> REGENERATIVE_FLUID = registerSimpleItem("regenerative_fluid");
	public static final DeferredItem<SimpleItem> WITHERING_OOZE = registerSimpleItem("withering_ooze");
	public static final DeferredItem<SimpleItem> HORMONE_SECRETION = registerSimpleItem("hormone_secretion");
	public static final DeferredItem<SimpleItem> TOXIN_EXTRACT = registerSimpleItem("toxin_extract");
	public static final DeferredItem<SimpleItem> ACID_EXTRACT = registerItem("acid_extract", properties -> new SimpleItem(properties.food(ModFoods.GASTRIC_JUICE)));
	public static final DeferredItem<SimpleItem> BILE = registerSimpleItem("bile");
	public static final DeferredItem<SimpleItem> VOLATILE_FLUID = registerSimpleItem("volatile_fluid");
	public static final DeferredItem<SimpleItem> VIAL = registerSimpleItem("vial");
	public static final DeferredItem<SimpleItem> ORGANIC_COMPOUND = registerSimpleVialItem("organic_compound");
	public static final DeferredItem<UnstableCompoundItem> UNSTABLE_COMPOUND = registerItem("unstable_compound", UnstableCompoundItem::new);
	public static final DeferredItem<SimpleItem> GENETIC_COMPOUND = registerSimpleVialItem("genetic_compound");
	public static final DeferredItem<SimpleItem> EXOTIC_COMPOUND = registerSimpleVialItem("exotic_compound");
	public static final DeferredItem<SimpleItem> HEALING_ADDITIVE = registerSimpleVialItem("healing_additive");
	public static final DeferredItem<SimpleItem> DECAYING_ADDITIVE = registerSimpleVialItem("decaying_additive");
	public static final DeferredItem<SerumItem> REJUVENATION_SERUM = registerSerumItem(ModSerums.REJUVENATION_SERUM);
	public static final DeferredItem<SerumItem> AGEING_SERUM = registerSerumItem(ModSerums.AGEING_SERUM);
	public static final DeferredItem<SerumItem> ENLARGEMENT_SERUM = registerSerumItem(ModSerums.ENLARGEMENT_SERUM);
	public static final DeferredItem<SerumItem> SHRINKING_SERUM = registerSerumItem(ModSerums.SHRINKING_SERUM);
	public static final DeferredItem<SerumItem> BREEDING_STIMULANT = registerSerumItem(ModSerums.BREEDING_STIMULANT);
	public static final DeferredItem<SerumItem> ABSORPTION_BOOST = registerSerumItem(ModSerums.ABSORPTION_BOOST);
	public static final DeferredItem<SerumItem> CLEANSING_SERUM = registerSerumItem(ModSerums.CLEANSING_SERUM);
	public static final DeferredItem<SerumItem> INSOMNIA_CURE = registerSerumItem(ModSerums.INSOMNIA_CURE);
	public static final DeferredItem<SerumItem> FRENZY_SERUM = registerSerumItem(ModSerums.FRENZY_SERUM);
	public static final DeferredItem<PotionSerumItem> POTION_SERUM = registerItem("potion_serum", props -> new PotionSerumItem(props.stacksTo(16).rarity(ModRarities.UNCOMMON)));
	public static final DeferredItem<SimpleItem> PRIMORDIAL_CORE = registerSimpleItem("primordial_core", ModRarities.VERY_RARE);
	public static final DeferredItem<SimpleItem> LIVING_FLESH = registerItem("living_flesh", props -> new SimpleItem(props.food(ModFoods.LIVING_FLESH).rarity(ModRarities.VERY_RARE)));
	public static final DeferredItem<EssenceItem> ESSENCE = registerItem("essence", EssenceItem::new);
	public static final DeferredItem<DespoilingSwordItem> DESPOIL_SICKLE = registerItem("despoil_sickle", props -> SwordSmithy.forge(DespoilingSwordItem::new, ModTiers.PRIMAL_FLESH, 10, 1, props.rarity(ModRarities.VERY_RARE)));
	public static final DeferredItem<ExtractorItem> ESSENCE_EXTRACTOR = registerItem("extractor", props -> new ExtractorItem(props.durability(200).rarity(ModRarities.RARE)));
	public static final DeferredItem<InjectorItem> INJECTOR = registerItem("injector", props -> new InjectorItem(props.durability(200).rarity(ModRarities.RARE)));
	public static final DeferredItem<RavenousClawsItem> RAVENOUS_CLAWS = registerItem("ravenous_claws", props -> new RavenousClawsItem(ModTiers.BIOFLESH, 4.5f, 3f, 250, props.stacksTo(1).rarity(ModRarities.VERY_RARE)));
	public static final DeferredItem<CausticGunbladeItem> CAUSTIC_GUNBLADE = registerItem("caustic_gunblade", props -> new CausticGunbladeItem(250, props.stacksTo(1).rarity(ModRarities.VERY_RARE)));
	public static final DeferredItem<ImpalerItem> IMPALER = registerItem("impaler", props -> new ImpalerItem(200, props.stacksTo(1).rarity(ModRarities.ULTRA_RARE)));
	public static final DeferredItem<SimpleItem> GRENADE_CASING = registerSimpleItem("grenade_casing");
	public static final DeferredItem<GrenadeItem> TOXIN_GRENADE = registerItem("toxin_grenade", GrenadeItem::new);
	public static final DeferredItem<GrenadeItem> ACID_GRENADE = registerItem("acid_grenade", GrenadeItem::new);
	public static final DeferredItem<GrenadeItem> DECAY_GRENADE = registerItem("decay_grenade", GrenadeItem::new);
	public static final DeferredItem<GrenadeItem> VOLATILE_GRENADE = registerItem("volatile_grenade", GrenadeItem::new);
	public static final DeferredItem<ThornShieldItem> THORN_SHIELD = registerItem("thorn_shield", props -> new ThornShieldItem(250, props.stacksTo(1).rarity(ModRarities.VERY_RARE)));
	public static final DeferredItem<AcolyteArmorItem> ACOLYTE_ARMOR_HELMET = registerLivingArmorHelmet("acolyte_armor", ModArmorMaterials.ACOLYTE, 200, AcolyteArmorItem::new);
	public static final DeferredItem<AcolyteArmorItem> ACOLYTE_ARMOR_CHESTPLATE = registerLivingArmorChestplate("acolyte_armor", ModArmorMaterials.ACOLYTE, 250, AcolyteArmorItem::new);
	public static final DeferredItem<AcolyteArmorItem> ACOLYTE_ARMOR_LEGGINGS = registerLivingArmorLeggings("acolyte_armor", ModArmorMaterials.ACOLYTE, 250, AcolyteArmorItem::new);
	public static final DeferredItem<AcolyteArmorItem> ACOLYTE_ARMOR_BOOTS = registerLivingArmorBoots("acolyte_armor", ModArmorMaterials.ACOLYTE, 200, AcolyteArmorItem::new);
	public static final DeferredItem<WarriorArmorItem> WARRIOR_ARMOR_HELMET = registerLivingArmorHelmet("warrior_armor", ModArmorMaterials.WARRIOR, 200 * 2, WarriorArmorItem::new);
	public static final DeferredItem<WarriorArmorItem> WARRIOR_ARMOR_CHESTPLATE = registerLivingArmorChestplate("warrior_armor", ModArmorMaterials.WARRIOR, 250 * 2, WarriorArmorItem::new);
	public static final DeferredItem<WarriorArmorItem> WARRIOR_ARMOR_LEGGINGS = registerLivingArmorLeggings("warrior_armor", ModArmorMaterials.WARRIOR, 250 * 2, WarriorArmorItem::new);
	public static final DeferredItem<WarriorArmorItem> WARRIOR_ARMOR_BOOTS = registerLivingArmorBoots("warrior_armor", ModArmorMaterials.WARRIOR, 200 * 2, WarriorArmorItem::new);
	public static final DeferredItem<EffectCureItem> NUTRIENT_PASTE = registerItem("nutrient_paste", props -> new EffectCureItem(props.food(ModFoods.NUTRIENT_PASTE)));
	public static final DeferredItem<EffectCureItem> NUTRIENT_BAR = registerItem("nutrient_bar", props -> new EffectCureItem(props.food(ModFoods.NUTRIENT_BAR)));
	public static final DeferredItem<BloomberryItem> BLOOMBERRY = registerItem("bloomberry", props -> new BloomberryItem(props.food(ModFoods.NUTRIENT_PASTE)));
	public static final DeferredItem<FertilizerItem> FERTILIZER = registerItem("fertilizer", props -> new FertilizerItem(props.rarity(ModRarities.UNCOMMON)));
	public static final DeferredItem<SimpleItem> CREATOR_MIX = registerSimpleItem("creator_mix");
	public static final DeferredItem<BucketItem> ACID_BUCKET = registerItem("acid_bucket", properties -> new BucketItem(ModFluids.ACID.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.COMMON)));
	public static final DeferredItem<SimpleItem> GELLING_AGENT = registerSimpleItem("gelling_agent");

	public static final DeferredItem<MaykerBannerPatternItem> MASCOT_BANNER_PATTERNS = registerItem("mascot_patterns", props -> new MaykerBannerPatternItem(ModBannerPatterns.TAG_MASCOT, props));
	public static final DeferredItem<SimpleItem> TAB_ICON = registerSimpleItem("tab_icon");
	public static final DeferredItem<GuideBookItem> DEV_GUIDE_BOOK = registerDevItem("guide_book", props -> new GuideBookItem(props.stacksTo(1).rarity(ModRarities.RARE)));
	public static final DeferredItem<BEWLBlockItem> PRIMORDIAL_CRADLE = registerBlockItem(ModBlocks.PRIMORDIAL_CRADLE, block -> new BEWLBlockItem(block, createProperties().rarity(ModRarities.VERY_RARE)));
	public static final DeferredItem<SimpleBlockItem> BIO_FORGE = registerSimpleBlockItem(ModBlocks.BIO_FORGE, ModRarities.RARE);
	public static final DeferredItem<SimpleBlockItem> DECOMPOSER = registerSimpleBlockItem(ModBlocks.DECOMPOSER, ModRarities.RARE);
	public static final DeferredItem<SimpleBlockItem> BIO_LAB = registerSimpleBlockItem(ModBlocks.BIO_LAB, ModRarities.RARE);
	public static final DeferredItem<SimpleBlockItem> DIGESTER = registerSimpleBlockItem(ModBlocks.DIGESTER, ModRarities.RARE);
	public static final DeferredItem<SimpleBlockItem> TONGUE = registerSimpleBlockItem(ModBlocks.TONGUE, ModRarities.UNCOMMON);
	public static final DeferredItem<SimpleBlockItem> MAW_HOPPER = registerSimpleBlockItem(ModBlocks.MAW_HOPPER, ModRarities.UNCOMMON);
	public static final DeferredItem<FleshkinChestBlockItem> FLESHKIN_CHEST = registerBlockItem(ModBlocks.FLESHKIN_CHEST, FleshkinChestBlockItem::new, ModRarities.UNCOMMON);
	public static final DeferredItem<StorageSacBlockItem> STORAGE_SAC = registerBlockItem(ModBlocks.STORAGE_SAC, block -> new StorageSacBlockItem(block, createProperties().stacksTo(1)));
	public static final DeferredItem<SimpleBlockItem> VIAL_HOLDER = registerSimpleBlockItem(ModBlocks.VIAL_HOLDER);
	public static final DeferredItem<SimpleBlockItem> JUMP_PAD = registerSimpleBlockItem(ModBlocks.JUMP_PAD);
	public static final DeferredItem<ChrysalisBlockItem> CHRYSALIS = registerBlockItem(ModBlocks.CHRYSALIS, ChrysalisBlockItem::new, ModRarities.VERY_RARE);
	public static final DeferredItem<SimpleBlockItem> MODULAR_LARYNX = registerSimpleBlockItem(ModBlocks.MODULAR_LARYNX);
	public static final DeferredItem<SimpleBlockItem> FLESH_SPIKE = registerSimpleBlockItem(ModBlocks.FLESH_SPIKE);
	public static final DeferredItem<SimpleBlockItem> FLESHKIN_PRESSURE_PLATE = registerSimpleBlockItem(ModBlocks.FLESHKIN_PRESSURE_PLATE);
	public static final DeferredItem<SimpleBlockItem> WATER_GEL_BLOCK = registerSimpleBlockItem(ModBlocks.WATER_GEL_BLOCK);
	public static final DeferredItem<SimpleBlockItem> ACID_SPLATTER = registerSimpleBlockItem(ModBlocks.ACID_SPLATTER);
	public static final DeferredItem<SimpleBlockItem> VOLATILE_SPLATTER = registerSimpleBlockItem(ModBlocks.VOLATILE_SPLATTER);

	public static final DeferredItem<SimpleBlockItem> FLESH_IRIS_DOOR = registerSimpleBlockItem(ModBlocks.FLESH_IRIS_DOOR);
	public static final DeferredItem<SimpleBlockItem> FLESH_DOOR = registerSimpleBlockItem(ModBlocks.FLESH_DOOR);
	public static final DeferredItem<SimpleBlockItem> FULL_FLESH_DOOR = registerSimpleBlockItem(ModBlocks.FULL_FLESH_DOOR);
	public static final DeferredItem<FleshChainBlockItem> TENDON_CHAIN = registerBlockItem(ModBlocks.TENDON_CHAIN, FleshChainBlockItem::new);
	public static final DeferredItem<SimpleBlockItem> FLESH_LADDER = registerSimpleBlockItem(ModBlocks.FLESH_LADDER);
	public static final DeferredItem<SimpleBlockItem> FLESH_FENCE = registerSimpleBlockItem(ModBlocks.FLESH_FENCE);
	public static final DeferredItem<SimpleBlockItem> FLESH_FENCE_GATE = registerSimpleBlockItem(ModBlocks.FLESH_FENCE_GATE);
	public static final DeferredItem<SimpleBlockItem> YELLOW_BIO_LANTERN = registerSimpleBlockItem(ModBlocks.YELLOW_BIO_LANTERN);
	public static final DeferredItem<SimpleBlockItem> BLUE_BIO_LANTERN = registerSimpleBlockItem(ModBlocks.BLUE_BIO_LANTERN);
	public static final DeferredItem<SimpleBlockItem> PRIMORDIAL_BIO_LANTERN = registerSimpleBlockItem(ModBlocks.PRIMORDIAL_BIO_LANTERN);
	public static final DeferredItem<SimpleBlockItem> BIOMETRIC_MEMBRANE = registerBlockItem(ModBlocks.BIOMETRIC_MEMBRANE, BiometricMembraneBlockItem::new, ModRarities.VERY_RARE);
	public static final DeferredItem<SimpleBlockItem> ONEWAY_MEMBRANE = registerSimpleBlockItem(ModBlocks.ONEWAY_MEMBRANE);
	public static final DeferredItem<SimpleBlockItem> IMPERMEABLE_MEMBRANE = registerSimpleBlockItem(ModBlocks.IMPERMEABLE_MEMBRANE);
	public static final DeferredItem<SimpleBlockItem> IMPERMEABLE_MEMBRANE_PANE = registerSimpleBlockItem(ModBlocks.IMPERMEABLE_MEMBRANE_PANE);
	public static final DeferredItem<SimpleBlockItem> BABY_PERMEABLE_MEMBRANE = registerSimpleBlockItem(ModBlocks.BABY_PERMEABLE_MEMBRANE);
	public static final DeferredItem<SimpleBlockItem> BABY_PERMEABLE_MEMBRANE_PANE = registerSimpleBlockItem(ModBlocks.BABY_PERMEABLE_MEMBRANE_PANE);
	public static final DeferredItem<SimpleBlockItem> ADULT_PERMEABLE_MEMBRANE = registerSimpleBlockItem(ModBlocks.ADULT_PERMEABLE_MEMBRANE);
	public static final DeferredItem<SimpleBlockItem> ADULT_PERMEABLE_MEMBRANE_PANE = registerSimpleBlockItem(ModBlocks.ADULT_PERMEABLE_MEMBRANE_PANE);
	public static final DeferredItem<SimpleBlockItem> PRIMAL_PERMEABLE_MEMBRANE = registerSimpleBlockItem(ModBlocks.PRIMAL_PERMEABLE_MEMBRANE);
	public static final DeferredItem<SimpleBlockItem> PRIMAL_PERMEABLE_MEMBRANE_PANE = registerSimpleBlockItem(ModBlocks.PRIMAL_PERMEABLE_MEMBRANE_PANE);
	public static final DeferredItem<SimpleBlockItem> UNDEAD_PERMEABLE_MEMBRANE = registerSimpleBlockItem(ModBlocks.UNDEAD_PERMEABLE_MEMBRANE);
	public static final DeferredItem<SimpleBlockItem> UNDEAD_PERMEABLE_MEMBRANE_PANE = registerSimpleBlockItem(ModBlocks.UNDEAD_PERMEABLE_MEMBRANE_PANE);
	public static final DeferredItem<SimpleBlockItem> FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.FLESH);
	public static final DeferredItem<SimpleBlockItem> FLESH_SLAB = registerSimpleBlockItem(ModBlocks.FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> FLESH_STAIRS = registerSimpleBlockItem(ModBlocks.FLESH_STAIRS);
	public static final DeferredItem<SimpleBlockItem> FLESH_WALL = registerSimpleBlockItem(ModBlocks.FLESH_WALL);
	public static final DeferredItem<SimpleBlockItem> PACKED_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.PACKED_FLESH);
	public static final DeferredItem<SimpleBlockItem> PACKED_FLESH_SLAB = registerSimpleBlockItem(ModBlocks.PACKED_FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> PACKED_FLESH_STAIRS = registerSimpleBlockItem(ModBlocks.PACKED_FLESH_STAIRS);
	public static final DeferredItem<SimpleBlockItem> PACKED_FLESH_WALL = registerSimpleBlockItem(ModBlocks.PACKED_FLESH_WALL);
	public static final DeferredItem<SimpleBlockItem> FIBROUS_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.FIBROUS_FLESH);
	public static final DeferredItem<SimpleBlockItem> FIBROUS_FLESH_SLAB = registerSimpleBlockItem(ModBlocks.FIBROUS_FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> FIBROUS_FLESH_STAIRS = registerSimpleBlockItem(ModBlocks.FIBROUS_FLESH_STAIRS);
	public static final DeferredItem<SimpleBlockItem> FIBROUS_FLESH_WALL = registerSimpleBlockItem(ModBlocks.FIBROUS_FLESH_WALL);
	public static final DeferredItem<SimpleBlockItem> FLESH_PILLAR = registerSimpleBlockItem(ModBlocks.FLESH_PILLAR);
	public static final DeferredItem<SimpleBlockItem> CHISELED_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.CHISELED_FLESH);
	public static final DeferredItem<SimpleBlockItem> ORNATE_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.ORNATE_FLESH);
	public static final DeferredItem<SimpleBlockItem> ORNATE_FLESH_SLAB = registerSimpleBlockItem(ModBlocks.ORNATE_FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> TUBULAR_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.TUBULAR_FLESH_BLOCK);

	public static final DeferredItem<SimpleBlockItem> PRIMAL_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.PRIMAL_FLESH);
	public static final DeferredItem<SimpleBlockItem> PRIMAL_FLESH_SLAB = registerSimpleBlockItem(ModBlocks.PRIMAL_FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> PRIMAL_FLESH_STAIRS = registerSimpleBlockItem(ModBlocks.PRIMAL_FLESH_STAIRS);
	public static final DeferredItem<SimpleBlockItem> PRIMAL_FLESH_WALL = registerSimpleBlockItem(ModBlocks.PRIMAL_FLESH_WALL);
	public static final DeferredItem<SimpleBlockItem> SMOOTH_PRIMAL_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.SMOOTH_PRIMAL_FLESH);
	public static final DeferredItem<SimpleBlockItem> SMOOTH_PRIMAL_FLESH_SLAB = registerSimpleBlockItem(ModBlocks.SMOOTH_PRIMAL_FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> SMOOTH_PRIMAL_FLESH_STAIRS = registerSimpleBlockItem(ModBlocks.SMOOTH_PRIMAL_FLESH_STAIRS);
	public static final DeferredItem<SimpleBlockItem> SMOOTH_PRIMAL_FLESH_WALL = registerSimpleBlockItem(ModBlocks.SMOOTH_PRIMAL_FLESH_WALL);
	public static final DeferredItem<SimpleBlockItem> FIBROUS_PRIMAL_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.FIBROUS_PRIMAL_FLESH);
	public static final DeferredItem<SimpleBlockItem> FIBROUS_PRIMAL_FLESH_SLAB = registerSimpleBlockItem(ModBlocks.FIBROUS_PRIMAL_FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> FIBROUS_PRIMAL_FLESH_STAIRS = registerSimpleBlockItem(ModBlocks.FIBROUS_PRIMAL_FLESH_STAIRS);
	public static final DeferredItem<SimpleBlockItem> FIBROUS_PRIMAL_FLESH_WALL = registerSimpleBlockItem(ModBlocks.FIBROUS_PRIMAL_FLESH_WALL);
	public static final DeferredItem<SimpleBlockItem> POROUS_PRIMAL_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.POROUS_PRIMAL_FLESH);
	public static final DeferredItem<SimpleBlockItem> POROUS_PRIMAL_FLESH_SLAB = registerSimpleBlockItem(ModBlocks.POROUS_PRIMAL_FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> POROUS_PRIMAL_FLESH_STAIRS = registerSimpleBlockItem(ModBlocks.POROUS_PRIMAL_FLESH_STAIRS);
	public static final DeferredItem<SimpleBlockItem> POROUS_PRIMAL_FLESH_WALL = registerSimpleBlockItem(ModBlocks.POROUS_PRIMAL_FLESH_WALL);
	public static final DeferredItem<SimpleBlockItem> MALIGNANT_FLESH_BLOCK = registerSimpleBlockItem(ModBlocks.MALIGNANT_FLESH);
	public static final DeferredItem<SimpleBlockItem> MALIGNANT_FLESH_SLAB = registerSimpleBlockItem(ModBlocks.MALIGNANT_FLESH_SLAB);
	public static final DeferredItem<SimpleBlockItem> MALIGNANT_FLESH_STAIRS = registerSimpleBlockItem(ModBlocks.MALIGNANT_FLESH_STAIRS);
	public static final DeferredItem<SimpleBlockItem> MALIGNANT_FLESH_WALL = registerSimpleBlockItem(ModBlocks.MALIGNANT_FLESH_WALL);
	public static final DeferredItem<SimpleBlockItem> MALIGNANT_FLESH_VEINS = registerSimpleBlockItem(ModBlocks.MALIGNANT_FLESH_VEINS);
	public static final DeferredItem<SimpleBlockItem> PRIMAL_BLOOM = registerSimpleBlockItem(ModBlocks.PRIMAL_BLOOM);
	public static final DeferredItem<SimpleBlockItem> BLOOMLIGHT = registerSimpleBlockItem(ModBlocks.BLOOMLIGHT);
	public static final DeferredItem<SimpleBlockItem> PRIMAL_ORIFICE = registerSimpleBlockItem(ModBlocks.PRIMAL_ORIFICE);
	public static final DeferredItem<SimpleBlockItem> PRIMAL_BONE = registerSimpleBlockItem(ModBlocks.PRIMAL_BONE);
	public static final DeferredItem<DeferredSpawnEggItem> HUNGRY_FLESH_BLOB_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.HUNGRY_FLESH_BLOB, 0xe9967a, 0xf6d2c6);
	public static final DeferredItem<DeferredSpawnEggItem> FLESH_BLOB_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.FLESH_BLOB, 0xe9967a, 0xf6d2c6);
	public static final DeferredItem<DeferredSpawnEggItem> LEGACY_FLESH_BLOB_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.LEGACY_FLESH_BLOB, 0xeec5da, 0xffc0cb);
	public static final DeferredItem<DeferredSpawnEggItem> PRIMORDIAL_FLESH_BLOB_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.PRIMORDIAL_FLESH_BLOB, 0xde6074, 0xc343fe);
	public static final DeferredItem<DeferredSpawnEggItem> PRIMORDIAL_HUNGRY_FLESH_BLOB_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.PRIMORDIAL_HUNGRY_FLESH_BLOB, 0x752144, 0x752144);
	public static final DeferredItem<DeferredSpawnEggItem> FLESH_COW_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.FLESH_COW, 0xe9967a, 0x9d7572);
	public static final DeferredItem<DeferredSpawnEggItem> FLESH_SHEEP_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.FLESH_SHEEP, 0xe9967a, 0xf9bbd4);
	public static final DeferredItem<DeferredSpawnEggItem> FLESH_PIG_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.FLESH_PIG, 0xe9967a, 0xed7684);
	public static final DeferredItem<DeferredSpawnEggItem> FLESH_CHICKEN_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.FLESH_CHICKEN, 0xe9967a, 0xce4e65);
	public static final DeferredItem<DeferredSpawnEggItem> CHROMA_SHEEP_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.CHROMA_SHEEP, 0xe9967a, 0xf9bbd4);
	public static final DeferredItem<DeferredSpawnEggItem> THICK_FUR_SHEEP_SPAWN_EGG = registerSpawnEgg(ModEntityTypes.THICK_FUR_SHEEP, 0xe9967a, 0xf9bbd4);

	private ModItems() {}

	public static Stream<Item> stream() {
		return ModItems.ITEMS.getEntries().stream().map(DeferredHolder::get);
	}

	public static <T extends Item> Stream<T> findItems(Class<T> clazz) {
		return ModItems.ITEMS.getEntries().stream()
				.map(DeferredHolder::get)
				.filter(clazz::isInstance)
				.map(clazz::cast);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Item> Stream<DeferredHolder<Item, T>> findEntries(Class<T> clazz) {
		return ModItems.ITEMS.getEntries().stream()
				.filter(registryObject -> clazz.isInstance(registryObject.get()))
				.map(registryObject -> (DeferredHolder<Item, T>) registryObject);
	}

	private static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> factory) {
		return ITEMS.register(name, () -> factory.apply(createProperties()));
	}

	private static <T extends Item> DeferredItem<T> registerDevItem(String name, Function<Item.Properties, T> factory) {
		return ITEMS.register(name, () -> factory.apply(createProperties()));
	}

	private static <T extends Block> DeferredItem<SimpleBlockItem> registerSimpleBlockItem(DeferredBlock<T> blockHolder) {
		return ITEMS.register(blockHolder.getId().getPath(), () -> new SimpleBlockItem(blockHolder.get(), createProperties()));
	}

	private static <T extends Block> DeferredItem<SimpleBlockItem> registerSimpleBlockItem(DeferredBlock<T> blockHolder, Rarity rarity) {
		return registerSimpleBlockItem(blockHolder, () -> createProperties().rarity(rarity));
	}

	private static <T extends Block> DeferredItem<SimpleBlockItem> registerSimpleBlockItem(DeferredBlock<T> blockHolder, Supplier<Item.Properties> properties) {
		return ITEMS.register(blockHolder.getId().getPath(), () -> new SimpleBlockItem(blockHolder.get(), properties.get()));
	}

	private static <T extends Block, I extends BlockItem> DeferredItem<I> registerBlockItem(DeferredBlock<T> blockHolder, Function<T, I> factory) {
		return ITEMS.register(blockHolder.getId().getPath(), () -> factory.apply(blockHolder.get()));
	}

	private static <T extends Block, I extends BlockItem> DeferredItem<I> registerBlockItem(DeferredBlock<T> blockHolder, IBlockItemFactory<T, I> factory) {
		return ITEMS.register(blockHolder.getId().getPath(), () -> factory.create(blockHolder.get(), createProperties()));
	}

	private static <T extends Block, I extends BlockItem> DeferredItem<I> registerBlockItem(DeferredBlock<T> blockHolder, IBlockItemFactory<T, I> factory, Rarity rarity) {
		return ITEMS.register(blockHolder.getId().getPath(), () -> factory.create(blockHolder.get(), createProperties().rarity(rarity)));
	}

	private static <I extends ArmorItem> DeferredItem<I> registerArmorHelmet(String name, Holder<ArmorMaterial> material, ArmorFactory<I> factory) {
		return registerArmor(name + "_helmet", material, ArmorItem.Type.HELMET, factory);
	}

	private static <I extends ArmorItem> DeferredItem<I> registerArmorChestplate(String name, Holder<ArmorMaterial> material, ArmorFactory<I> factory) {
		return registerArmor(name + "_chestplate", material, ArmorItem.Type.CHESTPLATE, factory);
	}

	private static <I extends ArmorItem> DeferredItem<I> registerArmorLeggings(String name, Holder<ArmorMaterial> material, ArmorFactory<I> factory) {
		return registerArmor(name + "_leggings", material, ArmorItem.Type.LEGGINGS, factory);
	}

	private static <I extends ArmorItem> DeferredItem<I> registerArmorBoots(String name, Holder<ArmorMaterial> material, ArmorFactory<I> factory) {
		return registerArmor(name + "_boots", material, ArmorItem.Type.BOOTS, factory);
	}

	private static <I extends ArmorItem> DeferredItem<I> registerArmor(String name, Holder<ArmorMaterial> material, ArmorItem.Type type, ArmorFactory<I> factory) {
		return ITEMS.register(name, () -> factory.create(material, type, createProperties().durability(ModArmorMaterials.getDurability(material, type))));
	}

	private static <I extends ArmorItem> DeferredItem<I> registerLivingArmorHelmet(String name, Holder<ArmorMaterial> material, int maxNutrients, LivingArmorFactory<I> factory) {
		return registerLivingArmor(name + "_helmet", material, ArmorItem.Type.HELMET, maxNutrients, factory);
	}

	private static <I extends ArmorItem> DeferredItem<I> registerLivingArmorChestplate(String name, Holder<ArmorMaterial> material, int maxNutrients, LivingArmorFactory<I> factory) {
		return registerLivingArmor(name + "_chestplate", material, ArmorItem.Type.CHESTPLATE, maxNutrients, factory);
	}

	private static <I extends ArmorItem> DeferredItem<I> registerLivingArmorLeggings(String name, Holder<ArmorMaterial> material, int maxNutrients, LivingArmorFactory<I> factory) {
		return registerLivingArmor(name + "_leggings", material, ArmorItem.Type.LEGGINGS, maxNutrients, factory);
	}

	private static <I extends ArmorItem> DeferredItem<I> registerLivingArmorBoots(String name, Holder<ArmorMaterial> material, int maxNutrients, LivingArmorFactory<I> factory) {
		return registerLivingArmor(name + "_boots", material, ArmorItem.Type.BOOTS, maxNutrients, factory);
	}

	private static <I extends ArmorItem> DeferredItem<I> registerLivingArmor(String name, Holder<ArmorMaterial> material, ArmorItem.Type type, int maxNutrients, LivingArmorFactory<I> factory) {
		return ITEMS.register(name, () -> factory.create(material, type, maxNutrients, createProperties().rarity(ModRarities.VERY_RARE).durability(ModArmorMaterials.getDurability(material, type))));
	}

	private static <T extends EntityType<? extends Mob>> DeferredItem<DeferredSpawnEggItem> registerSpawnEgg(DeferredHolder<EntityType<?>, T> mobHolder, int primaryColor, int accentColor) {
		return ITEMS.register(mobHolder.getId().getPath() + "_spawn_egg", () -> new DeferredSpawnEggItem(mobHolder, primaryColor, accentColor, createProperties()));
	}

	private static <T extends Serum> DeferredItem<SerumItem> registerSerumItem(DeferredHolder<Serum, T> registryObject) {
		return ITEMS.register(registryObject.getId().getPath(), () -> new SerumItem(createProperties().stacksTo(16).rarity(ModRarities.UNCOMMON), registryObject));
	}

	private static DeferredItem<SimpleItem> registerSimpleVialItem(String name) {
		return ITEMS.register(name, () -> new SimpleItem(createProperties()));
	}

	private static DeferredItem<SimpleItem> registerSimpleItem(String name) {
		return ITEMS.register(name, () -> new SimpleItem(createProperties()));
	}

	private static DeferredItem<SimpleItem> registerSimpleItem(String name, Rarity rarity) {
		return registerSimpleItem(name, () -> createProperties().rarity(rarity));
	}

	private static Item.Properties createProperties() {
		return new Item.Properties().rarity(ModRarities.COMMON);
	}

	private static DeferredItem<SimpleItem> registerSimpleItem(String name, Supplier<Item.Properties> properties) {
		return ITEMS.register(name, () -> new SimpleItem(properties.get()));
	}

	private interface SwordSmithy<T extends SwordItem> {
		AttributeSupplier PLAYER_ATTRIBUTES = Player.createAttributes().build();

		static <T extends SwordItem> T forge(SwordSmithy<T> smithy, Tier tier, int attackDamage, float attackSpeed, Item.Properties properties) {
			int attackDamageModifier = Mth.floor(attackDamage - (PLAYER_ATTRIBUTES.getValue(Attributes.ATTACK_DAMAGE) + tier.getAttackDamageBonus()));
			float attackSpeedModifier = attackSpeed - (float) PLAYER_ATTRIBUTES.getValue(Attributes.ATTACK_SPEED);
			return smithy.forge(tier, attackDamageModifier, attackSpeedModifier, properties);
		}

		T forge(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties);
	}

	interface IBlockItemFactory<T extends Block, I extends BlockItem> {
		I create(T block, Item.Properties properties);
	}

	interface ArmorFactory<I extends ArmorItem> {
		I create(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties);
	}

	interface LivingArmorFactory<I extends ArmorItem> {
		I create(Holder<ArmorMaterial> material, ArmorItem.Type type, int maxNutrients, Item.Properties properties);
	}

}
