package appeng.client.gui.style;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Text that will be drawn on top of a {@link appeng.client.gui.AEBaseScreen}.
 */
public class Text {

    /**
     * A unique id for overriding properties of a text component.
     */
    private String id;

    /**
     * The text to display.
     */
    private ITextComponent text = StringTextComponent.EMPTY;

    /**
     * The color to show the text in.
     */
    private PaletteColor color;

    /**
     * The position of the text on the screen.
     */
    private Position position;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ITextComponent getText() {
        return text;
    }

    public void setText(ITextComponent text) {
        this.text = text;
    }

    public PaletteColor getColor() {
        return color;
    }

    public void setColor(PaletteColor color) {
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

}
