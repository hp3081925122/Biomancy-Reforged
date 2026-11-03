package com.github.elenterius.biomancy.item.armor;

import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.client.render.item.armor.AcolyteArmorRenderer;
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
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public final class AcolyteArmorItem extends LivingArmorGeoItem implements KnowledgeReader, ItemTooltipStyleProvider {

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public AcolyteArmorItem(Holder<ArmorMaterial> material, Type type, int maxNutrients, Properties properties) {
		super(material, type, maxNutrients, properties);
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		consumer.accept(new GeoRenderProvider() {
			private @Nullable GeoArmorRenderer<?> renderer;

			@Override
			public <E extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable E livingEntity, ItemStack itemStack, @Nullable EquipmentSlot slot, @Nullable HumanoidModel<E> original) {
				if (renderer == null) {
					renderer = new AcolyteArmorRenderer();
				}

				return renderer;
			}
		});
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
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
		AcolyteArmorUpgrades.appendHoverText(stack, tooltip);

		tooltip.add(ComponentUtil.EMPTY_LINE);
		tooltip.add(TextComponentUtil.getAbilityText("fleshkin_affinity").withStyle(ChatFormatting.GRAY));
		tooltip.add(ComponentUtil.literal(" ").append(TextComponentUtil.getAbilityText("fleshkin_affinity.desc")).withStyle(ChatFormatting.DARK_GRAY));

		tooltip.add(ComponentUtil.EMPTY_LINE);
		tooltip.add(TextComponentUtil.getAbilityText("bio_alchemical_epidermis").withStyle(ChatFormatting.GRAY));
		tooltip.add(ComponentUtil.literal(" ").append(TextComponentUtil.getAbilityText("bio_alchemical_epidermis.desc")).withStyle(ChatFormatting.DARK_GRAY));

		tooltip.add(ComponentUtil.EMPTY_LINE);

		appendLivingToolTooltip(stack, tooltip);

		if (stack.isEnchanted()) {
			tooltip.add(ComponentUtil.EMPTY_LINE);
		}
	}

}