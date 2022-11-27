package appeng.client.guidebook.style;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.render.ColorRef;

public record ResolvedTextStyle(
        float fontScale,
        boolean bold,
        boolean italic,
        boolean underlined,
        boolean strikethrough,
        boolean obfuscated,
        ResourceLocation font,
        ColorRef color,
        WhiteSpaceMode whiteSpace) {
}
