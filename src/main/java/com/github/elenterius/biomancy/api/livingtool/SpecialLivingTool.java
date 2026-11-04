package com.github.elenterius.biomancy.api.livingtool;

import java.util.function.Consumer;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.sounds.SoundUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Experimental
public interface SpecialLivingTool extends LivingTool {

	default LivingToolState getLivingToolState(ItemStack livingTool) {
		return livingTool.getOrDefault(ModDataComponents.LIVING_TOOL_STATE, LivingToolState.BROKEN);
	}

	default void setLivingToolState(ItemStack livingTool, LivingToolState state) {
		livingTool.set(ModDataComponents.LIVING_TOOL_STATE, state);
	}

	default void updateLivingToolState(ItemStack livingTool, ServerLevel level, Player player) {
		LivingToolState state = getLivingToolState(livingTool);
		boolean hasNutrients = hasNutrients(livingTool);

		if (state == LivingToolState.DORMANT) {
			setLivingToolState(livingTool, hasNutrients ? LivingToolState.AWAKENED : LivingToolState.BROKEN);
			SoundUtil.Server.playItemSound(level, player, ModSoundEvents.FLESH_BLOCK_PLACE.get());
		}
		else if (state == LivingToolState.AWAKENED) {
			setLivingToolState(livingTool, hasNutrients ? LivingToolState.DORMANT : LivingToolState.BROKEN);
			SoundUtil.Server.playItemSound(level, player, ModSoundEvents.FLESH_BLOCK_HIT.get());
		}
	}

	@Override
	default void onNutrientsChanged(ItemStack livingTool, int oldValue, int newValue) {
		LivingToolState prevState = getLivingToolState(livingTool);
		LivingToolState state = prevState;

		if (newValue <= 0) {
			if (state != LivingToolState.BROKEN) setLivingToolState(livingTool, LivingToolState.BROKEN);
			return;
		}

		if (state == LivingToolState.BROKEN) {
			state = LivingToolState.DORMANT;
		}

		int maxCost = getLivingToolMaxActionCost(livingTool, state);

		if (newValue < maxCost) {
			if (state == LivingToolState.AWAKENED) state = LivingToolState.DORMANT;
			else if (state == LivingToolState.DORMANT) state = LivingToolState.BROKEN;
		}

		if (state != prevState) setLivingToolState(livingTool, state);
	}

	default int getLivingToolActionCost(ItemStack livingTool, ItemAbility itemAbility) {
		LivingToolState state = getLivingToolState(livingTool);
		return getLivingToolActionCost(livingTool, state, itemAbility);
	}

	default int getLivingToolActionCost(ItemStack livingTool, LivingToolState state, ItemAbility itemAbility) {
		return switch (state) {
			case BROKEN -> 0;
			case DORMANT -> 1;
			case AWAKENED -> 2;
		};
	}

	default int getLivingToolMaxActionCost(ItemStack livingTool, LivingToolState state) {
		return ItemAbility.getActions().stream()
				.filter(livingTool::canPerformAction)
				.map(itemAbility -> getLivingToolActionCost(livingTool, state, itemAbility))
				.max(Integer::compareTo)
				.orElse(0);
	}

	default void appendLivingToolTooltip(ItemStack stack, Consumer<Component> tooltip) {
		tooltip.accept(getLivingToolState(stack).getTooltip().withStyle(TextStyles.ITALIC_GRAY));
		tooltip.accept(ComponentUtil.EMPTY_LINE);
		LivingTool.super.appendLivingToolTooltip(stack, tooltip);
	}

}
