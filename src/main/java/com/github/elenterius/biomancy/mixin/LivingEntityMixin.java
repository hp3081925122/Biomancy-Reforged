package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.entity.misc.LivingEntityData;
import com.github.elenterius.biomancy.event.LivingEventHandler;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.item.ShieldBlockingListener;
import com.github.elenterius.biomancy.serum.FrenzySerum;
import com.github.elenterius.biomancy.statuseffect.StatusEffectHandler;
import com.github.elenterius.biomancy.item.shield.LivingShieldItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlocksAttacks;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityData.TransientDataProvider {

	@Shadow
	protected ItemStack useItem;

	private LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	public abstract boolean hasEffect(Holder<MobEffect> effect);

	@Shadow
	public abstract AttributeMap getAttributes();

	@Shadow
	@Final
	private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

	@Shadow
	public abstract Collection<MobEffectInstance> getActiveEffects();

	@Unique
	DataHolder biomancy$transientData = new DataHolder();

	@Override
	public DataHolder biomancy$getData() {
		return biomancy$transientData;
	}

	@Inject(method = "checkAutoSpinAttack", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;isEmpty()Z"), cancellable = true)
	private void onAutoSpinHorizontalCollision(AABB aabbBeforeSpin, AABB aabbAfterSpin, CallbackInfo ci, @Local List<Entity> list) {
		if (list.isEmpty() && horizontalCollision && LivingEventHandler.onAutoSpinHorizontalCollision(biomancy$self())) {
			ci.cancel();
		}
	}

	@Inject(method = "shouldDiscardFriction", at = @At("HEAD"), cancellable = true)
	private void onShouldDiscardFriction(CallbackInfoReturnable<Boolean> cir) {
		if (biomancy$transientData.shouldDiscardFriction()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getAttributeValue(Lnet/minecraft/core/Holder;)D", at = @At("HEAD"), cancellable = true)
	protected void onGetAttributeValue(Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
		if (attribute.is(Attributes.ATTACK_DAMAGE) && !getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE) && hasEffect(ModMobEffects.FRENZY)) {
			cir.setReturnValue(FrenzySerum.ATTACK_DAMAGE_FALLBACK);
		}
	}

	@Inject(method = "isSensitiveToWater", at = @At(value = "HEAD"), cancellable = true)
	private void onIsSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
		if (hasEffect(ModMobEffects.CORROSIVE)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;blockUsingItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"))
	private void onBlockUsingShield(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Float> cir, @Local LivingEntity attacker) {
		if (useItem.getItem() instanceof ShieldBlockingListener listener) {
			listener.onShieldBlocking(useItem, (LivingEntity) (Object) this, attacker);
		}
	}

	@WrapOperation(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;FI)V"))
	private void onHurtBlockingItem(BlocksAttacks blocksAttacks, Level level, ItemStack item, LivingEntity user, InteractionHand hand, float damage, int fixedDamage, Operation<Void> original) {
		if (user instanceof Player && item.getItem() instanceof LivingShieldItem livingShield) {
			if (!level.isClientSide() && damage >= 3f) {
				livingShield.damageCurrentlyUsedLivingShield(item, damage, user);
			}
			return;
		}
		original.call(blocksAttacks, level, item, user, hand, damage, fixedDamage);
	}

	@Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "HEAD"), cancellable = true)
	private void onAddEffect(MobEffectInstance effectInstance, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
		if (source instanceof AreaEffectCloud || source instanceof AbstractThrownPotion || source instanceof Arrow) {
			if (!StatusEffectHandler.canApplySplashEffectIfAllowed(effectInstance.getEffect(), biomancy$self(), StatusEffectHandler.CONSUME_ONE_NUTRIENT_PER_ARMOR_PIECE)) {
				cir.setReturnValue(false);
			}
		}
	}

	@ModifyArg(
			method = "tickEffects",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onEffectsRemoved(Ljava/util/Collection;)V")
	)
	private Collection<MobEffectInstance> onTickEffects(Collection<MobEffectInstance> effectInstances, @Share("expiredFrenzy") LocalRef<MobEffectInstance> expiredFrenzyRef) {
		for (MobEffectInstance effectInstance : effectInstances) {
			if (effectInstance.getEffect().is(ModMobEffects.FRENZY)) {
				expiredFrenzyRef.set(effectInstance);
			}
		}
		return effectInstances;
	}

	@Inject(method = "tickEffects", at = @At(value = "TAIL"))
	private void onPostTickEffects(CallbackInfo ci, @Share("expiredFrenzy") LocalRef<MobEffectInstance> expiredFrenzyRef) {
		if (level().isClientSide()) return;

		MobEffectInstance removedFrenzyEffect = expiredFrenzyRef.get();
		if (removedFrenzyEffect == null) return;

		StatusEffectHandler.addWithdrawalAfterFrenzy(biomancy$self(), removedFrenzyEffect);
	}

	@Unique
	private LivingEntity biomancy$self() {
		return (LivingEntity) (Object) this;
	}

}
