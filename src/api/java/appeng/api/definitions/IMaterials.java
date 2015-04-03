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


/**
 * A list of all materials in AE
 */
public interface IMaterials
{
	IItemDefinition cell2SpatialPart();

	IItemDefinition cell16SpatialPart();

	IItemDefinition cell128SpatialPart();

	IItemDefinition silicon();

	IItemDefinition skyDust();

	IItemDefinition calcProcessorPress();

	IItemDefinition engProcessorPress();

	IItemDefinition logicProcessorPress();

	IItemDefinition calcProcessorPrint();

	IItemDefinition engProcessorPrint();

	IItemDefinition logicProcessorPrint();

	IItemDefinition siliconPress();

	IItemDefinition siliconPrint();

	IItemDefinition namePress();

	IItemDefinition logicProcessor();

	IItemDefinition calcProcessor();

	IItemDefinition engProcessor();

	IItemDefinition basicCard();

	IItemDefinition advCard();

	IItemDefinition purifiedCertusQuartzCrystal();

	IItemDefinition purifiedNetherQuartzCrystal();

	IItemDefinition purifiedFluixCrystal();

	IItemDefinition cell1kPart();

	IItemDefinition cell4kPart();

	IItemDefinition cell16kPart();

	IItemDefinition cell64kPart();

	IItemDefinition emptyStorageCell();

	IItemDefinition cardRedstone();

	IItemDefinition cardSpeed();

	IItemDefinition cardCapacity();

	IItemDefinition cardFuzzy();

	IItemDefinition cardInverter();

	IItemDefinition cardCrafting();

	IItemDefinition enderDust();

	IItemDefinition flour();

	IItemDefinition goldDust();

	IItemDefinition ironDust();

	IItemDefinition fluixDust();

	IItemDefinition certusQuartzDust();

	IItemDefinition netherQuartzDust();

	IItemDefinition matterBall();

	IItemDefinition ironNugget();

	IItemDefinition certusQuartzCrystal();

	IItemDefinition certusQuartzCrystalCharged();

	IItemDefinition fluixCrystal();

	IItemDefinition fluixPearl();

	IItemDefinition woodenGear();

	IItemDefinition wireless();

	IItemDefinition wirelessBooster();

	IItemDefinition annihilationCore();

	IItemDefinition formationCore();

	IItemDefinition singularity();

	IItemDefinition qESingularity();

	IItemDefinition blankPattern();
}
