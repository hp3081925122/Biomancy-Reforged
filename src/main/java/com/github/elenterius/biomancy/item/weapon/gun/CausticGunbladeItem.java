package com.github.elenterius.biomancy.item.weapon.gun;

import com.github.elenterius.biomancy.util.CombatUtil;
import net.minecraft.world.item.ItemInstance;
import com.github.elenterius.biomancy.init.ModItemAbilities;
import com.github.elenterius.biomancy.util.animation.ModDataTickets;
import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.component.AcidRefluxState;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.livingtool.SimpleLivingTool;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.entity.projectile.AcidSprayProjectile;
import com.github.elenterius.biomancy.init.*;
import com.github.elenterius.biomancy.item.CriticalHitListener;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import com.github.elenterius.biomancy.item.MeleeDamageSourceProviderItem;
import com.github.elenterius.biomancy.item.weapon.BladeProperties;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.MobUtil;
import com.github.elenterius.biomancy.util.animation.TriggerableAnimation;
import com.github.elenterius.biomancy.util.shooting.*;
import com.github.elenterius.geckolibextras.GLibExtras;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLEnvironment;
import com.geckolib.animatable.client.GeoRenderProvider;
import net.neoforged.neoforge.common.ItemAbilities;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.constant.DataTickets;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CausticGunbladeItem extends LivingGunItem<AcidSprayProjectile> implements SimpleLivingTool, CriticalHitListener, MeleeDamageSourceProviderItem, ItemTooltipStyleProvider, GeoItem {

	public static final GunSounds GUN_SOUNDS = new GunSounds(
			null, ModSoundEvents.FLESHKIN_NO.get(),
			SoundEvents.WITCH_DRINK, SoundEvents.WITCH_DRINK,
			SoundEvents.TROPICAL_FISH_FLOP, SoundEvents.TROPICAL_FISH_FLOP, ModSoundEvents.FLESHKIN_BURP.get()
	);
	protected static final Identifier CROSSHAIR = BiomancyMod.rl("textures/gui/gunblade_crosshair.png");
	protected final ItemAttributeModifiers defaultModifiers;
	protected final ItemAttributeModifiers disabledModifiers;

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public CausticGunbladeItem(int maxNutrients, Properties itemProperties) {
		super(maxNutrients, itemProperties,
				GunProperties.<AcidSprayProjectile>builder()
						.fireRate(20f).damage(0.25f).accuracy(0.98f).spreadBias(SpreadBias.SLIGHTLY_CENTER_HEAVY)
						.maxAmmo(100).reloadDuration(10 * 20).autoReload()
						.projectile(ModEntityTypes.ACID_SPRAY_PROJECTILE).velocity(0.95f)
						.localOffset(new Vector3f(0.25f, -0.2f, 0.55f))
						.sounds(GUN_SOUNDS)
						.build()
		);

		defaultModifiers = createDefaultModifiers(BladeProperties.builder().attackDamage(6).attackSpeed(1.2f).build());
		disabledModifiers = createDisabledModifiers(defaultModifiers);

		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	private static void playSwipeFX(LivingEntity attacker) {
		attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), ModSoundEvents.CLAWS_ATTACK_STRONG.get(), attacker.getSoundSource(), 1f, 1f + attacker.getRandom().nextFloat() * 0.5f);
		if (attacker.level() instanceof ServerLevel serverLevel) {
			double xOffset = -Mth.sin(attacker.getYRot() * Mth.DEG_TO_RAD);
			double zOffset = Mth.cos(attacker.getYRot() * Mth.DEG_TO_RAD);
			serverLevel.sendParticles(ModParticleTypes.CORROSIVE_SWIPE_ATTACK.get(), attacker.getX() + xOffset, attacker.getY(0.52f), attacker.getZ() + zOffset, 0, xOffset, 0, zOffset, 0);
		}
	}

	protected ItemAttributeModifiers createDefaultModifiers(BladeProperties bladeProperties) {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, bladeProperties.attackDamageModifier(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, bladeProperties.attackSpeedModifier(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.build();
	}

	private static ItemAttributeModifiers createDisabledModifiers(ItemAttributeModifiers modifiers) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
			if (entry.attribute().is(Attributes.ATTACK_SPEED)) builder.add(entry.attribute(), entry.modifier(), entry.slot());
		}
		return builder.build();
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		return hasNutrients(stack) ? defaultModifiers : disabledModifiers;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);

		if (Abilities.ACID_REFLUX.isActive(stack)) {
			player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("acid_reflux"));
			gunProperties.sounds().playLocalFail(level, player);
			return InteractionResult.FAIL;
		}

		return super.use(level, player, usedHand);
	}

	@Override
	public void onUseTick(Level level, LivingEntity shooter, ItemStack stack, int remainingUseDuration) {
		if (level.isClientSide()) {
			if (FMLEnvironment.getDist().isClient()) {
				if (getGunState(stack) == GunState.SHOOTING_OR_CHARGING) {
					int useTime = shooter.getTicksUsingItem();
					long startTime = level.getGameTime() - useTime;
					ClientSoundAccess.updateShootLoop(shooter, startTime);
				}
				else {
					ClientSoundAccess.removeShootLoop(shooter);
				}
			}

			return;
		}

		if (!(level instanceof ServerLevel serverLevel)) return;
		if (getGunState(stack) != GunState.SHOOTING_OR_CHARGING) return;

		if (Abilities.ACID_REFLUX.isActive(stack)) {
			shooter.releaseUsingItem();
			stopShooting(stack, serverLevel, shooter);
			if (shooter instanceof ServerPlayer player) {
				player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("acid_reflux"));
			}
			return;
		}

		super.onUseTick(level, shooter, stack, remainingUseDuration);
	}

	@Override
	public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
		if (entity.level().isClientSide() && FMLEnvironment.getDist().isClient()) {
			ClientSoundAccess.removeShootLoop(entity);
		}
	}

	// 将客户端音频层控制隔离到静态内部类，避免专用服加载 ClientSoundHandler
	private static final class ClientSoundAccess {
		private ClientSoundAccess() {}

		private static void updateShootLoop(LivingEntity shooter, long startTime) {
			com.github.elenterius.biomancy.sounds.ClientSoundHandler.updateSoundLayer(shooter, "use_item", ModSoundEvents.CAUSTIC_GUNBLADE_SHOOT_LOOP.get(), startTime, 1f);
		}

		private static void removeShootLoop(LivingEntity entity) {
			com.github.elenterius.biomancy.sounds.ClientSoundHandler.removeSoundLayer(entity, "use_item", true);
		}
	}

	@Override
	public void shoot(ServerLevel level, LivingEntity shooter, InteractionHand usedHand, ItemStack projectileWeapon) {
		broadcastAnimation(level, shooter, projectileWeapon, Animations.SHOOT);

		super.shoot(level, shooter, usedHand, projectileWeapon);

		boolean hadReflux = Abilities.ACID_REFLUX.isActive(projectileWeapon);
		Abilities.ACID_REFLUX.setActive(level, projectileWeapon, shooter);
		if (!hadReflux && Abilities.ACID_REFLUX.isActive(projectileWeapon)) {
			broadcastAnimation(level, shooter, projectileWeapon, Animations.COAT_BLADES);
			GunSounds.play(level, shooter, ModSoundEvents.FLESHKIN_BECOME_DORMANT.get());
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity livingEntity) {
			Abilities.ACID_REFLUX.tick(serverLevel, stack, livingEntity);
		}

		super.inventoryTick(stack, level, entity, slot);
	}

	@Override
	public boolean canReload(ItemStack stack, LivingEntity shooter) {
		return !Abilities.ACID_REFLUX.isActive(stack) && super.canReload(stack, shooter);
	}

	@Override
	public int getReloadCost(ItemStack stack) {
		return (getMaxAmmo(stack) - getAmmo(stack)) / 2;
	}

	@Override
	public boolean canPerformAction(ItemInstance stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
		if (!(stack instanceof ItemStack itemStack) || !hasNutrients(itemStack)) return false;
		return itemAbility != ItemAbilities.SWORD_SWEEP && ModItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker.level().isClientSide()) return;

		if (!MobUtil.isCreativePlayer(attacker)) {
			consumeNutrients(stack, 1);
		}

		if (Abilities.ACID_REFLUX.isActive(stack)) {
			boolean isFullAttackStrength = !(attacker instanceof Player player) || player.getAttackStrengthScale(0.5f) >= 0.9f;
			if (isFullAttackStrength) {
				playSwipeFX(attacker);
				target.addEffect(new MobEffectInstance(ModMobEffects.CORROSIVE, 20 + 20 / 2, 0));
				target.addEffect(new MobEffectInstance(ModMobEffects.ARMOR_SHRED, 4 * 20, 0));
			}
			Abilities.ACID_REFLUX.use(attacker.level(), stack, attacker);
		}

	}

	@Override
	public @Nullable DamageSource getMeleeDamageSource(ItemStack stack, Entity target, LivingEntity attacker, float attackStrengthScale) {
		if (!Abilities.ACID_REFLUX.isActive(stack)) return null;

		DamageSource damageSource = ModDamageSources.acid(attacker.level(), attacker);
		if (CombatUtil.isInvulnerableTo(target, damageSource)) return null;
		return damageSource;
	}

	@Override
	public void onCriticalHitEntity(ItemStack stack, LivingEntity attacker, LivingEntity target) {
		if (attacker.level().isClientSide()) return;

		if (Abilities.ACID_REFLUX.isActive(stack)) {
			target.addEffect(new MobEffectInstance(ModMobEffects.CORROSIVE, 4 * 20, 0));
			target.addEffect(new MobEffectInstance(ModMobEffects.ARMOR_SHRED, 6 * 20, 0));
			Abilities.ACID_REFLUX.use(attacker.level(), stack, attacker);
		}
	}

	@Override
	public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
		return !(user instanceof Player player && player.isCreative());
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (!hasNutrients(stack)) return 1f;
		if (state.is(Blocks.COBWEB)) return 15f;
		return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5f : 1f;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
		if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0f && !MobUtil.isCreativePlayer(miningEntity)) {
			consumeNutrients(stack, 2);
		}
		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		return state.is(Blocks.COBWEB);
	}

	@Override
	public boolean isValidEnchantment(ItemStack livingTool, Holder<Enchantment> enchantment) {
		return super.isValidEnchantment(livingTool, enchantment);
	}

	@Override
	public Component getHighlightTip(ItemStack stack, Component displayName) {
		return !Abilities.ACID_REFLUX.isActive(stack) ? displayName : ComponentUtil.mutable().append(displayName).append(" (").append(ComponentUtil.translatable(Abilities.ACID_REFLUX.getTranslationKey())).append(")");
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);

		if (!ClientTextUtil.hasControlDown()) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);
			appendGunStats(stack, tooltip);
		}

		tooltip.accept(ComponentUtil.EMPTY_LINE);
		appendLivingToolTooltip(stack, tooltip);

		tooltip.accept(ComponentUtil.EMPTY_LINE);
		tooltip.accept(ClientTextUtil.pressButtonTo(ClientTextUtil.getDefaultKey(), TextComponentUtil.getActionText("reload")).withStyle(TextStyles.DARK_GRAY));

		if (stack.isEnchanted()) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);
		}
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	protected void broadcastAnimation(ServerLevel level, Entity relatedEntity, ItemStack stack, TriggerableAnimation animation) {
		long id = GeoItem.getOrAssignId(stack, level);
		triggerAnim(relatedEntity, id, animation.controller(), animation.name());
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		Animations.registerControllers(this, controllers);
	}

	@Override
	public Identifier getCrosshairTexture(ItemStack stack, Player player) {
		return CROSSHAIR;
	}

	public interface ItemAbility {
		String name();

		void setActive(ServerLevel level, ItemStack stack, LivingEntity itemOwner);

		boolean isActive(ItemStack stack);

		void tick(Level level, ItemStack stack, LivingEntity itemOwner);

		void use(Level level, ItemStack stack, LivingEntity itemOwner);

		void cancel(ServerLevel level, ItemStack stack, LivingEntity itemOwner);

		default String getTranslationKey() {
			return BiomancyMod.translationKey("ability", name());
		}

		default void appendAbilityDescription(ItemStack stack, List<Component> components) {
			String translationKey = getTranslationKey();
			components.add(ComponentUtil.translatable(translationKey).withStyle(TextStyles.GRAY));
			components.addAll(ClientTextUtil.splitLinesByNewLine(ComponentUtil.translatable(translationKey + ".desc").withStyle(TextStyles.DARK_GRAY)));
		}

		default int getCharge(ItemStack stack) {
			return 0;
		}

		default float getChargePct(ItemStack stack) {
			return 0f;
		}

	}

	public static final class Abilities {

		public static final ItemAbility ACID_REFLUX = new ItemAbility() {
			static final String NAME = "acid_reflux";
			static final byte MAX_ACID_LEVEL = 120;
			static final int MAX_DELAY = SharedConstants.TICKS_PER_SECOND * 10;
			static final int MIN_DELAY = SharedConstants.TICKS_PER_SECOND / 2;

			@Override
			public String name() {
				return NAME;
			}

			@Override
			public int getCharge(ItemStack stack) {
				return getState(stack).acidLevel();
			}

			@Override
			public float getChargePct(ItemStack stack) {
				return getState(stack).acidLevel() / (float) MAX_ACID_LEVEL;
			}

			@Override
			public boolean isActive(ItemStack stack) {
				return getState(stack).hasReflux();
			}

			@Override
			public void setActive(ServerLevel level, ItemStack stack, LivingEntity itemOwner) {
				AcidRefluxState state = getState(stack);

				byte currAcidLevel = state.acidLevel();
				if (currAcidLevel >= MAX_ACID_LEVEL) return;

				int fx = Math.round((currAcidLevel * 0.0165f) * (currAcidLevel * 0.0165f) + 1f);
				byte acidLevel = (byte) Math.min(currAcidLevel + fx, MAX_ACID_LEVEL);
				state = state.withAcidLevel(acidLevel).withDelayStartTime(level.getGameTime());

				if (acidLevel >= MAX_ACID_LEVEL) {
					state = state.withReflux(true);
				}

				stack.set(ModDataComponents.ACID_REFLUX, state);
			}

			@Override
			public void tick(Level level, ItemStack stack, LivingEntity itemOwner) {
				AcidRefluxState state = getState(stack);
				byte currAcidLevel = state.acidLevel();

				long elapsedTime = level.getGameTime() - state.delayStartTime();
				int delay = Mth.lerpInt((float) currAcidLevel / MAX_ACID_LEVEL, MIN_DELAY, MAX_DELAY);
				if (elapsedTime < delay) return;

				int acidLevel = Math.max(0, currAcidLevel - 2);
				reduceAcidLevel(level, stack, itemOwner, state, acidLevel);
			}

			@Override
			public void use(Level level, ItemStack stack, LivingEntity itemOwner) {
				AcidRefluxState state = getState(stack);

				int acidLevel = Math.max(0, state.acidLevel() - 10);
				reduceAcidLevel(level, stack, itemOwner, state, acidLevel);
			}

			@Override
			public void cancel(ServerLevel level, ItemStack stack, LivingEntity itemOwner) {
				stack.set(ModDataComponents.ACID_REFLUX, AcidRefluxState.EMPTY);
			}

			private static AcidRefluxState getState(ItemStack stack) {
				return stack.getOrDefault(ModDataComponents.ACID_REFLUX, AcidRefluxState.EMPTY);
			}

			private static void reduceAcidLevel(Level level, ItemStack stack, LivingEntity itemOwner, AcidRefluxState state, int acidLevel) {
				state = state.withAcidLevel((byte) acidLevel);

				if (acidLevel == 0 && state.hasReflux()) {
					state = state.withReflux(false);
					stack.set(ModDataComponents.ACID_REFLUX, state);
					GunSounds.play(level, itemOwner, ModSoundEvents.FLESHKIN_BECOME_AWAKENED.get());
					return;
				}

				stack.set(ModDataComponents.ACID_REFLUX, state);
			}
		};
	}

	protected static final class Animations {
		static final String MAIN_CONTROLLER = "main";
		static final String ACID_COAT_CONTROLLER = "acid_blades";
		static final String AMMO_CONTROLLER = "ammo";

		static final RawAnimation IDLE_RANGED = RawAnimation.begin().thenPlay("idle_ranged");
		static final RawAnimation IDLE_MELEE = RawAnimation.begin().thenPlay("idle_melee");
		static final RawAnimation RANGED_TO_MELEE = RawAnimation.begin().thenPlay("ranged_to_melee").thenPlay("idle_melee");
		static final RawAnimation MELEE_TO_RANGED = RawAnimation.begin().thenPlay("melee_to_ranged").thenPlay("idle_ranged");
		static final RawAnimation COATED_BLADES = RawAnimation.begin().thenPlay("coated_blades");
		static final RawAnimation UNCOATED_BLADES = RawAnimation.begin().thenPlay("uncoated_blades");
		static final RawAnimation FULL_AMMO = RawAnimation.begin().thenPlay("full_ammo");
		static final RawAnimation HALF_AMMO = RawAnimation.begin().thenPlay("half_ammo");
		static final RawAnimation NO_AMMO = RawAnimation.begin().thenPlay("no_ammo");

		private static final List<TriggerableAnimation> TRIGGERABLE_ANIMATIONS = new ArrayList<>();
		static final TriggerableAnimation SHOOT = register(MAIN_CONTROLLER, "shoot", RawAnimation.begin().thenPlay("shoot"));
		static final TriggerableAnimation COAT_BLADES = register(MAIN_CONTROLLER, "coat_blades", RawAnimation.begin().thenPlay("coat_blades"));

		private Animations() {}

		static <T extends CausticGunbladeItem> PlayState handleMain(AnimationTest<T> state) {
			if (state.controller().isPlayingTriggeredAnimation()) return PlayState.CONTINUE;

			ItemStack itemStack = state.getData(ModDataTickets.ITEMSTACK);
			if (state.getData(GLibExtras.ITEM_HOST_TICKET) instanceof LivingEntity livingEntity) {
				if (livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack) {
					return state.setAndContinue(Animations.MELEE_TO_RANGED);
				}
			}

			return state.setAndContinue(Animations.RANGED_TO_MELEE);
		}

		static <T extends CausticGunbladeItem> PlayState handleAcidCoat(AnimationTest<T> state) {
			ItemStack itemStack = state.getData(ModDataTickets.ITEMSTACK);
			boolean hasCoatedBlades = Abilities.ACID_REFLUX.isActive(itemStack);
			return state.setAndContinue(hasCoatedBlades ? Animations.COATED_BLADES : Animations.UNCOATED_BLADES);
		}

		static <T extends CausticGunbladeItem> PlayState handleAmmo(AnimationTest<T> state) {
			ItemStack itemStack = state.getData(ModDataTickets.ITEMSTACK);
			Gun<?> gun = (Gun<?>) itemStack.getItem();

			int ammo = gun.getAmmo(itemStack);
			int maxAmmo = gun.getMaxAmmo(itemStack);

			if (ammo <= 0) {
				return state.setAndContinue(Animations.NO_AMMO);
			}

			return state.setAndContinue(ammo < maxAmmo ? Animations.HALF_AMMO : Animations.FULL_AMMO);
		}

		static void registerControllers(CausticGunbladeItem animatable, AnimatableManager.ControllerRegistrar controllers) {
			AnimationController<CausticGunbladeItem> mainController = new AnimationController<>(MAIN_CONTROLLER, 0, Animations::handleMain);
			registerTriggerableAnimations(mainController);
			controllers.add(mainController);

			controllers.add(new AnimationController<CausticGunbladeItem>(ACID_COAT_CONTROLLER, 0, Animations::handleAcidCoat));
			controllers.add(new AnimationController<CausticGunbladeItem>(AMMO_CONTROLLER, 0, Animations::handleAmmo));
		}

		private static TriggerableAnimation register(String controller, String name, RawAnimation rawAnimation) {
			TriggerableAnimation animation = new TriggerableAnimation(controller, name, rawAnimation);
			TRIGGERABLE_ANIMATIONS.add(animation);
			return animation;
		}

		private static void registerTriggerableAnimations(AnimationController<?> controller) {
			for (TriggerableAnimation animation : TRIGGERABLE_ANIMATIONS) {
				if (animation.controller().equals(controller.getName())) {
					controller.triggerableAnim(animation.name(), animation.rawAnimation());
				}
			}
		}
	}

}
