package appeng.client.gui.me.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import appeng.api.config.ActionItems;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.IPagedScreen;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.ItemListMod;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;

public class TerminalSettingsScreen<C extends MEStorageMenu> extends AESubScreen<C, MEStorageScreen<C>>
        implements IPagedScreen {

    private final String TEXT_SEARCH_SETTINGS_TITLE = "search_settings_title";

    private final AECheckbox pinAutoCraftedItemsCheckbox;
    private final AECheckbox notifyForFinishedCraftingJobsCheckbox;
    private final AECheckbox clearGridOnCloseCheckbox;

    private final AECheckbox useInternalSearchRadio;
    private final AECheckbox useExternalSearchRadio;

    private final AECheckbox rememberCheckbox;
    private final AECheckbox autoFocusCheckbox;
    private final AECheckbox syncWithExternalCheckbox;
    private final AECheckbox clearExternalCheckbox;

    private final AECheckbox autoPauseTerminalCheckbox;

    private final ActionButton nextPageButton;
    private final ActionButton previousPageButton;

    private final Object2IntOpenHashMap<AbstractWidget> pages = new Object2IntOpenHashMap<>();

    private int page;

    public TerminalSettingsScreen(MEStorageScreen<C> parent) {
        super(parent, "/screens/terminals/terminal_settings.json");
        this.page = 0;

        addBackButton();

        Component externalSearchMod;
        boolean hasExternalSearch;
        if (ItemListMod.isEnabled()) {
            externalSearchMod = Component.literal(ItemListMod.getShortName());
            hasExternalSearch = true;
        } else {
            // User doesn't have any, so disable the buttons but show what *would* be possible
            externalSearchMod = Component.literal("JEI/REI/EMI");
            hasExternalSearch = false;
        }

        pinAutoCraftedItemsCheckbox = makeCheckbox("pinAutoCraftedItemsCheckbox",
                GuiText.TerminalSettingsPinAutoCraftedItems.text(), this::save);
        notifyForFinishedCraftingJobsCheckbox = makeCheckbox("notifyForFinishedCraftingJobsCheckbox",
                GuiText.TerminalSettingsNotifyForFinishedJobs.text(), this::save);
        clearGridOnCloseCheckbox = makeCheckbox("clearGridOnCloseCheckbox",
                GuiText.TerminalSettingsClearGridOnClose.text(), this::save);

        useInternalSearchRadio = makeCheckbox("useInternalSearchRadio", GuiText.SearchSettingsUseInternalSearch.text(),
                this::switchToAeSearch);
        useInternalSearchRadio.setRadio(true);
        useExternalSearchRadio = makeCheckbox("useExternalSearchRadio",
                GuiText.SearchSettingsUseExternalSearch.text(externalSearchMod), this::switchToExternalSearch);
        useExternalSearchRadio.setRadio(true);
        useExternalSearchRadio.visible = hasExternalSearch;

        rememberCheckbox = makeCheckbox("rememberCheckbox", GuiText.SearchSettingsRememberSearch.text(), this::save);
        autoFocusCheckbox = makeCheckbox("autoFocusCheckbox", GuiText.SearchSettingsAutoFocus.text(), this::save);

        syncWithExternalCheckbox = makeCheckbox("syncWithExternalCheckbox",
                GuiText.SearchSettingsSyncWithExternal.text(externalSearchMod), this::save);
        clearExternalCheckbox = makeCheckbox("clearExternalCheckbox",
                GuiText.SearchSettingsClearExternal.text(externalSearchMod), this::save);

        autoPauseTerminalCheckbox = makeCheckbox("autoPauseTerminalCheckbox", GuiText.TerminalSettingsAutoPause.text(),
                this::save);

        nextPageButton = addToLeftToolbar(new ActionButton(ActionItems.NEXT_PAGE, () -> setCurrentPage(page + 1)));
        previousPageButton = addToLeftToolbar(new ActionButton(ActionItems.PREV_PAGE, () -> setCurrentPage(page - 1)));

        updateState();
    }

    @Override
    protected void init() {
        super.init();

        // The screen JSON includes the toolbox, but we don't actually have a need for it here
        setSlotsHidden(SlotSemantics.TOOLBOX, true);
        setSlotsHidden(SlotSemantics.PATTERNBOX, true);
    }

    private void switchToAeSearch() {
        useInternalSearchRadio.setSelected(true);
        useExternalSearchRadio.setSelected(false);
        save();
    }

    private void switchToExternalSearch() {
        useInternalSearchRadio.setSelected(false);
        useExternalSearchRadio.setSelected(true);
        save();
    }

    private void addBackButton() {
        var icon = menu.getHost().getMainMenuIcon();
        var label = icon.getHoverName();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        TabButton button = new TabButton(icon, label, btn -> returnToParent());
        widgets.add("back", button);
    }

    private void updateState() {
        pinAutoCraftedItemsCheckbox.setSelected(config.isPinAutoCraftedItems());
        notifyForFinishedCraftingJobsCheckbox.setSelected(config.isNotifyForFinishedCraftingJobs());
        clearGridOnCloseCheckbox.setSelected(config.isClearGridOnClose());

        useInternalSearchRadio.setSelected(!config.isUseExternalSearch());
        useExternalSearchRadio.setSelected(config.isUseExternalSearch());
        rememberCheckbox.setSelected(config.isRememberLastSearch());
        autoFocusCheckbox.setSelected(config.isAutoFocusSearch());
        syncWithExternalCheckbox.setSelected(config.isSyncWithExternalSearch());
        clearExternalCheckbox.setSelected(config.isClearExternalSearchOnOpen());

        autoPauseTerminalCheckbox.setSelected(config.isAutoPauseTerminal());

        int highestPage = 0;
        for (var entry : pages.object2IntEntrySet()) {
            var widget = entry.getKey();
            widget.visible = entry.getIntValue() == page;
            highestPage = Math.max(highestPage, entry.getIntValue());
        }

        // Need to do this jank-ish stuff so it doesn't override the page
        rememberCheckbox.visible = useInternalSearchRadio.isSelected() && useInternalSearchRadio.visible;
        autoFocusCheckbox.visible = useInternalSearchRadio.isSelected() && useInternalSearchRadio.visible;
        syncWithExternalCheckbox.visible = useInternalSearchRadio.isSelected() && useInternalSearchRadio.visible;

        clearExternalCheckbox.visible = useExternalSearchRadio.isSelected() && useExternalSearchRadio.visible;

        setTextHidden(TEXT_SEARCH_SETTINGS_TITLE, !useInternalSearchRadio.visible);

        nextPageButton.visible = page < highestPage;
        previousPageButton.visible = page > 0;
    }

    private void save() {
        config.setUseExternalSearch(useExternalSearchRadio.isSelected());
        config.setRememberLastSearch(rememberCheckbox.isSelected());
        config.setAutoFocusSearch(autoFocusCheckbox.isSelected());
        config.setSyncWithExternalSearch(syncWithExternalCheckbox.isSelected());
        config.setClearExternalSearchOnOpen(clearExternalCheckbox.isSelected());
        config.setPinAutoCraftedItems(pinAutoCraftedItemsCheckbox.isSelected());
        config.setNotifyForFinishedCraftingJobs(notifyForFinishedCraftingJobsCheckbox.isSelected());
        config.setClearGridOnClose(clearGridOnCloseCheckbox.isSelected());
        config.setAutoPauseTerminal(autoPauseTerminalCheckbox.isSelected());

        updateState();
    }

    private AECheckbox makeCheckbox(String id, Component text, Runnable changeListener) {
        var checkbox = widgets.addCheckbox(id, text, changeListener);
        pages.put(checkbox, style.getWidget(id).getPage());
        return checkbox;
    }

    @Override
    public int getCurrentPage() {
        return page;
    }

    @Override
    public void setCurrentPage(int page) {
        this.page = page;
        updateState();
    }
}
