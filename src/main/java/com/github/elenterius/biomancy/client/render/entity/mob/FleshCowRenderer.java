package com.github.elenterius.biomancy.client.render.entity.mob;

import com.github.elenterius.biomancy.entity.mob.FleshCow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class FleshCowRenderer<T extends FleshCow> extends FleshAnimalRenderer<T> {

	public FleshCowRenderer(EntityRendererProvider.Context context) {
		super(context, new FleshCowModel<>(), 0.7f);
	}

}
