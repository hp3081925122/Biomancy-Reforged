package com.github.elenterius.biomancy.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;

public final class InteractionUtil {

	private InteractionUtil() {}

	public static ItemInteractionResult toItemInteractionResult(InteractionResult result) {
		return switch (result) {
			case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
			case CONSUME -> ItemInteractionResult.CONSUME;
			case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
			case FAIL -> ItemInteractionResult.FAIL;
			case PASS -> ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
		};
	}

}
