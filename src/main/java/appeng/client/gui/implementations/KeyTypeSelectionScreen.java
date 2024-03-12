package appeng.client.gui.implementations;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.ISubMenuHost;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.AEBaseMenu;
import appeng.menu.interfaces.KeyTypeSelectionMenu;

public class KeyTypeSelectionScreen<C extends AEBaseMenu & KeyTypeSelectionMenu, P extends AEBaseScreen<C>>
        extends AESubScreen<C, P> {
    private final KeyTypeCheckboxes keyTypesWidget = new KeyTypeCheckboxes();

    public KeyTypeSelectionScreen(P parent, ISubMenuHost subMenuHost, Component dialogTitle) {
        super(parent, "/screens/key_type_selection.json");

        addBackButton(subMenuHost);

        widgets.add("keytypes", keyTypesWidget);

        setTextContent("dialog_title", dialogTitle);
    }

    private void addBackButton(ISubMenuHost subMenuHost) {
        var icon = subMenuHost.getMainMenuIcon();
        var label = icon.getHoverName();
        TabButton button = new TabButton(icon, label, btn -> returnToParent());
        widgets.add("back", button);
    }

    private void setHeight(int height) {
        this.style.getGeneratedBackground().setHeight(height);
        this.imageHeight = height;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        int selectedEntryCount = 0;
        AECheckbox selectedEntry = null;

        for (var entry : keyTypesWidget.checkboxes.entrySet()) {
            boolean selected = getMenu().getClientKeyTypeSelection().keyTypes().get(entry.getKey());
            entry.getValue().setSelected(selected);
            entry.getValue().active = true;

            if (selected) {
                selectedEntryCount++;
                selectedEntry = entry.getValue();
            }
        }

        // Prevent disabling all the key types
        if (selectedEntryCount == 1) {
            selectedEntry.active = false;
        }
    }

    private class KeyTypeCheckboxes implements ICompositeWidget {
        private static final int PADDING = 6;
        private static final int KEY_TYPE_SPACING = AECheckbox.SIZE + PADDING;

        private Rect2i bounds = new Rect2i(0, 0, 0, 0);
        private final Map<AEKeyType, AECheckbox> checkboxes = new LinkedHashMap<>();

        @Override
        public void setPosition(Point position) {
            bounds = new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
        }

        @Override
        public void setSize(int width, int height) {
            bounds = new Rect2i(bounds.getX(), bounds.getY(), width, height);
        }

        @Override
        public Rect2i getBounds() {
            return bounds;
        }

        @Override
        public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
            int xPos = this.bounds.getX() + bounds.getX();
            int yPos = this.bounds.getY() + bounds.getY();

            checkboxes.clear();

            for (var keyType : getMenu().getClientKeyTypeSelection().keyTypes().keySet()) {
                var text = keyType.getDescription();

                // Dynamic width based on text size
                int textboxWidth = AECheckbox.SIZE + 2 + Minecraft.getInstance().font.width(text);

                var checkbox = new AECheckbox(xPos, yPos, textboxWidth, AECheckbox.SIZE, screen.getStyle(),
                        keyType.getDescription());
                checkbox.setChangeListener(() -> getMenu().selectKeyType(keyType, checkbox.isSelected()));
                addWidget.accept(checkbox);
                checkboxes.put(keyType, checkbox);

                yPos += KEY_TYPE_SPACING;
            }

            int height = this.bounds.getY() + AEKeyTypes.getAll().size() * KEY_TYPE_SPACING + PADDING;
            KeyTypeSelectionScreen.this.setHeight(height);
        }
    }
}
