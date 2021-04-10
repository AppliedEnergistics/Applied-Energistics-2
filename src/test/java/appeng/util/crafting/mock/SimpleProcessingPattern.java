package appeng.util.crafting.mock;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleProcessingPattern implements ICraftingPatternDetails {
    private final List<IAEItemStack> inputs;
    private final List<IAEItemStack> outputs;

    public SimpleProcessingPattern(IAEItemStack input, IAEItemStack output) {
        this.inputs = Collections.singletonList(input);
        this.outputs = Collections.singletonList(output);
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public List<IAEItemStack> getInputs() {
        return inputs;
    }

    @Override
    public List<IAEItemStack> getOutputs() {
        return outputs;
    }

    @Override
    public ItemStack getPattern() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public IAEItemStack[] getSparseInputs() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public IAEItemStack[] getSparseOutputs() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public boolean canSubstitute() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public List<IAEItemStack> getSubstituteInputs(int slot) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public ItemStack getOutput(CraftingInventory craftingInv, World world) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public int getPriority() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void setPriority(int priority) {
        throw new UnsupportedOperationException("mock");
    }
}
