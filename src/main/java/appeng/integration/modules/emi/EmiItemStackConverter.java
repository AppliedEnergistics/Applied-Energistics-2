package appeng.integration.modules.emi;

import appeng.api.integrations.emi.EmiStackConverter;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

class EmiItemStackConverter implements EmiStackConverter {
    @Override
    public Class<?> getKeyType() {
        return Item.class;
    }

    @Override
    public @Nullable EmiStack getIngredientFromStack(GenericStack stack) {
        if (stack.what() instanceof AEFluidKey fluidKey) {
            return EmiStack.of(fluidKey.getFluid(), fluidKey.copyTag(), stack.amount());
        }
        return null;
    }

    @Override
    public @Nullable GenericStack getStackFromIngredient(EmiStack stack) {
        var item = stack.getKeyOfType(Item.class);
        if (item != null && item != Items.AIR) {
            var itemKey = AEItemKey.of(item, stack.getNbt());
            return new GenericStack(itemKey, stack.getAmount());
        }
        return null;
    }
}
