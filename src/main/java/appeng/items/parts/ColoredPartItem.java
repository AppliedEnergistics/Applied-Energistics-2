package appeng.items.parts;

import java.util.function.Function;

import net.minecraft.item.ItemStack;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;

public class ColoredPartItem<T extends IPart> extends PartItem<T> {

    private final AEColor color;

    public ColoredPartItem(Settings properties, PartType type, Function<ItemStack, T> factory, AEColor color) {
        super(properties, type, factory);
        this.color = color;
    }

    public AEColor getColor() {
        return color;
    }

}
