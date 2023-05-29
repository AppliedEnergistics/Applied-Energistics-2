package appeng.client.guidebook.scene.annotation;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.document.block.LytBlock;

/**
 * An annotation to show additional information to the user about content in a
 * {@link appeng.client.guidebook.scene.GuidebookScene}.
 */
public abstract class SceneAnnotation {
    @Nullable
    private LytBlock content;

    private boolean hovered;

    /**
     * Additional content describing what this annotation means. Will be shown in tooltip.
     */
    @Nullable
    public final LytBlock getContent() {
        return content;
    }

    public final void setContent(@Nullable LytBlock content) {
        this.content = content;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }
}
