package com.github.elenterius.biomancy.block.ownable;

import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.HolderLookup;
import com.github.elenterius.biomancy.block.base.SimpleContainerBlockEntity;
import com.github.elenterius.biomancy.util.ownable.Ownable;
import com.github.elenterius.biomancy.util.permission.Actions;
import com.github.elenterius.biomancy.util.permission.IRestrictedInteraction;
import com.github.elenterius.biomancy.util.permission.UserType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class OwnableContainerBlockEntity extends SimpleContainerBlockEntity implements Ownable, IRestrictedInteraction {

	public static final int MAX_USERS = 10;
	private final HashMap<UUID, UserType> users = new HashMap<>(6);
	@Nullable
	private UUID ownerId;

	protected OwnableContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public boolean canPlayerInteract(Player player) {
		return super.canPlayerInteract(player) && isActionAllowed(player, Actions.USE_BLOCK);
	}

	@Override
	public UserType getUserType(UUID userId) {
		if (isOwner(userId)) return UserType.OWNER;
		return users.getOrDefault(userId, UserType.NONE);
	}

	@Override
	public boolean setUserType(UUID userId, UserType userType) {
		if (users.size() >= MAX_USERS && !users.containsKey(userId)) return false;
		users.put(userId, userType);
		syncToClient();
		setChanged();
		return true;
	}

	@Override
	public void removeUser(UUID userId) {
		if (users.containsKey(userId)) {
			users.remove(userId);
			syncToClient();
			setChanged();
		}
	}

	@Override
	public Optional<UUID> getOptionalOwnerUUID() {
		return Optional.ofNullable(ownerId);
	}

	@Override
	public void setOwner(UUID userId) {
		ownerId = userId;
		syncToClient();
		setChanged();
	}

	@Override
	public boolean hasOwner() {
		return ownerId != null;
	}

	@Override
	public boolean isOwner(UUID userId) {
		if (ownerId == null) return false;
		return userId.equals(ownerId);
	}

	@Override
	public void removeOwner() {
		ownerId = null;
		syncToClient();
		setChanged();
	}

	protected void syncToClient() {
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		saveForSyncToClient(output);
		return output.buildResult();
	}

	@Override
	@Nullable
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	protected void saveForSyncToClient(ValueOutput output) {
		if (ownerId != null) output.store("OwnerUUID", UUIDUtil.CODEC, ownerId);

		UserType.serializeUsers(output, "UserList", users);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ownerId = input.read("OwnerUUID", UUIDUtil.CODEC).orElse(null);

		users.clear();
		UserType.deserializeUsers(input, "UserList", users);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (ownerId != null) output.store("OwnerUUID", UUIDUtil.CODEC, ownerId);

		UserType.serializeUsers(output, "UserList", users);
	}

}
