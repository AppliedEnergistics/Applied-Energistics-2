package appeng.menu.me.interaction;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.FluidContainerHelper;
import appeng.util.fluid.FluidSoundHelper;

public class FluidContainerItemStrategy
        implements ContainerItemStrategy<AEFluidKey, FluidContainerItemStrategy.Context> {
    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        return FluidContainerHelper.getContainedStack(stack);
    }

    @Override
    public @Nullable Context findCarriedContext(Player player, AbstractContainerMenu menu) {
        if (menu.getCarried().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()) {
            return new Context(player, menu);
        }
        return null;
    }

    @Override
    public long extract(Context context, AEFluidKey what, long amount, Actionable mode) {
        ItemStack held = context.menu.getCarried();
        ItemStack copy = ItemHandlerHelper.copyStackWithSize(held, 1);
        var fluidHandler = copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
        if (fluidHandler == null) {
            return 0;
        }

        int extracted = fluidHandler.drain(what.toStack(Ints.saturatedCast(amount)), mode.getFluidAction()).getAmount();
        if (mode == Actionable.MODULATE) {
            held.shrink(1);
            if (held.isEmpty()) {
                context.menu.setCarried(fluidHandler.getContainer());
            } else {
                context.player.getInventory().placeItemBackInInventory(fluidHandler.getContainer());
            }
        }
        return extracted;
    }

    @Override
    public long insert(Context context, AEFluidKey what, long amount, Actionable mode) {
        ItemStack held = context.menu.getCarried();
        ItemStack copy = ItemHandlerHelper.copyStackWithSize(held, 1);
        var fluidHandler = copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
        if (fluidHandler == null) {
            return 0;
        }

        int filled = fluidHandler.fill(what.toStack(Ints.saturatedCast(amount)), mode.getFluidAction());
        if (mode == Actionable.MODULATE) {
            held.shrink(1);
            if (held.isEmpty()) {
                context.menu.setCarried(fluidHandler.getContainer());
            } else {
                context.player.getInventory().placeItemBackInInventory(fluidHandler.getContainer());
            }
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
        return getContainedStack(context.menu.getCarried());
    }

    static class Context {
        private final Player player;
        private final AbstractContainerMenu menu;

        private Context(Player player, AbstractContainerMenu menu) {
            this.player = player;
            this.menu = menu;
        }
    }
}
