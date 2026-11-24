package com.github.elenterius.biomancy.client.util;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public final class ClientTextUtil {

	private static final String MAIN_HAND_ITEM_ACTION_KEY = BiomancyMod.translationKey("key", "main_hand_item_action");
	private static final String EQUIPPED_ARMOR_ACTION_KEY = BiomancyMod.translationKey("key", "equipped_armor_action");

	private static final MutableComponent CTRL_KEY_TEXT = BiomancyMod.translatableFrom("keyboard", "ctrl");
	private static final MutableComponent ALT_KEY_TEXT = BiomancyMod.translatableFrom("keyboard", "alt");
	private static final MutableComponent SHIFT_KEY_TEXT = BiomancyMod.translatableFrom("keyboard", "shift");
	private static final MutableComponent RIGHT_MOUSE_KEY_TEXT = BiomancyMod.translatableFrom("keyboard", "right_mouse");
	private static final MutableComponent SHOW_INFO = BiomancyMod.translatableFrom("tooltip", "action.show_info");

	private ClientTextUtil() {}

	private static MutableComponent getItemTooltip(ItemStack stack) {
		Item item = stack.getItem();

		if (item instanceof ItemTooltipStyleProvider iTooltip) {
			return iTooltip.getTooltipText(stack);
		}

		return TextComponentUtil.getItemTooltip(item);
	}

	public static boolean hasControlDown() {
		return FMLEnvironment.getDist().isClient() && ClientOnly.hasControlDown();
	}

	public static boolean hasAltDown() {
		return FMLEnvironment.getDist().isClient() && ClientOnly.hasAltDown();
	}

	public static Locale getLocale() {
		return FMLEnvironment.getDist().isClient() ? ClientOnly.getLocale() : Locale.US;
	}

	public static List<Component> getItemInfoTooltip(ItemStack stack) {
		if (hasControlDown()) {
			return splitLinesByNewLine(getItemTooltip(stack).withStyle(TextStyles.LORE));
		}

		return List.of(holdButtonTo(CTRL_KEY_TEXT.plainCopy(), SHOW_INFO).withStyle(TextStyles.LORE));
	}

	public static boolean showItemInfo(Consumer<Component> tooltip) {
		boolean flag = hasControlDown();
		if (!flag) tooltip.accept(holdButtonTo(CTRL_KEY_TEXT.plainCopy(), SHOW_INFO).withStyle(TextStyles.LORE));
		return flag;
	}

	public static boolean showExtraInfo(Consumer<Component> tooltip) {
		boolean flag = hasAltDown();
		if (!flag) tooltip.accept(holdButtonTo(ALT_KEY_TEXT.plainCopy(), SHOW_INFO).withStyle(TextStyles.LORE));
		return flag;
	}

	public static MutableComponent holdButtonTo(MutableComponent key, Object action) {
		return ComponentUtil.translatable(BiomancyMod.translationKey("tooltip", "hold_button_to"), key.withStyle(TextStyles.KEYBOARD_INPUT), action);
	}

	public static MutableComponent pressButtonTo(MutableComponent key, Object action) {
		return ComponentUtil.translatable(BiomancyMod.translationKey("tooltip", "press_button_to"), key.withStyle(TextStyles.KEYBOARD_INPUT), action);
	}

	public static MutableComponent getAltKey() {
		return ALT_KEY_TEXT.plainCopy();
	}

	public static MutableComponent getCtrlKey() {
		return CTRL_KEY_TEXT.plainCopy();
	}

	public static MutableComponent getShiftKey() {
		return SHIFT_KEY_TEXT.plainCopy();
	}

	public static MutableComponent getRightMouseKey() {
		return RIGHT_MOUSE_KEY_TEXT.plainCopy();
	}

	public static MutableComponent getDefaultKey() {
		// 直接使用按键翻译键构造 keybind 组件，避免在专用服触发 ModKeyBindings 和 KeyMapping 类加载
		return ComponentUtil.keybind(MAIN_HAND_ITEM_ACTION_KEY);
	}

	public static MutableComponent getEquippedArmorActionKey() {
		return ComponentUtil.keybind(EQUIPPED_ARMOR_ACTION_KEY);
	}

	public static String tryToGetPlayerNameOnClientSide(UUID uuid) {
		if (FMLEnvironment.getDist().isClient()) {
			return ClientOnly.tryToGetPlayerName(uuid);
		}
		return uuid.toString();
	}

	public static List<Component> splitLinesByNewLine(Component component) {
		Locale locale = getLocale();
		String text = component.getString();
		Style style = component.getStyle();
		return ComponentUtil.splitLines(locale, text, style);
	}

	public static List<Component> splitLinesByNewLine(String text, Style style) {
		Locale locale = getLocale();
		return ComponentUtil.splitLines(locale, text, style);
	}

	public static List<Component> splitLines(Component component, int maxLength) {
		Locale locale = getLocale();
		String text = component.getString();
		Style style = component.getStyle();
		return ComponentUtil.splitLines(locale, text, maxLength, style);
	}

	public static List<Component> splitLines(String text, int maxLength, Style style) {
		Locale locale = getLocale();
		return ComponentUtil.splitLines(locale, text, maxLength, style);
	}

	// 将所有仅客户端类调用隔离在静态内部类中，确保专用服调用物品提示构建时不加载客户端类
	private static final class ClientOnly {
		private ClientOnly() {}

		private static boolean hasControlDown() {
			net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
			return minecraft != null && minecraft.hasControlDown();
		}

		private static boolean hasAltDown() {
			net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
			return minecraft != null && minecraft.hasAltDown();
		}

		private static Locale getLocale() {
			net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
			return minecraft != null ? minecraft.getLocale() : Locale.US;
		}

		private static String tryToGetPlayerName(UUID uuid) {
			net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
			if (minecraft != null && minecraft.level != null) {
				Player player = minecraft.level.getPlayerByUUID(uuid);
				if (player != null) {
					return player.getGameProfile().name();
				}
			}
			return uuid.toString();
		}
	}

}
