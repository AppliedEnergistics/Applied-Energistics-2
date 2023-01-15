package appeng.api.behaviors;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.util.GenericContainerHelper;
import appeng.util.fluid.FluidSoundHelper;

class FluidContainerItemStrategy
        implements ContainerItemStrategy<AEFluidKey, FluidContainerItemStrategy.Context> {
    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        return GenericContainerHelper.getContainedFluidStack(stack);
    }

    @Override
    public @Nullable Context findCarriedContext(Player player, AbstractContainerMenu menu) {
        if (menu.getCarried().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
            return new CarriedContext(player, menu);
        }
        return null;
    }

    @Override
    public @Nullable Context findPlayerSlotContext(Player player, int slot) {
        if (player.getInventory().getItem(slot).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
            return new PlayerInvContext(player, slot);
        }

        return null;
    }

    @Override
    public long extract(Context context, AEFluidKey what, long amount, Actionable mode) {
        var stack = context.getStack();
        var copy = ItemHandlerHelper.copyStackWithSize(stack, 1);
        var fluidHandler = copy.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (fluidHandler == null) {
            return 0;
        }

        int extracted = fluidHandler.drain(what.toStack(Ints.saturatedCast(amount)), mode.getFluidAction()).getAmount();
        if (mode == Actionable.MODULATE) {
            stack.shrink(1);
            context.addOverflow(fluidHandler.getContainer());
        }
        return extracted;
    }

    @Override
    public long insert(Context context, AEFluidKey what, long amount, Actionable mode) {
        var stack = context.getStack();
        var copy = ItemHandlerHelper.copyStackWithSize(stack, 1);
        var fluidHandler = copy.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (fluidHandler == null) {
            return 0;
        }

        int filled = fluidHandler.fill(what.toStack(Ints.saturatedCast(amount)), mode.getFluidAction());
        if (mode == Actionable.MODULATE) {
            stack.shrink(1);
            context.addOverflow(fluidHandler.getContainer());
        }
        return filled;
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
    public @Nullable GenericStack getExtractableContent(Context context) {
        return getContainedStack(context.getStack());
    }

    interface Context {
        ItemStack getStack();

        void setStack(ItemStack stack);

        void addOverflow(ItemStack stack);
    }

    private record CarriedContext(Player player, AbstractContainerMenu menu) implements Context {
        @Override
        public ItemStack getStack() {
            return menu.getCarried();
        }

        @Override
        public void setStack(ItemStack stack) {
            menu.setCarried(stack);
        }

        public void addOverflow(ItemStack stack) {
            if (menu.getCarried().isEmpty()) {
                menu.setCarried(stack);
            } else {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }

    private record PlayerInvContext(Player player, int slot) implements Context {
        @Override
        public ItemStack getStack() {
            return player.getInventory().getItem(slot);
        }

        @Override
        public void setStack(ItemStack stack) {
            player.getInventory().setItem(slot, stack);
        }

        public void addOverflow(ItemStack stack) {
            player.getInventory().placeItemBackInInventory(stack);
        }
    }
}
