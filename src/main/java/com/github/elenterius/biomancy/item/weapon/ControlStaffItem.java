package com.github.elenterius.biomancy.item.weapon;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.entity.mob.ControllableMob;
import com.github.elenterius.biomancy.entity.mob.FleshkinHumanoid;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import com.github.elenterius.biomancy.item.KeyPressListener;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.ownable.OwnableMob;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ControlStaffItem extends Item implements KeyPressListener, ItemTooltipStyleProvider {

	public ControlStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public KeyPressResult onClientKeyPress(ItemStack stack, Level level, Player player, EquipmentSlot slot, byte flags) {
		if (slot != EquipmentSlot.MAINHAND) return KeyPressResult.fail();

		ControllableMob.Command command = getCommand(stack).cycle();
		player.playSound(SoundEvents.GENERIC_HURT, 0.8f, 0.25f + level.getRandom().nextFloat() * 0.25f);
		return KeyPressResult.success(command.serialize());
	}

	@Override
	public void onServerReceiveKeyPress(ItemStack stack, ServerLevel level, Player player, byte flags) {
		ControllableMob.Command command = ControllableMob.Command.deserialize(flags);
		setCommand(stack, command);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Player player = context.getPlayer();
		if (context.getLevel().isClientSide() || player == null) return InteractionResult.FAIL;

		List<Mob> mobs = context.getLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(16d),
				mob -> mob instanceof ControllableMob && mob instanceof OwnableMob ownable && ownable.isOwner(player));

		int size = mobs.size();
		if (size == 0) {
			MutableComponent component = ComponentUtil.literal("Couldn't find any controllable mobs nearby!").withStyle(ChatFormatting.RED);
			player.sendOverlayMessage(component);
			return InteractionResult.FAIL;
		}

		ControllableMob.Command command = getCommand(stack);
		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
		for (Mob mob : mobs) {
			ControllableMob<Mob> controllable = ControllableMob.cast(mob);
			controllable.updateRestriction(command, pos);
			controllable.setActiveCommand(command);
		}

		FleshkinHumanoid.displayCommandSetMsg(player, ComponentUtil.literal(size + (size > 1 ? " mobs" : " mob")), command);
		return InteractionResult.SUCCESS;
	}

	public ControllableMob.Command getCommand(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.CONTROL_STAFF_COMMAND, ControllableMob.Command.deserialize((byte) 0));
	}

	public void setCommand(ItemStack stack, ControllableMob.Command command) {
		stack.set(ModDataComponents.CONTROL_STAFF_COMMAND, command);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);
		tooltip.accept(ComponentUtil.EMPTY_LINE);
		tooltip.accept(TextComponentUtil.getTooltipText("command").append(": ").withStyle().withStyle(ChatFormatting.GRAY).append(ComponentUtil.literal(getCommand(stack).toString()).withStyle(ChatFormatting.AQUA)));
		tooltip.accept(ClientTextUtil.pressButtonTo(ClientTextUtil.getDefaultKey(), TextComponentUtil.getActionText("cycle")).withStyle(ChatFormatting.DARK_GRAY));
	}

}
