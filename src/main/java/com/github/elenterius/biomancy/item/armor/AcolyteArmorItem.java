package com.github.elenterius.biomancy.item.armor;

import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import com.github.elenterius.biomancy.item.KnowledgeReader;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.client.GeoRenderProvider;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public final class AcolyteArmorItem extends LivingArmorGeoItem implements KnowledgeReader, ItemTooltipStyleProvider {

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public AcolyteArmorItem(ArmorMaterial material, ArmorType type, int maxNutrients, Properties properties) {
		super(material, type, maxNutrients, properties);
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	@Override
	public boolean canShowKnowledgeOverlay(ItemStack stack, Player player) {
		return AcolyteArmorUpgrades.hasUpgrade(stack, AcolyteArmorUpgrades.PRIMORDIAL_SIGHT) && hasNutrients(stack);
	}

	@Override
	public boolean canTranslatePrimordialRunes(ItemStack stack, Player player) {
		return canShowKnowledgeOverlay(stack, player);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		AcolyteArmorUpgrades.appendHoverText(stack, tooltip);

		tooltip.accept(ComponentUtil.EMPTY_LINE);
		tooltip.accept(TextComponentUtil.getAbilityText("fleshkin_affinity").withStyle(ChatFormatting.GRAY));
		tooltip.accept(ComponentUtil.literal(" ").append(TextComponentUtil.getAbilityText("fleshkin_affinity.desc")).withStyle(ChatFormatting.DARK_GRAY));

		tooltip.accept(ComponentUtil.EMPTY_LINE);
		tooltip.accept(TextComponentUtil.getAbilityText("bio_alchemical_epidermis").withStyle(ChatFormatting.GRAY));
		tooltip.accept(ComponentUtil.literal(" ").append(TextComponentUtil.getAbilityText("bio_alchemical_epidermis.desc")).withStyle(ChatFormatting.DARK_GRAY));

		tooltip.accept(ComponentUtil.EMPTY_LINE);

		appendLivingToolTooltip(stack, tooltip);

		if (stack.isEnchanted()) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);
		}
	}

}