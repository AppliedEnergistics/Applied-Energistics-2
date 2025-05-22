package appeng.client.guidebook.style;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.color.ColorValue;

public record ResolvedTextStyle(
        float fontScale,
        boolean bold,
        boolean italic,
        boolean underlined,
        boolean strikethrough,
        boolean obfuscated,
        ResourceLocation font,
        ColorValue color,
        WhiteSpaceMode whiteSpace,
        TextAlignment alignment) {
}
