package appeng.debug;

import java.util.Arrays;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEPartLocation;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;

/**
 * This tool will try to place anything that is registered as a {@link PartItem}
 * (and not a colored one) onto an existing cable to quickly test parts and
 * their rendering.
 */
public class DebugPartPlacerItem extends AEBaseItem implements AEToolItem {

    public DebugPartPlacerItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) {
            return ActionResult.PASS;
        }

        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();

        if (player == null) {
            return ActionResult.PASS;
        }

        if (!player.isCreative()) {
            player.sendSystemMessage(new LiteralText("Only usable in creative mode"), Util.NIL_UUID);
            return ActionResult.FAIL;
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof IPartHost)) {
            player.sendSystemMessage(new LiteralText("Right-click something that will accept parts"), Util.NIL_UUID);
            return ActionResult.FAIL;
        }
        IPartHost center = (IPartHost) te;
        IPart cable = center.getPart(AEPartLocation.INTERNAL);
        if (cable == null) {
            player.sendSystemMessage(new LiteralText("Clicked part host must have an INSIDE part"), Util.NIL_UUID);
            return ActionResult.FAIL;
        }

        Direction face = context.getSide();
        Vec3i offset = face.getVector();
        Direction[] perpendicularFaces = Arrays.stream(Direction.values()).filter(d -> d.getAxis() != face.getAxis())
                .toArray(Direction[]::new);

        BlockPos nextPos = pos;
        for (Item item : Registry.ITEM) {
            if (!(item instanceof PartItem)) {
                continue;
            }

            if (item instanceof ColoredPartItem) {
                continue; // Cables and such
            }

            nextPos = nextPos.add(offset);
            if (!world.setBlockState(nextPos, te.getCachedState())) {
                continue;
            }

            BlockEntity t = world.getBlockEntity(nextPos);
            if (!(t instanceof IPartHost)) {
                continue;
            }

            IPartHost partHost = (IPartHost) t;
            if (partHost.addPart(cable.getItemStack(PartItemStack.PICK), AEPartLocation.INTERNAL, player,
                    null) == null) {
                continue;
            }
            for (Direction dir : perpendicularFaces) {
                ItemStack itemStack = new ItemStack(item, 1);
                partHost.addPart(itemStack, AEPartLocation.fromFacing(dir), player, null);
            }
        }

        return ActionResult.SUCCESS;
    }

}
