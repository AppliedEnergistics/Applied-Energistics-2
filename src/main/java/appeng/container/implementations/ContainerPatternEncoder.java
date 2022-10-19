package appeng.container.implementations;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.IContainerCraftingPacket;
import appeng.parts.reporting.AbstractPartEncoder;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ContainerPatternEncoder extends ContainerMEMonitorable implements IAEAppEngInventory, IOptionalSlotHost, IContainerCraftingPacket {

    private final AbstractPartEncoder patternTerminal;

    final AppEngInternalInventory cOut = new AppEngInternalInventory(null, 1);

    protected IItemHandler crafting;
    protected SlotPatternTerm craftSlot;
    protected SlotRestrictedInput patternSlotIN;
    protected SlotRestrictedInput patternSlotOUT;
    private IRecipe currentRecipe;

    protected SlotFakeCraftingMatrix[] craftingSlots;
    protected OptionalSlotFake[] outputSlots;

    @GuiSync(97)
    public boolean craftingMode = true;
    @GuiSync(96)
    public boolean substitute = false;

    public ContainerPatternEncoder(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        patternTerminal = (AbstractPartEncoder) monitorable;
    }

    protected ContainerPatternEncoder(InventoryPlayer ip, ITerminalHost monitorable, boolean bindInventory) {
        super(ip, monitorable, bindInventory);
        patternTerminal = (AbstractPartEncoder) monitorable;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
        if (Platform.isClient()) {
            return ItemStack.EMPTY;
        }
        if (this.inventorySlots.get(idx) instanceof SlotPlayerInv || this.inventorySlots.get(idx) instanceof SlotPlayerHotBar) {
            final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx); // require AE SLots!
            ItemStack itemStack = clickSlot.getStack();
            if (AEApi.instance().definitions().materials().blankPattern().isSameAs(itemStack)) {
                IItemHandler patternInv = this.getPart().getInventoryByName("pattern");
                ItemStack remainder = patternInv.insertItem(0, itemStack, false);
                clickSlot.putStack(remainder);
            }
        }
        return super.transferStackInSlot(p, idx);
    }

    public AbstractPartEncoder getPart() {
        return this.patternTerminal;
    }

    @Override
    public abstract boolean isSlotEnabled(int idx);

    @Override
    public IItemHandler getInventoryByName(String name) {
        if (name.equals("player")) {
            return new PlayerInvWrapper(this.getInventoryPlayer());
        }
        return this.getPart().getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {

    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        super.putStackInSlot(slotID, stack);
        this.getAndUpdateOutput();
    }

    protected void updateOrderOfOutputSlots() {
        if (!this.isCraftingMode()) {
            if (craftSlot != null) {
                this.craftSlot.xPos = -9000;
            }

            for (int y = 0; y < 3; y++) {
                this.outputSlots[y].xPos = this.outputSlots[y].getX();
            }
        } else {
            if (craftSlot != null) {
                this.craftSlot.xPos = this.craftSlot.getX();
            }
            for (int y = 0; y < 3; y++) {
                this.outputSlots[y].xPos = -9000;
            }
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (s == this.patternSlotOUT && Platform.isServer()) {
            for (final IContainerListener listener : this.listeners) {
                for (final Slot slot : this.inventorySlots) {
                    if (slot instanceof OptionalSlotFake || slot instanceof SlotFakeCraftingMatrix) {
                        listener.sendSlotContents(this, slot.slotNumber, slot.getStack());
                    }
                }
                if (listener instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) listener).isChangingQuantityOnly = false;
                }
            }
            this.detectAndSendChanges();
        }
    }

    public void encodeAndMoveToInventory() {
        encode();
        ItemStack output = this.patternSlotOUT.getStack();
        if (!output.isEmpty()) {
            if (!getPlayerInv().addItemStackToInventory(output)) {
                getPlayerInv().player.dropItem(output, false);
            }
            this.patternSlotOUT.putStack(ItemStack.EMPTY);
        }
    }

    public void encode() {
        ItemStack output = this.patternSlotOUT.getStack();

        final ItemStack[] in = this.getInputs();
        final ItemStack[] out = this.getOutputs();

        // if there is no input, this would be silly.
        if (in == null || out == null) {
            return;
        }

        // first check the output slots, should either be null, or a pattern
        if (!output.isEmpty() && !this.isPattern(output)) {
            return;
        } // if nothing is there we should snag a new pattern.
        else if (output.isEmpty()) {
            output = this.patternSlotIN.getStack();
            if (output.isEmpty() || !this.isPattern(output)) {
                return; // no blanks.
            }

            // remove one, and clear the input slot.
            output.setCount(output.getCount() - 1);
            if (output.getCount() == 0) {
                this.patternSlotIN.putStack(ItemStack.EMPTY);
            }

            // add a new encoded pattern.
            Optional<ItemStack> maybePattern = AEApi.instance().definitions().items().encodedPattern().maybeStack(1);
            if (maybePattern.isPresent()) {
                output = maybePattern.get();
                this.patternSlotOUT.putStack(output);
            }
        }

        // encode the slot.
        final NBTTagCompound encodedValue = new NBTTagCompound();

        final NBTTagList tagIn = new NBTTagList();
        final NBTTagList tagOut = new NBTTagList();

        for (final ItemStack i : in) {
            tagIn.appendTag(this.createItemTag(i));
        }

        for (final ItemStack i : out) {
            tagOut.appendTag(this.createItemTag(i));
        }

        encodedValue.setTag("in", tagIn);
        encodedValue.setTag("out", tagOut);
        encodedValue.setBoolean("crafting", this.isCraftingMode());
        encodedValue.setBoolean("substitute", this.isSubstitute());

        output.setTagCompound(encodedValue);
    }

    public void multiply(int multiple) {
        ItemStack[] input = new ItemStack[craftingSlots.length];
        boolean canMultiplyInputs = true;
        boolean canMultiplyOutputs = true;

        for (int x = 0; x < this.craftingSlots.length; x++) {
            input[x] = this.craftingSlots[x].getStack();
            if (!input[x].isEmpty() && input[x].getCount() * multiple < 1) {
                canMultiplyInputs = false;
            }
        }
        for (final OptionalSlotFake outputSlot : this.outputSlots) {
            final ItemStack out = outputSlot.getStack();
            if (!out.isEmpty() && out.getCount() * multiple < 1) {
                canMultiplyOutputs = false;
            }
        }
        if (canMultiplyInputs && canMultiplyOutputs) {
            for (SlotFakeCraftingMatrix craftingSlot : this.craftingSlots) {
                ItemStack stack = craftingSlot.getStack();
                if (!stack.isEmpty()) {
                    craftingSlot.getStack().setCount(stack.getCount() * multiple);
                }
            }
            for (OptionalSlotFake outputSlot : this.outputSlots) {
                ItemStack stack = outputSlot.getStack();
                if (!stack.isEmpty()) {
                    outputSlot.getStack().setCount(stack.getCount() * multiple);
                }
            }
        }
    }

    public void divide(int divide) {
        ItemStack[] input = new ItemStack[craftingSlots.length];
        boolean canDivideInputs = true;
        boolean canDivideOutputs = true;

        for (int x = 0; x < this.craftingSlots.length; x++) {
            input[x] = this.craftingSlots[x].getStack();
            if (!input[x].isEmpty() && input[x].getCount() % divide != 0) {
                canDivideInputs = false;
            }
        }
        for (final OptionalSlotFake outputSlot : this.outputSlots) {
            final ItemStack out = outputSlot.getStack();
            if (!out.isEmpty() && out.getCount() % divide != 0) {
                canDivideOutputs = false;
            }
        }
        if (canDivideInputs && canDivideOutputs) {
            for (SlotFakeCraftingMatrix craftingSlot : this.craftingSlots) {
                ItemStack stack = craftingSlot.getStack();
                if (!stack.isEmpty()) {
                    craftingSlot.getStack().setCount(stack.getCount() / divide);
                }
            }
            for (OptionalSlotFake outputSlot : this.outputSlots) {
                ItemStack stack = outputSlot.getStack();
                if (!stack.isEmpty()) {
                    outputSlot.getStack().setCount(stack.getCount() / divide);
                }
            }
        }
    }

    public void increase(int increase) {
        ItemStack[] input = new ItemStack[craftingSlots.length];
        boolean canIncreaseInputs = true;
        boolean canIncreaseOutputs = true;

        for (int x = 0; x < this.craftingSlots.length; x++) {
            input[x] = this.craftingSlots[x].getStack();
            if (!input[x].isEmpty() && input[x].getCount() + increase < 1) {
                canIncreaseInputs = false;
            }
        }
        for (final OptionalSlotFake outputSlot : this.outputSlots) {
            final ItemStack out = outputSlot.getStack();
            if (!out.isEmpty() && out.getCount() + increase < 1) {
                canIncreaseOutputs = false;
            }
        }
        if (canIncreaseInputs && canIncreaseOutputs) {
            for (SlotFakeCraftingMatrix craftingSlot : this.craftingSlots) {
                ItemStack stack = craftingSlot.getStack();
                if (!stack.isEmpty()) {
                    craftingSlot.getStack().setCount(stack.getCount() + increase);
                }
            }
            for (OptionalSlotFake outputSlot : this.outputSlots) {
                ItemStack stack = outputSlot.getStack();
                if (!stack.isEmpty()) {
                    outputSlot.getStack().setCount(stack.getCount() + increase);
                }
            }
        }
    }

    public void decrease(int decrease) {
        ItemStack[] input = new ItemStack[craftingSlots.length];
        boolean canDecreaseInputs = true;
        boolean canDecreaseOutputs = true;

        for (int x = 0; x < this.craftingSlots.length; x++) {
            input[x] = this.craftingSlots[x].getStack();
            if (!input[x].isEmpty() && input[x].getCount() - decrease < 1) {
                canDecreaseInputs = false;
            }
        }
        for (final OptionalSlotFake outputSlot : this.outputSlots) {
            final ItemStack out = outputSlot.getStack();
            if (!out.isEmpty() && out.getCount() - decrease < 1) {
                canDecreaseOutputs = false;
            }
        }
        if (canDecreaseInputs && canDecreaseOutputs) {
            for (SlotFakeCraftingMatrix craftingSlot : this.craftingSlots) {
                ItemStack stack = craftingSlot.getStack();
                if (!stack.isEmpty()) {
                    craftingSlot.getStack().setCount(stack.getCount() - decrease);
                }
            }
            for (OptionalSlotFake outputSlot : this.outputSlots) {
                ItemStack stack = outputSlot.getStack();
                if (!stack.isEmpty()) {
                    outputSlot.getStack().setCount(stack.getCount() - decrease);
                }
            }
        }
    }

    public void maximizeCount() {
        ItemStack[] input = new ItemStack[craftingSlots.length];
        boolean canGrowInputs = true;
        boolean canGrowOutputs = true;
        int maxInputStackGrowth = 0;
        int maxOutputStackGrowth = 0;

        for (int x = 0; x < this.craftingSlots.length; x++) {
            input[x] = this.craftingSlots[x].getStack();
            if (!input[x].isEmpty() && input[x].getMaxStackSize() - input[x].getCount() > maxInputStackGrowth) {
                maxInputStackGrowth = input[x].getMaxStackSize() - input[x].getCount();
            }
            if (!input[x].isEmpty() && input[x].getCount() + maxInputStackGrowth > input[x].getMaxStackSize()) {
                canGrowInputs = false;
            }
        }
        for (final OptionalSlotFake outputSlot : this.outputSlots) {
            final ItemStack out = outputSlot.getStack();
            {
                maxOutputStackGrowth = out.getMaxStackSize() - out.getCount();
            }
            if (!out.isEmpty() && out.getCount() + maxOutputStackGrowth > out.getMaxStackSize()) {
                canGrowOutputs = false;
            }
        }
        if (canGrowInputs && canGrowOutputs) {
            int maxStackGrowth = Math.min(maxInputStackGrowth, maxOutputStackGrowth);
            for (SlotFakeCraftingMatrix craftingSlot : this.craftingSlots) {
                ItemStack stack = craftingSlot.getStack();
                if (!stack.isEmpty()) {
                    craftingSlot.getStack().setCount(stack.getCount() + maxStackGrowth);
                }
            }
            for (OptionalSlotFake outputSlot : this.outputSlots) {
                ItemStack stack = outputSlot.getStack();
                if (!stack.isEmpty()) {
                    outputSlot.getStack().setCount(stack.getCount() + maxStackGrowth);
                }
            }
        }
    }

    protected ItemStack[] getInputs() {
        final ItemStack[] input = new ItemStack[craftingSlots.length];
        boolean hasValue = false;

        for (int x = 0; x < this.craftingSlots.length; x++) {
            input[x] = this.craftingSlots[x].getStack();
            if (!input[x].isEmpty()) {
                hasValue = true;
            }
        }

        if (hasValue) {
            return input;
        }

        return null;
    }

    protected ItemStack[] getOutputs() {
        if (this.isCraftingMode()) {
            final ItemStack out = this.getAndUpdateOutput();

            if (!out.isEmpty() && out.getCount() > 0) {
                return new ItemStack[]{out};
            }
        } else {
            final List<ItemStack> list = new ArrayList<>(outputSlots.length);
            boolean hasValue = false;

            for (final OptionalSlotFake outputSlot : this.outputSlots) {
                final ItemStack out = outputSlot.getStack();

                if (!out.isEmpty() && out.getCount() > 0) {
                    list.add(out);
                    hasValue = true;
                }
            }

            if (hasValue) {
                return list.toArray(new ItemStack[outputSlots.length]);
            }
        }

        return null;
    }

    protected ItemStack getAndUpdateOutput() {
        final World world = this.getPlayerInv().player.world;
        final InventoryCrafting ic = new InventoryCrafting(this, 3, 3);

        for (int x = 0; x < ic.getSizeInventory(); x++) {
            ic.setInventorySlotContents(x, this.crafting.getStackInSlot(x));
        }

        if (this.currentRecipe == null || !this.currentRecipe.matches(ic, world)) {
            this.currentRecipe = CraftingManager.findMatchingRecipe(ic, world);
        }

        final ItemStack is;

        if (this.currentRecipe == null) {
            is = ItemStack.EMPTY;
        } else {
            is = this.currentRecipe.getCraftingResult(ic);
        }

        this.cOut.setStackInSlot(0, is);
        return is;
    }

    public boolean isCraftingMode() {
        return this.craftingMode;
    }

    void setCraftingMode(final boolean craftingMode) {
        this.craftingMode = craftingMode;
    }


    boolean isSubstitute() {
        return this.substitute;
    }

    public void setSubstitute(final boolean substitute) {
        this.substitute = substitute;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            if (this.isCraftingMode() != this.getPart().isCraftingRecipe()) {
                this.setCraftingMode(this.getPart().isCraftingRecipe());
                this.updateOrderOfOutputSlots();
            }

            this.substitute = this.getPart().isSubstitution();
        }
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        super.onUpdate(field, oldValue, newValue);

        if (field.equals("craftingMode")) {
            this.getAndUpdateOutput();
            this.updateOrderOfOutputSlots();
        }
    }

    boolean isPattern(final ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }

        final IDefinitions definitions = AEApi.instance().definitions();

        boolean isPattern = definitions.items().encodedPattern().isSameAs(output);
        isPattern |= definitions.materials().blankPattern().isSameAs(output);

        return isPattern;
    }

    NBTBase createItemTag(final ItemStack i) {
        final NBTTagCompound c = new NBTTagCompound();

        if (!i.isEmpty()) {
            i.writeToNBT(c);
            if (i.getCount() > i.getMaxStackSize()) {
                c.setInteger("stackSize", i.getCount());
            }
        }

        return c;
    }

    public void clear() {
        for (final Slot s : this.craftingSlots) {
            s.putStack(ItemStack.EMPTY);
        }

        for (final Slot s : this.outputSlots) {
            s.putStack(ItemStack.EMPTY);
        }

        this.detectAndSendChanges();
        this.getAndUpdateOutput();
    }
}
