package appeng.integration.modules.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import com.google.common.base.Stopwatch;
import mezz.jei.api.gui.ITooltipCallback;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CraftableCallBack implements ITooltipCallback<ItemStack> {
    private final IItemList<IAEItemStack> list;
    private final Container container;
    private final Stopwatch lastClicked = Stopwatch.createStarted();


    public CraftableCallBack(Container container, IItemList<IAEItemStack> ir) {
        this.list = ir;
        this.container = container;
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        if (!input) return;
        if (list != null) {
            IAEItemStack found = list.findPrecise(AEItemStack.fromItemStack(ingredient));
            if (found != null) {
                if (found.getStackSize() == 0) {
                    String line = "§c[" + I18n.translateToLocalFormatted("gui.appliedenergistics2.Missing") + "]";
                    tooltip.add(line);
                }
                if (found.isCraftable()) {
                    String line = "§1[" + I18n.translateToLocalFormatted("gui.tooltips.appliedenergistics2.Craftable") + "]";
                    tooltip.add(line);
                    if (Mouse.isButtonDown(2) && this.lastClicked.elapsed(TimeUnit.MILLISECONDS) > 200) {
                        this.lastClicked.reset().start();
                        ((AEBaseContainer) container).setTargetStack(found);
                        final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.AUTO_CRAFT, container.getInventory().size(), 0);
                        NetworkHandler.instance().sendToServer(p);
                    }
                }
            } else {
                String line = "§c[" + I18n.translateToLocalFormatted("gui.appliedenergistics2.Missing") + "]";
                tooltip.add(line);
            }
        }
    }
}
