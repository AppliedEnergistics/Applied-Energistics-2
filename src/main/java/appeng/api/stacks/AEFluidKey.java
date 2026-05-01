package appeng.api.stacks;

import java.util.List;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;

public final class AEFluidKey extends AEKey {
    public static final MapCodec<AEFluidKey> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BuiltInRegistries.FLUID.holderByNameCodec().validate(
                            holder -> holder.is(Fluids.EMPTY.builtInRegistryHolder())
                                    ? DataResult.error(() -> "Fluid must not be minecraft:empty")
                                    : DataResult.success(holder))
                            .fieldOf("id").forGetter(key -> key.stack.typeHolder()),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(key -> key.stack.getComponentsPatch()))
                    .apply(instance, (fluidHolder,
                            dataComponentPatch) -> new AEFluidKey(new FluidStack(fluidHolder, 1, dataComponentPatch))));
    public static final Codec<AEFluidKey> CODEC = MAP_CODEC.codec();

    public static final int AMOUNT_BUCKET = 1000;
    public static final int AMOUNT_BLOCK = 1000;

    private final FluidStack stack;
    private final int hashCode;

    private AEFluidKey(FluidStack stack) {
        Preconditions.checkArgument(!stack.isEmpty(), "stack was empty");
        this.stack = stack;
        this.hashCode = FluidStack.hashFluidAndComponents(stack);
    }

    public static AEFluidKey of(Fluid fluid) {
        return of(new FluidStack(fluid, 1));
    }

    @Nullable
    public static AEFluidKey of(FluidStack fluidVariant) {
        if (fluidVariant.isEmpty()) {
            return null;
        }
        return new AEFluidKey(fluidVariant.copyWithAmount(1));
    }

    @Nullable
    public static AEFluidKey of(FluidResource resource) {
        if (resource.isEmpty()) {
            return null;
        }
        return new AEFluidKey(resource.toStack(1));
    }

    public static boolean matches(AEKey what, FluidStack fluid) {
        return what instanceof AEFluidKey fluidKey && fluidKey.matches(fluid);
    }

    public static boolean is(AEKey what) {
        return what instanceof AEFluidKey;
    }

    public static AEKeyFilter filter() {
        return AEFluidKey::is;
    }

    public boolean matches(FluidStack variant) {
        return FluidStack.isSameFluidSameComponents(this.stack, variant);
    }

    @Override
    public AEKeyType getType() {
        return AEKeyType.fluids();
    }

    @Override
    public AEFluidKey dropSecondary() {
        return of(new FluidStack(getFluid(), 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEFluidKey aeFluidKey = (AEFluidKey) o;
        // The hash code comparison is a fast-fail cheap check
        return hashCode == aeFluidKey.hashCode && FluidStack.isSameFluidSameComponents(this.stack, aeFluidKey.stack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEFluidKey fromTag(ValueInput input) {
        try {
            return input.read(MAP_CODEC).orElseThrow();
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid fluid key from NBT: %s", input, e);
            return null;
        }
    }

    @Override
    public void toTag(ValueOutput output) {
        output.store(MAP_CODEC, this);
    }

    @Override
    public Object getPrimaryKey() {
        return getFluid();
    }

    @Override
    public Identifier getId() {
        return BuiltInRegistries.FLUID.getKey(getFluid());
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        // Fluids are voided
    }

    @Override
    protected Component computeDisplayName() {
        return stack.getHoverName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isTagged(TagKey<?> tag) {
        // This will just return false for incorrectly cast tags
        return stack.is((TagKey<Fluid>) tag);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<T> type) {
        return stack.get(type);
    }

    @Override
    public boolean hasComponents() {
        return stack.getComponents().isEmpty();
    }

    public FluidResource toResource() {
        return FluidResource.of(stack);
    }

    public FluidStack toStack(int amount) {
        return stack.copyWithAmount(amount);
    }

    public Fluid getFluid() {
        return stack.getFluid();
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        FluidStack.STREAM_CODEC.encode(data, stack);
    }

    public static AEFluidKey fromPacket(RegistryFriendlyByteBuf data) {
        var stack = FluidStack.STREAM_CODEC.decode(data);
        return new AEFluidKey(stack);
    }

    public static boolean is(@Nullable GenericStack stack) {
        return stack != null && stack.what() instanceof AEFluidKey;
    }

    @Override
    public String toString() {
        var id = BuiltInRegistries.FLUID.getKey(getFluid());
        String idString = id != BuiltInRegistries.FLUID.getDefaultKey() ? id.toString()
                : getFluid().getClass().getName() + "(unregistered)";
        return stack.getComponents().isEmpty() ? idString : idString + " (+components)";
    }
}
