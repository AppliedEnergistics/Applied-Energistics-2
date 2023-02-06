package appeng.core.api;


import appeng.api.config.IncludeExclude;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IClientHelper;
import appeng.core.localization.GuiText;
import appeng.fluids.items.FluidDummyItem;
import appeng.fluids.util.AEFluidStack;
import appeng.util.item.AEItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;

import java.util.Collection;
import java.util.List;


public class ApiClientHelper implements IClientHelper {
    @Override
    public <T extends IAEStack<T>> void addCellInformation(ICellInventoryHandler<T> handler, List<String> lines) {
        if (handler == null) {
            return;
        }

        final ICellInventory<?> cellInventory = handler.getCellInv();

        if (cellInventory != null) {
            lines.add(cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal());

            lines.add(cellInventory.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalItemTypes() + ' ' + GuiText.Types
                    .getLocal());
        }

        IItemList<?> itemList = cellInventory.getChannel().createList();

        if (handler.isPreformatted()) {
            final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded).getLocal();

            if (handler.isFuzzy()) {
                lines.add("[" + GuiText.Partitioned.getLocal() + "]" + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
            } else {
                lines.add("[" + GuiText.Partitioned.getLocal() + "]" + " - " + list + ' ' + GuiText.Precise.getLocal());
            }

            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                IItemHandler inv = cellInventory.getConfigInventory();
                cellInventory.getAvailableItems((IItemList) itemList);
                for (int i = 0; i < inv.getSlots(); i++) {
                    final ItemStack is = inv.getStackInSlot(i);
                    if (!is.isEmpty()) {
                        if (cellInventory.getChannel() instanceof IItemStorageChannel) {
                            if (!handler.isFuzzy()) {
                                final IAEItemStack ais = AEItemStack.fromItemStack(is);
                                IAEItemStack stocked = ((IItemList<IAEItemStack>) itemList).findPrecise(ais);
                                lines.add("[" + is.getDisplayName() + "]" + ": " + (stocked == null ? "0" : String.valueOf(stocked.getStackSize())));
                            } else {
                                final IAEItemStack ais = AEItemStack.fromItemStack(is);
                                Collection<IAEItemStack> stocked = ((IItemList<IAEItemStack>) itemList).findFuzzy(ais, handler.getCellInv().getFuzzyMode());

                                int[] ids = OreDictionary.getOreIDs(is);
                                long size = 0;
                                for (IAEItemStack ist : stocked) {
                                    size += ist.getStackSize();
                                }

                                if (is.getItem().isDamageable()) {
                                    lines.add("[" + is.getDisplayName() + "]" + ": " + size);
                                } else if (ids.length > 0) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int j : ids) {
                                        sb.append(OreDictionary.getOreName(j)).append(", ");
                                    }
                                    lines.add("[{" + sb.substring(0, sb.length() - 2) + "}]" + ": " + size);
                                }
                            }
                        } else if (cellInventory.getChannel() instanceof IFluidStorageChannel) {
                            final AEFluidStack ais;
                            if (is.getItem() instanceof FluidDummyItem) {
                                ais = AEFluidStack.fromFluidStack(((FluidDummyItem) is.getItem()).getFluidStack(is));
                            } else {
                                ais = AEFluidStack.fromFluidStack(FluidUtil.getFluidContained(is));
                            }
                            IAEFluidStack stocked = ((IItemList<IAEFluidStack>) itemList).findPrecise(ais);
                            lines.add("[" + is.getDisplayName() + "]" + ": " + (stocked == null ? "0" : String.valueOf(stocked.getStackSize())));
                        }
                    }
                }
            }
        } else {
            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                cellInventory.getAvailableItems((IItemList) itemList);
                for (IAEStack<?> s : itemList) {
                    if (s instanceof IAEItemStack) {
                        lines.add(((IAEItemStack) s).getDefinition().getDisplayName() + ": " + s.getStackSize());
                    } else if (s instanceof IAEFluidStack) {
                        lines.add(((IAEFluidStack) s).getFluidStack().getLocalizedName() + ": " + s.getStackSize() + "mB");
                    }
                }
            }
        }
    }
}
