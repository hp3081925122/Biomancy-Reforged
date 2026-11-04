package com.github.elenterius.biomancy.client.render.entity.mob.fleshblob;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.fleshblob.AdulteratedHangryEaterFleshBlob;
import com.github.elenterius.biomancy.entity.mob.fleshblob.EaterFleshBlob;
import com.github.elenterius.biomancy.init.client.ModRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import com.github.elenterius.biomancy.client.render.BiomancyGeoModel;

import java.util.Locale;

public class FleshBlobModel<T extends EaterFleshBlob> extends BiomancyGeoModel<T> {

	protected static final Identifier BASE_TEXTURE = BiomancyMod.rl("textures/entity/mob/flesh_blob/flesh_blob_neutral.png");
	protected static final Identifier HUNGRY_TEXTURE = BiomancyMod.rl("textures/entity/mob/flesh_blob/flesh_blob_hostile.png");
	protected static final Identifier CLOWN_TEXTURE = BiomancyMod.rl("textures/entity/mob/flesh_blob/flesh_blob_clown.png");
	protected static final Identifier TROLL_TEXTURE = BiomancyMod.rl("textures/entity/mob/flesh_blob/flesh_blob_troll.png");
	protected static final Identifier WATCHER_TEXTURE = BiomancyMod.rl("textures/entity/mob/flesh_blob/flesh_blob_watcher.png");

	protected static final Identifier MODEL = BiomancyMod.rl("entity/mob/flesh_blob");
	protected static final Identifier ANIMATION = BiomancyMod.rl("entity/mob/flesh_blob");

	@Override
	public Identifier getModelResource(T fleshBlob) {
		return MODEL;
	}

	@Override
	public Identifier getTextureResource(T fleshBlob) {
		if (BiomancyMod.EVENT_CALENDAR.isCarnivalStart()) return CLOWN_TEXTURE;

		if (fleshBlob.hasCustomName()) {
			String name = fleshBlob.getName().getString().toLowerCase(Locale.ENGLISH);
			if (name.equals("trololo") || name.equals("u mad bro?")) return TROLL_TEXTURE;
			if (name.contains("krusty")) return CLOWN_TEXTURE;
			if (name.contains("beholder") || name.contains("observer")) return WATCHER_TEXTURE;
		}

		return fleshBlob instanceof AdulteratedHangryEaterFleshBlob ? HUNGRY_TEXTURE : BASE_TEXTURE;
	}

	@Override
	public Identifier getAnimationResource(T fleshBlob) {
		return ANIMATION;
	}

	@Override
	public @Nullable RenderType getRenderType(T fleshBlob, Identifier texture) {
		if (BiomancyMod.EVENT_CALENDAR.isAprilFools()) return ModRenderTypes.getCutoutPartyTime(TROLL_TEXTURE);

		if (fleshBlob.hasCustomName()) {
			Component customName = fleshBlob.getCustomName();
			if (customName != null) {
				String name = customName.getContents().toString().toLowerCase(Locale.ENGLISH);
				if (name.contains("party_blob")) {
					return ModRenderTypes.getCutoutPartyTime(texture);
				}
			}
		}

		return null;
	}

}