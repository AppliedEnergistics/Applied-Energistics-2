package appeng.client.renderer.spatialstorage;

import org.joml.Matrix4f;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomCloudsRenderer;

public class SpatialStorageCloudsRenderer implements CustomCloudsRenderer {
    @Override
    public boolean renderClouds(LevelRenderState levelRenderState, Vec3 camPos, CloudStatus cloudStatus, int cloudColor,
            float cloudHeight, Matrix4f modelViewMatrix) {
        return true; // Skip rendering Vanilla clouds.
    }
}
