package com.github.elenterius.biomancy.network;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.sounds.ClientSoundHandler;
import com.github.elenterius.biomancy.util.ClientUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class EntitySoundLayerMessages {

	private EntitySoundLayerMessages() {}

	public record UpdateMessage(int entityId, String soundLayerId, Holder<SoundEvent> sound, long startTime, float volume) implements CustomPacketPayload {

		public static final Type<UpdateMessage> TYPE = new Type<>(BiomancyMod.rl("sound_layer_update"));

		public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMessage> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, UpdateMessage::entityId,
				ByteBufCodecs.STRING_UTF8, UpdateMessage::soundLayerId,
				SoundEvent.STREAM_CODEC, UpdateMessage::sound,
				ByteBufCodecs.VAR_LONG, UpdateMessage::startTime,
				ByteBufCodecs.FLOAT, UpdateMessage::volume,
				UpdateMessage::new
		);

		public UpdateMessage(Entity entity, String soundLayerId, SoundEvent soundEvent, long startTime, float volume) {
			this(entity.getId(), soundLayerId, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), startTime, volume);
		}

		@Override
		public Type<UpdateMessage> type() {
			return TYPE;
		}

		public static void handle(UpdateMessage message, IPayloadContext context) {
			Entity entity = ClientUtil.getEntity(message.entityId);
			if (entity != null) {
				ClientSoundHandler.updateSoundLayer(entity, message.soundLayerId, message.sound.value(), message.startTime, message.volume);
			}
		}

	}

	public record RemovalMessage(int entityId, String soundLayerId, boolean fadeOut) implements CustomPacketPayload {

		public static final Type<RemovalMessage> TYPE = new Type<>(BiomancyMod.rl("sound_layer_removal"));

		public static final StreamCodec<ByteBuf, RemovalMessage> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, RemovalMessage::entityId,
				ByteBufCodecs.STRING_UTF8, RemovalMessage::soundLayerId,
				ByteBufCodecs.BOOL, RemovalMessage::fadeOut,
				RemovalMessage::new
		);

		public RemovalMessage(Entity entity, String soundLayerId, boolean fadeOut) {
			this(entity.getId(), soundLayerId, fadeOut);
		}

		@Override
		public Type<RemovalMessage> type() {
			return TYPE;
		}

		public static void handle(RemovalMessage message, IPayloadContext context) {
			Entity entity = ClientUtil.getEntity(message.entityId);
			if (entity != null) {
				ClientSoundHandler.removeSoundLayer(entity, message.soundLayerId, message.fadeOut);
			}
		}

	}

}
