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
package appeng.api.definitions;


import appeng.api.util.AEItemDefinition;


/**
 * A list of all materials in AE
 */
public interface IMaterials
{
	AEItemDefinition cell2SpatialPart();

	AEItemDefinition cell16SpatialPart();

	AEItemDefinition cell128SpatialPart();

	AEItemDefinition silicon();

	AEItemDefinition skyDust();

	AEItemDefinition calcProcessorPress();

	AEItemDefinition engProcessorPress();

	AEItemDefinition logicProcessorPress();

	AEItemDefinition calcProcessorPrint();

	AEItemDefinition engProcessorPrint();

	AEItemDefinition logicProcessorPrint();

	AEItemDefinition siliconPress();

	AEItemDefinition siliconPrint();

	AEItemDefinition namePress();

	AEItemDefinition logicProcessor();

	AEItemDefinition calcProcessor();

	AEItemDefinition engProcessor();

	AEItemDefinition basicCard();

	AEItemDefinition advCard();

	AEItemDefinition purifiedCertusQuartzCrystal();

	AEItemDefinition purifiedNetherQuartzCrystal();

	AEItemDefinition purifiedFluixCrystal();

	AEItemDefinition cell1kPart();

	AEItemDefinition cell4kPart();

	AEItemDefinition cell16kPart();

	AEItemDefinition cell64kPart();

	AEItemDefinition emptyStorageCell();

	AEItemDefinition cardRedstone();

	AEItemDefinition cardSpeed();

	AEItemDefinition cardCapacity();

	AEItemDefinition cardFuzzy();

	AEItemDefinition cardInverter();

	AEItemDefinition cardCrafting();

	AEItemDefinition enderDust();

	AEItemDefinition flour();

	AEItemDefinition goldDust();

	AEItemDefinition ironDust();

	AEItemDefinition fluixDust();

	AEItemDefinition certusQuartzDust();

	AEItemDefinition netherQuartzDust();

	AEItemDefinition matterBall();

	AEItemDefinition ironNugget();

	AEItemDefinition certusQuartzCrystal();

	AEItemDefinition certusQuartzCrystalCharged();

	AEItemDefinition fluixCrystal();

	AEItemDefinition fluixPearl();

	AEItemDefinition woodenGear();

	AEItemDefinition wireless();

	AEItemDefinition wirelessBooster();

	AEItemDefinition annihilationCore();

	AEItemDefinition formationCore();

	AEItemDefinition singularity();

	AEItemDefinition qESingularity();

	AEItemDefinition blankPattern();
}
