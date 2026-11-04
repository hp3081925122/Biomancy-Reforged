package com.github.elenterius.biomancy.block.membrane;

import com.github.elenterius.biomancy.block.base.BlockTooltipProvider;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import com.github.elenterius.biomancy.util.BlockItemUtil;
import net.minecraft.world.item.Item;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BiometricMembraneBlock extends MembraneBlock implements BlockTooltipProvider, EntityBlock {

	public BiometricMembraneBlock(Properties properties) {
		super(properties, IgnoreEntityCollisionPredicate.IS_VALID_FOR_BLOCK_ENTITY_MEMBRANE);
	}

	public static ItemStack createItem(@Nullable EntityType<?> entityType, @Nullable UUID entityUUID, int[] entityColors, boolean isInverted) {
		CompoundTag tag = new CompoundTag();

		if (entityUUID != null) {
			tag.store(BiometricMembraneBlockEntity.ENTITY_UUID_KEY, UUIDUtil.CODEC, entityUUID);
		}
		else if (entityType != null) {
			tag.putString(BiometricMembraneBlockEntity.ENTITY_TYPE_KEY, EntityType.getKey(entityType).toString());
		}

		if (entityColors.length == 2 && !(entityColors[0] == 0xFFFF_FFFF && entityColors[1] == 0xFFFF_FFFF)) {
			tag.putIntArray(BiometricMembraneBlockEntity.ENTITY_COLORS_KEY, entityColors);
		}

		if (isInverted) {
			tag.putBoolean(BiometricMembraneBlockEntity.IS_INVERTED_KEY, true);
		}

		CompoundTag compoundTag = new CompoundTag();
		compoundTag.put(BiometricMembraneBlockEntity.MEMBRANE_KEY, tag);
		ItemStack stack = new ItemStack(ModItems.BIOMETRIC_MEMBRANE.get());
		BlockItemUtil.setBlockEntityData(stack, ModBlockEntities.BIOMETRIC_MEMBRANE.get(), compoundTag);
		return stack;
	}

	public static int getTintColor(ItemStack stack, int tintIndex) {
		CompoundTag compoundTag = BlockItemUtil.getBlockEntityData(stack);
		if (compoundTag == null || !compoundTag.contains(BiometricMembraneBlockEntity.MEMBRANE_KEY)) return 0xFFFF_FFFF;
		CompoundTag tag = compoundTag.getCompoundOrEmpty(BiometricMembraneBlockEntity.MEMBRANE_KEY);

		if (tag.getIntArray(BiometricMembraneBlockEntity.ENTITY_COLORS_KEY).isPresent()) {
			return tag.getIntArray(BiometricMembraneBlockEntity.ENTITY_COLORS_KEY).orElseThrow()[tintIndex];
		}

		return 0xFFFF_FFFF;
	}

	public static int getTintColor(BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int tintIndex) {
		if (level == null || pos == null) return 0xFFFF_FFFF;

		if (level.getBlockEntity(pos) instanceof BiometricMembraneBlockEntity membrane) {
			return membrane.getColors()[tintIndex];
		}

		return 0xFFFF_FFFF;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return ModBlockEntities.BIOMETRIC_MEMBRANE.get().create(pos, state);
	}

	
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {

		CompoundTag compoundTag = BlockItemUtil.getBlockEntityData(stack);
		if (compoundTag == null || !compoundTag.contains(BiometricMembraneBlockEntity.MEMBRANE_KEY)) return;
		CompoundTag tag = compoundTag.getCompoundOrEmpty(BiometricMembraneBlockEntity.MEMBRANE_KEY);

		if (tag.read(BiometricMembraneBlockEntity.ENTITY_UUID_KEY, UUIDUtil.CODEC).isPresent()) {
			UUID entityUUID = tag.read(BiometricMembraneBlockEntity.ENTITY_UUID_KEY, UUIDUtil.CODEC).orElseThrow();
			tooltip.accept(ComponentUtil.literal("UUID: " + entityUUID).withStyle(ChatFormatting.GRAY));
		}
		else {
			Optional<EntityType<?>> optional = EntityType.byString(tag.getStringOr(BiometricMembraneBlockEntity.ENTITY_TYPE_KEY, ""));
			optional.ifPresent(entityType -> tooltip.accept(ComponentUtil.literal("Type: ").append(entityType.getDescription()).withStyle(ChatFormatting.GRAY)));
		}
	}

}
