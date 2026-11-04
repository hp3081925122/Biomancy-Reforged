package com.github.elenterius.biomancy.init.client;

import com.github.elenterius.biomancy.client.render.item.ItemGeoRenderProviders;
import com.github.elenterius.biomancy.client.util.MolangUtil;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.membrane.BiometricMembraneBlock;
import com.github.elenterius.biomancy.block.vialholder.VialHolderBlock;
import com.github.elenterius.biomancy.client.gui.ScreenOverlays;
import com.github.elenterius.biomancy.client.gui.tooltip.EmptyLineClientComponent;
import com.github.elenterius.biomancy.client.gui.tooltip.HrTooltipClientComponent;
import com.github.elenterius.biomancy.client.gui.tooltip.StorageSacTooltipClientComponent;
import com.github.elenterius.biomancy.client.particle.*;
import com.github.elenterius.biomancy.client.render.block.bioforge.BioForgeRenderer;
import com.github.elenterius.biomancy.client.render.block.biolab.BioLabRenderer;
import com.github.elenterius.biomancy.client.render.block.cradle.PrimordialCradleRenderer;
import com.github.elenterius.biomancy.client.render.block.decomposer.DecomposerRenderer;
import com.github.elenterius.biomancy.client.render.block.digester.DigesterRenderer;
import com.github.elenterius.biomancy.client.render.block.fleshkinchest.FleshkinChestRenderer;
import com.github.elenterius.biomancy.client.render.block.mawhopper.MawHopperRenderer;
import com.github.elenterius.biomancy.client.render.block.storagesac.StorageSacRenderer;
import com.github.elenterius.biomancy.client.render.block.tongue.TongueRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.FleshChickenRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.FleshCowRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.FleshPigRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.FleshSheepRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.fleshblob.FleshBlobRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.fleshblob.LegacyFleshBlobRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.fleshblob.PrimordialFleshBlobRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.sheep.ChromaSheepRenderer;
import com.github.elenterius.biomancy.client.render.entity.mob.sheep.ThickFurSheepRenderer;
import com.github.elenterius.biomancy.client.render.entity.projectile.AcidSpitProjectileRenderer;
import com.github.elenterius.biomancy.client.render.entity.projectile.AcidSprayRenderer;
import com.github.elenterius.biomancy.client.render.entity.projectile.ImpalerProjectileRenderer;
import com.github.elenterius.biomancy.client.render.entity.projectile.acidblob.AcidBlobProjectileRenderer;
import com.github.elenterius.biomancy.client.render.entity.projectile.bloomberry.BloomberryProjectileRenderer;
import com.github.elenterius.biomancy.init.*;
import com.github.elenterius.biomancy.integration.ModsCompatHandler;
import com.github.elenterius.biomancy.tooltip.EmptyLineTooltipComponent;
import com.github.elenterius.biomancy.tooltip.HrTooltipComponent;
import com.github.elenterius.biomancy.tooltip.StorageSacTooltipComponent;
import com.github.elenterius.biomancy.util.TransliterationUtil;
import net.minecraft.client.particle.AttackSweepParticle;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterDebugRenderersEvent;
import com.github.elenterius.biomancy.client.render.EntityDebugInfoRenderer;
import net.minecraft.client.particle.BubblePopParticle;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.PlayerCloudParticle;
import com.github.elenterius.biomancy.client.gui.BioForgeScreen;
import com.github.elenterius.biomancy.crafting.recipe.RecipeMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.github.elenterius.biomancy.client.extensions.*;
import com.github.elenterius.biomancy.fluid.TintedFluidType;
import com.github.elenterius.biomancy.client.render.item.BlockEntityItemSpecialRenderer;
import com.github.elenterius.biomancy.item.weapon.gun.CausticGunbladeItem;
import com.github.elenterius.biomancy.item.weapon.gun.ImpalerItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Supplier;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID, value = Dist.CLIENT)
public final class ClientSetupHandler {

	private ClientSetupHandler() {}

	@SubscribeEvent
	public static void onSetup(final FMLClientSetupEvent event) {
		ItemGeoRenderProviders.register();

		event.enqueueWork(ClientSetupHandler::onPostSetup);

		TransliterationUtil.init();

		ModsCompatHandler.onBiomancyClientSetup(event);
	}

	private static void onPostSetup() {
		ModArmPoses.init();
		MolangUtil.registerVariables();
	}

	@SubscribeEvent
	public static void onRegisterDebugRenderers(final RegisterDebugRenderersEvent event) {
		event.register(EntityDebugInfoRenderer::new);
	}

	@SubscribeEvent
	public static void onRegisterMenuScreens(final RegisterMenuScreensEvent event) {
		ModScreens.registerMenuScreens(event);
	}

