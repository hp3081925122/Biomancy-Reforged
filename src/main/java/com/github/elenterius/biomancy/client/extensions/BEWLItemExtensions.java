package com.github.elenterius.biomancy.client.extensions;

import com.github.elenterius.biomancy.client.render.item.BEWLItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class BEWLItemExtensions implements IClientItemExtensions {

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return BEWLItemRenderer.INSTANCE;
	}

}
