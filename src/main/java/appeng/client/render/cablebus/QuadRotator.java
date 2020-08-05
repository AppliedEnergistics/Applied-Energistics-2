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

import java.util.EnumMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

import appeng.client.render.FacingToRotation;

/**
 * Assuming a default-orientation of forward=NORTH and up=UP, this class rotates
 * a given list of quads to the desired facing
 */
@Environment(EnvType.CLIENT)
public class QuadRotator implements RenderContext.QuadTransform {

    public static final RenderContext.QuadTransform NULL_TRANSFORM = quad -> true;

    private static final EnumMap<FacingToRotation, RenderContext.QuadTransform> TRANSFORMS = new EnumMap<>(
            FacingToRotation.class);

    static {
        for (FacingToRotation rotation : FacingToRotation.values()) {
            if (rotation.isRedundant()) {
                TRANSFORMS.put(rotation, NULL_TRANSFORM);
            } else {
                TRANSFORMS.put(rotation, new QuadRotator(rotation));
            }
        }
    }

    private final FacingToRotation rotation;

    private final Quaternion quaternion;

    private QuadRotator(FacingToRotation rotation) {
        this.rotation = rotation;
        this.quaternion = rotation.getRot();
    }

    public static RenderContext.QuadTransform get(Direction newForward, Direction newUp) {
        return get(getRotation(newForward, newUp));
    }

    public static RenderContext.QuadTransform get(FacingToRotation rotation) {
        if (rotation.isRedundant()) {
            return NULL_TRANSFORM; // This is the default orientation
        }
        return TRANSFORMS.get(rotation);
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        Vector3f tmp = new Vector3f();

        for (int i = 0; i < 4; i++) {
            // Transform the position (center of rotation is 0.5, 0.5, 0.5)
            quad.copyPos(i, tmp);
            tmp.add(-0.5f, -0.5f, -0.5f);
            tmp.rotate(quaternion);
            tmp.add(0.5f, 0.5f, 0.5f);
            quad.pos(i, tmp);

            // Transform the normal
            if (quad.hasNormal(i)) {
                quad.copyNormal(i, tmp);
                tmp.rotate(quaternion);
                quad.normal(i, tmp);
            }
        }

        // FIXME FABRIC: Wait for Grondags response about this one
        MutableQuadViewImpl quadImpl = (MutableQuadViewImpl) quad;
        quadImpl.lightFace(rotation.rotate(quad.lightFace()));

        // Transform the nominal face, setting the cull face will also overwrite the nominialFace,
        // hence saving both first and the order here.
        Direction nominalFace = quad.nominalFace();
        Direction cullFace = quad.cullFace();
        if (cullFace != null) {
            quad.cullFace(rotation.rotate(cullFace));
        }
        quad.nominalFace(rotation.rotate(nominalFace));

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
