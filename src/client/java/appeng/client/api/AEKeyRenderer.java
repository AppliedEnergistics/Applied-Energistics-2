package appeng.client.api;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public interface AEKeyRenderer<T, S> {
    /**
     * Draw the stack, for example the item or the fluid sprite, but not the amount.
     */
    void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, T stack);

    Class<S> stateClass();

    /**
     * @return Create state used for storing this handlers render state.
     */
    S createState();

    /**
     * Draw the representation of a key in-world on the face of a block. Used for displaying it on screens and monitors.
     */
    void extract(S state, T what, @Nullable Level level, int seed);

    /**
     * Draw the representation of a key in-world on the face of a block. Used for displaying it on screens and monitors.
     */
    void submit(PoseStack poseStack,
            S state,
            SubmitNodeCollector nodes,
            int lightCoords);

    /**
     * Return the full tooltip, with the name of the stack and any additional lines.
     */
    List<Component> getTooltip(T stack);
}
