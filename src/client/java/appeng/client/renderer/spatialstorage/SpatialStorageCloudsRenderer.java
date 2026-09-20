package appeng.client.renderer.spatialstorage;

import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;

import org.joml.Matrix4fc;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.oit.OitRenderPassProvider;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.neoforge.client.CustomCloudsRenderer;

public class SpatialStorageCloudsRenderer implements CustomCloudsRenderer {
    // CustomCloudsRenderer#renderClouds lost the camera position / cloud colour / height / range
    // parameters (they moved to the new prepare(...) hook) and gained the active RenderPass. 26.3 also added
    // a second, order-independent-transparency code path (renderCloudsOit), which is the one actually used
    // when GameRenderer#useImprovedTransparency() is on - both have to be suppressed to keep the spatial
    // storage dimension cloudless.
    @Override
    public boolean renderClouds(LevelRenderState levelRenderState, CloudStatus cloudStatus,
            Matrix4fc modelViewMatrix, RenderPass renderPass) {
        return true; // Skip rendering Vanilla clouds.
    }

    // Upstream bug: in NeoForge 26.3.0.7-beta, LevelRenderer#executeOit (LevelRenderer.java:645)
    // calls this as `customCloudsRenderer == null || customCloudsRenderer.renderCloudsOit(...)` - it is
    // MISSING the `!` that the non-OIT path at line 737 has. With improved transparency enabled, returning
    // true (the documented "suppress vanilla" value) therefore still renders vanilla clouds. We follow the
    // documented contract here so this is correct once NeoForge fixes the negation.
    @Override
    public boolean renderCloudsOit(LevelRenderState levelRenderState, CloudStatus cloudStatus,
            Matrix4fc modelViewMatrix, OitStage stage, GpuTextureView mainDepthTextureView,
            OitRenderPassProvider.Parameters params) {
        return true; // Skip rendering Vanilla clouds.
    }
}
