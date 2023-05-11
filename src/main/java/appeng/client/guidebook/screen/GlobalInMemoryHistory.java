package appeng.client.guidebook.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import appeng.client.guidebook.PageAnchor;

public class GlobalInMemoryHistory implements GuideScreenHistory {
    public static final GuideScreenHistory INSTANCE = new GlobalInMemoryHistory();

    private static final int HISTORY_SIZE = 100;
    private static final List<PageAnchor> history = new ArrayList<>();
    private static int historyPosition;

    @Override
    public PageAnchor get(int index) {
        return null;
    }

    @Override
    public void push(PageAnchor anchor) {
        // Remove anything from the history after the current page when we navigate to a new one
        if (historyPosition + 1 < history.size()) {
            history.subList(historyPosition + 1, history.size()).clear();
        }
        // Clamp history length
        if (history.size() >= HISTORY_SIZE) {
            history.subList(0, history.size() - HISTORY_SIZE).clear();
        }
        // Append to history
        historyPosition = history.size();
        history.add(anchor);
    }

    public Optional<PageAnchor> current() {
        if (historyPosition < history.size()) {
            return Optional.of(history.get(historyPosition));
        }
        return Optional.empty();
    }

    @Override
    public Optional<PageAnchor> forward() {
        var page = peekForward();
        if (page.isPresent()) {
            ++historyPosition;
        }
        return page;
    }

    @Override
    public Optional<PageAnchor> back() {
        var page = peekBack();
        if (page.isPresent()) {
            --historyPosition;
        }
        return page;
    }

    @Override
    public Optional<PageAnchor> peekForward() {
        if (historyPosition + 1 < history.size()) {
            return Optional.of(history.get(historyPosition + 1));
        }
        return Optional.empty();
    }

    @Override
    public Optional<PageAnchor> peekBack() {
        if (historyPosition > 0) {
            return Optional.of(history.get(historyPosition - 1));
        }
        return Optional.empty();
    }
}
