package appeng.client.guidebook.style;

import appeng.client.guidebook.render.ColorRef;
import net.minecraft.resources.ResourceLocation;

public record ResolvedTextStyle(
        float fontScale,
        boolean bold,
        boolean italic,
        boolean underlined,
        boolean strikethrough,
        boolean obfuscated,
        ResourceLocation font,
        ColorRef color) {
}
