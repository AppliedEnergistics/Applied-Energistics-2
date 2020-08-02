package appeng.client.render.tesr;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;

import appeng.client.render.renderable.ItemRenderable;
import appeng.tile.misc.ChargerTileEntity;

public final class ChargerTESR {

    private ChargerTESR() {
    }

    public static Function<TileEntityRendererDispatcher, TileEntityRenderer<ChargerTileEntity>> FACTORY = dispatcher -> new ModularTESR<>(
            dispatcher, new ItemRenderable<>(ChargerTESR::getRenderedItem));

    private static Pair<ItemStack, TransformationMatrix> getRenderedItem(ChargerTileEntity tile) {
        TransformationMatrix transform = new TransformationMatrix(new Vector3f(0.5f, 0.375f, 0.5f), null, null, null);
        return new ImmutablePair<>(tile.getInternalInventory().getStackInSlot(0), transform);
    }

}
