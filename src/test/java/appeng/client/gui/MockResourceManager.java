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

package appeng.client.gui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;

import appeng.core.AppEng;

/**
 * Fake resource manager that more or less loads AE2 resource pack resources.
 */
public final class MockResourceManager {
    private MockResourceManager() {
    }

    public static ReloadableResourceManager create() {
        ReloadableResourceManager resourceManager = mock(ReloadableResourceManager.class, withSettings().lenient());

        // Delegate default methods to real impls
        try {
            when(resourceManager.openAsReader(any())).thenCallRealMethod();
            when(resourceManager.open(any())).thenCallRealMethod();
            when(resourceManager.getResourceOrThrow(any())).thenCallRealMethod();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        when(resourceManager.getResource(any())).thenAnswer(invoc -> {
            net.minecraft.resources.ResourceLocation loc = invoc.getArgument(0);
            return getResource(loc);
        });
        when(resourceManager.getResourceStack(any())).thenAnswer(invoc -> {
            ResourceLocation loc = invoc.getArgument(0);
            return getResource(loc)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        });

        when(resourceManager.getNamespaces()).thenReturn(
                ImmutableSet.of("minecraft", AppEng.MOD_ID));

        return resourceManager;
    }

    private static Optional<Resource> getResource(net.minecraft.resources.ResourceLocation loc) {
        var resourceLocation = "/assets/" + loc.getNamespace() + "/" + loc.getPath();
        if (MockResourceManager.class.getResource(resourceLocation) == null) {
            return Optional.empty();
        }

        return Optional.of(new Resource(
                "ae2",
                () -> {
                    return MockResourceManager.class.getResourceAsStream(resourceLocation);
                }));
    }
}
