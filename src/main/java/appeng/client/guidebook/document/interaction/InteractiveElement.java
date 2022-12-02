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

    default Optional<GuideTooltip> getTooltip() {
        return Optional.empty();
    }
}
