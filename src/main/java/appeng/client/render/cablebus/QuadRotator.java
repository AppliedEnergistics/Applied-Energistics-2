/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.cablebus;

import appeng.client.render.FacingToRotation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;

/**
 * Assuming a default-orientation of forward=NORTH and up=UP, this class rotates
 * a given list of quads to the desired facing
 */
@Environment(EnvType.CLIENT)
public class QuadRotator implements RenderContext.QuadTransform {
// FIXME    private static final ThreadLocal<BakedPipeline> pipelines = ThreadLocal.withInitial(() -> //
// FIXME    BakedPipeline.builder()//
// FIXME            .addElement("transformer", QuadMatrixTransformer.FACTORY)//
// FIXME            .build());
// FIXME    private static final ThreadLocal<Quad> collectors = ThreadLocal.withInitial(Quad::new);

    private static final RenderContext.QuadTransform NULL_TRANSFORM = quad -> true;

    private final FacingToRotation rotation;

    public QuadRotator(FacingToRotation rotation) {
        this.rotation = rotation;
    }

    public static RenderContext.QuadTransform get(Direction newForward, Direction newUp) {
        if (newForward == Direction.NORTH && newUp == Direction.UP) {
            return NULL_TRANSFORM; // This is the default orientation
        }
        FacingToRotation rotation = getRotation(newForward, newUp);
        if (rotation.isRedundant()) {
            return NULL_TRANSFORM;
        }
        return new QuadRotator(rotation);
    }

    @Override
    public boolean transform(MutableQuadView quad) {

        // FIXME: Temporary rotation fix
        Matrix4f mat = new Matrix4f();
        mat.addToLastColumn(new Vector3f(-0.5f, -0.5f, -0.5f));
        mat.multiply(rotation.getMat());
        mat.addToLastColumn(new Vector3f(0.5f, 0.5f, 0.5f));

// FIXME ROTATION        pipeline.reset(format);
// FIXME ROTATION        collector.reset(format);
// FIXME ROTATION
// FIXME ROTATION        transformer.setMatrix(mat);
// FIXME ROTATION        pipeline.prepare(collector);
// FIXME ROTATION        quad.pipe(pipeline);

        return true;
    }

    private static FacingToRotation getRotation(Direction forward, Direction up) {
        // Sanitize forward/up
        if (forward.getAxis() == up.getAxis()) {
            if (up.getAxis() == Direction.Axis.Y) {
                up = Direction.NORTH;
            } else {
                up = Direction.UP;
            }
        }

        return FacingToRotation.get(forward, up);
    }

}
