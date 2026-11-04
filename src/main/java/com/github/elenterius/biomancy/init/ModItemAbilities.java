package com.github.elenterius.biomancy.init;

import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Set;

public final class ModItemAbilities {

	public static final ItemAbility SWORD_DIG = ItemAbility.get("sword_dig");
	public static final Set<ItemAbility> DEFAULT_SWORD_ACTIONS = Set.of(SWORD_DIG, ItemAbilities.SWORD_SWEEP);

	private ModItemAbilities() {}

}
