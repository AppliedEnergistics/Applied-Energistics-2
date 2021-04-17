package appeng.client.gui;

import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

/**
 * Properties of {@link appeng.client.gui.style.Text} can be overridden by their ID.
 * This class stores those overrides.
 */
public class TextOverride {

    /**
     * If this is not-null, this overrides the content to be displayed.
     */
    @Nullable
    private ITextComponent content;

    /**
     * If true, the text will not be drawn.
     */
    private boolean hidden;

    @Nullable
    public ITextComponent getContent() {
        return content;
    }

    public void setContent(@Nullable ITextComponent content) {
        this.content = content;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

}
