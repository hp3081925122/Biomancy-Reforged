package com.github.elenterius.biomancy.datagen.particles;

import net.minecraft.server.packs.resources.ResourceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class ParticleSpriteProvider implements DataProvider {

	private static final Logger LOGGER = LogManager.getLogger(ParticleSpriteProvider.class);
	private final PackOutput packOutput;
	private final String modId;
	private final ResourceManager resourceManager;

	private final Map<Identifier, ParticleSprite> particles = new LinkedHashMap<>();

	protected ParticleSpriteProvider(PackOutput packOutput, String modId, ResourceManager resourceManager) {
		this.packOutput = packOutput;
		this.modId = modId;
		this.resourceManager = resourceManager;
	}

	private static <O extends ParticleOptions> Identifier getId(ParticleType<O> particleType) {
		Identifier key = BuiltInRegistries.PARTICLE_TYPE.getKey(particleType);
		if (key == null) {
			throw new IllegalStateException("No registry key found for the particle type: " + particleType);
		}
		return key;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		particles.clear();
		registerParticles();
		validate();
		if (!particles.isEmpty()) {
			return generate(cachedOutput);
		}

		return CompletableFuture.allOf();
	}

	private CompletableFuture<?> generate(final CachedOutput cache) {
		List<CompletableFuture<?>> futures = new ArrayList<>();

		for (Map.Entry<Identifier, ParticleSprite> entry : particles.entrySet()) {
			Identifier particleId = entry.getKey();
			ParticleSprite particleSprite = entry.getValue();
			futures.add(DataProvider.saveStable(cache, particleSprite.toJson(), getPath(particleId)));
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	private Path getPath(Identifier particleId) {
		return packOutput.getOutputFolder().resolve("assets/" + particleId.getNamespace() + "/particles/" + particleId.getPath() + ".json");
	}

	@Override
	public String getName() {
		return StringUtils.capitalize(modId) + " Particle Sprite Frames";
	}

	public abstract void registerParticles();

	protected void addParticle(final DeferredHolder<ParticleType<?>, ? extends ParticleType<?>> registryObject) {
		Identifier id = registryObject.getId();
		addParticle(id, ParticleSprite.create(id));
	}

	protected void addParticle(final DeferredHolder<ParticleType<?>, ? extends ParticleType<?>> registryObject, String texture) {
		Identifier id = registryObject.getId();
		addParticle(id, ParticleSprite.create(texture));
	}

	protected void addParticle(final DeferredHolder<ParticleType<?>, ? extends ParticleType<?>> registryObject, Identifier texture) {
		Identifier id = registryObject.getId();
		addParticle(id, ParticleSprite.create(texture));
	}

	protected void addParticle(final DeferredHolder<ParticleType<?>, ? extends ParticleType<?>> registryObject, int frameCount, int frameStartNumber) {
		Identifier id = registryObject.getId();
		addParticle(id, ParticleSprite.create(id, frameCount, frameStartNumber));
	}

	protected void addParticle(final DeferredHolder<ParticleType<?>, ? extends ParticleType<?>> registryObject, int frameCount) {
		Identifier id = registryObject.getId();
		addParticle(id, ParticleSprite.create(id, frameCount));
	}

	protected <O extends ParticleOptions> void addParticle(ParticleType<O> particleType, int frameCount) {
		Identifier id = getId(particleType);
		addParticle(id, ParticleSprite.create(id, frameCount));
	}

	protected void addParticle(Identifier key, ParticleSprite particleSprite) {
		if (particles.put(key, particleSprite) != null) {
			throw new IllegalStateException("Particle '%s' already exists for mod %s".formatted(key, modId));
		}
	}

	private void validate() {
		final List<String> invalidParticles = particles.entrySet().stream()
				.filter(entry -> !validate(entry.getKey(), entry.getValue()))
				.map(Map.Entry::getKey)
				.map(Identifier::toString)
				.toList();

		if (!invalidParticles.isEmpty()) {
			throw new IllegalStateException("Found invalid Particle Sprite Frames: " + invalidParticles);
		}
	}

	private boolean validate(final Identifier particleId, final ParticleSprite particleSprite) {
		boolean isValid = true;
		for (String sprite : particleSprite.spriteFrames) {
			Identifier spriteResource = Identifier.parse(sprite);
			boolean valid = resourceManager.getResource(spriteResource.withPath(path -> "textures/particle/" + path + ".png")).isPresent();
			if (!valid) {
				final String path = "%s:textures/particle/%s.png".formatted(spriteResource.getNamespace(), spriteResource.getPath());
				LOGGER.warn("Unable to find sprite frame '{}' for particle '{}'", path, particleId);
				isValid = false;
			}
		}

		return isValid;
	}

	protected record ParticleSprite(Identifier texture, int frameCount, String[] spriteFrames) {

		public static ParticleSprite create(String texture) {
			Identifier rl = Identifier.parse(texture);
			return new ParticleSprite(rl, 1, new String[]{rl.toString()});
		}

		public static ParticleSprite create(Identifier texture) {
			return new ParticleSprite(texture, 1, new String[]{texture.toString()});
		}

		public static ParticleSprite create(Identifier texture, int frameCount) {
			String[] sprites = createSpriteFrames(texture, frameCount, 0);
			return new ParticleSprite(texture, frameCount, sprites);
		}

		public static ParticleSprite create(Identifier texture, int frameCount, int frameStartNumber) {
			String[] sprites = createSpriteFrames(texture, frameCount, frameStartNumber);
			return new ParticleSprite(texture, frameCount, sprites);
		}

		private static String[] createSpriteFrames(Identifier texture, int frameCount, int frameStartNumber) {
			String[] sprites = new String[frameCount];
			String baseTexture = texture.toString();

			for (int i = 0; i < frameCount; i++) {
				int frameNumber = frameStartNumber + i;
				sprites[i] = baseTexture + "_" + frameNumber;
			}

			return sprites;
		}

		public JsonElement toJson() {
			JsonArray array = new JsonArray();
			Arrays.stream(spriteFrames).forEachOrdered(array::add);
			JsonObject json = new JsonObject();
			json.add("textures", array);
			return json;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof ParticleSprite that)) return false;
			return frameCount == that.frameCount && Objects.equals(texture, that.texture);
		}

		@Override
		public int hashCode() {
			return Objects.hash(texture, frameCount);
		}

		@Override
		public String toString() {
			return "ParticleSprite{texture=%s, frameCount=%d, spriteFrames=%s}".formatted(texture, frameCount, Arrays.toString(spriteFrames));
		}
	}
}
