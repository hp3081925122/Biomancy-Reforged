package com.github.elenterius.biomancy.network;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.util.ClientUtil;
import com.github.elenterius.biomancy.util.explosion.ExplosionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CustomExplosionMessage implements CustomPacketPayload {

	public static final Type<CustomExplosionMessage> TYPE = new Type<>(BiomancyMod.rl("custom_explosion"));

	public static final StreamCodec<FriendlyByteBuf, CustomExplosionMessage> STREAM_CODEC = StreamCodec.ofMember(CustomExplosionMessage::encode, CustomExplosionMessage::decode);

	private final ExplosionType type;

	private final double x;
	private final double y;
	private final double z;

	private final float radius;
	private final boolean spawnParticles;
	private final boolean largeExplosion;

	private final float knockbackX;
	private final float knockbackY;
	private final float knockbackZ;

	public CustomExplosionMessage(ExplosionType type, ServerExplosion explosion, ServerPlayer serverPlayer, boolean spawnParticles) {
		this.type = type;

		Vec3 position = explosion.center();
		x = position.x;
		y = position.y;
		z = position.z;

		radius = explosion.radius();
		this.spawnParticles = spawnParticles;
		largeExplosion = !(radius < 2f) && explosion.getBlockInteraction() != Explosion.BlockInteraction.KEEP;

		Vec3 knockback = explosion.getHitPlayers().get(serverPlayer);

		if (knockback != null) {
			knockbackX = (float) knockback.x;
			knockbackY = (float) knockback.y;
			knockbackZ = (float) knockback.z;
		}
		else {
			knockbackX = 0f;
			knockbackY = 0f;
			knockbackZ = 0f;
		}
	}

	public CustomExplosionMessage(ExplosionType type, double x, double y, double z, float radius, boolean spawnParticles, boolean largeExplosion, float knockbackX, float knockbackY, float knockbackZ) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = radius;
		this.spawnParticles = spawnParticles;
		this.largeExplosion = largeExplosion;
		this.knockbackX = knockbackX;
		this.knockbackY = knockbackY;
		this.knockbackZ = knockbackZ;
	}

	@Override
	public Type<CustomExplosionMessage> type() {
		return TYPE;
	}

	public static void handle(CustomExplosionMessage packet, IPayloadContext context) {
		ClientHandler.handle(packet);
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeEnum(type);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(z);
		buffer.writeFloat(radius);
		buffer.writeBoolean(spawnParticles);
		buffer.writeBoolean(largeExplosion);
		buffer.writeFloat(knockbackX);
		buffer.writeFloat(knockbackY);
		buffer.writeFloat(knockbackZ);
	}

	public static CustomExplosionMessage decode(final FriendlyByteBuf buffer) {
		ExplosionType type = buffer.readEnum(ExplosionType.class);
		double x = buffer.readDouble();
		double y = buffer.readDouble();
		double z = buffer.readDouble();
		float radius = buffer.readFloat();
		boolean spawnParticles = buffer.readBoolean();
		boolean largeExplosion = buffer.readBoolean();
		float knockbackX = buffer.readFloat();
		float knockbackY = buffer.readFloat();
		float knockbackZ = buffer.readFloat();
		return new CustomExplosionMessage(type, x, y, z, radius, spawnParticles, largeExplosion, knockbackX, knockbackY, knockbackZ);
	}

	private static class ClientHandler {

		private static void handle(CustomExplosionMessage packet) {
			Level level = ClientUtil.getLevel();
			Player player = ClientUtil.getPlayer();
			if (level == null || player == null) return;

			level.playLocalSound(packet.x, packet.y, packet.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4f, (1f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f) * 0.7f, false);

			if (packet.spawnParticles) {
				level.addParticle(packet.largeExplosion ? packet.type.largeParticle.get() : packet.type.smallParticle.get(), packet.x, packet.y, packet.z, 1d, 0d, 0d);
			}

			player.setDeltaMovement(player.getDeltaMovement().add(packet.knockbackX, packet.knockbackY, packet.knockbackZ));
		}

	}

}
