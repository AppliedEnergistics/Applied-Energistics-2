package appeng.items.materials;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.items.AEBaseItem;

public class NamePressItem extends AEBaseItem {
    /**
     * NBT property used by the name press to store the name to be inscribed.
     */
    public static final String TAG_INSCRIBE_NAME = "InscribeName";

    public NamePressItem(Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);

        final CompoundNBT c = stack.getOrCreateTag();
        if (c.contains(TAG_INSCRIBE_NAME)) {
            lines.add(new StringTextComponent(c.getString(TAG_INSCRIBE_NAME)));
        }
    }
}
