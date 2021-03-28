package appeng.util.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TestItemWithCaps extends Item {
    public TestItemWithCaps() {
        super(new Properties());
        setRegistryName("appliedenergistics2:test_item");
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt == null) {
            return null;
        } else {
            return new CapabilityProvider();
        }
    }

    /**
     * Simple capability provider that just has a single counter value to produce different NBT.
     */
    public static class CapabilityProvider implements ICapabilityProvider, INBTSerializable<IntNBT> {
        private int counter;

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return LazyOptional.empty();
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
            return LazyOptional.empty();
        }

        @Override
        public IntNBT serializeNBT() {
            return IntNBT.valueOf(counter);
        }

        @Override
        public void deserializeNBT(IntNBT nbt) {
            counter = nbt.getInt();
        }
    }
}
