package com.github.elenterius.biomancy.integration;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.integration.alexscaves.AlexsCavesCompat;
import com.github.elenterius.biomancy.integration.farmersdelight.FarmersDelightCompat;
import com.github.elenterius.biomancy.integration.iceandfire.IceAndFireCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public final class ModsCompatHandler {

	static final Marker LOG_MARKER = MarkerManager.getMarker(ModsCompatHandler.class.getSimpleName());

	private ModsCompatHandler() {}

	public static void onBiomancyInit(final IEventBus eventBus) {
		if (ModList.get().isLoaded("nerb")) {
			BiomancyMod.LOGGER.fatal(LOG_MARKER, "Detected incompatible mods: Biomancy is not compatible with 'Not Enough Recipe Book (NERB)'. Please remove/replace NERB with ORB.");
		}
	}

	public static void onBiomancyCommonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			if (ModList.get().isLoaded("farmersdelight")) {
				BiomancyMod.LOGGER.info(LOG_MARKER, "Setting up Farmer's Delight compat...");
				FarmersDelightCompat.onPostSetup();
			}

			if (ModList.get().isLoaded("alexscaves")) {
				BiomancyMod.LOGGER.info(LOG_MARKER, "Setting up Alex's Caves compat...");
				AlexsCavesCompat.onPostSetup();
			}

			if (ModList.get().isLoaded("iceandfire")) {
				BiomancyMod.LOGGER.info(LOG_MARKER, "Setting up Ice And Fire compat...");
				IceAndFireCompat.onPostSetup();
			}
		});
	}

	public static void onBiomancyClientSetup(final FMLClientSetupEvent event) {
	}

}
