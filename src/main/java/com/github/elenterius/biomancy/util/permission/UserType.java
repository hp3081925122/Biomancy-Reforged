package com.github.elenterius.biomancy.util.permission;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Map;
import java.util.UUID;

public enum UserType {
	NONE(0), DEFAULT(1), ADMIN(2), OWNER(3);

	static final UserType[] SORTED_LEVELS = new UserType[]{NONE, DEFAULT, ADMIN, OWNER};
	private final byte level;

	UserType(int accessLevel) {
		this.level = (byte) accessLevel;
	}

	public static UserType fromId(byte level) {
		if (level < 0 || level > SORTED_LEVELS.length - 1) return NONE;
		return SORTED_LEVELS[level];
	}

	public static UserType fromTag(CompoundTag tag) {
		return UserType.fromId(tag.getByteOr("AccessLevel", (byte) 0));
	}

	public static UserType deserialize(ValueInput input) {
		return UserType.fromId(input.getByteOr("AccessLevel", (byte) 0));
	}

	public boolean isUserLevel() {
		return level > NONE.level;
	}

	public boolean isAdminLevel() {
		return level >= ADMIN.level;
	}

	public void serialize(ValueOutput output) {
		output.putByte("AccessLevel", level);
	}

	public static void serializeUsers(ValueOutput output, String key, Map<UUID, UserType> users) {
		if (users.isEmpty()) return;
		ValueOutput.ValueOutputList list = output.childrenList(key);
		for (Map.Entry<UUID, UserType> user : users.entrySet()) {
			ValueOutput child = list.addChild();
			child.store("UserUUID", UUIDUtil.CODEC, user.getKey());
			user.getValue().serialize(child);
		}
	}

	public static void deserializeUsers(ValueInput input, String key, Map<UUID, UserType> users) {
		input.childrenListOrEmpty(key).stream().forEach(child -> child.read("UserUUID", UUIDUtil.CODEC).ifPresent(uuid -> users.put(uuid, deserialize(child))));
	}

	public byte getAccessLevel() {
		return level;
	}

}
