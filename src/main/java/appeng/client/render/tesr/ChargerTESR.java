package appeng.client.render.tesr;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;

import appeng.client.render.renderable.ItemRenderable;
import appeng.tile.misc.ChargerBlockEntity;

public final class ChargerTESR {

    private ChargerTESR() {
    }

    public static Function<BlockEntityRenderDispatcher, BlockEntityRenderer<ChargerBlockEntity>> FACTORY = dispatcher -> new ModularTESR<>(
            dispatcher, new ItemRenderable<>(ChargerTESR::getRenderedItem));

    private static Pair<ItemStack, Transformation> getRenderedItem(ChargerBlockEntity tile) {
        Transformation transform = new Transformation(new Vector3f(), new Vector3f(0.5f, 0.375f, 0.5f),
                new Vector3f(1f, 1f, 1f));
        return new ImmutablePair<>(tile.getInternalInventory().getInvStack(0), transform);
    }

}
