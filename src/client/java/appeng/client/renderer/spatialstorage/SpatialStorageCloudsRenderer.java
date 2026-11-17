package appeng.client.renderer.spatialstorage;

import com.mojang.renderpearl.api.commands.RenderPass;

import org.joml.Matrix4fc;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.neoforge.client.CustomCloudsRenderer;

public class SpatialStorageCloudsRenderer implements CustomCloudsRenderer {
    @Override
    public boolean renderClouds(LevelRenderState levelRenderState, CloudStatus cloudStatus, Matrix4fc modelViewMatrix,
            RenderPass renderPass) {
        return true; // Skip rendering Vanilla clouds.
    }
}
