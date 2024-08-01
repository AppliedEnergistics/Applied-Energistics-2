package appeng.client.gui.widgets;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.api.stacks.AEKeyType;
import appeng.api.storage.ISubMenuHost;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.KeyTypeSelectionScreen;
import appeng.menu.AEBaseMenu;
import appeng.menu.interfaces.KeyTypeSelectionMenu;

public class KeyTypeSelectionButton extends IconButton {
    public static <C extends AEBaseMenu & KeyTypeSelectionMenu, P extends AEBaseScreen<C>> KeyTypeSelectionButton create(
            P parentScreen,
            ISubMenuHost subMenuHost,
            Component title) {
        return new KeyTypeSelectionButton(
                () -> {
                    if (Screen.hasShiftDown()) {
                        handleShiftClick(parentScreen.getMenu());
                    } else {
                        parentScreen.switchToScreen(new KeyTypeSelectionScreen<>(parentScreen, subMenuHost, title));
                    }
                },
                title,
                () -> {
                    return Component.literal(
                            parentScreen.getMenu().getClientKeyTypeSelection().enabledSet().stream()
                                    .map(x -> x.getDescription().getString())
                                    .collect(Collectors.joining(", ")));
                });
    }

    private static <C extends AEBaseMenu & KeyTypeSelectionMenu> void handleShiftClick(C menu) {
        // Compute new selection
        Set<AEKeyType> newSelection = getNextSelection(menu.getClientKeyTypeSelection());

        // First enable new keys
        for (var keyType : newSelection) {
            menu.selectKeyType(keyType, true);
        }
        // Only then disable old keys, to avoid disabling all keys
        for (var keyType : menu.getClientKeyTypeSelection().enabledSet()) {
            if (!newSelection.contains(keyType)) {
                menu.selectKeyType(keyType, false);
            }
        }
    }

    private static Set<AEKeyType> getNextSelection(KeyTypeSelectionMenu.SyncedKeyTypes keyTypes) {
        int totalCount = keyTypes.keyTypes().size();
        int enabledCount = keyTypes.enabledSet().size();

        if (totalCount == enabledCount) {
            // From full selection to first key only
            return Set.of(keyTypes.keyTypes().keySet().stream().findFirst().orElseThrow());
        } else if (enabledCount > 1) {
            // From mixed to full selection
            return Set.copyOf(keyTypes.keyTypes().keySet());
        } else {
            // Switch to next key
            AEKeyType currentKey = keyTypes.enabledSet().get(0);
            boolean foundCurrent = false;

            for (var keyType : keyTypes.keyTypes().keySet()) {
                if (foundCurrent) {
                    return Set.of(keyType);
                }

                if (keyType == currentKey) {
                    foundCurrent = true;
                }
            }

            // If it was the last, go back to full selection
            return Set.copyOf(keyTypes.keyTypes().keySet());
        }
    }

    private final Component title;
    private final Supplier<Component> descriptionSupplier;

    private KeyTypeSelectionButton(Runnable onPress, Component title, Supplier<Component> descriptionSupplier) {
        super(btn -> onPress.run());
        this.title = title;
        this.descriptionSupplier = descriptionSupplier;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(
                title,
                descriptionSupplier.get());
    }

    @Override
    protected ResourceLocation getSprite() {
        return Icon.TYPE_FILTER_ALL;
    }
}
