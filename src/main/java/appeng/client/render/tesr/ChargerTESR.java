package appeng.client.render.tesr;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import appeng.client.render.renderable.ItemRenderable;
import appeng.tile.misc.ChargerBlockEntity;

public final class ChargerTESR {

    private ChargerTESR() {
    }

    public static Function<TileEntityRendererDispatcher, TileEntityRenderer<ChargerBlockEntity>> FACTORY = dispatcher -> new ModularTESR<>(
            dispatcher, new ItemRenderable<>(ChargerTESR::getRenderedItem));

    private static Pair<ItemStack, ItemTransformVec3f> getRenderedItem(ChargerBlockEntity tile) {
        ItemTransformVec3f transform = new ItemTransformVec3f(new Vector3f(), new Vector3f(0.5f, 0.375f, 0.5f),
                new Vector3f(1f, 1f, 1f));
        return new ImmutablePair<>(tile.getInternalInventory().getInvStack(0), transform);
    }

}
