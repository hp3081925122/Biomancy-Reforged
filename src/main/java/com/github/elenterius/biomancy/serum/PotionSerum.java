package com.github.elenterius.biomancy.serum;

import net.minecraft.nbt.NbtOps;
import java.util.function.Consumer;
import com.github.elenterius.biomancy.api.serum.Serum;
import com.github.elenterius.biomancy.init.ModSerums;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class PotionSerum implements Serum {

	public static final String COLOR_TAG = "color";
	public static final String POTION_TAG = "potion";
	public static final String STATUS_EFFECTS_TAG = "status_effects";
	public static final String NAME_TAG = "name";

	public PotionSerum() {}

	@Override
	public int getColor(CompoundTag tag) {
		return tag.getIntOr(COLOR_TAG, Serum.EMPTY_COLOR);
	}

	@Override
	public boolean canAffectEntity(CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		return true;
	}

	@Override
	public boolean canAffectPlayerSelf(CompoundTag tag, Player targetSelf) {
		return true;
	}

	@Override
	public void affectEntity(ServerLevel level, CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		applyPotionEffects(level, tag, source, target);
	}

	@Override
	public void affectPlayerSelf(ServerLevel level, CompoundTag tag, ServerPlayer targetSelf) {
		applyPotionEffects(level, tag, targetSelf, targetSelf);
	}

	private void applyPotionEffects(ServerLevel level, CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		List<MobEffectInstance> effects = getAllEffects(tag);

		for (MobEffectInstance effectInstance : effects) {
			MobEffect effect = effectInstance.getEffect().value();
			if (effect.isInstantenous()) {
				effect.applyInstantenousEffect(level, source, source, target, effectInstance.getAmplifier(), 1);
			}
			else {
				target.addEffect(new MobEffectInstance(effectInstance), source);
			}
		}

		Holder<Potion> potion = getPotion(tag);
		if (effects.isEmpty() && potion != null && potion.is(Potions.WATER)) {
			if (target.isSensitiveToWater()) {
				target.hurt(level.damageSources().indirectMagic(source, source), 1f);
			}

			if (target.isOnFire() && target.isAlive()) {
				target.extinguishFire();
			}

			if (target instanceof Axolotl axolotl) {
				axolotl.rehydrate();
			}
		}
	}

	@Override
	public String getNameTranslationKey() {
		return Serum.makeTranslationKey(Objects.requireNonNull(ModSerums.REGISTRY.getKey(this)));
	}

	@Override
	public MutableComponent getDisplayName(CompoundTag tag) {
		String translationKey = tag.getStringOr(NAME_TAG, getNameTranslationKey());
		return ComponentUtil.translatable(translationKey);
	}

	public void appendTooltip(CompoundTag tag, @Nullable Level level, Consumer<Component> tooltip, TooltipFlag flag) {
		float tickRate = level != null ? level.tickRateManager().tickrate() : 20f;
		PotionContents.addPotionTooltip(getAllEffects(tag), tooltip, 1f, tickRate);
	}

	@Override
	public String toString() {
		return "Serum{name=%s}".formatted(ModSerums.REGISTRY.getKey(this));
	}

	public static void setData(CompoundTag tag, @Nullable Holder<Potion> potion, Collection<MobEffectInstance> statusEffects) {
		setPotion(tag, potion);
		setStatusEffects(tag, statusEffects);

		List<MobEffectInstance> allEffects = new ArrayList<>();
		allEffects.addAll(getPotionEffects(getPotion(tag)));
		allEffects.addAll(statusEffects);

		tag.putInt(COLOR_TAG, computeColor(allEffects));

		MobEffect primaryEffect = getPrimaryEffect(allEffects);
		if (primaryEffect != null) {
			tag.putString(NAME_TAG, primaryEffect.getDescriptionId());
		}
	}

	public static void setData(CompoundTag tag, @Nullable Holder<Potion> potion, Collection<MobEffectInstance> statusEffects, int color) {
		setPotion(tag, potion);
		setStatusEffects(tag, statusEffects);
		tag.putInt(COLOR_TAG, color);

		List<MobEffectInstance> allEffects = new ArrayList<>();
		allEffects.addAll(getPotionEffects(getPotion(tag)));
		allEffects.addAll(statusEffects);

		MobEffect primaryEffect = getPrimaryEffect(allEffects);
		if (primaryEffect != null) {
			tag.putString(NAME_TAG, primaryEffect.getDescriptionId());
		}
	}

	public static void setPotion(CompoundTag tag, @Nullable Holder<Potion> potion) {
		if (potion == null) {
			tag.remove(POTION_TAG);
		}
		else {
			tag.putString(POTION_TAG, BuiltInRegistries.POTION.getKey(potion.value()).toString());
		}
	}

	public static @Nullable Holder<Potion> getPotion(CompoundTag tag) {
		Identifier id = Identifier.tryParse(tag.getStringOr(POTION_TAG, ""));
		if (id == null) return null;
		return BuiltInRegistries.POTION.get(id).orElse(null);
	}

	public static List<MobEffectInstance> getPotionEffects(@Nullable Holder<Potion> potion) {
		return potion != null ? potion.value().getEffects() : List.of();
	}

	public static void setStatusEffects(CompoundTag tag, Collection<MobEffectInstance> effects) {
		if (effects.isEmpty()) return;

		ListTag list = new ListTag();

		for (MobEffectInstance mobeffectinstance : effects) {
			list.add(MobEffectInstance.CODEC.encodeStart(NbtOps.INSTANCE, mobeffectinstance).getOrThrow());
		}

		tag.put(STATUS_EFFECTS_TAG, list);
	}

	public static void addStatusEffects(CompoundTag tag, Collection<MobEffectInstance> effects) {
		if (effects.isEmpty()) return;

		ListTag list = tag.getListOrEmpty(STATUS_EFFECTS_TAG);

		for (MobEffectInstance mobeffectinstance : effects) {
			list.add(MobEffectInstance.CODEC.encodeStart(NbtOps.INSTANCE, mobeffectinstance).getOrThrow());
		}

		tag.put(STATUS_EFFECTS_TAG, list);
	}

	public static List<MobEffectInstance> getStatusEffects(CompoundTag tag) {
		if (!tag.contains(STATUS_EFFECTS_TAG)) return new ArrayList<>();

		List<MobEffectInstance> list = new ArrayList<>();

		ListTag tagList = tag.getListOrEmpty(STATUS_EFFECTS_TAG);
		for (int i = 0; i < tagList.size(); i++) {
			MobEffectInstance instance = MobEffectInstance.CODEC.parse(NbtOps.INSTANCE, tagList.getCompoundOrEmpty(i)).result().orElse(null);
			if (instance != null) list.add(instance);
		}

		return list;
	}

	public static List<MobEffectInstance> getAllEffects(CompoundTag tag) {
		List<MobEffectInstance> effects = getStatusEffects(tag);
		effects.addAll(0, getPotionEffects(getPotion(tag)));
		return effects;
	}

	public static int computeColor(List<MobEffectInstance> allEffects) {
		return PotionContents.getColorOptional(allEffects).orElse(PotionContents.BASE_POTION_COLOR);
	}

	@Nullable
	public static MobEffect getPrimaryEffect(Collection<MobEffectInstance> allEffects) {
		MobEffectInstance instance = getPrimaryEffectInstance(allEffects);
		return instance != null ? instance.getEffect().value() : null;
	}

	@Nullable
	public static MobEffectInstance getPrimaryEffectInstance(Collection<MobEffectInstance> allEffects) {
		if (allEffects.isEmpty()) return null;

		Iterator<MobEffectInstance> iterator = allEffects.iterator();
		MobEffectInstance primary = iterator.next();

		while (iterator.hasNext()) {
			MobEffectInstance instance = iterator.next();
			boolean isInstanceHarmful = instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;
			boolean isPrimaryHarmful = primary.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;

			if (isInstanceHarmful && !isPrimaryHarmful) {
				continue;
			}

			if (isPrimaryHarmful && !isInstanceHarmful) {
				primary = instance;
				continue;
			}

			int amplifier = instance.getAmplifier();
			if (amplifier > primary.getAmplifier()) {
				primary = instance;
			}
			else if (amplifier == primary.getAmplifier() && !primary.isInfiniteDuration()) {
				if (instance.isInfiniteDuration() || instance.getDuration() > primary.getDuration()) {
					primary = instance;
				}
			}
		}

		return primary;
	}

}
