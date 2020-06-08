package appeng.items.parts;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import net.minecraft.item.ItemStack;

import java.util.function.Function;

public class ColoredPartItem<T extends IPart> extends ItemPart<T> {

    private final AEColor color;

    public ColoredPartItem(Properties properties, PartType type, Function<ItemStack, T> factory, AEColor color) {
        super(properties, type, factory);
        this.color = color;
    }

    public AEColor getColor() {
        return color;
    }

}
