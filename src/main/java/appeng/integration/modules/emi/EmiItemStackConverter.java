package appeng.integration.modules.emi;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import dev.emi.emi.api.stack.EmiStack;

import appeng.api.integrations.emi.EmiStackConverter;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

class EmiItemStackConverter implements EmiStackConverter {
    @Override
    public Class<?> getKeyType() {
        return Item.class;
    }

    @Override
    public @Nullable EmiStack toEmiStack(GenericStack stack) {
        if (stack.what() instanceof AEItemKey itemKey) {
            return EmiStack.of(itemKey.getReadOnlyStack()).setAmount(stack.amount() == 0 ? 1 : stack.amount());
            // The {@link appeng.api.stacks.GenericStack} taken from the craft confirm/status screens,
            // always returns a stack size of 0 for items making the stack unable to be favorited
            // Hence why we do this check
        }
        return null;
    }

    @Override
    public @Nullable GenericStack toGenericStack(EmiStack stack) {
        var item = stack.getKeyOfType(Item.class);
        if (item != null && item != Items.AIR) {
            var itemKey = AEItemKey.of(stack.getItemStack());
            return new GenericStack(itemKey, stack.getAmount());
        }
        return null;
    }
}
