package appeng.siteexport;

import java.io.IOException;
import java.nio.file.Paths;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL12;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import appeng.api.definitions.IDefinitions;
import appeng.core.Api;
import appeng.core.CreativeTab;

public class Entrypoint implements ClientModInitializer {

    private static final int FB_WIDTH = 128;
    private static final int FB_HEIGHT = 128;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            client.currentScreen = new Screen(Text.of("DUMMY")) {
                @Override
                protected void init() {
                    while (true) {
                        runExport(client);
                    }
//                    client.getWindow().close();
                }
            };
        });
    }

    private void runExport(MinecraftClient client) {

        // Set up GL state for GUI rendering
        RenderSystem.matrixMode(GL12.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, 16, 16, 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL12.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        DiffuseLighting.enableGuiDepthLighting();
        MatrixStack matrixStack = new MatrixStack();

        IDefinitions definitions = Api.instance().definitions();

        NativeImage nativeImage = new NativeImage(FB_WIDTH, FB_HEIGHT, true);
        Framebuffer fb = new Framebuffer(FB_WIDTH, FB_HEIGHT, true, false);
        fb.setClearColor(0, 0, 0, 0);
        fb.clear(false);

        // Iterate over all Applied Energistics items
        DefaultedList<ItemStack> stacks = DefaultedList.of();
        CreativeTab.INSTANCE.appendStacks(stacks);

        // Compute the square grid size needed to have enough cells for the number of
        // items we
        // want to render
        int s = (int) Math.ceil(Math.sqrt(stacks.size()));
        // Add a transparent margin to avoid problems with texture interpolation
        // grabbing pixels
        // from adjacent items.
        final int MARGIN = 1;
        final int CELL_WIDTH = FB_WIDTH + 2 * MARGIN;
        final int CELL_HEIGHT = FB_HEIGHT + 2 * MARGIN;
        NativeImage result = new NativeImage(s * CELL_WIDTH, s * CELL_HEIGHT, true);

        SpriteIndexWriter indexWriter = new SpriteIndexWriter(result.getWidth(), result.getHeight(), s, s, MARGIN);

        int idx = 0;
        for (ItemStack stack : stacks) {
            // Render the item normally
            fb.beginWrite(true);
            GlStateManager.clear(GL12.GL_COLOR_BUFFER_BIT | GL12.GL_DEPTH_BUFFER_BIT, false);
            client.getItemRenderer().renderInGui(stack, 0, 0);
            fb.endWrite();

            // Load the rendered item back into CPU memory
            fb.beginRead();
            nativeImage.loadFromTextureImage(0, false);
            nativeImage.mirrorVertically();
            fb.endRead();

            // Copy it to the sprite-sheet
            int xIdx = idx % s;
            int xOut = xIdx * CELL_WIDTH + MARGIN;
            int yIdx = idx / s;
            int yOut = yIdx * CELL_HEIGHT + MARGIN;
            idx++;
            for (int y = 0; y < FB_HEIGHT; y++) {
                for (int x = 0; x < FB_WIDTH; x++) {
                    result.setPixelColor(xOut + x, yOut + y, nativeImage.getPixelColor(x, y));
                }
            }

            // Add it to the index
            indexWriter.add(stack, xIdx, yIdx);
        }

        try {
            result.writeFile(Paths.get("item_sheet.png"));
            indexWriter.write(Paths.get("item_sheet.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        nativeImage.close();
        result.close();
        fb.delete();

    }
}
