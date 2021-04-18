package appeng.client.gui.style;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;

import appeng.container.SlotSemantic;

public class ScreenStyle {

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeAdapter(Palette.class, new Palette.Deserializer())
            .registerTypeAdapter(Blitter.class, BlitterDeserializer.INSTANCE)
            .registerTypeAdapter(Rectangle2d.class, Rectangle2dDeserializer.INSTANCE)
            .create();

    private final Map<SlotSemantic, SlotPosition> slots = new EnumMap<>(SlotSemantic.class);

    private final List<String> includes = new ArrayList<>();

    private final List<Text> text = new ArrayList<>();

    private Palette palette;

    @Nullable
    private Blitter background;

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
        return background;
    }

    public void setBackground(Blitter background) {
        this.background = background;
    }

    public ScreenStyle merge(ScreenStyle otherStyle) {
        ScreenStyle merged = new ScreenStyle();
        merged.background = background;
        if (otherStyle.background != null) {
            merged.background = otherStyle.background;
        }

        // Slots are merged by simply overwriting
        merged.slots.putAll(this.slots);
        merged.slots.putAll(otherStyle.slots);

        // Text is combined
        merged.text.addAll(text);
        merged.text.addAll(otherStyle.text);

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
