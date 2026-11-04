package com.github.elenterius.biomancy.client.render.entity.mob.fleshblob;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.PrimordialFleshkin;
import com.github.elenterius.biomancy.entity.mob.fleshblob.EaterFleshBlob;
import com.github.elenterius.biomancy.entity.mob.fleshblob.PrimordialHangryEaterFleshBlob;
import com.github.elenterius.biomancy.init.client.ModRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import com.github.elenterius.biomancy.client.render.BiomancyGeoModel;

import java.util.Locale;

import static com.github.elenterius.biomancy.client.render.entity.mob.fleshblob.FleshBlobModel.TROLL_TEXTURE;

public class PrimordialFleshBlobModel<T extends EaterFleshBlob & PrimordialFleshkin> extends BiomancyGeoModel<T> {

	protected static final Identifier BASE_TEXTURE = BiomancyMod.rl("textures/entity/mob/flesh_blob/primordial_flesh_blob_neutral.png");
	protected static final Identifier HUNGRY_TEXTURE = BiomancyMod.rl("textures/entity/mob/flesh_blob/primordial_flesh_blob_hostile.png");

	@Override
	public Identifier getModelResource(T fleshBlob) {
		return FleshBlobModel.MODEL;
	}

	@Override
	public Identifier getTextureResource(T fleshBlob) {
		return fleshBlob instanceof PrimordialHangryEaterFleshBlob ? HUNGRY_TEXTURE : BASE_TEXTURE;
	}

	@Override
	public Identifier getAnimationResource(T fleshBlob) {
		return FleshBlobModel.ANIMATION;
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