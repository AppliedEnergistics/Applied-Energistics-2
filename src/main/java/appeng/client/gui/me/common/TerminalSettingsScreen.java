package appeng.client.gui.me.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.JEIFacade;
import appeng.integration.abstraction.REIFacade;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;

public class TerminalSettingsScreen<C extends MEStorageMenu> extends AESubScreen<C, MEStorageScreen<C>> {

    private final AECheckbox pinAutoCraftedItemsCheckbox;
    private final AECheckbox notifyForFinishedCraftingJobsCheckbox;
    private final AECheckbox clearGridOnCloseCheckbox;

    private final AECheckbox useInternalSearchRadio;
    private final AECheckbox useExternalSearchRadio;

    private final AECheckbox rememberCheckbox;
    private final AECheckbox autoFocusCheckbox;
    private final AECheckbox syncWithExternalCheckbox;
    private final AECheckbox clearExternalCheckbox;
    private final AECheckbox searchTooltipsCheckbox;

    public TerminalSettingsScreen(MEStorageScreen<C> parent) {
        super(parent, "/screens/terminals/terminal_settings.json");

        addBackButton();

        Component externalSearchMod;
        boolean hasExternalSearch;
        if (JEIFacade.instance().isEnabled()) {
            externalSearchMod = Component.literal("JEI");
            hasExternalSearch = true;
        } else if (REIFacade.instance().isEnabled()) {
            externalSearchMod = Component.literal("REI");
            hasExternalSearch = true;
        } else {
            // User doesn't have either, so disable the buttons but show what *would* be possible
            externalSearchMod = Component.literal("JEI/REI");
            hasExternalSearch = false;
        }

        pinAutoCraftedItemsCheckbox = widgets.addCheckbox("pinAutoCraftedItemsCheckbox",
                GuiText.TerminalSettingsPinAutoCraftedItems.text(), this::save);
        notifyForFinishedCraftingJobsCheckbox = widgets.addCheckbox("notifyForFinishedCraftingJobsCheckbox",
                GuiText.TerminalSettingsNotifyForFinishedJobs.text(), this::save);
        clearGridOnCloseCheckbox = widgets.addCheckbox("clearGridOnCloseCheckbox",
                GuiText.TerminalSettingsClearGridOnClose.text(), this::save);

        useInternalSearchRadio = widgets.addCheckbox("useInternalSearchRadio",
                GuiText.SearchSettingsUseInternalSearch.text(), this::switchToAeSearch);
        useInternalSearchRadio.setRadio(true);
        useExternalSearchRadio = widgets.addCheckbox("useExternalSearchRadio",
                GuiText.SearchSettingsUseExternalSearch.text(externalSearchMod), this::switchToExternalSearch);
        useExternalSearchRadio.setRadio(true);
        useExternalSearchRadio.active = hasExternalSearch;

        searchTooltipsCheckbox = widgets.addCheckbox("searchTooltipsCheckbox",
                GuiText.SearchSettingsSearchTooltips.text(), this::save);
        rememberCheckbox = widgets.addCheckbox("rememberCheckbox", GuiText.SearchSettingsRememberSearch.text(),
                this::save);
        autoFocusCheckbox = widgets.addCheckbox("autoFocusCheckbox", GuiText.SearchSettingsAutoFocus.text(),
                this::save);

        syncWithExternalCheckbox = widgets.addCheckbox("syncWithExternalCheckbox",
                GuiText.SearchSettingsSyncWithExternal.text(externalSearchMod), this::save);
        clearExternalCheckbox = widgets.addCheckbox("clearExternalCheckbox",
                GuiText.SearchSettingsClearExternal.text(externalSearchMod), this::save);

        updateState();
    }

    @Override
    protected void init() {
        super.init();

        // The screen JSON includes the toolbox, but we don't actually have a need for it here
        setSlotsHidden(SlotSemantics.TOOLBOX, true);
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
        TabButton button = new TabButton(icon, label, itemRenderer, btn -> returnToParent());
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
        searchTooltipsCheckbox.setSelected(config.isSearchTooltips());

        rememberCheckbox.visible = useInternalSearchRadio.isSelected();
        autoFocusCheckbox.visible = useInternalSearchRadio.isSelected();
        syncWithExternalCheckbox.visible = useInternalSearchRadio.isSelected();

        clearExternalCheckbox.visible = useExternalSearchRadio.isSelected();
    }

    private void save() {
        config.setUseExternalSearch(useExternalSearchRadio.isSelected());
        config.setRememberLastSearch(rememberCheckbox.isSelected());
        config.setAutoFocusSearch(autoFocusCheckbox.isSelected());
        config.setSyncWithExternalSearch(syncWithExternalCheckbox.isSelected());
        config.setClearExternalSearchOnOpen(clearExternalCheckbox.isSelected());
        config.setSearchTooltips(searchTooltipsCheckbox.isSelected());
        config.setPinAutoCraftedItems(pinAutoCraftedItemsCheckbox.isSelected());
        config.setNotifyForFinishedCraftingJobs(notifyForFinishedCraftingJobsCheckbox.isSelected());
        config.setClearGridOnClose(clearGridOnCloseCheckbox.isSelected());

        updateState();
    }
}
