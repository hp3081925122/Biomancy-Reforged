package com.github.elenterius.biomancy.util.ownable;

import com.github.elenterius.biomancy.util.BlockItemUtil;
import net.minecraft.core.UUIDUtil;
import com.github.elenterius.biomancy.block.ownable.OwnableBlockEntity;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.permission.UserType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.Locale;

public interface OwnableEntityBlock extends EntityBlock {

	String NBT_KEY_OWNER = "OwnerUUID";
	String NBT_KEY_USER_LIST = "UserList";
	String NBT_KEY_USER = "UserUUID";

	static void setBlockEntityOwner(Level level, Ownable ownable, @Nullable LivingEntity placer, ItemStack stack) {
		if (placer == null) return;

		CompoundTag entityData = BlockItemUtil.getBlockEntityData(stack);
		boolean containsOwner = entityData != null && entityData.read(OwnableEntityBlock.NBT_KEY_OWNER, UUIDUtil.CODEC).isPresent();
		if (!containsOwner && !ownable.hasOwner()) {
			ownable.setOwner(placer.getUUID());
		}
	}

	static void dropForCreativePlayer(Level worldIn, Block block, BlockPos pos, Player player) {
		if (!worldIn.isClientSide() && player.isCreative()) {
			if (worldIn instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.BLOCK_DROPS)) {
				if (worldIn.getBlockEntity(pos) instanceof OwnableBlockEntity blockEntity) {
					ItemStack stack = new ItemStack(block);
					BlockItemUtil.saveToItem(blockEntity, stack, worldIn.registryAccess());
					if (!stack.getComponentsPatch().isEmpty()) {
						ItemEntity itemEntity = new ItemEntity(worldIn, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, stack);
						itemEntity.setDefaultPickUpDelay();
						worldIn.addFreshEntity(itemEntity);
					}
				}
			}
		}
	}

	static void appendUserListToTooltip(ItemStack stack, Consumer<Component> tooltip) {
		CompoundTag entityData = BlockItemUtil.getBlockEntityData(stack);
		if (entityData == null) return;

		appendUserListToTooltip(entityData, tooltip);
	}

	static void appendUserListToTooltip(CompoundTag entityData, Consumer<Component> tooltip) {

		String ownerName = "NULL";
		if (entityData.read(NBT_KEY_OWNER, UUIDUtil.CODEC).isPresent()) {
			ownerName = ClientTextUtil.tryToGetPlayerNameOnClientSide(entityData.read(NBT_KEY_OWNER, UUIDUtil.CODEC).orElseThrow());
		}

		tooltip.accept(ComponentUtil.EMPTY_LINE);
		tooltip.accept(TextComponentUtil.getTooltipText("owner", ComponentUtil.literal(ownerName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));

		if (entityData.contains(NBT_KEY_USER_LIST)) {
			ListTag nbtList = entityData.getListOrEmpty(NBT_KEY_USER_LIST);
			tooltip.accept(ComponentUtil.literal("Users: ").withStyle(ChatFormatting.GRAY));
			int limit = Minecraft.getInstance().hasControlDown() ? Math.min(5, nbtList.size()) : nbtList.size();
			for (int i = 0; i < limit; i++) {
				CompoundTag userNbt = nbtList.getCompoundOrEmpty(i);
				String userName = ClientTextUtil.tryToGetPlayerNameOnClientSide(userNbt.read(NBT_KEY_USER, UUIDUtil.CODEC).orElseThrow());
				UserType level = UserType.fromTag(userNbt);
				tooltip.accept(ComponentUtil.literal(String.format(" - %s (%s)", userName, level.name().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.GRAY));
			}
			int remainder = nbtList.size() - limit;
			if (remainder > 0) {
				tooltip.accept(ClientTextUtil.holdButtonTo(ClientTextUtil.getCtrlKey(), "show " + remainder + " more users..."));
			}
		}
	}

}
