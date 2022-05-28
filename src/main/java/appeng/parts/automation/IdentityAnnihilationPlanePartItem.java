package appeng.parts.automation;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.parts.PartItem;

@Deprecated
public class IdentityAnnihilationPlanePartItem extends PartItem<IdentityAnnihilationPlanePart> {
    public IdentityAnnihilationPlanePartItem(Properties properties) {
        super(properties, IdentityAnnihilationPlanePart.class, IdentityAnnihilationPlanePart::new);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag isAdvanced) {
        lines.add(Tooltips.of(GuiText.Deprecated));
    }
}
