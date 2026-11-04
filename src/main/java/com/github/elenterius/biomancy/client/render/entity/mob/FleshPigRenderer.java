package com.github.elenterius.biomancy.client.render.entity.mob;

import com.github.elenterius.biomancy.entity.mob.FleshPig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class FleshPigRenderer<T extends FleshPig> extends FleshAnimalRenderer<T> {

	public FleshPigRenderer(EntityRendererProvider.Context context) {
		super(context, new FleshPigModel<>(), 0.7f);
	}

}
