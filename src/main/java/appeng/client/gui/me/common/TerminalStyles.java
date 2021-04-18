package appeng.client.gui.me.common;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TerminalStyle;
import net.minecraft.client.renderer.Rectangle2d;

import appeng.client.gui.me.fluids.FluidStackSizeRenderer;

import java.util.function.Function;

public final class TerminalStyles {

    private TerminalStyles() {
    }

    public static Function<ScreenStyle, TerminalStyle> ITEM_TERMINAL = style -> new TerminalStyle(
            style,
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSupportsAutoCrafting(true);

    public static Function<ScreenStyle, TerminalStyle> PORTABLE_CELL = style -> new TerminalStyle(
            style,
            9,
            new Rectangle2d(80, 4, 90, 12),
            3);

    public static Function<ScreenStyle, TerminalStyle> WIRELESS_TERMINAL = style -> new TerminalStyle(
            style,
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSupportsAutoCrafting(true);

    public static Function<ScreenStyle, TerminalStyle> SECURITY_STATION = style -> new TerminalStyle(
            style,
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSortByButton(false);

    public static Function<ScreenStyle, TerminalStyle> CRAFTING_TERMINAL = style -> new TerminalStyle(
            style,
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSupportsAutoCrafting(true);

    public static Function<ScreenStyle, TerminalStyle> PATTERN_TERMINAL = style -> new TerminalStyle(
            style,
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSupportsAutoCrafting(true);

    public static Function<ScreenStyle, TerminalStyle> FLUID_TERMINAL = style -> new TerminalStyle(
            style,
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setStackSizeRenderer(new FluidStackSizeRenderer()).setShowTooltipsWithItemInHand(true);

}
