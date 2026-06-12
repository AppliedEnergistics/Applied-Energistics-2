package appeng.client.renderer.spatialstorage;

import org.joml.Matrix4fc;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomCloudsRenderer;

public class SpatialStorageCloudsRenderer implements CustomCloudsRenderer {
    @Override
    public boolean renderClouds(LevelRenderState levelRenderState, Vec3 camPos, CloudStatus cloudStatus, int cloudColor,
            float cloudHeight, int cloudRange, Matrix4fc modelViewMatrix) {
        return true; // Skip rendering Vanilla clouds.
    }
}
