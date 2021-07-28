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

package appeng.client.render.crafting;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.PerspectiveMapWrapper;

import appeng.client.render.DelegateBakedModel;
import appeng.items.misc.EncodedPatternItem;

/**
 * This model is used to substitute the crafting result's item model for our encoded pattern model if these two
 * conditions are met: - The player is holding shift - The itemstack is being rendered in the UI (not on the ground,
 * etc.)
 * <p>
 * We do this by abusing a custom {@link ItemOverrides} since it will be called each frame with the itemstack that is
 * about to be rendered. We return a custom IBakedModel ({@link ShiftHoldingModelWrapper} if the player is holding down
 * shift from the override list. This custom baked model implements {@link #doesHandlePerspectives()} and returns the
 * crafting result model if the model for {@link TransformType#GUI} is requested.
 */
public class EncodedPatternBakedModel extends DelegateBakedModel {

    private final CustomOverrideList overrides;

    EncodedPatternBakedModel(BakedModel baseModel) {
        super(baseModel);
        this.overrides = new CustomOverrideList();
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    /**
     * Since the ItemOverrideList handling comes before handling the perspective awareness (which is the first place
     * where we know how we are being rendered) we need to remember the model of the crafting output, and make the
     * decision on which to render later on. Sadly, Forge is pretty inconsistent when it will call the handlePerspective
     * method, so some methods are called even on this interim-model. Usually those methods only matter for rendering on
     * the ground and other cases, where we wouldn't render the crafting output model anyway, so in those cases we
     * delegate to the model of the encoded pattern.
     */
    private static class ShiftHoldingModelWrapper extends DelegateBakedModel {

        private final BakedModel outputModel;

        private ShiftHoldingModelWrapper(BakedModel patternModel, BakedModel outputModel) {
            super(patternModel);
            this.outputModel = outputModel;
        }

        @Override
        public boolean doesHandlePerspectives() {
            return true;
        }

        @Override
        public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat) {
            // No need to re-check for shift being held since this model is only handed out
            // in that case
            if (cameraTransformType == TransformType.GUI) {
                ImmutableMap<TransformType, Transformation> transforms = PerspectiveMapWrapper
                        .getTransforms(outputModel.getTransforms());
                return PerspectiveMapWrapper.handlePerspective(this.outputModel, transforms, cameraTransformType, mat);
            } else {
                return getBaseModel().handlePerspective(cameraTransformType, mat);
            }
        }

        // This determines diffuse lighting in the UI, and since we want to render
        // the outputModel in the UI, we need to use it's setting here
        @Override
        public boolean usesBlockLight() {
            return outputModel.usesBlockLight();
        }

    }

    @Override
    public boolean doesHandlePerspectives() {
        return true;
    }

    @Override
    public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat) {
        return getBaseModel().handlePerspective(cameraTransformType, mat);
    }

    /**
     * Item Override Lists are the only point during item rendering where we can access the item stack that is being
     * rendered. So this is the point where we actually check if shift is being held, and if so, determine the crafting
     * output model.
     */
    private class CustomOverrideList extends ItemOverrides {

        @Nullable
        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world,
                @Nullable LivingEntity entity, int seed) {
            boolean shiftHeld = Screen.hasShiftDown();
            if (shiftHeld) {
                EncodedPatternItem iep = (EncodedPatternItem) stack.getItem();
                ItemStack output = iep.getOutput(stack);
                if (!output.isEmpty()) {
                    BakedModel realModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper()
                            .getItemModel(output);
                    // Give the item model a chance to handle the overrides as well
                    realModel = realModel.getOverrides().resolve(realModel, output, world, entity, seed);
                    return new ShiftHoldingModelWrapper(getBaseModel(), realModel);
                }
            }

            return getBaseModel().getOverrides().resolve(originalModel, stack, world, entity, seed);
        }
    }

}
