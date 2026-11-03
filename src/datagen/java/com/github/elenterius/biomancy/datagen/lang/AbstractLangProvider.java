package com.github.elenterius.biomancy.datagen.lang;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.fluids.FluidType;
import org.apache.commons.lang3.text.WordUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.github.elenterius.biomancy.BiomancyMod.LOGGER;

public abstract class AbstractLangProvider implements DataProvider, LangProvider {

	private final Map<String, String> translations = new LinkedHashMap<>();
	private final PackOutput packOutput;
	protected final String modId;
	protected final String languageLocale;

	protected AbstractLangProvider(PackOutput packOutput, String modId, String languageLocale) {
		this.packOutput = packOutput;
		this.modId = modId;
		this.languageLocale = languageLocale;
	}

	protected abstract void addTranslations();

	protected abstract boolean hasMissingTranslations();

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public CompletableFuture<?> run(CachedOutput cache) {

		LinkedHashMap<String, String> preAdded = null;
		if (!translations.isEmpty()) {
			preAdded = new LinkedHashMap<>(translations);
			translations.clear();
		}

		addTranslations();

		if (preAdded != null && !preAdded.isEmpty()) {
			translations.putAll(preAdded);
		}

		if (!translations.isEmpty()) {
			JsonObject json = new JsonObject();
			translations.forEach(json::addProperty);

			if (hasMissingTranslations()) {
				LOGGER.error("Has Missing Translations!");
			}

			Path path = packOutput.getOutputFolder().resolve("assets/%s/lang/%s.json".formatted(modId, languageLocale));

			return CompletableFuture.runAsync(() -> {
				try {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), outputStream);
					JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8));
					jsonWriter.setSerializeNulls(false);
					jsonWriter.setIndent("  ");

					try {
						GsonHelper.writeValue(jsonWriter, json, null);
					}
					catch (IOException e) {
						try {
							jsonWriter.close();
						}
						catch (IOException suppressed) {
							e.addSuppressed(suppressed);
						}
						throw e;
					}

					jsonWriter.close();
					cache.writeIfNeeded(path, outputStream.toByteArray(), hashingOutputStream.hash());
				}
				catch (IOException e) {
					LOGGER.error("Failed to save file to {}", path, e);
				}

			}, Util.backgroundExecutor());
		}

		return CompletableFuture.allOf();
	}

	@Override
	public String getName() {
		return "Translation Language: " + languageLocale;
	}

	public void add(String key, String translation) {
		if (translation.contains(key)) throw new IllegalStateException("Duplicate translation key " + key);

		translations.put(key, translation);
	}

	public void addBlock(Supplier<? extends Block> supplier, String name) {
		add(supplier.get(), name);
	}

	public void add(Block block, String name) {
		add(block.getDescriptionId(), name);
	}

	public void addItem(Supplier<? extends Item> supplier, String name) {
		add(supplier.get(), name);
	}

	public void add(Item item, String name) {
		add(item.getDescriptionId(), name);
	}

	public void addItemStack(Supplier<ItemStack> supplier, String name) {
		add(supplier.get(), name);
	}

	public void add(ItemStack stack, String name) {
		add(stack.getDescriptionId(), name);
	}

	public void addEnchantment(ResourceKey<Enchantment> key, String name) {
		add(key.location().toLanguageKey("enchantment"), name);
	}

	public void addEffect(Holder<? extends MobEffect> holder, String name) {
		add(holder.value(), name);
	}

	public void add(MobEffect effect, String name) {
		add(effect.getDescriptionId(), name);
	}

	public void addEntityType(Supplier<? extends EntityType<?>> supplier, String name) {
		add(supplier.get(), name);
	}

	public void add(EntityType<?> entityType, String name) {
		add(entityType.getDescriptionId(), name);
	}

	public void addFluidType(Supplier<? extends FluidType> supplier, String name) {
		add(supplier.get(), name);
	}

	public void add(FluidType fluidType, String name) {
		add(fluidType.getDescriptionId(), name);
	}

	public void add(Component component, String translation) {
		if (!(component.getContents() instanceof TranslatableContents translatableContents)) {
			throw new IllegalArgumentException("Provided component does not contain translatable contents");
		}

		add(translatableContents.getKey(), translation);
	}

	public void addPainting(ResourceKey<PaintingVariant> key, String title, String author) {
		ResourceLocation id = key.location();
		add(id.toLanguageKey("painting", "title"), title);
		add(id.toLanguageKey("painting", "author"), author);
	}

	@SuppressWarnings("deprecation")
	public void addBannerPattern(ResourceKey<BannerPattern> key, String name) {
		ResourceLocation rl = ResourceLocation.parse(key.location().toShortLanguageKey());
		for (DyeColor dyeColor : DyeColor.values()) {
			String dyeColorName = WordUtils.capitalize(dyeColor.getName().replace("_", " "));
			add("block.%s.banner.%s.%s".formatted(rl.getNamespace(), rl.getPath(), dyeColor.getName()), dyeColorName + " " + name);
		}
	}

	public void addDeathMessage(ResourceKey<DamageType> damageType, String causedByDefault) {
		add(damageType.location().toLanguageKey("death.attack"), causedByDefault);
	}

	public void addDeathMessage(ResourceKey<DamageType> damageType, String causedByDefault, String causedByMobOrPlayer, String causedByItem) {
		ResourceLocation damageTypeId = damageType.location();
		add(damageTypeId.toLanguageKey("death.attack"), causedByDefault);
		add(damageTypeId.toLanguageKey("death.attack", "player"), causedByMobOrPlayer);
		add(damageTypeId.toLanguageKey("death.attack", "item"), causedByItem);
	}

	public void addSound(Supplier<SoundEvent> supplier, String text) {
		ResourceLocation rl = supplier.get().getLocation();
		add("sounds.%s.%s".formatted(rl.getNamespace(), rl.getPath()), text);
	}

}
