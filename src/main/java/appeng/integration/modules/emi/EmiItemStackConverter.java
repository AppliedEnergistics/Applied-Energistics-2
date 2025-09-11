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
            return EmiStack.of(itemKey.wrapForDisplayOrFilter()).setAmount(stack.amount());
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
