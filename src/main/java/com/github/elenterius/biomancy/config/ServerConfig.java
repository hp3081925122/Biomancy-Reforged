package com.github.elenterius.biomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {

	public final ModConfigSpec.BooleanValue doBioForgeRecipeProgression;
	public final ModConfigSpec.BooleanValue doTransliteration;
	public final ModConfigSpec.BooleanValue addTradesToVillagers;
	public final ModConfigSpec.BooleanValue addTradesToWanderingTrader;
	public final ModConfigSpec.EnumValue<PrimalEnergySettings.SupplyAmount> primalEnergySupplyOfCradle;

	public final ModConfigSpec.DoubleValue absorptionMaxHearts;
	public final ModConfigSpec.DoubleValue absorptionHearts;

	public ServerConfig(ModConfigSpec.Builder builder) {
		builder.push("recipes");
		doBioForgeRecipeProgression = builder
				.comment("Determines if the BioForge recipes need to be unlocked to be able to craft them")
				.define("doBioForgeRecipeProgression", true);
		builder.pop();

		builder.push("transliteration");
		doTransliteration = builder
				.comment(
						"Determines if clients (afflicted by primordial infestation) transliterate non-latin characters before sending text (chat messages, sign editing, item renaming) to the server.",
						"Disabling this feature makes only sense if you use a resource pack that expands the 'Caro Invitica' font with the missing non-latin characters from your language."
				)
				.define("doTransliteration", true);
		builder.pop();

		builder.push("trades");
		addTradesToVillagers = builder
				.comment("Determines if villagers will sell biomancy items")
				.define("addTradesToVillagers", true);

		addTradesToWanderingTrader = builder
				.comment("Determines if wandering traders will sell biomancy items")
				.define("addTradesToWanderingTrader", true);
		builder.pop();

		builder.push("flesh-growth");
		primalEnergySupplyOfCradle = builder
				.comment("Determines how much primal energy the Cradle can supply to nearby malignant flesh veins")
				.defineEnum("primalEnergySupplyOfCradle", PrimalEnergySettings.SupplyAmount.LIMITED);
		builder.pop();

		builder.push("absorption-serum");
		absorptionMaxHearts = builder
				.comment("Maximum number of absorption hearts.")
				.defineInRange("maxHearts", 10d, 0.5d, 1000d);
		absorptionHearts = builder
				.comment("How many absorption hearts to add on each injection.")
				.defineInRange("heartsIncrement", 2d, 0.5d, 100d);
		builder.pop();
	}

}
