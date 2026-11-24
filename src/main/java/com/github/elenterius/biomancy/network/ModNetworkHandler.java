package com.github.elenterius.biomancy.network;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.crafting.recipe.BioForgingRecipe;
import com.github.elenterius.biomancy.util.ItemStackFilterList;
import com.github.elenterius.biomancy.util.explosion.ExplosionType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ServerExplosion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class ModNetworkHandler {

	private static final String PROTOCOL_VERSION = "1";

	private ModNetworkHandler() {}

	public static void sendKeyBindPressToServer(InteractionHand hand, byte flag) {
		EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
		sendKeyBindPressToServer(slot, flag);
	}

	public static void sendKeyBindPressToServer(EquipmentSlot slot, byte flag) {
		if (FMLEnvironment.getDist().isClient()) {
			ClientSender.sendKeyPress(slot.getFilterBit(0), flag);
		}
	}

	public static void sendKeyBindPressToServer(int slotIndex, byte flag) {
		if (FMLEnvironment.getDist().isClient()) {
			ClientSender.sendKeyPress(slotIndex, flag);
		}
	}

	public static void sendBioForgeRecipeToServer(int containerId, RecipeHolder<BioForgingRecipe> recipe) {
		if (FMLEnvironment.getDist().isClient()) {
			ClientSender.sendBioForgeRecipe(containerId, recipe);
		}
	}

	public static void sendBioLabFilterToClient(ServerPlayer player, int containerId, ItemStackFilterList filters) {
		PacketDistributor.sendToPlayer(player, new BioLabFilterMessage(containerId, filters));
	}

	public static void sendCustomExplosionToClients(ServerLevel level, ExplosionType explosionType, ServerExplosion explosion, boolean spawnParticles) {
		double radius = Math.min(explosion.radius() + 64d, 64d + 32d);
		double radiusSqr = radius * radius;

		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(explosion.center()) < radiusSqr) {
				PacketDistributor.sendToPlayer(player, new CustomExplosionMessage(explosionType, explosion, player, spawnParticles));
			}
		}
	}

	public static void sendSoundLayerUpdateToClient(ServerPlayer player, Entity entity, String soundLayerId, SoundEvent soundEvent, long startTime, float volume) {
		PacketDistributor.sendToPlayer(player, new EntitySoundLayerMessages.UpdateMessage(entity, soundLayerId, soundEvent, startTime, volume));
	}

	public static void sendSoundLayerUpdateToClients(Entity entity, String soundLayerId, SoundEvent soundEvent, long startTime, float volume) {
		PacketDistributor.sendToPlayersTrackingEntity(entity, new EntitySoundLayerMessages.UpdateMessage(entity, soundLayerId, soundEvent, startTime, volume));
	}

	public static void sendSoundLayerRemovalToClients(Entity entity, String soundLayerId, boolean fadeOut) {
		PacketDistributor.sendToPlayersTrackingEntity(entity, new EntitySoundLayerMessages.RemovalMessage(entity, soundLayerId, fadeOut));
	}

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
		registrar.playToServer(KeyPressMessage.TYPE, KeyPressMessage.STREAM_CODEC, KeyPressMessage::handle);
		registrar.playToServer(BioForgeRecipeMessage.TYPE, BioForgeRecipeMessage.STREAM_CODEC, BioForgeRecipeMessage::handle);
		registrar.playToClient(BioLabFilterMessage.TYPE, BioLabFilterMessage.STREAM_CODEC, BioLabFilterMessage::handle);
		registrar.playToClient(CustomExplosionMessage.TYPE, CustomExplosionMessage.STREAM_CODEC, CustomExplosionMessage::handle);
		registrar.playToClient(EntitySoundLayerMessages.UpdateMessage.TYPE, EntitySoundLayerMessages.UpdateMessage.STREAM_CODEC, EntitySoundLayerMessages.UpdateMessage::handle);
		registrar.playToClient(EntitySoundLayerMessages.RemovalMessage.TYPE, EntitySoundLayerMessages.RemovalMessage.STREAM_CODEC, EntitySoundLayerMessages.RemovalMessage::handle);
	}

	// 将客户端发包器隔离到独立静态内部类，避免通用网络处理器类在专用服引用客户端包
	private static final class ClientSender {
		private ClientSender() {}

		private static void sendKeyPress(int slotIndex, byte flag) {
			net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new KeyPressMessage(slotIndex, flag));
		}

		private static void sendBioForgeRecipe(int containerId, RecipeHolder<BioForgingRecipe> recipe) {
			net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new BioForgeRecipeMessage(containerId, recipe.id().identifier()));
		}
	}

}
