package appeng.client.guidebook.document.interaction;

import appeng.client.guidebook.screen.GuideScreen;

import java.util.Optional;

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
