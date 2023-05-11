package appeng.client.guidebook.screen;

import java.util.Optional;

import appeng.client.guidebook.PageAnchor;

public interface GuideScreenHistory {
    /**
     * Append to history if it's not already appended
     */
    void push(PageAnchor anchor);

    PageAnchor get(int index);

    Optional<PageAnchor> current();

    Optional<PageAnchor> forward();

    Optional<PageAnchor> peekForward();

    Optional<PageAnchor> back();

    Optional<PageAnchor> peekBack();
}
