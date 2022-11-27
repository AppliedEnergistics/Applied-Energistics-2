package appeng.client.guidebook.document;

import net.minecraft.client.Minecraft;

import appeng.client.guidebook.render.SymbolicColor;
import appeng.client.guidebook.style.ResolvedTextStyle;
import appeng.client.guidebook.style.TextStyle;
import appeng.client.guidebook.style.WhiteSpaceMode;

public class DefaultStyles {

    public static final ResolvedTextStyle BASE_STYLE = new ResolvedTextStyle(
            1,
            false,
            false,
            false,
            false,
            false,
            Minecraft.UNIFORM_FONT,
            SymbolicColor.BODY_TEXT.ref(),
            WhiteSpaceMode.NORMAL);

    public static final TextStyle BODY_TEXT = TextStyle.builder()
            .font(Minecraft.UNIFORM_FONT)
            .color(SymbolicColor.BODY_TEXT.ref())
            .build();

}
