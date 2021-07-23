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

package appeng.block.qnb;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.EffectType;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.QNBContainer;
import appeng.core.AppEng;
import appeng.core.AppEngClient;
import appeng.helpers.AEMaterials;
import appeng.tile.qnb.QuantumBridgeTileEntity;
import appeng.util.InteractionUtil;

public class QuantumLinkChamberBlock extends QuantumBaseBlock {

    private static final VoxelShape SHAPE;

    static {
        final double onePixel = 2.0 / 16.0;
        SHAPE = Shapes.create(
                new AABB(onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel));
    }

    public QuantumLinkChamberBlock() {
        super(defaultProps(AEMaterials.GLASS));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState state, final Level w, final BlockPos pos, final Random rand) {
        final QuantumBridgeTileEntity bridge = this.getTileEntity(w, pos);
        if (bridge != null && bridge.hasQES() && AppEngClient.instance().shouldAddParticles(rand)) {
            AppEng.instance().spawnEffect(EffectType.Energy, w, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    null);
        }
    }

    @Override
    public InteractionResult onActivated(final Level w, final net.minecraft.core.BlockPos pos, final Player p, final InteractionHand hand,
                                         final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return InteractionResult.PASS;
        }

        final QuantumBridgeTileEntity tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (!w.isClientSide()) {
                ContainerOpener.openContainer(QNBContainer.TYPE, p, ContainerLocator.forTileEntity(tg));
            }
            return InteractionResult.sidedSuccess(w.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(net.minecraft.world.level.block.state.BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

}
