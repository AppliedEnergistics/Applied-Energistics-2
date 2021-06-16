package appeng.items.materials;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorItemHandler;

public class UpgradeCardItem extends AEBaseItem implements IUpgradeModule {
    private final Upgrades cardType;

    public UpgradeCardItem(Properties properties, Upgrades cardType) {
        super(properties);
        this.cardType = cardType;
    }

    @Override
    public Upgrades getType(final ItemStack itemstack) {
        return cardType;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> lines,
            ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);

        final Upgrades u = this.getType(stack);
        if (u != null) {
            lines.addAll(u.getTooltipLines());
        }
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        if (player != null && InteractionUtil.isInAlternateUseMode(player)) {
            final TileEntity te = context.getWorld().getTileEntity(context.getPos());
            IItemHandler upgrades = null;

            if (te instanceof IPartHost) {
                final SelectedPart sp = ((IPartHost) te).selectPart(context.getHitVec());
                if (sp.part instanceof IUpgradeableHost) {
                    upgrades = ((IUpgradeableHost) sp.part).getUpgradeInventory();
                }
            } else if (te instanceof IUpgradeableHost) {
                upgrades = ((IUpgradeableHost) te).getUpgradeInventory();
            }

            ItemStack heldStack = player.getHeldItem(hand);
            if (upgrades != null && !heldStack.isEmpty() && heldStack.getItem() instanceof IUpgradeModule) {
                final IUpgradeModule um = (IUpgradeModule) heldStack.getItem();
                final Upgrades u = um.getType(heldStack);

                if (u != null) {
                    if (player.getEntityWorld().isRemote()) {
                        return ActionResultType.PASS;
                    }

                    final InventoryAdaptor ad = new AdaptorItemHandler(upgrades);
                    player.setHeldItem(hand, ad.addItems(heldStack));
                    return ActionResultType.func_233537_a_(player.getEntityWorld().isRemote());
                }
            }
        }

        return super.onItemUseFirst(stack, context);
    }
}
