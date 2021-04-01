package appeng.client.gui.me.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.integration.abstraction.JEIFacade;
import appeng.util.prioritylist.IPartitionList;

/**
 * For showing the network content of a storage channel, this class will maintain a client-side copy of the current
 * server-side storage, which is continuously synchronized to the client while it is open.
 */
public abstract class Repo<T extends IAEStack<T>> {

    private int rowSize = 9;

    private String searchString = "";
    private boolean hasPower;

    private final IItemList<T> list;
    private final ArrayList<T> view = new ArrayList<>();
    private IPartitionList<T> myPartitionList;

    private final IScrollSource src;
    private final ISortSource sortSrc;
    private boolean synchronizeWithJEI;

    public Repo(Class<? extends IStorageChannel<T>> storageChannel, IScrollSource src, ISortSource sortSrc) {
        this.list = Api.instance().storage().getStorageChannel(storageChannel)
                .createList();
        this.src = src;
        this.sortSrc = sortSrc;
    }

    protected void setMyPartitionList(IPartitionList<T> myPartitionList) {
        this.myPartitionList = myPartitionList;
    }

    public final void postUpdate(T is) {
        T st = list.findPrecise(is);

        if (st != null) {
            st.reset();
            st.add(is);
        } else {
            list.add(is);
        }
    }

    public final void updateView() {
        this.view.clear();

        this.view.ensureCapacity(this.list.size());

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
        boolean needsZeroStackSize = viewMode == ViewItems.CRAFTABLE;

        for (T stack : this.list) {
            if (this.myPartitionList != null) {
                if (!this.myPartitionList.isListed(stack)) {
                    continue;
                }
            }

            if (viewMode == ViewItems.CRAFTABLE && !stack.isCraftable()) {
                continue;
            }

            if (viewMode == ViewItems.STORED && stack.getStackSize() == 0) {
                continue;
            }

            if (matchesSearch(searchMode, m, stack)) {
                if (needsZeroStackSize && stack.getStackSize() > 0) {
                    stack = stack.copy().setStackSize(0);
                }

                this.view.add(stack);
            }
        }

        SortOrder sortOrder = this.sortSrc.getSortBy();
        SortDir sortDir = this.sortSrc.getSortDir();

        this.view.sort(getComparator(sortOrder, sortDir));
    }

    public final T get(int idx) {
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
        this.list.resetStatus();
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
