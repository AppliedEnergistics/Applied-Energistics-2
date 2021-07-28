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

package appeng.client.gui.style;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import appeng.client.gui.MockResourceManager;
import appeng.container.SlotSemantic;

@MockitoSettings(strictness = Strictness.LENIENT)
class StyleManagerTest {

    @Captor
    ArgumentCaptor<PreparableReloadListener> reloadCaptor;

    @Test
    void testInitialize() throws IOException {
        ReloadableResourceManager resourceManager = MockResourceManager.create();
        StyleManager.initialize(resourceManager);
        verify(resourceManager).registerReloadListener(reloadCaptor.capture());
        assertThat(reloadCaptor.getValue())
                .isNotNull()
                .isInstanceOf(ResourceManagerReloadListener.class);
        ((ResourceManagerReloadListener) reloadCaptor.getValue()).onResourceManagerReload(resourceManager);
    }

    @Test
    void testLoadStyleDoc() throws IOException {
        StyleManager.initialize(MockResourceManager.create());

        ScreenStyle style = StyleManager.loadStyleDoc("/screens/cell_workbench.json");

        assertThat(style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB()).isEqualTo(0xff404040);
        assertThat(style.getText()).hasSize(2);
        assertThat(style.getSlots()).containsOnlyKeys(
                SlotSemantic.TOOLBOX,
                SlotSemantic.PLAYER_INVENTORY,
                SlotSemantic.PLAYER_HOTBAR,
                SlotSemantic.CONFIG,
                SlotSemantic.STORAGE_CELL);
    }
}
