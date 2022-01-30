package appeng.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.inventory.Slot;

import appeng.client.gui.style.StyleManager;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;

/**
 * Base class for screens that are opened on the client-side to temporarily replace another screen.
 */
public class AESubScreen<C extends AEBaseMenu, P extends AEBaseScreen<C>> extends AEBaseScreen<C> {
    private final P parent;
    /**
     * Keeps track of any client-side slots added by this sub-screen to clean them up when we return to the parent.
     */
    private final List<Slot> clientSideSlots = new ArrayList<>();

    public AESubScreen(P parent, String stylePath) {
        super(parent.getMenu(),
                parent.getMenu().getPlayerInventory(),
                parent.getTitle(),
                StyleManager.loadStyleDoc(stylePath));
        this.parent = parent;
    }

    public P getParent() {
        return parent;
    }

    protected final void returnToParent() {
        // Remove any client-side slots added by this screen
        for (var clientSideSlot : clientSideSlots) {
            menu.removeClientSideSlot(clientSideSlot);
        }
        clientSideSlots.clear();

        onReturnToParent();
        switchToScreen(getParent());
    }

    protected void onReturnToParent() {
    }

    protected final Slot addClientSideSlot(Slot slot, SlotSemantic semantic) {
        clientSideSlots.add(slot);
        return menu.addClientSideSlot(slot, semantic);
    }
}
