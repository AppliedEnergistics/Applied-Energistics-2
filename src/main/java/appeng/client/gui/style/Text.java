package appeng.client.gui.style;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Text that will be drawn on top of a {@link appeng.client.gui.AEBaseScreen}.
 */
public class Text {

    /**
     * The text to display.
     */
    private ITextComponent text = StringTextComponent.EMPTY;

    /**
     * The color to show the text in.
     */
    private PaletteColor color = PaletteColor.DEFAULT_TEXT_COLOR;

    /**
     * The position of the text on the screen.
     */
    private Position position;

    /**
     * Center on the computed x position.
     */
    private boolean centerHorizontally;

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

    public boolean isCenterHorizontally() {
        return centerHorizontally;
    }

    public void setCenterHorizontally(boolean centerHorizontally) {
        this.centerHorizontally = centerHorizontally;
    }
}
