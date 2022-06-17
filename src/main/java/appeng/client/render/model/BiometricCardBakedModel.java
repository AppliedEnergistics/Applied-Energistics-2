/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.implementations.items.IBiometricCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;

class BiometricCardBakedModel implements BakedModel, FabricBakedModel {

    private final BakedModel baseModel;

    private final TextureAtlasSprite texture;

    BiometricCardBakedModel(BakedModel baseModel, TextureAtlasSprite texture) {
        this.baseModel = baseModel;
        this.texture = texture;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context) {
        // Not intended as a block
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        context.fallbackConsumer().accept(this.baseModel);

        // Get the player's name hash from the card
        int hash = getHash(stack);

        emitColorCode(context.getEmitter(), hash);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return this.baseModel.getQuads(state, side, rand);
    }

    private void emitColorCode(QuadEmitter emitter, int hash) {
        CubeBuilder builder = new CubeBuilder(emitter);

        builder.setTexture(this.texture);

        AEColor col = AEColor.values()[Math.abs(3 + hash) % AEColor.values().length];
        if (hash == 0) {
            col = AEColor.BLACK;
        }

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 6; y++) {
                final boolean isLit;

                // This makes the border always use the darker color
                if (x == 0 || y == 0 || x == 7 || y == 5) {
                    isLit = false;
                } else {
                    isLit = (hash & 1 << x) != 0 || (hash & 1 << y) != 0;
                }

                if (isLit) {
                    builder.setColorRGB(col.mediumVariant);
                } else {
                    final float scale = 0.3f / 255.0f;
                    builder.setColorRGB((col.blackVariant >> 16 & 0xff) * scale,
                            (col.blackVariant >> 8 & 0xff) * scale, (col.blackVariant & 0xff) * scale);
                }

                builder.addCube(4 + x, 6 + y, 7.5f, 4 + x + 1, 6 + y + 1, 8.5f);
            }
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;// TODO
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.baseModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    private static int getHash(ItemStack stack) {
        String username = "";
        if (stack.getItem() instanceof IBiometricCard biometricCard) {
            var gp = biometricCard.getProfile(stack);
            if (gp != null) {
                if (gp.getId() != null) {
                    username = gp.getId().toString();
                } else {
                    username = gp.getName();
                }
            }
        }

        return !username.isEmpty() ? username.hashCode() : 0;
    }

}
