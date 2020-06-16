package appeng.debug;

import java.util.Arrays;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEPartLocation;
import appeng.items.AEBaseItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.ItemPart;

/**
 * This tool will try to place anything that is registered as a {@link ItemPart}
 * (and not a colored one) onto an existing cable to quickly test parts and
 * their rendering.
 */
public class ToolDebugPartPlacer extends AEBaseItem {

    public ToolDebugPartPlacer(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote()) {
            return ActionResultType.PASS;
        }

        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();

        if (player == null) {
            return ActionResultType.PASS;
        }

        if (!player.abilities.isCreativeMode) {
            player.sendMessage(new StringTextComponent("Only usable in creative mode"));
            return ActionResultType.FAIL;
        }

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof IPartHost)) {
            player.sendMessage(new StringTextComponent("Right-click something that will accept parts"));
            return ActionResultType.FAIL;
        }
        IPartHost center = (IPartHost) te;
        IPart cable = center.getPart(AEPartLocation.INTERNAL);
        if (cable == null) {
            player.sendMessage(new StringTextComponent("Clicked part host must have an INSIDE part"));
            return ActionResultType.FAIL;
        }

        Direction face = context.getFace();
        Vec3i offset = face.getDirectionVec();
        Direction[] perpendicularFaces = Arrays.stream(Direction.values()).filter(d -> d.getAxis() != face.getAxis())
                .toArray(Direction[]::new);

        BlockPos nextPos = pos;
        for (Item item : ForgeRegistries.ITEMS) {
            if (!(item instanceof ItemPart)) {
                continue;
            }

            if (item instanceof ColoredPartItem) {
                continue; // Cables and such
            }

            nextPos = nextPos.add(offset);
            if (!world.setBlockState(nextPos, te.getBlockState())) {
                continue;
            }

            TileEntity t = world.getTileEntity(nextPos);
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

        return ActionResultType.SUCCESS;
    }

}