	@SubscribeEvent
	public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
		ModKeyBindings.register(event);
	}

	@SubscribeEvent
	public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModBlockEntities.PRIMORDIAL_CRADLE.get(), PrimordialCradleRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.DECOMPOSER.get(), DecomposerRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.DIGESTER.get(), DigesterRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.BIO_FORGE.get(), BioForgeRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.BIO_LAB.get(), BioLabRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.TONGUE.get(), TongueRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.FLESHKIN_CHEST.get(), FleshkinChestRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.STORAGE_SAC.get(), StorageSacRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.MAW_HOPPER.get(), MawHopperRenderer::new);

		event.registerEntityRenderer(ModEntityTypes.HUNGRY_FLESH_BLOB.get(), FleshBlobRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.FLESH_BLOB.get(), FleshBlobRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.LEGACY_FLESH_BLOB.get(), LegacyFleshBlobRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.PRIMORDIAL_FLESH_BLOB.get(), PrimordialFleshBlobRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.PRIMORDIAL_HUNGRY_FLESH_BLOB.get(), PrimordialFleshBlobRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.FLESH_COW.get(), FleshCowRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.FLESH_SHEEP.get(), FleshSheepRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.FLESH_PIG.get(), FleshPigRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.FLESH_CHICKEN.get(), FleshChickenRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.CHROMA_SHEEP.get(), ChromaSheepRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.THICK_FUR_SHEEP.get(), ThickFurSheepRenderer::new);

		event.registerEntityRenderer(ModEntityTypes.ACID_SPIT_PROJECTILE.get(), AcidSpitProjectileRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.TOOTH_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.IMPALER_PROJECTILE.get(), ImpalerProjectileRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.BLOOMBERRY_PROJECTILE.get(), BloomberryProjectileRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.ACID_BLOB_PROJECTILE.get(), AcidBlobProjectileRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.GRENADE_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.ACIDIC_EGG_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.ACID_SPRAY_PROJECTILE.get(), AcidSprayRenderer::new);

		event.registerEntityRenderer(ModEntityTypes.GAS_CLOUD.get(), NoopRenderer::new);
	}

	@SubscribeEvent
	public static void registerParticles(final RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(ModParticleTypes.BLOODY_CLAWS_ATTACK.get(), AttackSweepParticle.Provider::new);
		event.registerSpriteSet(ModParticleTypes.FALLING_BLOOD.get(), BloodDripParticle.FallingBloodFactory::new);
		event.registerSpriteSet(ModParticleTypes.LANDING_BLOOD.get(), BloodDripParticle.LandingBloodFactory::new);
		event.registerSpriteSet(ModParticleTypes.CORROSIVE_SWIPE_ATTACK.get(), AttackSweepParticle.Provider::new);
		event.registerSpriteSet(ModParticleTypes.DRIPPING_ACID.get(), ParticleProviders.AcidHangProvider::new);
		event.registerSpriteSet(ModParticleTypes.FALLING_ACID.get(), ParticleProviders.AcidFallProvider::new);
		event.registerSpriteSet(ModParticleTypes.LANDING_ACID.get(), ParticleProviders.AcidLandProvider::new);
		event.registerSpriteSet(ModParticleTypes.PINK_GLOW.get(), sprites -> new CustomGlowParticle.TwoColorProvider(sprites, 0xf740fd, 0xff6fff));
		event.registerSpriteSet(ModParticleTypes.LIGHT_GREEN_GLOW.get(), sprites -> new CustomGlowParticle.TwoColorProvider(sprites, 0x53ff53, 0x64e986));
		event.registerSpriteSet(ModParticleTypes.HOSTILE.get(), CustomGlowParticle.GenericProvider::new);
		event.registerSpriteSet(ModParticleTypes.BIOHAZARD.get(), sprites -> new CustomGlowParticle.TwoColorProvider(sprites, 0xab274f, 0x7e2a43));
		event.registerSpriteSet(ModParticleTypes.ACID_BUBBLE.get(), sprites -> new BubbleParticle.AcidProvider(sprites, ModParticleTypes.ACID_BUBBLE_POP.get()));
		event.registerSpriteSet(ModParticleTypes.ACID_BUBBLE_POP.get(), BubblePopParticle.Provider::new);
		event.registerSpriteSet(ModParticleTypes.VOLATILE_BUBBLE.get(), sprites -> new BubbleParticle.VolatileProvider(sprites, ModParticleTypes.VOLATILE_BUBBLE_POP.get()));
		event.registerSpriteSet(ModParticleTypes.VOLATILE_BUBBLE_POP.get(), BubblePopParticle.Provider::new);
		event.registerSpriteSet(ModParticleTypes.TOXIN_GAS.get(), PlayerCloudParticle.Provider::new);
		registerSimpleExplosionEmitter(event, ModParticleTypes.TOXIN_GAS_EXPLOSION_EMITTER, ModParticleTypes.TOXIN_GAS_EXPLOSION);
		registerSimpleExplosionEmitter(event, ModParticleTypes.DECAY_EXPLOSION_EMITTER, ModParticleTypes.DECAY_EXPLOSION);
		registerSimpleExplosionEmitter(event, ModParticleTypes.ACID_EXPLOSION_EMITTER, ModParticleTypes.ACID_EXPLOSION);
		registerSimpleExplosionEmitter(event, ModParticleTypes.VOLATILE_EXPLOSION_EMITTER, ModParticleTypes.VOLATILE_EXPLOSION);
	}

	private static <T extends SimpleParticleType> void registerSimpleExplosionEmitter(RegisterParticleProvidersEvent event, Supplier<T> emitter, Supplier<T> particle) {
		event.registerSpriteSet(particle.get(), HugeExplosionParticle.Provider::new);
		event.registerSpecial(emitter.get(), new SimpleExplosionParticleEmitter.Provider(particle.get()));
	}

	@SubscribeEvent
	public static void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
	}

	@SubscribeEvent
	public static void registerLayers(final EntityRenderersEvent.AddLayers event) {
	}

	@SubscribeEvent
	public static void onItemTintSourceRegistry(final RegisterColorHandlersEvent.ItemTintSources event) {
		ModTintSources.registerItemTintSources(event);
	}

	@SubscribeEvent
	public static void onBlockTintSourceRegistry(final RegisterColorHandlersEvent.BlockTintSources event) {
		ModTintSources.registerBlockTintSources(event);
	}

	@SubscribeEvent
	public static void registerFluidModels(final RegisterFluidModelsEvent event) {
		event.register(TexturedFluidTypeExtensions.createTranslucentFluidModel("acid"), ModFluids.ACID, ModFluids.FLOWING_ACID);
		for (Fluid fluid : BuiltInRegistries.FLUID) {
			if (fluid.getFluidType() instanceof TintedFluidType tintedFluidType) {
				event.register(TintedFluidTypeExtensions.createFluidModel(tintedFluidType.getTintColor()), fluid);
			}
		}
	}

	@SubscribeEvent
	public static void onRecipesReceived(final RecipesReceivedEvent event) {
		RecipeMaps.setClientRecipeMap(event.getRecipeMap());
		if (Minecraft.getInstance().screen instanceof BioForgeScreen screen) screen.onRecipeBookUpdated();
	}

	@SubscribeEvent
	public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
		RecipeMaps.setClientRecipeMap(RecipeMap.EMPTY);
	}

	@SubscribeEvent
	public static void registerGameOverlays(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, BiomancyMod.rl("frenzy"), ScreenOverlays.FRENZY_OVERLAY);
		event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS, BiomancyMod.rl("inside_block"), ScreenOverlays.INSIDE_BLOCK_OVERLAY);

		event.registerAbove(VanillaGuiLayers.CROSSHAIR, BiomancyMod.rl("injector"), ScreenOverlays.INJECTOR_OVERLAY);
		event.registerAbove(VanillaGuiLayers.CROSSHAIR, BiomancyMod.rl("charge_bar"), ScreenOverlays.CHARGE_BAR_OVERLAY);
		event.registerAbove(VanillaGuiLayers.CROSSHAIR, BiomancyMod.rl("gun"), ScreenOverlays.GUN_OVERLAY);
		event.registerAbove(VanillaGuiLayers.CROSSHAIR, BiomancyMod.rl("knowledge"), ScreenOverlays.KNOWLEDGE_OVERLAY);
	}

	@SubscribeEvent
	public static void registerClientExtensions(final RegisterClientExtensionsEvent event) {
		event.registerFluidType(new TexturedFluidTypeExtensions("acid"), ModFluids.ACID_TYPE.get());
		for (FluidType fluidType : NeoForgeRegistries.FLUID_TYPES) {
			if (fluidType instanceof TintedFluidType tintedFluidType) {
				event.registerFluidType(new TintedFluidTypeExtensions(tintedFluidType.getTintColor()), tintedFluidType);
			}
		}

		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof ImpalerItem) {
				event.registerItem(new ImpalerItemExtensions(), item);
			}
			else if (item instanceof CausticGunbladeItem) {
				event.registerItem(new CausticGunbladeItemExtensions(), item);
			}
		}
	}

	@SubscribeEvent
	static void registerSpecialModelRenderers(final RegisterSpecialModelRendererEvent event) {
		event.register(BlockEntityItemSpecialRenderer.ID, BlockEntityItemSpecialRenderer.Unbaked.MAP_CODEC);
	}

	@SubscribeEvent
	static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(HrTooltipComponent.class, HrTooltipClientComponent::new);
		event.register(EmptyLineTooltipComponent.class, EmptyLineClientComponent::new);
		event.register(StorageSacTooltipComponent.class, StorageSacTooltipClientComponent::new);
	}

}
