package com.github.elenterius.biomancy.datagen.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public record EnhancedTagAppender<T>(TagAppender<T, T> delegate, TagBuilder builder, Registry<T> registry, String modId) {

	public EnhancedTagAppender<T> addTag(TagKey<T> tagKey) {
		if (isValidNamespace(tagKey.location().getNamespace())) {
			delegate.addTag(tagKey);
		}
		else {
			addOptionalTag(tagKey.location());
		}
		return this;
	}

	@SafeVarargs
	public final EnhancedTagAppender<T> addTag(TagKey<T>... tagKeys) {
		for (TagKey<T> tagKey : tagKeys) {
			addTag(tagKey);
		}
		return this;
	}

	@SafeVarargs
	public final EnhancedTagAppender<T> add(T... entries) {
		for (T entry : entries) {
			add(entry);
		}
		return this;
	}

	public EnhancedTagAppender<T> add(Stream<T> stream) {
		stream.forEach(this::add);
		return this;
	}

	public EnhancedTagAppender<T> add(Collection<T> entries) {
		for (T entry : entries) {
			add(entry);
		}
		return this;
	}

	public EnhancedTagAppender<T> add(T entry) {
		Identifier registryName = Objects.requireNonNull(registry.getKey(entry));
		if (isValidNamespace(registryName.getNamespace())) {
			delegate.add(entry);
		}
		else {
			addOptional(registryName);
		}
		return this;
	}

	private boolean isValidNamespace(String namespace) {
		return "c".equals(namespace) || "minecraft".equals(namespace) || modId.equals(namespace);
	}

	@SafeVarargs
	public final EnhancedTagAppender<T> add(ResourceKey<T>... resourceKeys) {
		for (ResourceKey<T> resourceKey : resourceKeys) {
			if (isValidNamespace(resourceKey.identifier().getNamespace())) {
				builder.addElement(resourceKey.identifier());
			}
			else {
				addOptional(resourceKey.identifier());
			}
		}
		return this;
	}

	public EnhancedTagAppender<T> addOptional(Identifier resourceLocation) {
		builder.addOptionalElement(resourceLocation);
		return this;
	}

	public EnhancedTagAppender<T> addOptional(Identifier... resourceLocations) {
		for (Identifier resourceLocation : resourceLocations) {
			builder.addOptionalElement(resourceLocation);
		}
		return this;
	}

	public EnhancedTagAppender<T> addOptional(String... resourceLocations) {
		for (String resourceLocation : resourceLocations) {
			builder.addOptionalElement(Identifier.parse(resourceLocation));
		}
		return this;
	}

	public EnhancedTagAppender<T> addOptional(DeferredHolder<?, ?>... entries) {
		for (DeferredHolder<?, ?> deferredHolder : entries) {
			builder.addOptionalElement(deferredHolder.getId());
		}
		return this;
	}

	public EnhancedTagAppender<T> addOptionalTag(Identifier resourceLocation) {
		builder.addOptionalTag(resourceLocation);
		return this;
	}

	public EnhancedTagAppender<T> addOptionalTag(Identifier... resourceLocations) {
		for (Identifier resourceLocation : resourceLocations) {
			builder.addOptionalTag(resourceLocation);
		}
		return this;
	}

	public EnhancedTagAppender<T> addOptionalTag(String... resourceLocations) {
		for (String resourceLocation : resourceLocations) {
			builder.addOptionalTag(Identifier.parse(resourceLocation));
		}
		return this;
	}

	public TagBuilder getInternalBuilder() {
		return builder;
	}

	public String getModId() {
		return modId;
	}

	public EnhancedTagAppender<T> replace() {
		builder.setReplace(true);
		return this;
	}

	public EnhancedTagAppender<T> replace(boolean bool) {
		builder.setReplace(bool);
		return this;
	}

	public EnhancedTagAppender<T> remove(T entry) {
		builder.removeElement(Objects.requireNonNull(registry.getKey(entry)));
		return this;
	}

	@SafeVarargs
	public final EnhancedTagAppender<T> remove(T first, T... entries) {
		remove(first);
		for (T entry : entries) {
			remove(entry);
		}
		return this;
	}

	public EnhancedTagAppender<T> remove(Identifier resourceLocation) {
		builder.removeElement(resourceLocation);
		return this;
	}

	public EnhancedTagAppender<T> remove(Identifier first, Identifier... resourceLocations) {
		remove(first);
		for (Identifier resourceLocation : resourceLocations) {
			remove(resourceLocation);
		}
		return this;
	}

	public EnhancedTagAppender<T> remove(TagKey<T> tagKey) {
		builder.removeTag(tagKey.location());
		return this;
	}

	@SafeVarargs
	public final EnhancedTagAppender<T> remove(TagKey<T> first, TagKey<T>... tagKeys) {
		remove(first);
		for (TagKey<T> tagKey : tagKeys) {
			remove(tagKey);
		}
		return this;
	}

}
