package appeng.client.api;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;

/**
 * Similar in purpose to {@link net.minecraft.client.renderer.item.ItemStackRenderState}, but used to render arbitrary
 * {@link appeng.api.stacks.AEKey}.
 */
public final class AEKeyRenderState {
    private AEKeyRenderer<?, ?> renderer;
    private Object currentState;

    public boolean isEmpty() {
        return renderer == null;
    }

    public void clear() {
        renderer = null;
    }

    public void extract(@Nullable AEKey key, @Nullable Level level, int seed) {
        if (key != null) {
            var renderer = AEKeyRendering.get(key);
            if (renderer != null) {
                extract(renderer, key, level, seed);
                return;
            }
        }
        clear(); // If key null or renderer null
    }

    private <T, S> void extract(AEKeyRenderer<T, S> renderer, T key, @Nullable Level level, int seed) {
        Class<S> stateClass = renderer.stateClass();
        if (currentState == null || currentState.getClass() != stateClass) {
            currentState = renderer.createState();
        }

        renderer.extract(stateClass.cast(currentState), key, level, seed);
        this.renderer = renderer;
    }

    public void submit(PoseStack poseStack,
            SubmitNodeCollector nodes,
            int lightCoords) {
        if (renderer == null) {
            return;
        }

        submit(renderer, poseStack, nodes, lightCoords);
    }

    private <T, S> void submit(
            AEKeyRenderer<T, S> renderer,
            PoseStack poseStack,
            SubmitNodeCollector nodes,
            int lightCoords) {
        Class<S> stateClass = renderer.stateClass();
        if (currentState == null || currentState.getClass() != stateClass) {
            throw new IllegalStateException();
        }

        renderer.submit(poseStack, stateClass.cast(currentState), nodes, lightCoords);
    }
}
