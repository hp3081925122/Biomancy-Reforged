package com.github.elenterius.biomancy.item.armor;

import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.*;

@Deprecated
public final class AdaptiveDamageResistanceHandler {

	private static final float[] HEALTH_GATES = {0.25f, 0.45f, 0.65f, 0.8f};
	private static final float[] ADAPTATION_EFFECTIVENESS = {0.9f, 0.8f, 0.75f, 0.7f};
	private static final float ADDITIONAL_RESISTANCE = 0.4f;
	private static final float[] DAMAGE_GATE_PERCENTAGES = {0.25f, 0.2f, 0.2f, 0.15f};

	private static final Set<String> VALID_NAMESPACES = Set.of("minecraft", "neoforge", "c");
	private static final String TAG_PREFIX = "is_";

	public static DamageTypeResistanceTracker getResistanceTracker(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.DAMAGE_RESISTANCE_TRACKER, DamageTypeResistanceTracker.EMPTY).mutableCopy();
	}

	public static void saveResistanceTracker(DamageTypeResistanceTracker resistanceTracker, ItemStack stack) {
		stack.set(ModDataComponents.DAMAGE_RESISTANCE_TRACKER, resistanceTracker.immutableCopy());
	}

	public static float absorbDamage(LivingEntity livingEntity, DamageSource damageSource, float damage, ArmorItem armor, ItemStack stack) {
		List<TagKey<DamageType>> rootDamageTypes = damageSource.typeHolder().tags()
				.filter(tagKey -> VALID_NAMESPACES.contains(tagKey.location().getNamespace()) && tagKey.location().getPath().startsWith(TAG_PREFIX))
				.toList();

		if (rootDamageTypes.isEmpty()) return damage;

		DamageTypeResistanceTracker resistanceTracker = getResistanceTracker(stack);

		for (TagKey<DamageType> damageType : rootDamageTypes) {
			resistanceTracker.count(damageType);
		}

		float healthPct = 1f - armor.getDamage(stack) / (float) armor.getMaxDamage(stack);

		for (double healthGate : HEALTH_GATES) {
			if (healthPct <= healthGate) {
				adaptToDamageType(resistanceTracker);
				break;
			}
		}

		float reducedDamage = reduceDamage(livingEntity, damageSource, damage, resistanceTracker);

		saveResistanceTracker(resistanceTracker, stack);

		return reducedDamage;
	}

	private static float reduceDamage(LivingEntity livingEntity, DamageSource damageSource, float damage, DamageTypeResistanceTracker resistanceTracker) {

		Map<TagKey<DamageType>, Float> resistances = resistanceTracker.resistances;

		for (Map.Entry<TagKey<DamageType>, Float> resistance : resistances.entrySet()) {
			if (damageSource.is(resistance.getKey())) {
				damage *= (1 - resistance.getValue());
			}
		}

		if (resistanceTracker.hasResistanceForLargestDamageTypeCount()) {
			damage *= 1f - ADDITIONAL_RESISTANCE;
		}

		for (float damageGatePct : DAMAGE_GATE_PERCENTAGES) {
			float damageGate = livingEntity.getMaxHealth() * damageGatePct;
			if (livingEntity.getHealth() - damage <= damageGate) {
				damage = Math.max(damage - (damageGate - livingEntity.getHealth()), 0);
				break;
			}
		}

		return Math.max(damage, 0);
	}

	private static void adaptToDamageType(DamageTypeResistanceTracker damageTypeTracker) {
		int maxCount = 0;
		TagKey<DamageType> adaptedType = null;

		for (Map.Entry<TagKey<DamageType>, Integer> entry : damageTypeTracker.counter.entrySet()) {
			Integer value = entry.getValue();
			TagKey<DamageType> key = entry.getKey();

			if (value > maxCount && !damageTypeTracker.resistances.containsKey(key)) {
				maxCount = value;
				adaptedType = key;
			}
		}

		if (adaptedType != null) {
			Map<TagKey<DamageType>, Float> resistances = damageTypeTracker.resistances;
			int i = Math.min(resistances.size(), ADAPTATION_EFFECTIVENESS.length - 1);
			resistances.put(adaptedType, ADAPTATION_EFFECTIVENESS[i]);
		}
	}

	public record DamageTypeResistanceTracker(Map<TagKey<DamageType>, Integer> counter, Map<TagKey<DamageType>, Float> resistances) {

		public static final DamageTypeResistanceTracker EMPTY = new DamageTypeResistanceTracker(Map.of(), Map.of());

		public static final Codec<DamageTypeResistanceTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(TagKey.codec(Registries.DAMAGE_TYPE), Codec.INT).optionalFieldOf("counter", Map.of()).forGetter(DamageTypeResistanceTracker::counter),
				Codec.unboundedMap(TagKey.codec(Registries.DAMAGE_TYPE), Codec.FLOAT).optionalFieldOf("resistances", Map.of()).forGetter(DamageTypeResistanceTracker::resistances)
		).apply(instance, DamageTypeResistanceTracker::new));

		public static final StreamCodec<ByteBuf, DamageTypeResistanceTracker> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

		public DamageTypeResistanceTracker mutableCopy() {
			return new DamageTypeResistanceTracker(new HashMap<>(counter), new HashMap<>(resistances));
		}

		public DamageTypeResistanceTracker immutableCopy() {
			return new DamageTypeResistanceTracker(Map.copyOf(counter), Map.copyOf(resistances));
		}

		static void appendTooltipText(DamageTypeResistanceTracker tracker, List<Component> tooltip) {
			Set<TagKey<DamageType>> damageTypes = new HashSet<>();
			damageTypes.addAll(tracker.counter.keySet());
			damageTypes.addAll(tracker.resistances.keySet());

			if (damageTypes.isEmpty()) return;

			tooltip.add(ComponentUtil.EMPTY_LINE);
			tooltip.add(ComponentUtil.mutable().withStyle(TextStyles.GRAY)
					.append(ComponentUtil.literal("Adaptations:"))
			);

			for (TagKey<DamageType> damageType : damageTypes) {
				tooltip.add(ComponentUtil.mutable().withStyle(TextStyles.GRAY)
						.append(ComponentUtil.literal(damageType.location().toString()))
				);

				if (tracker.counter.containsKey(damageType)) {
					tooltip.add(ComponentUtil.mutable().withStyle(TextStyles.GRAY).append(ComponentUtil.SPACE)
							.append(ComponentUtil.literal("Count: " + tracker.counter.get(damageType)))
					);
				}

				if (tracker.resistances.containsKey(damageType)) {
					tooltip.add(ComponentUtil.mutable().withStyle(TextStyles.GRAY).append(ComponentUtil.SPACE)
							.append(ComponentUtil.literal("Resistance: " + tracker.resistances.get(damageType)))
					);
				}
			}
		}

		public void count(TagKey<DamageType> damageType) {
			counter.merge(damageType, 1, Integer::sum);
		}

		public boolean hasResistanceForLargestDamageTypeCount() {
			return counter.entrySet().stream()
					.max(Map.Entry.comparingByValue())
					.map(Map.Entry::getKey).filter(resistances::containsKey)
					.isPresent();
		}
	}

}
