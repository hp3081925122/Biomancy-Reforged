package com.github.elenterius.biomancy.item;

import com.github.elenterius.biomancy.styles.TextStyles;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.function.Consumer;

public class MaykerBannerPatternItem extends Item {

	public MaykerBannerPatternItem(TagKey<BannerPattern> bannerPattern, Properties properties) {
		super(properties.delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(bannerPattern)));
	}

	public MutableComponent getDisplayName() {
		return Component.translatable(getDescriptionId() + ".desc");
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(getDisplayName().withStyle(TextStyles.PRIMORDIAL_RUNES_GRAY));
	}

}
