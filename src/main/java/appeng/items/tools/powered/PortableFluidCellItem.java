package appeng.items.tools.powered;

import appeng.api.storage.AEKeySpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import appeng.api.config.Actionable;
import appeng.api.storage.data.AEFluidKey;
import appeng.helpers.FluidContainerHelper;
import appeng.menu.me.items.PortableFluidCellMenu;
import appeng.util.InteractionUtil;

public class PortableFluidCellItem extends PortableCellItem<AEFluidKey> {
    public PortableFluidCellItem(StorageTier tier, Properties props) {
        super(AEFluidKey.filter(), tier, props);
    }

    /**
     * Portable fluid cells can pick up fluids like buckets can.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        BlockHitResult blockHitResult = BucketItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (InteractionUtil.isInAlternateUseMode(player) || blockHitResult.getType() == HitResult.Type.MISS) {
            return super.use(level, player, usedHand);
        }

        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getDirection();
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos2, direction, itemStack)) {
                return super.use(level, player, usedHand);
            }

            BlockState blockState = level.getBlockState(blockPos);
            if (blockState.getBlock() instanceof BucketPickup bucketPickup) {
                // Try figuring out if there's enough space or not
                var fluid = AEFluidKey.of(blockState.getFluidState().getType());
                if (!level.isClientSide()) {
                    // This means that we'll void some if there's a little space left, but not enough for a full block
                    if (insert(player, itemStack, fluid, AEFluidKey.AMOUNT_BLOCK, Actionable.SIMULATE) <= 0) {
                        return super.use(level, player, usedHand);
                    }
                }

                var pickedUp = bucketPickup.pickupBlock(level, blockPos, blockState);
                var contained = FluidContainerHelper.getContainedStack(pickedUp);
                if (contained != null) {
                    if (!level.isClientSide()) {
                        insert(player, itemStack, contained.what(), contained.amount(), Actionable.MODULATE);

                        player.awardStat(Stats.ITEM_USED.get(this));
                        bucketPickup.getPickupSound().ifPresent(soundEvent -> player.playSound(soundEvent, 1.0f, 1.0f));
                        level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);
                    }
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }
            }
        }

        return super.use(level, player, usedHand);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return PortableFluidCellMenu.TYPE;
    }
}
