package appeng.integration.modules.jei;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.google.common.base.Stopwatch;
import mezz.jei.api.gui.ITooltipCallback;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.lwjgl.input.Mouse;

import java.util.Collection;
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

            IItemList<IAEItemStack> available = mergeInventories(list, (ContainerMEMonitorable) container);

            IAEItemStack search = AEItemStack.fromItemStack(ingredient);
            if (ingredient.getItem().isDamageable() || Platform.isGTDamageableItem(ingredient.getItem())) {
                Collection<IAEItemStack> fuzzy = available.findFuzzy(search, FuzzyMode.IGNORE_ALL);
                if (fuzzy.size() > 0) {
                    for (IAEItemStack itemStack : fuzzy) {
                        if (itemStack.getStackSize() > 0) {
                            if (Platform.isGTDamageableItem(ingredient.getItem())) {
                                if (!(ingredient.getMetadata() == itemStack.getDefinition().getMetadata())) {
                                    continue;
                                }
                            }

                            break;
                        } else {
                            String line = "§c[" + I18n.translateToLocalFormatted("gui.appliedenergistics2.Missing") + "]";
                            tooltip.add(line);
                            if (itemStack.isCraftable()) {
                                line = "§1[" + I18n.translateToLocalFormatted("gui.tooltips.appliedenergistics2.Craftable") + "]";
                                tooltip.add(line);
                                if (Mouse.isButtonDown(2) && this.lastClicked.elapsed(TimeUnit.MILLISECONDS) > 200) {
                                    this.lastClicked.reset().start();
                                    ((AEBaseContainer) container).setTargetStack(itemStack);
                                    final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.AUTO_CRAFT, container.getInventory().size(), 0);
                                    NetworkHandler.instance().sendToServer(p);
                                }
                            }
                        }
                    }
                } else {
                    String line = "§c[" + I18n.translateToLocalFormatted("gui.appliedenergistics2.Missing") + "]";
                    tooltip.add(line);
                }
            } else {
                IAEItemStack found = available.findPrecise(search);
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

    IItemList<IAEItemStack> mergeInventories(IItemList<IAEItemStack> repo, ContainerMEMonitorable containerCraftingTerm) {
        IItemList<IAEItemStack> itemList = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
        for (IAEItemStack i : repo) {
            itemList.addStorage(i);
        }

        PlayerMainInvWrapper invWrapper = new PlayerMainInvWrapper(containerCraftingTerm.getPlayerInv());
        for (int i = 0; i < invWrapper.getSlots(); i++) {
            itemList.addStorage(AEItemStack.fromItemStack(invWrapper.getStackInSlot(i)));
        }

        if (containerCraftingTerm instanceof IContainerCraftingPacket) {
            IItemHandler itemHandler = ((IContainerCraftingPacket) containerCraftingTerm).getInventoryByName("crafting");
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                itemList.addStorage(AEItemStack.fromItemStack(itemHandler.getStackInSlot(i)));
            }
        }
        return itemList;
    }
}
