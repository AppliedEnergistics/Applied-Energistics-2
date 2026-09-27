package appeng.api.behaviors;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.util.GenericContainerHelper;
import appeng.util.fluid.FluidSoundHelper;

class FluidContainerItemStrategy implements ContainerItemStrategy<AEFluidKey, ResourceHandler<FluidResource>> {
    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        return GenericContainerHelper.getContainedFluidStack(stack);
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> findCarriedContext(Player player, AbstractContainerMenu menu) {
        var itemAccess = ItemAccess.forPlayerCursor(player, menu);
        return itemAccess.getCapability(Capabilities.Fluid.ITEM);
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> findPlayerSlotContext(Player player, int slot) {
        var itemAccess = ItemAccess.forPlayerSlot(player, slot);
        return itemAccess.getCapability(Capabilities.Fluid.ITEM);
    }

    @Override
    public long extract(ResourceHandler<FluidResource> context, AEFluidKey what, long amount, Actionable mode) {
        try (var tx = Transaction.open(null)) {
            var extracted = context.extract(what.toResource(), Ints.saturatedCast(amount), tx);
            if (mode == Actionable.MODULATE) {
                tx.commit();
            }
            return extracted;
        }
    }

    @Override
    public long insert(ResourceHandler<FluidResource> context, AEFluidKey what, long amount, Actionable mode) {
        try (var tx = Transaction.open(null)) {
            var inserted = context.insert(what.toResource(), Ints.saturatedCast(amount), tx);
            if (mode == Actionable.MODULATE) {
                tx.commit();
            }
            return inserted;
        }
    }

    @Override
    public void playFillSound(Player player, AEFluidKey what) {
        FluidSoundHelper.playFillSound(player, what);
    }

    @Override
    public void playEmptySound(Player player, AEFluidKey what) {
        FluidSoundHelper.playEmptySound(player, what);
    }

    @Override
    public @Nullable GenericStack getExtractableContent(ResourceHandler<FluidResource> context) {
        try (var tx = Transaction.open(null)) {
            var stack = ResourceHandlerUtil.extractFirst(context, r -> true, Integer.MAX_VALUE, tx);
            if (stack != null) {
                return new GenericStack(AEFluidKey.of(stack.resource()), stack.amount());
            }
        }
        return null;
    }
}
