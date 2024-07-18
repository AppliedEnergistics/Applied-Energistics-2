package appeng.client.guidebook.document;

import net.minecraft.client.Minecraft;

import appeng.client.guidebook.color.ColorValue;
import appeng.client.guidebook.color.ConstantColor;
import appeng.client.guidebook.color.SymbolicColor;
import appeng.client.guidebook.style.ResolvedTextStyle;
import appeng.client.guidebook.style.TextAlignment;
import appeng.client.guidebook.style.TextStyle;
import appeng.client.guidebook.style.WhiteSpaceMode;

public final class DefaultStyles {
    private DefaultStyles() {
    }

    public static final ResolvedTextStyle BASE_STYLE = new ResolvedTextStyle(
            1,
            false,
            false,
            false,
            false,
            false,
            Minecraft.UNIFORM_FONT,
            (ColorValue) SymbolicColor.BODY_TEXT,
            WhiteSpaceMode.NORMAL,
            TextAlignment.LEFT);

    public static final TextStyle BODY_TEXT = TextStyle.builder()
            .font(Minecraft.UNIFORM_FONT)
            .color((ColorValue) SymbolicColor.BODY_TEXT)
            .build();

    public static final TextStyle CRAFTING_RECIPE_TYPE = TextStyle.builder()
            .font(Minecraft.UNIFORM_FONT)
            .color((ColorValue) SymbolicColor.CRAFTING_RECIPE_TYPE)
            .build();

    public static final TextStyle HEADING1 = TextStyle.builder()
            .fontScale(1.3f)
            .bold(true).font(Minecraft.DEFAULT_FONT)
            .color(ConstantColor.WHITE)
            .build();
    public static final TextStyle HEADING2 = TextStyle.builder()
            .fontScale(1.1f)
            .font(Minecraft.DEFAULT_FONT)
            .build();
    public static final TextStyle HEADING3 = TextStyle.builder()
            .fontScale(1f)
            .font(Minecraft.DEFAULT_FONT)
            .build();
    public static final TextStyle HEADING4 = TextStyle.builder()
            .fontScale(1.1f)
            .bold(true)
            .font(Minecraft.UNIFORM_FONT)
            .build();
    public static final TextStyle HEADING5 = TextStyle.builder()
            .fontScale(1f)
            .bold(true)
            .font(Minecraft.UNIFORM_FONT)
            .build();
    public static final TextStyle HEADING6 = TextStyle.builder()
            .fontScale(1f)
            .font(Minecraft.UNIFORM_FONT)
            .build();
}
