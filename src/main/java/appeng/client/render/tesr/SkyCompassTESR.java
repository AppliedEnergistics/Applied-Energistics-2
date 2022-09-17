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

package appeng.client.render.tesr;


import appeng.block.AEBaseTileBlock;
import appeng.block.misc.BlockSkyCompass;
import appeng.client.render.model.SkyCompassBakedModel;
import appeng.tile.misc.TileSkyCompass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class SkyCompassTESR extends FastTESR<TileSkyCompass> {

    private static BlockRendererDispatcher blockRenderer;

    @Override
    public void renderTileEntityFast(TileSkyCompass te, double x, double y, double z, float partialTicks, int destroyStage, float var10, BufferBuilder buffer) {

        if (!te.hasWorld()) {
            return;
        }

        if (blockRenderer == null) {
            blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }

        BlockPos pos = te.getPos();
        IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
        IBlockState state = world.getBlockState(pos);
        if (state.getPropertyKeys().contains(Properties.StaticProperty)) {
            state = state.withProperty(Properties.StaticProperty, false);
        }

        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState exState = (IExtendedBlockState) state.getBlock().getExtendedState(state, world, pos);

            IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(exState.getClean());
            exState = exState.withProperty(BlockSkyCompass.ROTATION, getRotation(te));

            // Flip forward/up for rendering, the base model is facing up without any rotation
            EnumFacing forward = exState.getValue(AEBaseTileBlock.FORWARD);
            EnumFacing up = exState.getValue(AEBaseTileBlock.UP);
            // This ensures the needle isn't flipped by the model rotator. Since the model is symmetrical, this should
            // not affect the appearance
            if (forward == EnumFacing.UP || forward == EnumFacing.DOWN) {
                up = EnumFacing.NORTH;
            }
            exState = exState.withProperty(AEBaseTileBlock.FORWARD, up)
                    .withProperty(AEBaseTileBlock.UP, forward);

            buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

            blockRenderer.getBlockModelRenderer().renderModel(world, model, exState, pos, buffer, false);
        }
    }

    private static float getRotation(TileSkyCompass skyCompass) {
        float rotation;

        if (skyCompass.getForward() == EnumFacing.UP || skyCompass.getForward() == EnumFacing.DOWN) {
            rotation = SkyCompassBakedModel.getAnimatedRotation(skyCompass.getPos(), false);
        } else {
            rotation = SkyCompassBakedModel.getAnimatedRotation(null, false);
        }

        if (skyCompass.getForward() == EnumFacing.DOWN) {
            rotation = flipidiy(rotation);
        }

        return rotation;
    }

    private static float flipidiy(float rad) {
        float x = (float) Math.cos(rad);
        float y = (float) Math.sin(rad);
        return (float) Math.atan2(-y, x);
    }
}
