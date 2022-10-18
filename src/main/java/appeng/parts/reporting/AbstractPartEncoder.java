package appeng.parts.reporting;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.GuiBridge;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractPartEncoder extends AbstractPartTerminal {

    protected AppEngInternalInventory crafting;
    protected AppEngInternalInventory output;
    protected AppEngInternalInventory pattern;

    protected boolean craftingMode = true;
    protected boolean substitute = false;

    public AbstractPartEncoder(ItemStack is) {
        super(is);
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        for (final ItemStack is : this.pattern) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.pattern.readFromNBT(data, "pattern");
        this.output.readFromNBT(data, "outputList");
        this.crafting.readFromNBT(data, "crafting");
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        this.pattern.writeToNBT(data, "pattern");
        this.output.writeToNBT(data, "outputList");
        this.crafting.writeToNBT(data, "crafting");
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = this.pattern.getStackInSlot(1);
            if (!is.isEmpty() && is.getItem() instanceof ICraftingPatternItem) {
                final ICraftingPatternItem pattern = (ICraftingPatternItem) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem(is, this.getHost().getTile().getWorld());
                if (details != null) {
                    this.setCraftingRecipe(details.isCraftable());
                    this.setSubstitution(details.canSubstitute());

                    for (int x = 0; x < this.crafting.getSlots() && x < details.getInputs().length; x++) {
                        final IAEItemStack item = details.getInputs()[x];
                        this.crafting.setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
                    }

                    for (int x = 0; x < this.output.getSlots() && x < details.getOutputs().length; x++) {
                        final IAEItemStack item = details.getOutputs()[x];
                        this.output.setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
                    }
                }
            }
        } else if (inv == this.crafting) {
            this.fixCraftingRecipes();
        }

        this.getHost().markForSave();
    }

    private void fixCraftingRecipes() {
        if (this.isCraftingRecipe()) {
            for (int x = 0; x < this.crafting.getSlots(); x++) {
                final ItemStack is = this.crafting.getStackInSlot(x);
                if (!is.isEmpty()) {
                    is.setCount(1);
                }
            }
        }
    }

    public boolean isCraftingRecipe() {
        return this.craftingMode;
    }

    public void setCraftingRecipe(final boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.fixCraftingRecipes();
    }

    public boolean isSubstitution() {
        return this.substitute;
    }

    public void setSubstitution(final boolean canSubstitute) {
        this.substitute = canSubstitute;
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        }

        if (name.equals("output")) {
            return this.output;
        }

        if (name.equals("pattern")) {
            return this.pattern;
        }

        return super.getInventoryByName(name);
    }

    @Override
    public GuiBridge getGui(final EntityPlayer p) {
        int x = (int) p.posX;
        int y = (int) p.posY;
        int z = (int) p.posZ;
        if (this.getHost().getTile() != null) {
            x = this.getTile().getPos().getX();
            y = this.getTile().getPos().getY();
            z = this.getTile().getPos().getZ();
        }

        if (getGuiBridge().hasPermissions(this.getHost().getTile(), x, y, z, this.getSide(), p)) {
            return getGuiBridge();
        }
        return GuiBridge.GUI_ME;
    }

    abstract public GuiBridge getGuiBridge();

    @Nonnull
    @Override
    abstract public IPartModel getStaticModels();
}
