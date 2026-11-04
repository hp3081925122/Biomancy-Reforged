package com.github.elenterius.biomancy.serum;

import java.util.function.Consumer;
import com.github.elenterius.biomancy.BiomancyConfig;
import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AbsorptionSerum extends BasicSerum {

	public static final Identifier MAX_ABSORPTION_MODIFIER_ID = BiomancyMod.rl("absorption_serum");

	public AbsorptionSerum(int color) {
		super(color);
	}

	@Override
	public void affectEntity(ServerLevel level, CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		addAbsorption(target);
	}

	@Override
	public void affectPlayerSelf(ServerLevel level, CompoundTag tag, ServerPlayer targetSelf) {
		addAbsorption(targetSelf);
	}

	private void addAbsorption(LivingEntity target) {
		float absorptionAmount = target.getAbsorptionAmount();
		float maxHearts = getMaxHearts();
		if (absorptionAmount < maxHearts * 2f) {
			AttributeInstance maxAbsorption = target.getAttribute(Attributes.MAX_ABSORPTION);
			if (maxAbsorption != null) {
				maxAbsorption.addOrReplacePermanentModifier(new AttributeModifier(MAX_ABSORPTION_MODIFIER_ID, maxHearts * 2f, AttributeModifier.Operation.ADD_VALUE));
			}
			target.setAbsorptionAmount(Math.min(maxHearts * 2f, absorptionAmount + getHearts() * 2));
		}
	}

	@Override
	public void appendTooltip(CompoundTag tag, @Nullable Level level, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(ComponentUtil.translatable(getDescriptionTranslationKey(), getHearts(), getMaxHearts()).withStyle(TextStyles.LORE));
	}

	public static void removeMaxAbsorptionIfDepleted(LivingEntity livingEntity) {
		if (livingEntity.getAbsorptionAmount() > 0f) return;
		AttributeInstance maxAbsorption = livingEntity.getAttribute(Attributes.MAX_ABSORPTION);
		if (maxAbsorption != null) maxAbsorption.removeModifier(MAX_ABSORPTION_MODIFIER_ID);
	}

	protected float getHearts() {
		return BiomancyConfig.SERVER_SYNCED.absorptionHearts.get().floatValue();
	}

	protected float getMaxHearts() {
		return BiomancyConfig.SERVER_SYNCED.absorptionMaxHearts.get().floatValue();
	}

}
