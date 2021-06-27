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

package appeng.init.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import appeng.core.definitions.AEEntities;
import appeng.entity.TinyTNTPrimedRenderer;

/**
 * Registers custom renderers for our {@link AEEntities}.
 */
public final class InitEntityRendering {

    private InitEntityRendering() {

    }

    public static void init() {

        RenderingRegistry.registerEntityRenderingHandler(AEEntities.TINY_TNT_PRIMED, TinyTNTPrimedRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(AEEntities.SINGULARITY,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(AEEntities.GROWING_CRYSTAL,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(AEEntities.CHARGED_QUARTZ,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
    }

}
