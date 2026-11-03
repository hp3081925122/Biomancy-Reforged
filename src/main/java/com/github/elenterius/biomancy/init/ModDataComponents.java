package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.livingtool.LivingToolState;
import com.github.elenterius.biomancy.component.*;
import com.github.elenterius.biomancy.entity.mob.ControllableMob;
import com.github.elenterius.biomancy.item.armor.AdaptiveDamageResistanceHandler;
import com.github.elenterius.biomancy.item.weapon.gun.GunbladeItem;
import com.github.elenterius.biomancy.util.shooting.GunState;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.UUID;

public final class ModDataComponents {

	public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(BiomancyMod.MOD_ID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> NUTRIENTS = DATA_COMPONENTS.registerComponentType("nutrients",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE = DATA_COMPONENTS.registerComponentType("charge",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<LivingToolState>> LIVING_TOOL_STATE = DATA_COMPONENTS.registerComponentType("living_tool_state",
			builder -> builder.persistent(Codec.BYTE.xmap(LivingToolState::deserialize, LivingToolState::serialize))
					.networkSynchronized(ByteBufCodecs.BYTE.map(LivingToolState::deserialize, LivingToolState::serialize)));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> SERUM_DATA = DATA_COMPONENTS.registerComponentType("serum_data",
			builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> OWNER = DATA_COMPONENTS.registerComponentType("owner",
			builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GUN_AMMO = DATA_COMPONENTS.registerComponentType("ammo",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> GUN_RELOAD_TIMESTAMP = DATA_COMPONENTS.registerComponentType("reload_timestamp",
			builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> GUN_SHOOT_TIMESTAMP = DATA_COMPONENTS.registerComponentType("shoot_timestamp",
			builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<GunState>> GUN_STATE = DATA_COMPONENTS.registerComponentType("projectile_weapon_state",
			builder -> builder.persistent(Codec.BYTE.xmap(GunState::fromId, GunState::getId))
					.networkSynchronized(ByteBufCodecs.BYTE.map(GunState::fromId, GunState::getId)));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<GunbladeItem.GunbladeMode>> GUNBLADE_MODE = DATA_COMPONENTS.registerComponentType("gunblade_mode",
			builder -> builder.persistent(Codec.BYTE.xmap(GunbladeItem.GunbladeMode::fromOrdinal, GunbladeItem.GunbladeMode::toOrdinal))
					.networkSynchronized(ByteBufCodecs.BYTE.map(GunbladeItem.GunbladeMode::fromOrdinal, GunbladeItem.GunbladeMode::toOrdinal)));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<AcidRefluxState>> ACID_REFLUX = DATA_COMPONENTS.registerComponentType("acid_reflux",
			builder -> builder.persistent(AcidRefluxState.CODEC).networkSynchronized(AcidRefluxState.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<EssenceData>> ESSENCE = DATA_COMPONENTS.registerComponentType("essence",
			builder -> builder.persistent(EssenceData.CODEC).networkSynchronized(EssenceData.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<InjectorContents>> INJECTOR_CONTENTS = DATA_COMPONENTS.registerComponentType("injector_contents",
			builder -> builder.persistent(InjectorContents.CODEC).networkSynchronized(InjectorContents.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> INJECTOR_HOST = DATA_COMPONENTS.registerComponentType("injector_host",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> INJECTOR_VICTIM = DATA_COMPONENTS.registerComponentType("injector_victim",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INJECTION_SUCCESS = DATA_COMPONENTS.registerComponentType("injection_success",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<InjectionSchedule>> INJECTION_SCHEDULE = DATA_COMPONENTS.registerComponentType("injection_schedule",
			builder -> builder.persistent(InjectionSchedule.CODEC).networkSynchronized(InjectionSchedule.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<AdaptiveDamageResistanceHandler.DamageTypeResistanceTracker>> DAMAGE_RESISTANCE_TRACKER = DATA_COMPONENTS.registerComponentType("damage_resistance_tracker",
			builder -> builder.persistent(AdaptiveDamageResistanceHandler.DamageTypeResistanceTracker.CODEC).networkSynchronized(AdaptiveDamageResistanceHandler.DamageTypeResistanceTracker.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BulletJumpState>> BULLET_JUMP = DATA_COMPONENTS.registerComponentType("bullet_jump",
			builder -> builder.persistent(BulletJumpState.CODEC).networkSynchronized(BulletJumpState.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ResourceLocation>>> ARMOR_UPGRADES = DATA_COMPONENTS.registerComponentType("armor_upgrades",
			builder -> builder.persistent(ResourceLocation.CODEC.listOf()).networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ControllableMob.Command>> CONTROL_STAFF_COMMAND = DATA_COMPONENTS.registerComponentType("command",
			builder -> builder.persistent(Codec.BYTE.xmap(ControllableMob.Command::deserialize, ControllableMob.Command::serialize))
					.networkSynchronized(ByteBufCodecs.BYTE.map(ControllableMob.Command::deserialize, ControllableMob.Command::serialize)));

	private ModDataComponents() {}

}
