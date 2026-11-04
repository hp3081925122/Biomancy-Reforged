package com.github.elenterius.biomancy.integration.kubejs;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.nutrients.Nutrients;
import com.github.elenterius.biomancy.api.serum.Serum;
import com.github.elenterius.biomancy.api.tribute.SimpleTribute;
import com.github.elenterius.biomancy.api.tribute.Tributes;
import com.github.elenterius.biomancy.block.cradle.PrimordialCradleEvents;
import com.github.elenterius.biomancy.crafting.EssenceIngredient;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.VariableOutput;
import com.github.elenterius.biomancy.crafting.recipe.RecipeUtil;
import com.github.elenterius.biomancy.entity.mob.fleshblob.FleshBlob;
import com.github.elenterius.biomancy.init.ModBioForgeTabs;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.init.ModSerums;
import com.github.elenterius.biomancy.item.EssenceItem;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.util.ID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;

public class BiomancyKubeJSPlugin implements KubeJSPlugin {

	public static final Logger LOGGER = LogManager.getLogger("Biomancy KubeJS Plugin");

	@Override
	public void init() {
		NeoForge.EVENT_BUS.addListener(BiomancyKJSEvents::canCradleSpawnMob);
		NeoForge.EVENT_BUS.addListener(BiomancyKJSEvents::onCradleSpawnMob);
	}

	@Override
	public void registerBuilderTypes(BuilderTypeRegistry registry) {
		registry.<Serum>of(ResourceKey.createRegistryKey(ModSerums.SERUMS.getRegistryName()), callback -> {
			callback.addDefault(SerumBuilder.class, SerumBuilder::new);
			callback.add(ID.kjs("basic"), SerumBuilder.class, SerumBuilder::new);
		});

		registry.of(Registries.ITEM, callback -> callback.add(BiomancyMod.rl("basic_serum"), SerumItemBuilder.class, SerumItemBuilder::new));

		registry.<BioForgeTab>of(ResourceKey.createRegistryKey(ModBioForgeTabs.REGISTRY_KEY), callback -> {
			callback.addDefault(BioForgeTabBuilder.class, BioForgeTabBuilder::new);
			callback.add(ID.kjs("basic"), BioForgeTabBuilder.class, BioForgeTabBuilder::new);
		});
	}

	@Override
	public void registerEvents(EventGroupRegistry registry) {
		registry.register(BiomancyKJSEvents.GROUP);
	}

	@Override
	public void registerClasses(ClassFilter filter) {
		filter.allow("com.github.elenterius.biomancy");

		filter.deny("com.github.elenterius.biomancy.integration");
		filter.deny("com.github.elenterius.biomancy.mixin");
		filter.deny("com.github.elenterius.biomancy.network");
		filter.deny("com.github.elenterius.spatialdb");
		filter.deny("com.github.elenterius.geckolibextras");
	}

	@Override
	public void registerBindings(BindingRegistry bindings) {
		bindings.add("Biomancy$EssenceIngredient", EssenceIngredientUtil.class);
		bindings.add("Biomancy$EssenceItem", EssenceItemUtil.class);
		bindings.add("Biomancy$Nutrients", Nutrients.class);
		bindings.add("Biomancy$Tributes", Tributes.class);
		bindings.add("Biomancy$SimpleTribute", SimpleTribute.class);
		bindings.add("Biomancy$FleshBlob", FleshBlob.class);
		bindings.add("Biomancy$CradleEvent$CanSpawnMob", PrimordialCradleEvents.CanSpawnMob.class);
		bindings.add("Biomancy$CradleEvent$OnSpawnMob", PrimordialCradleEvents.OnSpawnMob.class);
	}

