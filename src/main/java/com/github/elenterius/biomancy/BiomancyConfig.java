package com.github.elenterius.biomancy;

import com.github.elenterius.biomancy.config.ServerConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class BiomancyConfig {

	private static final ModConfigSpec SERVER_SPECIFICATION;
	public static final ServerConfig SERVER_SYNCED;

	private BiomancyConfig() {}

	static {
		Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_SYNCED = specPair.getLeft();
		SERVER_SPECIFICATION = specPair.getRight();
	}

	public static void register(ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.SERVER, BiomancyConfig.SERVER_SPECIFICATION);
	}

}
