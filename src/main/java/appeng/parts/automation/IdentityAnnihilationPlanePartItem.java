package appeng.parts.automation;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.parts.PartItem;
import appeng.util.InteractionUtil;

@Deprecated
public class IdentityAnnihilationPlanePartItem extends PartItem<IdentityAnnihilationPlanePart> {
    public IdentityAnnihilationPlanePartItem(Properties properties) {
        super(properties, IdentityAnnihilationPlanePart.class, IdentityAnnihilationPlanePart::new);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag isAdvanced) {
        lines.add(Tooltips.of(GuiText.Deprecated));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            int count = player.getItemInHand(hand).getCount();

            ItemStack newPlane = AEParts.ANNIHILATION_PLANE.stack(count);
            newPlane.enchant(Enchantments.SILK_TOUCH, 1);

            player.setItemInHand(hand, newPlane);
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return super.use(level, player, hand);
    }
}
