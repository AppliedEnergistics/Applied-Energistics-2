package appeng.client.gui.me.common;

import net.minecraft.client.renderer.Rectangle2d;

import appeng.client.gui.me.fluids.FluidStackSizeRenderer;
import appeng.client.gui.style.Blitter;

public final class TerminalStyles {

    private TerminalStyles() {
    }

    private static final String ITEM_TERMINAL_BACKGROUND = "guis/terminal.png";

    public static TerminalStyle ITEM_TERMINAL = new TerminalStyle(
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 0, 195, 17),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 17, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 35, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 53, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 71, 195, 97),
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSupportsAutoCrafting(true);

    public static TerminalStyle PORTABLE_CELL = new TerminalStyle(
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 0, 195, 17),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 17, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 35, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 53, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 71, 195, 97),
            9,
            new Rectangle2d(80, 4, 90, 12),
            3);

    public static TerminalStyle WIRELESS_TERMINAL = new TerminalStyle(
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 0, 195, 17),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 17, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 35, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 53, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 71, 195, 97),
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSupportsAutoCrafting(true);

    private static final String SECURITY_STATION_BACKGROUND = "guis/security_station.png";

    public static TerminalStyle SECURITY_STATION = new TerminalStyle(
            Blitter.texture(SECURITY_STATION_BACKGROUND).src(0, 0, 195, 17),
            Blitter.texture(SECURITY_STATION_BACKGROUND).src(0, 17, 195, 18),
            Blitter.texture(SECURITY_STATION_BACKGROUND).src(0, 35, 195, 18),
            Blitter.texture(SECURITY_STATION_BACKGROUND).src(0, 53, 195, 18),
            Blitter.texture(SECURITY_STATION_BACKGROUND).src(0, 71, 195, 130),
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSortByButton(false);

    private static final String CRAFTING_TERMINAL_BACKGROUND = "guis/crafting.png";

    public static TerminalStyle CRAFTING_TERMINAL = new TerminalStyle(
            Blitter.texture(CRAFTING_TERMINAL_BACKGROUND).src(0, 0, 195, 17),
            Blitter.texture(CRAFTING_TERMINAL_BACKGROUND).src(0, 17, 195, 18),
            Blitter.texture(CRAFTING_TERMINAL_BACKGROUND).src(0, 35, 195, 18),
            Blitter.texture(CRAFTING_TERMINAL_BACKGROUND).src(0, 53, 195, 18),
            Blitter.texture(CRAFTING_TERMINAL_BACKGROUND).src(0, 71, 195, 170),
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSupportsAutoCrafting(true);

    private static final String PATTERN_TERMINAL_BACKGROUND = "guis/pattern.png";

    public static TerminalStyle PATTERN_TERMINAL = new TerminalStyle(
            Blitter.texture(PATTERN_TERMINAL_BACKGROUND).src(0, 0, 195, 17),
            Blitter.texture(PATTERN_TERMINAL_BACKGROUND).src(0, 17, 195, 18),
            Blitter.texture(PATTERN_TERMINAL_BACKGROUND).src(0, 35, 195, 18),
            Blitter.texture(PATTERN_TERMINAL_BACKGROUND).src(0, 53, 195, 18),
            Blitter.texture(PATTERN_TERMINAL_BACKGROUND).src(0, 71, 195, 178),
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setSupportsAutoCrafting(true);

    public static TerminalStyle FLUID_TERMINAL = new TerminalStyle(
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 0, 195, 17),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 17, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 35, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 53, 195, 18),
            Blitter.texture(ITEM_TERMINAL_BACKGROUND).src(0, 71, 195, 97),
            9,
            new Rectangle2d(80, 4, 90, 12),
            null).setStackSizeRenderer(new FluidStackSizeRenderer()).setShowTooltipsWithItemInHand(true);

}
