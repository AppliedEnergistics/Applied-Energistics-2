package appeng.client.gui.me.common;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.container.me.common.GridInventoryEntry;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.integration.abstraction.JEIFacade;
import appeng.util.prioritylist.IPartitionList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * For showing the network content of a storage channel, this class will maintain a client-side copy of the current
 * server-side storage, which is continuously synchronized to the client while it is open.
 */
public abstract class Repo<T extends IAEStack<T>> {

    private int rowSize = 9;

    private String searchString = "";
    private boolean hasPower;

    private final BiMap<Long, GridInventoryEntry<T>> entries = HashBiMap.create();
    private final ArrayList<GridInventoryEntry<T>> view = new ArrayList<>();
    private IPartitionList<T> partitionList;

    private final IScrollSource src;
    private final ISortSource sortSrc;
    private boolean synchronizeWithJEI;

    public Repo(IScrollSource src, ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;
    }

    public void setPartitionList(IPartitionList<T> partitionList) {
        if (partitionList != this.partitionList) {
            this.partitionList = partitionList;
            this.updateView();
        }
    }

    public final void postUpdate(GridInventoryEntry<T> serverEntry) {

        GridInventoryEntry<T> localEntry = entries.get(serverEntry.getSerial());
        if (localEntry == null) {
            // First time we're seeing this serial -> create new entry
            if (serverEntry.getStack() == null) {
                AELog.warn("First time seeing serial %s, but incomplete info received", serverEntry.getSerial());
                return;
            }
            if (serverEntry.isMeaningful()) {
                entries.put(serverEntry.getSerial(), serverEntry);
            }
            return;
        }

        // Update the local entry
        if (!serverEntry.isMeaningful()) {
            entries.remove(serverEntry.getSerial());
        } else if (serverEntry.getStack() == null) {
            entries.put(serverEntry.getSerial(), new GridInventoryEntry<>(
                    serverEntry.getSerial(),
                    localEntry.getStack(),
                    serverEntry.getStoredAmount(),
                    serverEntry.getRequestableAmount(),
                    serverEntry.isCraftable()
            ));
        } else {
            entries.put(serverEntry.getSerial(), serverEntry);
        }
    }

    public final void updateView() {
        this.view.clear();

        this.view.ensureCapacity(this.entries.size());

        this.updateJEI(this.searchString);

        SearchMode searchMode = SearchMode.NAME;
        if (AEConfig.instance().getSearchTooltips() != YesNo.NO) {
            searchMode = SearchMode.NAME_OR_TOOLTIP;
        }

        String innerSearch = this.searchString;
        if (innerSearch.startsWith("@")) {
            searchMode = SearchMode.MOD;
            innerSearch = innerSearch.substring(1);
        }

        Pattern m;
        try {
            m = Pattern.compile(innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ignored) {
            m = Pattern.compile(Pattern.quote(innerSearch.toLowerCase()), Pattern.CASE_INSENSITIVE);
        }

        ViewItems viewMode = this.sortSrc.getSortDisplay();

        for (GridInventoryEntry<T> entry : this.entries.values()) {
            if (this.partitionList != null) {
                if (!this.partitionList.isListed(entry.getStack())) {
                    continue;
                }
            }

            if (viewMode == ViewItems.CRAFTABLE && !entry.isCraftable()) {
                continue;
            }

            if (viewMode == ViewItems.STORED && entry.getStoredAmount() == 0) {
                continue;
            }

            if (matchesSearch(searchMode, m, entry.getStack())) {
                this.view.add(entry);
            }
        }

        SortOrder sortOrder = this.sortSrc.getSortBy();
        SortDir sortDir = this.sortSrc.getSortDir();

        this.view.sort(Comparator.comparing(GridInventoryEntry::getStack, getComparator(sortOrder, sortDir)));
    }

    @Nullable
    public final GridInventoryEntry<T> get(int idx) {
        idx += this.src.getCurrentScroll() * this.rowSize;

        if (idx >= this.view.size()) {
            return null;
        }
        return this.view.get(idx);
    }

    public final int size() {
        return this.view.size();
    }

    public final void clear() {
        this.entries.clear();
        this.view.clear();
    }

    public final boolean hasPower() {
        return this.hasPower;
    }

    public final void setPower(final boolean hasPower) {
        this.hasPower = hasPower;
    }

    public final int getRowSize() {
        return this.rowSize;
    }

    public final void setRowSize(final int rowSize) {
        this.rowSize = rowSize;
    }

    public final String getSearchString() {
        return this.searchString;
    }

    public final void setSearchString(@Nonnull final String searchString) {
        this.searchString = searchString;
    }

    private void updateJEI(String filter) {
        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        if (synchronizeWithJEI && searchMode.isRequiresJei()) {
            JEIFacade.instance().setSearchText(filter);
        }
    }

    protected final void setSynchronizeWithJEI(boolean enable) {
        this.synchronizeWithJEI = enable;
    }

    protected abstract boolean matchesSearch(SearchMode searchMode, Pattern searchPattern, T stack);

    protected abstract Comparator<? super T> getComparator(SortOrder sortBy, SortDir sortDir);

    protected enum SearchMode {
        MOD,
        NAME,
        NAME_OR_TOOLTIP
    }

}
