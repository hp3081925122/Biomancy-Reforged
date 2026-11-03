package com.github.elenterius.biomancy.loot;

import com.github.elenterius.biomancy.init.ModLoot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Set;

public class CopyBlockEntityDataFunction extends LootItemConditionalFunction {

	public static final MapCodec<CopyBlockEntityDataFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
			.and(Codec.STRING.listOf().fieldOf("keys").forGetter(f -> f.keys))
			.apply(instance, CopyBlockEntityDataFunction::new));

	private final List<String> keys;

	private CopyBlockEntityDataFunction(List<LootItemCondition> conditions, List<String> keys) {
		super(conditions);
		this.keys = List.copyOf(keys);
	}

	public static LootItemConditionalFunction.Builder<?> copy(String... keys) {
		return simpleBuilder(conditions -> new CopyBlockEntityDataFunction(conditions, List.of(keys)));
	}

	@Override
	public LootItemFunctionType<CopyBlockEntityDataFunction> getType() {
		return ModLoot.COPY_BLOCK_ENTITY_DATA.get();
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.BLOCK_ENTITY);
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		BlockEntity blockEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
		if (blockEntity == null) return stack;

		CompoundTag source = blockEntity.saveCustomOnly(context.getLevel().registryAccess());
		CompoundTag target = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();

		boolean changed = false;
		for (String key : keys) {
			Tag value = source.get(key);
			if (value != null) {
				target.put(key, value.copy());
				changed = true;
			}
		}

		if (changed) {
			target.remove("id");
			BlockItem.setBlockEntityData(stack, blockEntity.getType(), target);
		}

		return stack;
	}

}
