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

package appeng.client.render.model;

import appeng.block.storage.BlockDrive;
import appeng.block.storage.DriveSlotState;
import appeng.block.storage.DriveSlotsState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriveBakedModel implements IBakedModel {
    private final IBakedModel bakedBase;
    private final Map<DriveSlotState, IBakedModel> bakedCells;

    public DriveBakedModel(IBakedModel bakedBase, Map<DriveSlotState, IBakedModel> bakedCells) {
        this.bakedBase = bakedBase;
        this.bakedCells = bakedCells;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

        List<BakedQuad> result = new ArrayList<>(this.bakedBase.getQuads(state, side, rand));

        if (side == null && state instanceof IExtendedBlockState) {
            IExtendedBlockState extState = (IExtendedBlockState) state;

            if (!extState.getUnlistedNames().contains(BlockDrive.SLOTS_STATE)) {
                return result;
            }

            DriveSlotsState slotsState = extState.getValue(BlockDrive.SLOTS_STATE);

            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 2; col++) {
                    DriveSlotState slotState = slotsState.getState(row * 2 + col);

                    IBakedModel bakedCell = this.bakedCells.get(slotState);

                    Matrix4f transform = new Matrix4f();
                    transform.setIdentity();

                    // Position this drive model copy at the correct slot. The transform is based on
                    // the
                    // cell-model being in slot 0,0 at the top left of the drive.
                    float xOffset = -col * 8 / 16.0f;
                    float yOffset = -row * 3 / 16.0f;

                    transform.setTranslation(new Vector3f(xOffset, yOffset, 0));

                    MatrixVertexTransformer transformer = new MatrixVertexTransformer(transform);
                    for (BakedQuad bakedQuad : bakedCell.getQuads(state, null, rand)) {
                        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(bakedQuad.getFormat());
                        transformer.setParent(builder);
                        transformer.setVertexFormat(builder.getVertexFormat());
                        bakedQuad.pipe(transformer);
                        result.add(builder.build());
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.bakedBase.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.bakedBase.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.bakedBase.isGui3d();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.bakedBase.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.bakedBase.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.bakedBase.getOverrides();
    }
}
