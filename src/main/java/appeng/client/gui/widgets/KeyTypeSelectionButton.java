package appeng.client.gui.widgets;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;

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
                () -> parentScreen.switchToScreen(new KeyTypeSelectionScreen<>(parentScreen, subMenuHost, title)),
                title,
                () -> {
                    return Component.literal(
                            parentScreen.getMenu().getClientKeyTypeSelection().enabledSet().stream()
                                    .map(x -> x.getDescription().getString())
                                    .collect(Collectors.joining(", ")));
                });
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
    protected Icon getIcon() {
        return Icon.TYPE_FILTER_ALL;
    }
}
