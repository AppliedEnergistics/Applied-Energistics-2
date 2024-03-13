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

package appeng.integration.modules.rei;

import java.util.stream.Stream;

import net.minecraft.client.gui.screens.Screen;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;

import appeng.client.gui.AEBaseScreen;
import appeng.integration.modules.itemlists.DropTargets;

/**
 * JEI allows ingredients to be dragged from a JEI panel onto compatible slots to set filters and the like without
 * having the actual item in hand.
 */
@SuppressWarnings("rawtypes")
class GhostIngredientHandler implements DraggableStackVisitor<AEBaseScreen> {

    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof AEBaseScreen;
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<AEBaseScreen> context,
            DraggableStack stack) {

        var genericStack = GenericEntryStackHelper.ingredientToStack(stack.getStack());
        if (genericStack == null) {
            return Stream.of();
        }

        return DropTargets.getTargets(context.getScreen())
                .stream()
                .filter(dropTarget -> dropTarget.canDrop(genericStack))
                .map(target -> {
                    var area = target.area();
                    return BoundsProvider.ofRectangle(new Rectangle(
                            area.getX(), area.getY(),
                            area.getWidth(), area.getHeight()));
                });
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<AEBaseScreen> context, DraggableStack stack) {
        var genericStack = GenericEntryStackHelper.ingredientToStack(stack.getStack());
        if (genericStack == null) {
            return DraggedAcceptorResult.PASS;
        }

        var pos = context.getCurrentPosition();
        if (pos == null) {
            return DraggedAcceptorResult.PASS;
        }

        for (var target : DropTargets.getTargets(context.getScreen())) {
            if (target.area().contains(pos.x, pos.y) && target.canDrop(genericStack)) {
                target.drop(genericStack);
                return DraggedAcceptorResult.ACCEPTED;
            }
        }

        return DraggedAcceptorResult.PASS;
    }

}
