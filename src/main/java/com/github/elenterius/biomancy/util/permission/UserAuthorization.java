package com.github.elenterius.biomancy.util.permission;


import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import java.util.Objects;
import java.util.UUID;

public class UserAuthorization implements ValueIOSerializable {

	private UUID userUUID;
	private UserType authority;

	public UserAuthorization(ValueInput input) {
		userUUID = input.read("UserUUID", UUIDUtil.CODEC).orElseThrow();
		authority = UserType.deserialize(input);
	}

	public UserAuthorization(UUID userUUID) {
		this(userUUID, UserType.DEFAULT);
	}

	public UserAuthorization(UUID userUUID, UserType type) {
		this.userUUID = userUUID;
		authority = type;
	}

	public UUID getUser() {
		return userUUID;
	}

	public UserType getAuthority() {
		return authority;
	}

	public void setAuthorityLevel(UserType level) {
		authority = level;
	}

	public int getAuthorityLevel() {
		return authority.getAccessLevel();
	}

	@Override
	public void serialize(ValueOutput output) {
		output.store("UserUUID", UUIDUtil.CODEC, userUUID);
		authority.serialize(output);
	}

	@Override
	public void deserialize(ValueInput input) {
		userUUID = input.read("UserUUID", UUIDUtil.CODEC).orElseThrow();
		authority = UserType.deserialize(input);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserAuthorization other) {
			return userUUID.equals(other.userUUID) && authority == other.authority;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(userUUID, authority);
	}

	@Override
	public String toString() {
		return "(" + userUUID.toString() + "," + authority + ")";
	}

}
