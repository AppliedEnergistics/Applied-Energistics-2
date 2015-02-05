/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

package appeng.api.definitions;


import com.google.common.base.Optional;

import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;


public interface IParts
{
	Optional<AEColoredItemDefinition> cableSmart();

	Optional<AEColoredItemDefinition> cableCovered();

	Optional<AEColoredItemDefinition> cableGlass();

	Optional<AEColoredItemDefinition> cableDense();

	Optional<AEColoredItemDefinition> lumenCableSmart();

	Optional<AEColoredItemDefinition> lumenCableCovered();

	Optional<AEColoredItemDefinition> lumenCableGlass();

	Optional<AEColoredItemDefinition> lumenCableDense();

	Optional<AEItemDefinition> quartzFiber();

	Optional<AEItemDefinition> toggleBus();

	Optional<AEItemDefinition> invertedToggleBus();

	Optional<AEItemDefinition> storageBus();

	Optional<AEItemDefinition> importBus();

	Optional<AEItemDefinition> exportBus();

	Optional<AEItemDefinition> iface();

	Optional<AEItemDefinition> levelEmitter();

	Optional<AEItemDefinition> annihilationPlane();

	Optional<AEItemDefinition> formationPlane();

	Optional<AEItemDefinition> p2PTunnelME();

	Optional<AEItemDefinition> p2PTunnelRedstone();

	Optional<AEItemDefinition> p2PTunnelItems();

	Optional<AEItemDefinition> p2PTunnelLiquids();

	Optional<AEItemDefinition> p2PTunnelMJ();

	Optional<AEItemDefinition> p2PTunnelEU();

	Optional<AEItemDefinition> p2PTunnelRF();

	Optional<AEItemDefinition> p2PTunnelLight();

	Optional<AEItemDefinition> cableAnchor();

	Optional<AEItemDefinition> monitor();

	Optional<AEItemDefinition> semiDarkMonitor();

	Optional<AEItemDefinition> darkMonitor();

	Optional<AEItemDefinition> interfaceTerminal();

	Optional<AEItemDefinition> patternTerminal();

	Optional<AEItemDefinition> craftingTerminal();

	Optional<AEItemDefinition> terminal();

	Optional<AEItemDefinition> storageMonitor();

	Optional<AEItemDefinition> conversionMonitor();
}
