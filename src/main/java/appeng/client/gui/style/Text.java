package appeng.client.gui.style;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class Text {

    private ITextComponent text = StringTextComponent.EMPTY;

    private PaletteColor color;

    private Position position;

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
