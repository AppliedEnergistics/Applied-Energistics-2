package appeng.items.contents;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;

public class PatternBoxMenuHost extends ItemMenuHost implements InternalInventoryHost {
    private final AppEngInternalInventory inventory;
    @Nullable
    private IInWorldGridNodeHost gridHost;

    public PatternBoxMenuHost(Player player, @Nullable Integer slot, ItemStack itemStack,
            @Nullable IInWorldGridNodeHost host) {
        super(player, slot, itemStack);
        this.gridHost = host;
        this.inventory = new AppEngInternalInventory(this, 27);
        this.inventory.setEnableClientEvents(true);
        this.inventory.setFilter(new PatternBoxInventoryFilter());
        if (itemStack.hasTag()) {
            this.inventory.readFromNBT(itemStack.getOrCreateTag(), "inv");
        }
    }

    @Override
    public void saveChanges() {
        this.inventory.writeToNBT(getItemStack().getOrCreateTag(), "inv");
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
    }

    @Nullable
    public IInWorldGridNodeHost getGridHost() {
        return this.gridHost;
    }

    public InternalInventory getInventory() {
        return this.inventory;
    }

    private static class PatternBoxInventoryFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return PatternDetailsHelper.isEncodedPattern(stack);
        }
    }
}