	@Override
	public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
		registry.unit(VariableOutputComponent.TYPE, VariableOutputComponent.INSTANCE);
	}

	@Override
	public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
		LOGGER.info("Registering Recipe Schemas...");

		registry.register(ModRecipes.DIGESTING_RECIPE_TYPE.getId(), Schemas.DIGESTING);
		registry.register(ModRecipes.BIO_BREWING_RECIPE_TYPE.getId(), Schemas.BIO_BREWING);
		registry.register(ModRecipes.BIO_FORGING_RECIPE_TYPE.getId(), Schemas.BIO_FORGING);
		registry.register(ModRecipes.DECOMPOSING_RECIPE_TYPE.getId(), Schemas.DECOMPOSING);
	}

	interface Components {
		RecipeComponent<SizedIngredient> INGREDIENT_STACK = SizedIngredientComponent.SIZED_INGREDIENT.withCodec(IngredientStack.CODEC.xmap(
				stack -> new SizedIngredient(stack.ingredient(), stack.count()),
				sized -> new IngredientStack(sized.ingredient(), sized.count())
		));
	}

	interface Keys {
		RecipeKey<Ingredient> INGREDIENT = IngredientComponent.INGREDIENT.inputKey(RecipeUtil.JsonKeys.INGREDIENT);
		RecipeKey<SizedIngredient> INGREDIENT_STACK = Components.INGREDIENT_STACK.inputKey(RecipeUtil.JsonKeys.INGREDIENT);
		RecipeKey<List<SizedIngredient>> INGREDIENT_STACKS = Components.INGREDIENT_STACK.asList().inputKey(RecipeUtil.JsonKeys.INGREDIENTS);
		RecipeKey<Ingredient> REACTANT = IngredientComponent.INGREDIENT.inputKey(RecipeUtil.JsonKeys.REACTANT);

		RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey(RecipeUtil.JsonKeys.RESULT);
		RecipeKey<List<VariableOutput>> RESULTS = VariableOutputComponent.INSTANCE.asList().outputKey(RecipeUtil.JsonKeys.RESULTS);

		RecipeKey<String> BIO_FORGE_TAB = StringComponent.ID.otherKey(RecipeUtil.JsonKeys.BIO_FORGE_TAB);

		RecipeKey<Integer> PROCESSING_TIME = NumberComponent.INT.otherKey(RecipeUtil.JsonKeys.PROCESSING_TIME).defaultOptional();
		RecipeKey<Integer> NUTRIENTS_COST = NumberComponent.INT.otherKey(RecipeUtil.JsonKeys.NUTRIENTS_COST).defaultOptional();
	}

	interface Schemas {
		RecipeSchema DIGESTING = new RecipeSchema(Keys.INGREDIENT, Keys.RESULT, Keys.PROCESSING_TIME, Keys.NUTRIENTS_COST);
		RecipeSchema BIO_BREWING = new RecipeSchema(Keys.INGREDIENT_STACKS, Keys.REACTANT, Keys.RESULT, Keys.PROCESSING_TIME, Keys.NUTRIENTS_COST);
		RecipeSchema BIO_FORGING = new RecipeSchema(Keys.INGREDIENT_STACKS, Keys.RESULT, Keys.BIO_FORGE_TAB, Keys.NUTRIENTS_COST);
		RecipeSchema DECOMPOSING = new RecipeSchema(Keys.INGREDIENT_STACK, Keys.RESULTS, Keys.PROCESSING_TIME, Keys.NUTRIENTS_COST);
	}

	interface EssenceIngredientUtil {

		@Info(
				value = "Creates a essence ingredient that matches a specific tier",
				params = {
						@Param(name = "entityType"),
						@Param(name = "tier", value = "The tier that this essence ingredient requires. Valid tiers are: 0, 1, 2 or 3")
				}
		)
		static Ingredient fromTier(EntityType<?> entityType, int tier) {
			BiomancyKubeJSPlugin.LOGGER.warn("Creating EssenceIngredient for {} with tier {}", entityType.getDescriptionId(), tier);
			return EssenceIngredient.of(entityType, tier);
		}

		@Info(
				value = "Creates a essence ingredient that matches any tier",
				params = {@Param(name = "entityType")}
		)
		static Ingredient from(EntityType<?> entityType) {
			BiomancyKubeJSPlugin.LOGGER.warn("Creating EssenceIngredient for {} with tier -1", entityType.getDescriptionId());
			return EssenceIngredient.of(entityType);
		}

	}

	interface EssenceItemUtil {

		@Info(
				value = "Creates a tier 1 essence from the EntityType of a LivingEntity",
				params = {@Param(name = "entityType")}
		)
		static ItemStack from(EntityType<?> entityType) {
			return EssenceItem.fromEntityType(entityType, 1);
		}

		@Info(
				value = "Creates a tier x essence from the EntityType of a LivingEntity",
				params = {
						@Param(name = "entityType"),
						@Param(name = "tier", value = "Quality tier of the essence. Valid tiers are: 1, 2 or 3")
				}
		)
		static ItemStack fromTier(EntityType<?> entityType, int tier) {
			return EssenceItem.fromEntityType(entityType, tier);
		}

		@Info(
				value = "Creates a unique essence from the EntityType of a LivingEntity",
				params = {
						@Param(name = "entityType"),
						@Param(name = "uuid", value = "UUID of the LivingEntity")
				}
		)
		static ItemStack fromUUID(EntityType<?> entityType, UUID uuid) {
			return EssenceItem.fromEntityType(entityType, uuid);
		}

		@Info(
				value = "Creates a tier 1 or 2 essence from a LivingEntity",
				params = {@Param(name = "livingEntity")}
		)
		static ItemStack fromLiving(LivingEntity livingEntity) {
			return EssenceItem.fromEntity(livingEntity, 0, 0);
		}

		@Info(
				value = "Creates a tier x essence from a LivingEntity. The tier depends on the enchantment level of surgical precision.",
				params = {
						@Param(name = "livingEntity"),
						@Param(name = "surgicalPrecisionLevel", value = "Level of surgical precision enchantment"),
						@Param(name = "lootingLevel", value = "Level of looting enchantment")
				}
		)
		static ItemStack fromLivingWith(LivingEntity livingEntity, int surgicalPrecisionLevel, int lootingLevel) {
			return EssenceItem.fromEntity(livingEntity, surgicalPrecisionLevel, lootingLevel);
		}

	}

}
