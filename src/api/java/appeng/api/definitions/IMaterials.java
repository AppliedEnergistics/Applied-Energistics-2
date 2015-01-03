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

import appeng.api.util.AEItemDefinition;


public interface IMaterials
{
	Optional<AEItemDefinition> cell2SpatialPart();

	Optional<AEItemDefinition> cell16SpatialPart();

	Optional<AEItemDefinition> cell128SpatialPart();

	Optional<AEItemDefinition> silicon();

	Optional<AEItemDefinition> skyDust();

	Optional<AEItemDefinition> calcProcessorPress();

	Optional<AEItemDefinition> engProcessorPress();

	Optional<AEItemDefinition> logicProcessorPress();

	Optional<AEItemDefinition> calcProcessorPrint();

	Optional<AEItemDefinition> engProcessorPrint();

	Optional<AEItemDefinition> logicProcessorPrint();

	Optional<AEItemDefinition> siliconPress();

	Optional<AEItemDefinition> siliconPrint();

	Optional<AEItemDefinition> namePress();

	Optional<AEItemDefinition> logicProcessor();

	Optional<AEItemDefinition> calcProcessor();

	Optional<AEItemDefinition> engProcessor();

	Optional<AEItemDefinition> basicCard();

	Optional<AEItemDefinition> advCard();

	Optional<AEItemDefinition> purifiedCertusQuartzCrystal();

	Optional<AEItemDefinition> purifiedNetherQuartzCrystal();

	Optional<AEItemDefinition> purifiedFluixCrystal();

	Optional<AEItemDefinition> cell1kPart();

	Optional<AEItemDefinition> cell4kPart();

	Optional<AEItemDefinition> cell16kPart();

	Optional<AEItemDefinition> cell64kPart();

	Optional<AEItemDefinition> emptyStorageCell();

	Optional<AEItemDefinition> cardRedstone();

	Optional<AEItemDefinition> cardSpeed();

	Optional<AEItemDefinition> cardCapacity();

	Optional<AEItemDefinition> cardFuzzy();

	Optional<AEItemDefinition> cardInverter();

	Optional<AEItemDefinition> cardCrafting();

	Optional<AEItemDefinition> enderDust();

	Optional<AEItemDefinition> flour();

	Optional<AEItemDefinition> goldDust();

	Optional<AEItemDefinition> ironDust();

	Optional<AEItemDefinition> fluixDust();

	Optional<AEItemDefinition> certusQuartzDust();

	Optional<AEItemDefinition> netherQuartzDust();

	Optional<AEItemDefinition> matterBall();

	Optional<AEItemDefinition> ironNugget();

	Optional<AEItemDefinition> certusQuartzCrystal();

	Optional<AEItemDefinition> certusQuartzCrystalCharged();

	Optional<AEItemDefinition> fluixCrystal();

	Optional<AEItemDefinition> fluixPearl();

	Optional<AEItemDefinition> woodenGear();

	Optional<AEItemDefinition> wireless();

	Optional<AEItemDefinition> wirelessBooster();

	Optional<AEItemDefinition> annihilationCore();

	Optional<AEItemDefinition> formationCore();

	Optional<AEItemDefinition> singularity();

	Optional<AEItemDefinition> qESingularity();

	Optional<AEItemDefinition> blankPattern();
}
