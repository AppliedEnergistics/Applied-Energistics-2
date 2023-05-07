package appeng.client.guidebook.document.interaction;

import java.util.Optional;

import appeng.client.guidebook.screen.GuideScreen;

public interface InteractiveElement {
    default boolean mouseClicked(GuideScreen screen, int x, int y, int button) {
        return false;
    }

    default boolean mouseReleased(GuideScreen screen, int x, int y, int button) {
        return false;
    }

    /**
     * @param x X position of the mouse in document coordinates.
     * @param y Y position of the mouse in document coordinates.
     */
    default Optional<GuideTooltip> getTooltip(float x, float y) {
        return Optional.empty();
    }
}
