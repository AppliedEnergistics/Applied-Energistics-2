package appeng.client.gui.style;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;

import appeng.container.SlotSemantic;

/**
 * A screen style document defines various visual aspects of AE2 screens.
 */
public class ScreenStyle {

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeAdapter(Blitter.class, BlitterDeserializer.INSTANCE)
            .registerTypeAdapter(Rectangle2d.class, Rectangle2dDeserializer.INSTANCE)
            .registerTypeAdapter(Color.class, ColorDeserializer.INSTANCE)
            .create();

    /**
     * Positioning information for groups of slots.
     */
    private final Map<SlotSemantic, SlotPosition> slots = new EnumMap<>(SlotSemantic.class);

    /**
     * Various text-labels positioned on the screen.
     */
    private final Map<String, Text> text = new HashMap<>();

    /**
     * Color-Palette for the screen.
     */
    private Map<PaletteColor, Color> palette = new EnumMap<PaletteColor, Color>(PaletteColor.class);

    /**
     * Additional images that are screen-specific.
     */
    private final Map<String, Blitter> images = new HashMap<>();

    /**
     * The screen background, which is optional. If defined, it is also used to size the dialog.
     */
    @Nullable
    private Blitter background;

    @Nullable
    private TerminalStyle terminalStyle;

    public Color getColor(PaletteColor color) {
        return palette.get(color);
    }

    public Map<SlotSemantic, SlotPosition> getSlots() {
        return slots;
    }

    public Map<String, Text> getText() {
        return text;
    }

    public Blitter getBackground() {
        return background != null ? background.copy() : null;
    }

    @Nonnull
    public Blitter getImage(String id) {
        Blitter blitter = images.get(id);
        if (blitter == null) {
            throw new IllegalStateException("Screen is missing required image " + id);
        }
        return blitter;
    }

    @Nullable
    public TerminalStyle getTerminalStyle() {
        return terminalStyle;
    }

    public void validate() {
        for (PaletteColor value : PaletteColor.values()) {
            if (!palette.containsKey(value)) {
                throw new RuntimeException("Palette is missing color " + value);
            }
        }

        if (terminalStyle != null) {
            terminalStyle.validate();
        }
    }

}
