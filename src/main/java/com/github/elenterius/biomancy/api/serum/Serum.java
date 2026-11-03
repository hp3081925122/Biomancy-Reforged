package com.github.elenterius.biomancy.api.serum;

import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Experimental
public interface Serum {

	String TRANSLATION_PREFIX = "serum.";

	int EMPTY_COLOR = 0xFF_FFFFFF;

	Serum EMPTY = new Serum() {

		@Override
		public boolean canAffectEntity(CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
			return false;
		}

		@Override
		public void affectEntity(ServerLevel level, CompoundTag nbt, @Nullable LivingEntity source, LivingEntity target) {}

		@Override
		public boolean canAffectPlayerSelf(CompoundTag tag, Player targetSelf) {
			return false;
		}

		@Override
		public void affectPlayerSelf(ServerLevel level, CompoundTag nbt, ServerPlayer targetSelf) {}

		@Override
		public void appendTooltip(CompoundTag tag, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public int getColor(CompoundTag tag) {
			return EMPTY_COLOR;
		}

		@Override
		public String getNameTranslationKey() {
			return TRANSLATION_PREFIX + "biomancy.empty";
		}

	};

	static CompoundTag getDataTag(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.SERUM_DATA, CustomData.EMPTY).copyTag();
	}

	static void updateDataTag(ItemStack stack, Consumer<CompoundTag> updater) {
		CustomData.update(ModDataComponents.SERUM_DATA.get(), stack, updater);
	}

	static void setDataTag(ItemStack stack, CompoundTag tag) {
		stack.set(ModDataComponents.SERUM_DATA, CustomData.of(tag));
	}

	static void removeDataTag(ItemStack stack) {
		stack.remove(ModDataComponents.SERUM_DATA);
	}

	static String makeTranslationKey(ResourceLocation key) {
		return TRANSLATION_PREFIX + key.getNamespace() + "." + key.getPath().replace("/", ".");
	}

	boolean canAffectEntity(CompoundTag tag, @Nullable LivingEntity source, LivingEntity target);

	void affectEntity(ServerLevel level, CompoundTag tag, @Nullable LivingEntity source, LivingEntity target);

	boolean canAffectPlayerSelf(CompoundTag tag, Player targetSelf);

	void affectPlayerSelf(ServerLevel level, CompoundTag tag, ServerPlayer targetSelf);

	default boolean isEmpty() {
		return false;
	}

	int getColor(CompoundTag tag);

	void appendTooltip(CompoundTag tag, @Nullable Level level, List<Component> tooltip, TooltipFlag flag);

	String getNameTranslationKey();

	default String getDescriptionTranslationKey() {
		return getNameTranslationKey() + ".tooltip";
	}

	default MutableComponent getDisplayName(CompoundTag tag) {
		return ComponentUtil.translatable(getNameTranslationKey());
	}

}
