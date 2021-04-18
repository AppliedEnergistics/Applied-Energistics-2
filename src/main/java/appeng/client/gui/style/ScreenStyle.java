package appeng.client.gui.style;

import appeng.container.SlotSemantic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A screen style document defines various visual aspects of AE2 screens.
 */
public class ScreenStyle {

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeAdapter(Palette.class, new Palette.Deserializer())
            .registerTypeAdapter(Blitter.class, BlitterDeserializer.INSTANCE)
            .registerTypeAdapter(Rectangle2d.class, Rectangle2dDeserializer.INSTANCE)
            .create();

    /**
     * A screen style can include other screen styles, these are merged together on load.
     */
    private final List<String> includes = new ArrayList<>();

    /**
     * Positioning information for groups of slots.
     */
    private final Map<SlotSemantic, SlotPosition> slots = new EnumMap<>(SlotSemantic.class);

    /**
     * Various text-labels positioned on the screen.
     */
    private final List<Text> text = new ArrayList<>();

    /**
     * Color-Palette for the screen.
     */
    private Palette palette;

    /**
     * The screen background, which is optional.
     * If defined, it is also used to size the dialog.
     */
    @Nullable
    private Blitter background;

    /**
     * Additional images that are screen-specific.
     */
    private final Map<String, Blitter> images = new HashMap<>();

    public List<String> getIncludes() {
        return includes;
    }

    public Color getColor(PaletteColor color) {
        return palette.get(color);
    }

    public Map<SlotSemantic, SlotPosition> getSlots() {
        return slots;
    }

    public List<Text> getText() {
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

    public ScreenStyle merge(ScreenStyle otherStyle) {
        ScreenStyle merged = new ScreenStyle();
        merged.background = background;
        if (otherStyle.background != null) {
            merged.background = otherStyle.background;
        }

        // Images are merged by overwriting
        merged.images.putAll(this.images);
        merged.images.putAll(otherStyle.images);

        // Slots are merged by simply overwriting
        merged.slots.putAll(this.slots);
        merged.slots.putAll(otherStyle.slots);

        // Text is combined
        merged.text.addAll(text);
        merged.text.addAll(otherStyle.text);

        // The palette is a bit harder to merge since it can be null
        if (this.palette != null) {
            if (otherStyle.palette != null) {
                merged.palette = this.palette.merge(otherStyle.palette);
            } else {
                merged.palette = palette;
            }
        } else {
            merged.palette = otherStyle.palette;
        }

        return merged;
    }

    public void validate() {
        if (palette == null) {
            throw new IllegalStateException("Palette is missing.");
        }
    }

}
