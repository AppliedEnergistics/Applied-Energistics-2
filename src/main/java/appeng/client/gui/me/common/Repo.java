package appeng.client.gui.me.common;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.container.me.items.GridInventoryEntry;
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

    private final BiMap<Long, Entry<T>> entries = HashBiMap.create();
    private final ArrayList<Entry<T>> view = new ArrayList<>();
    private IPartitionList<T> myPartitionList;

    private final IScrollSource src;
    private final ISortSource sortSrc;
    private boolean synchronizeWithJEI;

    public Repo(IScrollSource src, ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;
    }

    protected void setMyPartitionList(IPartitionList<T> myPartitionList) {
        this.myPartitionList = myPartitionList;
    }

    @Nullable
    protected abstract T createFromNetwork(GridInventoryEntry networkEntry);

    public final void postUpdate(GridInventoryEntry serverEntry) {

        Entry<T> localEntry = entries.get(serverEntry.getSerial());
        if (localEntry == null) {
            // First time we're seeing this serial -> create new entry
            T refStack = createFromNetwork(serverEntry);
            if (refStack == null) {
                AELog.warn("First time seeing serial %s, but incomplete info received", serverEntry.getSerial());
                return;
            }
            localEntry = new Entry<>(serverEntry.getSerial(), refStack);
            entries.put(serverEntry.getSerial(), localEntry);
        }

        localEntry.setStoredAmount(serverEntry.getStoredAmount());
        localEntry.setRequestableAmount(serverEntry.getRequestableAmount());
        localEntry.setCraftable(serverEntry.isCraftable());

        if (!localEntry.isMeaningful()) {
            entries.remove(serverEntry.getSerial());
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

        for (Entry<T> entry : this.entries.values()) {
            // TODO: This is kinda the only reason we're using an AEItemStack here
            if (this.myPartitionList != null) {
                if (!this.myPartitionList.isListed(entry.stack)) {
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

        this.view.sort(Comparator.comparing(e -> e.stack, getComparator(sortOrder, sortDir)));
    }

    @Nullable
    public final Entry<T> getEntry(int idx) {
        idx += this.src.getCurrentScroll() * this.rowSize;

        if (idx >= this.view.size()) {
            return null;
        }
        return this.view.get(idx);
    }

    public final T get(int idx) {
        idx += this.src.getCurrentScroll() * this.rowSize;

        if (idx >= this.view.size()) {
            return null;
        }
        return this.view.get(idx).stack;
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

    public static final class Entry<T> {
        private final long serial;
        private final T stack;
        private long storedAmount;
        private long requestableAmount;
        private boolean craftable;

        public Entry(long serial, T stack) {
            this.serial = serial;
            this.stack = stack;
        }

        public long getSerial() {
            return serial;
        }

        public T getStack() {
            return stack;
        }

        public long getStoredAmount() {
            return storedAmount;
        }

        public void setStoredAmount(long storedAmount) {
            this.storedAmount = storedAmount;
        }

        public long getRequestableAmount() {
            return requestableAmount;
        }

        public void setRequestableAmount(long requestableAmount) {
            this.requestableAmount = requestableAmount;
        }

        public boolean isCraftable() {
            return craftable;
        }

        public void setCraftable(boolean craftable) {
            this.craftable = craftable;
        }

        public boolean isMeaningful() {
            return storedAmount > 0 || requestableAmount > 0 || craftable;
        }
    }

}
