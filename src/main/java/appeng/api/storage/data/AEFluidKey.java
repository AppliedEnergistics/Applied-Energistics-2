package appeng.api.storage.data;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import appeng.api.storage.GenericStack;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.core.AELog;
import appeng.items.misc.WrappedGenericStack;

public class AEFluidKey extends AEKey {
    public static final int AMOUNT_BUCKET = (int) FluidConstants.BUCKET;
    public static final int AMOUNT_BLOCK = (int) FluidConstants.BLOCK;

    private final Fluid fluid;
    @Nullable
    private final CompoundTag tag;
    private final int hashCode;

    private AEFluidKey(Fluid Fluid, @Nullable CompoundTag tag) {
        this.fluid = Fluid;
        this.tag = tag;
        this.hashCode = Objects.hash(Fluid, tag);
    }

    public static AEFluidKey of(Fluid fluid, @Nullable CompoundTag tag) {
        // Do a defensive copy of the tag if we're not sure that we can take ownership
        return new AEFluidKey(fluid, tag != null ? tag.copy() : null);
    }

    public static AEFluidKey of(Fluid fluid) {
        return of(fluid, null);
    }

    @Nullable
    public static AEFluidKey of(FluidVariant fluidVariant) {
        if (fluidVariant.isBlank()) {
            return null;
        }
        return of(fluidVariant.getFluid(), fluidVariant.getNbt());
    }

    public boolean matches(FluidVariant variant) {
        return !variant.isBlank() && fluid.isSame(variant.getFluid()) && variant.nbtMatches(tag);
    }

    @Override
    public IStorageChannel<?> getChannel() {
        return StorageChannels.fluids();
    }

    @Override
    public AEFluidKey dropSecondary() {
        return of(fluid, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEFluidKey aeFluidKey = (AEFluidKey) o;
        // The hash code comparison is a fast-fail for two objects with different NBT or fluid
        return hashCode == aeFluidKey.hashCode && fluid == aeFluidKey.fluid && Objects.equals(tag, aeFluidKey.tag);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEFluidKey fromTag(CompoundTag tag) {
        try {
            var fluid = Registry.FLUID.getOptional(new ResourceLocation(tag.getString("id")))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown fluid id."));
            var extraTag = tag.contains("tag") ? tag.getCompound("tag") : null;
            return of(fluid, extraTag);
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid fluid key from NBT: %s", tag, e);
            return null;
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag result = new CompoundTag();
        result.putString("id", Registry.FLUID.getKey(fluid).toString());

        if (tag != null) {
            result.put("tag", tag.copy());
        }

        return result;
    }

    @Override
    public Object getPrimaryKey() {
        return fluid;
    }

    @Override
    public String getModId() {
        return Registry.FLUID.getKey(fluid).getNamespace();
    }

    @Override
    public ItemStack wrapForDisplayOrFilter() {
        return wrap(0);
    }

    @Override
    public ItemStack wrap(int amount) {
        return WrappedGenericStack.wrap(this, amount);
    }

    public FluidVariant toVariant() {
        return FluidVariant.of(fluid, tag);
    }

    public Fluid getFluid() {
        return fluid;
    }

    public boolean hasTag() {
        return tag != null;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
        data.writeVarInt(Registry.FLUID.getId(fluid));
        data.writeNbt(tag);
    }

    public static AEFluidKey fromPacket(FriendlyByteBuf data) {
        var fluid = Registry.FLUID.byId(data.readVarInt());
        var tag = data.readNbt();
        return new AEFluidKey(fluid, tag);
    }

    public static boolean is(@Nullable GenericStack stack) {
        return stack != null && stack.what() instanceof AEFluidKey;
    }

    @Override
    public String toString() {
        return tag == null ? fluid.toString() : fluid.toString() + " (+tag)";
    }
}
