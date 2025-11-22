package appeng.client.gui.me.common;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import appeng.api.config.ActionItems;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.IPagedScreen;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextDisplayWidget;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.IntegerTextField;
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

    private final IntegerTextField numberWidgetConfigInput;

    private final AETextDisplayWidget textDisplayWidgetConfig;
    private final AETextDisplayWidget textDisplayConfigBase;
    private final AETextDisplayWidget textDisplayConfigAlt;

    private final Button[] numberConfigButtons;

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

        // Config for NumberEntryWidget
        textDisplayWidgetConfig = makeTextDisplay("textDisplayConfig",
                GuiText.TerminalSettingsNumberWidgetTitle.text());

        numberWidgetConfigInput = makeIntegerTextField("numberWidgetConfigInput", 4, (text) -> {
        });

        textDisplayConfigBase = makeTextDisplay("textDisplayConfigBase",
                GuiText.TerminalSettingsNumberWidgetBase.text());
        textDisplayConfigAlt = makeTextDisplay("textDisplayConfigAlt", GuiText.TerminalSettingsNumberWidgetAlt.text());

        numberConfigButtons = new Button[] {
                makeButton("saveBaseNumber1", GuiText.TerminalSettingsSaveValue.text("1"),
                        () -> this.saveConfigValue(0)),
                makeButton("saveBaseNumber2", GuiText.TerminalSettingsSaveValue.text("2"),
                        () -> this.saveConfigValue(1)),
                makeButton("saveBaseNumber3", GuiText.TerminalSettingsSaveValue.text("3"),
                        () -> this.saveConfigValue(2)),
                makeButton("saveBaseNumber4", GuiText.TerminalSettingsSaveValue.text("4"),
                        () -> this.saveConfigValue(3)),
                makeButton("saveAltNumber1", GuiText.TerminalSettingsSaveValue.text("1"),
                        () -> this.saveConfigValue(4)),
                makeButton("saveAltNumber2", GuiText.TerminalSettingsSaveValue.text("2"),
                        () -> this.saveConfigValue(5)),
                makeButton("saveAltNumber3", GuiText.TerminalSettingsSaveValue.text("3"),
                        () -> this.saveConfigValue(6)),
                makeButton("saveAltNumber4", GuiText.TerminalSettingsSaveValue.text("4"),
                        () -> this.saveConfigValue(7)),

                makeButton("loadBaseNumber1", GuiText.TerminalSettingsLoadValue.text("1"),
                        () -> this.loadConfigValue(0)),
                makeButton("loadBaseNumber2", GuiText.TerminalSettingsLoadValue.text("2"),
                        () -> this.loadConfigValue(1)),
                makeButton("loadBaseNumber3", GuiText.TerminalSettingsLoadValue.text("3"),
                        () -> this.loadConfigValue(2)),
                makeButton("loadBaseNumber4", GuiText.TerminalSettingsLoadValue.text("4"),
                        () -> this.loadConfigValue(3)),
                makeButton("loadAltNumber1", GuiText.TerminalSettingsLoadValue.text("1"),
                        () -> this.loadConfigValue(4)),
                makeButton("loadAltNumber2", GuiText.TerminalSettingsLoadValue.text("2"),
                        () -> this.loadConfigValue(5)),
                makeButton("loadAltNumber3", GuiText.TerminalSettingsLoadValue.text("3"),
                        () -> this.loadConfigValue(6)),
                makeButton("loadAltNumber4", GuiText.TerminalSettingsLoadValue.text("4"),
                        () -> this.loadConfigValue(7)),
        };

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

    private void loadConfigValue(int index) {
        int value = config.getNumberWidgetValue(index);
        numberWidgetConfigInput.setIntValue(value);
    }

    private void saveConfigValue(int index) {
        int value = numberWidgetConfigInput.getIntValue();
        config.setNumberWidgetValue(value, index);
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

    private IntegerTextField makeIntegerTextField(String id, int maxLength, Consumer<String> onChanged) {
        var widget = widgets.addIntegerTextField(id, font, maxLength, onChanged);
        pages.put(widget, style.getWidget(id).getPage());
        return widget;
    }

    private AETextDisplayWidget makeTextDisplay(String id, Component text) {
        var widget = widgets.addTextDisplay(id, text);
        pages.put(widget, style.getWidget(id).getPage());
        return widget;
    }

    private Button makeButton(String id, Component text, Runnable onClicked) {
        var button = widgets.addButton(id, text, onClicked);
        pages.put(button, style.getWidget(id).getPage());
        return button;
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
