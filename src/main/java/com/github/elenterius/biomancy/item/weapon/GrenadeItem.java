package com.github.elenterius.biomancy.item.weapon;

import net.minecraft.world.InteractionResult;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.projectile.GrenadeProjectile;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.item.CrosshairProvider;
import com.github.elenterius.biomancy.item.SimpleItem;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GrenadeItem extends SimpleItem implements CrosshairProvider {

	public static final int COOL_DOWN_TICKS = 20;

	public GrenadeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (player.getCooldowns().isOnCooldown(stack)) {
			return InteractionResult.FAIL;
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.GRENADE_THROW.get(), SoundSource.PLAYERS, 0.5f, 1f - 0.375f * level.getRandom().nextFloat());

		if (!level.isClientSide()) {
			GrenadeProjectile grenade = new GrenadeProjectile(level, player);
			grenade.setItem(stack);
			grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), -20f, 0.65f, 0.9f);
			level.addFreshEntity(grenade);

			ModItems.findItems(GrenadeItem.class).forEach(grenadeItem -> player.getCooldowns().addCooldown(new ItemStack(grenadeItem), COOL_DOWN_TICKS));
		}

		player.awardStat(Stats.ITEM_USED.get(this));

		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}

		return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
	}

	protected static final Identifier CROSSHAIR = BiomancyMod.rl("textures/gui/grenade_crosshair.png");

	@Override
	public Identifier getCrosshairTexture(ItemStack stack, Player player) {
		return CROSSHAIR;
	}

}
