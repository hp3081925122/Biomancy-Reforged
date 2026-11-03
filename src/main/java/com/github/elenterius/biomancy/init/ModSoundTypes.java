package com.github.elenterius.biomancy.init;

import net.neoforged.neoforge.common.util.DeferredSoundType;

public final class ModSoundTypes {

	public static final DeferredSoundType FLESH_BLOCK = new DeferredSoundType(1f, 1f,
			ModSoundEvents.FLESH_BLOCK_BREAK, ModSoundEvents.FLESH_BLOCK_STEP,
			ModSoundEvents.FLESH_BLOCK_PLACE, ModSoundEvents.FLESH_BLOCK_HIT, ModSoundEvents.FLESH_BLOCK_FALL);

	public static final DeferredSoundType BONY_FLESH_BLOCK = new DeferredSoundType(1f, 1f,
			ModSoundEvents.BONY_FLESH_BLOCK_BREAK, ModSoundEvents.BONY_FLESH_BLOCK_STEP,
			ModSoundEvents.BONY_FLESH_BLOCK_PLACE, ModSoundEvents.BONY_FLESH_BLOCK_HIT, ModSoundEvents.BONY_FLESH_BLOCK_FALL);

	public static final DeferredSoundType GEL_BLOCK = new DeferredSoundType(1f, 1f,
			ModSoundEvents.GEL_BLOCK_BREAK, ModSoundEvents.GEL_BLOCK_STEP,
			ModSoundEvents.GEL_BLOCK_PLACE, ModSoundEvents.GEL_BLOCK_HIT, ModSoundEvents.GEL_BLOCK_FALL);

	private ModSoundTypes() {}

}
