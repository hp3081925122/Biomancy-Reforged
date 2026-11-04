package com.github.elenterius.biomancy.block.membrane;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.HolderLookup;
import com.github.elenterius.biomancy.block.base.SimpleSyncedBlockEntity;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.item.EssenceItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BiometricMembraneBlockEntity extends SimpleSyncedBlockEntity implements Membrane {

	public static final String MEMBRANE_KEY = "membrane";
	public static final String ENTITY_TYPE_KEY = "entity_type";
	public static final String ENTITY_UUID_KEY = "entity_uuid";
	public static final String ENTITY_COLORS_KEY = "entity_colors";
	public static final String IS_INVERTED_KEY = "is_inverted";
	public static final int[] DEFAULT_COLORS = {0xffff_ffff, 0xffff_ffff};

	private @Nullable EntityType<?> entityType;
	private @Nullable UUID entityUUID = null;
	private int[] entityColors = DEFAULT_COLORS;
	private boolean isInverted = false;

	public BiometricMembraneBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BIOMETRIC_MEMBRANE.get(), pos, state);
		reRenderBlockOnSync = true;
	}

	public void setEntityFilter(LivingEntity entity) {
		this.entityType = entity.getType();
		entityUUID = entity.getUUID();
		entityColors = EssenceItem.getEssenceColors(entity, 3);

		setChanged();
		syncToClient();
	}

	public void setEntityFilter(EntityType<?> entityType) {
		this.entityType = entityType;
		entityColors = EssenceItem.getEssenceColors(entityType);

		setChanged();
		syncToClient();
	}

	public boolean setEntityFilter(ItemStack stack, EssenceItem essenceItem) {
		Optional<EntityType<?>> optional = essenceItem.getEntityType(stack);
		if (optional.isEmpty()) return false;

		entityType = optional.orElse(null);
		entityUUID = essenceItem.getEntityUUID(stack).orElse(null);
		entityColors = essenceItem.getColors(stack);

		setChanged();
		syncToClient();

		return true;
	}

	public void invertFilters() {
		isInverted = !isInverted;

		setChanged();
		syncToClient();
	}

	@Override
	public boolean shouldIgnoreEntityCollisionAt(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {

		if (entityUUID != null) {
			boolean isSameEntityUUID = entity.getUUID().equals(entityUUID);
			return isInverted != isSameEntityUUID;
		}

		if (entityType != null) {
			boolean isSameEntityType = entity.getType() == entityType;
			return isInverted != isSameEntityType;
		}

		return false;
	}

	@Override
	protected void saveForSyncToClient(ValueOutput output) {
		save(output);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		save(output);
	}

	protected void save(ValueOutput output) {
		CompoundTag tag = new CompoundTag();

		if (entityType != null) {
			tag.putString(ENTITY_TYPE_KEY, EntityType.getKey(entityType).toString());
		}

		if (entityUUID != null) {
			tag.store(ENTITY_UUID_KEY, UUIDUtil.CODEC, entityUUID);
		}

		if (entityColors != DEFAULT_COLORS && !(entityColors[0] == 0xFFFF_FFFF && entityColors[1] == 0xFFFF_FFFF)) {
			tag.putIntArray(ENTITY_COLORS_KEY, entityColors);
		}

		if (isInverted) tag.putBoolean(IS_INVERTED_KEY, true);

		output.store(MEMBRANE_KEY, CompoundTag.CODEC, tag);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		CompoundTag tag = input.read(MEMBRANE_KEY, CompoundTag.CODEC).orElseGet(CompoundTag::new);

		entityType = EntityType.byString(tag.getStringOr(ENTITY_TYPE_KEY, "")).orElse(null);
		entityUUID = tag.read(ENTITY_UUID_KEY, UUIDUtil.CODEC).isPresent() ? tag.read(ENTITY_UUID_KEY, UUIDUtil.CODEC).orElseThrow() : null;
		entityColors = tag.getIntArray(ENTITY_COLORS_KEY).isPresent() ? tag.getIntArray(ENTITY_COLORS_KEY).orElseThrow() : DEFAULT_COLORS;
		isInverted = tag.getBooleanOr(IS_INVERTED_KEY, false);
	}

	public int[] getColors() {
		return entityColors;
	}

}
