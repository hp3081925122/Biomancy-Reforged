package com.github.elenterius.biomancy.item.injector;

import com.github.elenterius.biomancy.util.animation.ModDataTickets;
import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import com.github.elenterius.biomancy.util.MobUtil;
import net.minecraft.world.item.component.TooltipDisplay;
import com.github.elenterius.biomancy.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.github.elenterius.biomancy.inventory.ItemHandlerUtil;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.client.item.InjectorClientHooks;
import com.github.elenterius.biomancy.api.serum.Serum;
import com.github.elenterius.biomancy.api.serum.SerumContainer;
import com.github.elenterius.biomancy.api.serum.SerumInjector;
import com.github.elenterius.biomancy.component.InjectorContents;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.init.ModEnchantments;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.inventory.InjectorItemInventory;
import com.github.elenterius.biomancy.inventory.LargeSingleItemStackHandler;
import com.github.elenterius.biomancy.item.CrosshairProvider;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import com.github.elenterius.biomancy.item.KeyPressListener;
import com.github.elenterius.biomancy.item.armor.AcolyteArmorItem;
import com.github.elenterius.biomancy.sounds.SoundUtil;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.util.CombatUtil;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.AABB;
import com.geckolib.animatable.client.GeoRenderProvider;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class InjectorItem extends Item implements SerumInjector, ItemTooltipStyleProvider, CrosshairProvider, KeyPressListener, GeoItem {

	public static final short MAX_SLOT_SIZE = 16;
	public static final int COOL_DOWN_TICKS = 25;
	public static final int SCHEDULE_TICKS = Mth.ceil(0.32f * 20);

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public InjectorItem(Properties properties) {
		super(properties);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
		org.slf4j.LoggerFactory.getLogger(InjectorItem.class).debug("Injector client hooks are isolated from dedicated-server registration");
	}

	public static boolean tryInjectLivingEntity(ServerLevel level, BlockPos pos, ItemStack stack) {
		if (!(stack.getItem() instanceof InjectorItem injectorItem) || stack.getDamageValue() >= stack.getMaxDamage() - 1) return false;

		Serum serum = injectorItem.getSerum(stack);
		if (!serum.isEmpty()) {
			List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), EntitySelector.NO_SPECTATORS);
			if (entities.isEmpty()) return false;
			LivingEntity target = entities.get(0);
			if (target.isAlive() && dispenserAffectEntity(level, pos, serum, stack, injectorItem, target)) {
				level.playSound(null, pos, ModSoundEvents.INJECTOR_INJECT.get(), SoundSource.BLOCKS, 0.8f, 1f / (level.getRandom().nextFloat() * 0.5f + 1f) + 0.2f);
				level.levelEvent(LevelEvent.PARTICLES_DRAGON_BLOCK_BREAK, target.blockPosition(), 0);
				return true;
			}
		}

		return false;
	}

	private static boolean dispenserAffectEntity(ServerLevel level, BlockPos pos, Serum serum, ItemStack injectorStack, InjectorItem injectorItem, LivingEntity target) {
		if (CombatUtil.canPierceThroughArmor(injectorStack, target, null)) {
			CompoundTag dataTag = Serum.getDataTag(injectorStack);
			if (serum.canAffectEntity(dataTag, null, target)) {
				serum.affectEntity(level, dataTag, null, target);
				injectorItem.consumeSerum(injectorStack, null);
				injectorStack.hurtAndBreak(1, level, (ServerPlayer) null, item -> {});

				float damagePct = 1f;
				for (ItemStack itemStack : MobUtil.getArmorSlots(target)) {
					if (itemStack.getItem() instanceof AcolyteArmorItem armor && armor.hasNutrients(itemStack)) {
						damagePct -= 0.25f;
					}
				}

				if (EnchantmentUtil.getLevel(level.registryAccess(), ModEnchantments.ANESTHETIC, injectorStack) <= 0) {
					float damage = 0.5f * damagePct;
					if (damage > 0) {
						target.hurt(level.damageSources().sting(null), damage);
					}
				}
				return true;
			}
		}
		else {
			injectorStack.hurtAndBreak(2, level, (ServerPlayer) null, item -> {});
			level.playSound(null, pos, SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS, 0.5f, 1f / (level.getRandom().nextFloat() * 0.5f + 1f) + 0.2f);
		}
		return false;
	}

	public static Optional<LargeSingleItemStackHandler> getItemHandler(ItemStack stack) {
		if (!(stack.getItem() instanceof InjectorItem)) return Optional.empty();
		return Optional.of(InjectorItemInventory.create(MAX_SLOT_SIZE, stack).getItemHandler());
	}

	private void tryToOpenClientScreen(InteractionHand hand) {
		InjectorClientHooks.tryToOpenScreen(hand);
	}

	@Override
	public KeyPressResult onClientKeyPress(ItemStack stack, Level level, Player player, EquipmentSlot slot, byte flags) {
		if (slot.getType() == EquipmentSlot.Type.HAND) {
			InteractionHand hand = slot == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			tryToOpenClientScreen(hand);
		}
		return KeyPressResult.fail();
	}

	@Override
	public void onServerReceiveKeyPress(ItemStack stack, ServerLevel level, Player player, byte flags) {
		if (flags == -2) {
			clearInventory(stack, player);
		}

		if (flags >= 0) {
			setInventory(stack, player, flags);
		}
	}

	private void setInventory(ItemStack injector, Player player, int slotIndex) {
		final Inventory playerInventory = player.getInventory();
		final ItemStack foundStack = playerInventory.getItem(slotIndex);

		Item item = foundStack.getItem();
		if (!(item instanceof SerumContainer)) return;

		getItemHandler(injector).ifPresent(handler -> {
			ItemStack oldStack = ItemStack.EMPTY;
			if (!handler.getStack().isEmpty() && !ItemHandlerUtil.canItemStacksStack(foundStack, handler.getStack())) {
				oldStack = handler.extractItem(handler.getMaxAmount(), false);
			}

			ItemStack remainder = handler.insertItem(foundStack, false);
			playerInventory.setItem(slotIndex, remainder);

			if (remainder.isEmpty() && !handler.isFull()) {
				int slots = playerInventory.getContainerSize();
				for (int idx = 0; idx < slots; idx++) {
					ItemStack stack = playerInventory.getItem(idx);
					if (ItemHandlerUtil.canItemStacksStack(stack, handler.getStack())) {
						remainder = handler.insertItem(stack, false);
						playerInventory.setItem(idx, remainder);
						if (!remainder.isEmpty()) break;
					}
				}
			}

			if (!oldStack.isEmpty()) {
				playerInventory.placeItemBackInInventory(oldStack);
			}
		});
	}

	private void clearInventory(ItemStack injector, Player player) {
		getItemHandler(injector).ifPresent(handler -> {
			if (!handler.getStack().isEmpty()) {
				ItemStack result = handler.extractItem(handler.getMaxAmount(), false);
				player.getInventory().placeItemBackInInventory(result);
			}
		});
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);

		if (!player.isShiftKeyDown()) return InteractionResult.PASS;
		if (player.getCooldowns().isOnCooldown(stack) || !canInteractWithPlayerSelf(stack, player)) {
			SoundUtil.playItemSound(level, player, ModSoundEvents.INJECTOR_FAIL);
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			InjectionScheduler.schedule(this, stack, player, player, SCHEDULE_TICKS);
			broadcastAnimation((ServerLevel) level, player, stack, InjectorAnimation.INJECT_SELF);
			player.getCooldowns().addCooldown(stack, COOL_DOWN_TICKS);
		}
		return InteractionResult.CONSUME.heldItemTransformedTo(stack);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack copyOfStack, Player player, LivingEntity target, InteractionHand usedHand) {
		Level level = player.level();

		if (!canInteractWithLivingTarget(copyOfStack, player, target)) {
			if (level.isClientSide()) {
				SoundUtil.Client.playItemSound(level, player, ModSoundEvents.INJECTOR_FAIL.get());
			}
			return InteractionResult.FAIL;
		}

		if (level.isClientSide()) return InteractionResult.CONSUME;
		if (player.getCooldowns().isOnCooldown(copyOfStack)) return InteractionResult.FAIL;

		player.getCooldowns().addCooldown(copyOfStack, COOL_DOWN_TICKS);

		ItemStack realStack = player.getAbilities().instabuild ? player.getItemInHand(usedHand) : copyOfStack;
		InjectionScheduler.schedule(this, realStack, player, target, SCHEDULE_TICKS);
		broadcastAnimation((ServerLevel) target.level(), player, realStack, InjectorAnimation.INJECT_OTHER);

		return InteractionResult.CONSUME;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		if (entity instanceof ServerPlayer player && level instanceof ServerLevel serverLevel) {
			InjectionScheduler.tick(serverLevel, this, stack, player);
		}
	}

	@Override
	public Serum getSerum(ItemStack stack) {
		ItemStack storedStack = getStoredItemStack(stack);
		if (storedStack.getItem() instanceof SerumContainer container) {
			return container.getSerum(storedStack);
		}
		return Serum.EMPTY;
	}

	@Override
	public CompoundTag getSerumData(ItemStack stack) {
		ItemStack storedStack = getStoredItemStack(stack);
		return Serum.getDataTag(storedStack);
	}

	@Override
	public int getSerumColor(ItemStack stack) {
		ItemStack storedStack = getStoredItemStack(stack);
		if (storedStack.getItem() instanceof SerumContainer container) {
			return container.getSerumColor(storedStack);
		}
		return Serum.EMPTY_COLOR;
	}

	public ItemStack getStoredItemStack(ItemStack stack) {
		return getItemHandler(stack).map(LargeSingleItemStackHandler::getStack).orElse(ItemStack.EMPTY);
	}

	public void consumeSerum(ItemStack stack, @Nullable Player player) {
		if (player != null && player.isCreative()) return;
		getItemHandler(stack).ifPresent(handler -> consumeSerum(handler, player));
	}

	private void consumeSerum(LargeSingleItemStackHandler handler, @Nullable Player player) {
		if (handler.getStack().getItem() instanceof SerumContainer) {
			ItemStack stack = handler.extractItem(1, false);
			if (stack.getCraftingRemainder() != null) {
				ItemStack containerItem = stack.getCraftingRemainder().create();
				if (!containerItem.isEmpty() && player != null && !player.addItem(containerItem)) {
					player.drop(containerItem, false);
				}
			}
		}
	}

	@Override
	public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		return enchantment.is(Enchantments.PIERCING) || super.supportsEnchantment(stack, enchantment);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return false;
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	public void setEntityHost(ItemStack stack, Entity entity) {
		if (stack.isEmpty()) return;
		stack.set(ModDataComponents.INJECTOR_HOST, entity.getId());
	}

	public void setInjectionSuccess(ItemStack stack, boolean flag) {
		if (stack.isEmpty()) return;
		stack.set(ModDataComponents.INJECTION_SUCCESS, flag);
	}

	public boolean getInjectionSuccess(ItemStack stack) {
		if (stack.isEmpty()) return false;
		return stack.getOrDefault(ModDataComponents.INJECTION_SUCCESS, false);
	}

	public void removeEntityHost(ItemStack stack) {
		stack.remove(ModDataComponents.INJECTOR_HOST);
	}

	@Nullable
	public Entity getEntityHost(ItemStack stack, Level level) {
		Integer entityId = stack.get(ModDataComponents.INJECTOR_HOST);
		return entityId != null ? level.getEntity(entityId) : null;
	}

	public void setEntityVictim(ItemStack stack, Entity entity) {
		if (stack.isEmpty()) return;
		stack.set(ModDataComponents.INJECTOR_VICTIM, entity.getId());
	}

	public void removeEntityVictim(ItemStack stack) {
		stack.remove(ModDataComponents.INJECTOR_VICTIM);
	}

	@Nullable
	public Entity getEntityVictim(ItemStack stack, Level level) {
		Integer entityId = stack.get(ModDataComponents.INJECTOR_VICTIM);
		return entityId != null ? level.getEntity(entityId) : null;
	}

	private void onSoundKeyFrame(ItemStack stack, String soundId, double animationTick) {
		InjectorClientHooks.onSoundKeyFrame(this, stack, soundId, animationTick);
	}

	private void soundListener(KeyFrameEvent<InjectorItem, SoundKeyframeData> event) {
		ItemStack stack = event.renderState().getGeckolibData(ModDataTickets.ITEMSTACK);
		if (stack != null) {
			onSoundKeyFrame(stack, event.keyframeData().getSound(), event.getAnimationTick());
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		AnimationController<InjectorItem> mainController = new AnimationController<>(MAIN_CONTROLLER, 1, event -> PlayState.CONTINUE);
		InjectorAnimation.registerTriggerableAnimations(mainController);
		mainController.setSoundKeyframeHandler(this::soundListener);
		controllers.add(mainController);

		AnimationController<InjectorItem> needleController = new AnimationController<>(NEEDLE_CONTROLLER, 1, event -> PlayState.CONTINUE);
		InjectorAnimation.registerTriggerableAnimations(mainController);
		controllers.add(needleController);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	void broadcastAnimation(ServerLevel level, Player player, ItemStack stack, InjectorAnimation state) {
		long id = GeoItem.getOrAssignId(stack, level);
		triggerAnim(player, id, state.controller, state.name);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		InjectorClientHooks.appendHoverText(this, stack, context, display, tooltip, isAdvanced);
	}

	@Override
	public Component getHighlightTip(ItemStack stack, Component displayName) {
		Serum serum = getSerum(stack);
		return serum.isEmpty() ? displayName : ComponentUtil.mutable().append(displayName).append(" (").append(serum.getDisplayName(getSerumData(stack))).append(")");
	}

	protected static final Identifier CROSSHAIR = BiomancyMod.rl("textures/gui/injector_crosshair.png");

	@Override
	public Identifier getCrosshairTexture(ItemStack stack, Player player) {
		return CROSSHAIR;
	}

	protected static final String NEEDLE_CONTROLLER = "needle";
	protected static final String MAIN_CONTROLLER = "main";

	enum InjectorAnimation {
		INJECT_OTHER(MAIN_CONTROLLER, "inject", RawAnimation.begin().thenPlay("injector.inject")),
		INJECT_SELF(MAIN_CONTROLLER, "inject_self", RawAnimation.begin().thenPlay("injector.inject_self")),
		INJECT_FAIL(MAIN_CONTROLLER, "inject_fail", RawAnimation.begin().thenPlay("injector.inject_fail")),
		REGROW_NEEDLE(NEEDLE_CONTROLLER, "regrow_needle", RawAnimation.begin().thenPlay("injector.regrow_needle"));

		private final String controller;
		private final String name;
		private final RawAnimation rawAnimation;

		InjectorAnimation(String controller, String name, RawAnimation rawAnimation) {
			this.controller = controller;
			this.name = name;
			this.rawAnimation = rawAnimation;
		}

		public static void registerTriggerableAnimations(AnimationController<InjectorItem> controller) {
			for (InjectorAnimation injectorAnimation : values()) {
				if (injectorAnimation.controller.equals(controller.getName())) {
					controller.triggerableAnim(injectorAnimation.name, injectorAnimation.rawAnimation);
				}
			}
		}

	}

}
