package appeng.integration.modules.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import mezz.jei.api.gui.ITooltipCallback;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.input.Mouse;

import java.util.List;

public class CraftableCallBack implements ITooltipCallback<ItemStack> {
    private final IItemList<IAEItemStack> list;
    private final Container container;

    public CraftableCallBack(Container container, IItemList<IAEItemStack> ir) {
        this.list = ir;
        this.container = container;
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        if (list != null) {
            AEItemStack stack = AEItemStack.fromItemStack(ingredient);
            if (list.findPrecise(stack) != null) {
                tooltip.add(I18n.translateToLocalFormatted("gui.tooltips.appliedenergistics2.Craftable"));
                if (Mouse.isButtonDown(2)) {
                    ((AEBaseContainer) container).setTargetStack(stack);
                    final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.AUTO_CRAFT, container.getInventory().size(), 0);
                    NetworkHandler.instance().sendToServer(p);
                }
            }
        }
    }
}
