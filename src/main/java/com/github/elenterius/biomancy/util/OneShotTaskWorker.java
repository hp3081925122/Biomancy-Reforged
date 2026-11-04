package com.github.elenterius.biomancy.util;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class OneShotTaskWorker {

	private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

	private OneShotTaskWorker() {}

	public static void onNextTick(LivingEntity livingEntity, Consumer<LivingEntity> task) {
		TASKS.add(() -> {
			if (livingEntity.isAlive()) {
				task.accept(livingEntity);
			}
		});
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		int count = TASKS.size();
		for (int i = 0; i < count; i++) {
			Runnable task = TASKS.poll();
			if (task == null) break;
			task.run();
		}
	}

}
