package com.github.elenterius.biomancy.item.armor;

import java.util.function.Consumer;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
public final class AcolyteArmorUpgrades {

	private static final List<Upgrade> UPGRADES = new ArrayList<>();

	public static final Upgrade PRIMORDIAL_SIGHT = register("primordial_sight");

	private AcolyteArmorUpgrades() {}

	public static Upgrade register(Identifier name) {
		Upgrade upgrade = new Upgrade(name);
		UPGRADES.add(upgrade);
		return upgrade;
	}

	private static Upgrade register(String name) {
		Upgrade upgrade = new Upgrade(BiomancyMod.rl(name));
		UPGRADES.add(upgrade);
		return upgrade;
	}

	public static ItemStack addUpgrade(ItemStack stack, Upgrade upgrade) {
		List<Identifier> upgrades = stack.getOrDefault(ModDataComponents.ARMOR_UPGRADES, List.of());
		if (!upgrades.contains(upgrade.id)) {
			List<Identifier> newUpgrades = new ArrayList<>(upgrades);
			newUpgrades.add(upgrade.id);
			stack.set(ModDataComponents.ARMOR_UPGRADES, List.copyOf(newUpgrades));
		}
		return stack;
	}

	public static boolean hasUpgrade(ItemStack stack, Upgrade upgrade) {
		return stack.getOrDefault(ModDataComponents.ARMOR_UPGRADES, List.of()).contains(upgrade.id);
	}

	public static void appendHoverText(ItemStack stack, Consumer<Component> tooltip) {
		for (Upgrade upgrade : UPGRADES) {
			if (hasUpgrade(stack, upgrade)) {
				tooltip.accept(ComponentUtil.EMPTY_LINE);
				upgrade.appendHoverText(stack, tooltip);
			}
		}
	}

	public static class Upgrade {

		private final Identifier id;

		public Upgrade(Identifier id) {
			this.id = id;
		}

		public Identifier getId() {
			return id;
		}

		public void appendHoverText(ItemStack stack, Consumer<Component> tooltip) {
			String translationKey = id.getPath();
			tooltip.accept(TextComponentUtil.getAbilityText(translationKey).withStyle(TextStyles.GRAY));
			tooltip.accept(ComponentUtil.literal(" ").append(TextComponentUtil.getAbilityText(translationKey + ".desc")).withStyle(TextStyles.DARK_GRAY));
		}

	}

}