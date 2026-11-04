package com.github.elenterius.biomancy.mixin.accessor;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractHorse.class)
public interface AbstractHorseAccessor {

	@Accessor("owner")
	void biomancy$setOwnerReference(@Nullable EntityReference<LivingEntity> owner);

}
