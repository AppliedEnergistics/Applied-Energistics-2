package appeng.api.integrations.curios;

import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.util.SearchInventoryEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import top.theillusivec4.curios.api.CuriosCapability;

import java.util.stream.IntStream;

public class CuriosIntegration {
    public static void register() {
        MenuLocators.register(CuriosItemLocator.class, CuriosItemLocator::writeToPacket, CuriosItemLocator::readFromPacket);

        NeoForge.EVENT_BUS.addListener((SearchInventoryEvent event) -> {
            var cap = event.getEntity().getCapability(CuriosCapability.INVENTORY);
            if (cap == null)
                return;
            var handler = event.getEntity().getCapability(CuriosCapability.ITEM_HANDLER);
            if (handler == null) {
                return;
            }
            var equipped = cap.getEquippedCurios();
            event.add(IntStream.range(0, equipped.getSlots()).mapToObj(index -> {
                return new SearchInventoryEvent.InventoryItemAccessor() {
                    @Override
                    public ItemAccess getAccess() {
                        return ItemAccess.forHandlerIndexStrict(handler, index);
                    }

                    @Override
                    public ItemMenuHostLocator createLocator() {
                        return CuriosItemLocator.forCurioSlot(index);
                    }
                };
            }));
        });
    }
}
