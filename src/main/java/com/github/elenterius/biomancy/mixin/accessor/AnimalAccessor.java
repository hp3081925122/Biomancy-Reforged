package com.github.elenterius.biomancy.mixin.accessor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Animal.class)
public interface AnimalAccessor {

	@Accessor("loveCause")
	void biomancy$setLoveCause(EntityReference<ServerPlayer> loveCause);

}
