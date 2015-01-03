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


import com.google.common.base.Optional;

import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;


public interface IItems
{
	Optional<AEItemDefinition> certusQuartzAxe();

	Optional<AEItemDefinition> certusQuartzHoe();

	Optional<AEItemDefinition> certusQuartzShovel();

	Optional<AEItemDefinition> certusQuartzPick();

	Optional<AEItemDefinition> certusQuartzSword();

	Optional<AEItemDefinition> certusQuartzWrench();

	Optional<AEItemDefinition> certusQuartzKnife();

	Optional<AEItemDefinition> netherQuartzAxe();

	Optional<AEItemDefinition> netherQuartzHoe();

	Optional<AEItemDefinition> netherQuartzShovel();

	Optional<AEItemDefinition> netherQuartzPick();

	Optional<AEItemDefinition> netherQuartzSword();

	Optional<AEItemDefinition> netherQuartzWrench();

	Optional<AEItemDefinition> netherQuartzKnife();

	Optional<AEItemDefinition> entropyManipulator();

	Optional<AEItemDefinition> wirelessTerminal();

	Optional<AEItemDefinition> biometricCard();

	Optional<AEItemDefinition> chargedStaff();

	Optional<AEItemDefinition> massCannon();

	Optional<AEItemDefinition> memoryCard();

	Optional<AEItemDefinition> networkTool();

	Optional<AEItemDefinition> portableCell();

	Optional<AEItemDefinition> cellCreative();

	Optional<AEItemDefinition> viewCell();

	Optional<AEItemDefinition> cell1k();

	Optional<AEItemDefinition> cell4k();

	Optional<AEItemDefinition> cell16k();

	Optional<AEItemDefinition> cell64k();

	Optional<AEItemDefinition> spatialCell2();

	Optional<AEItemDefinition> spatialCell16();

	Optional<AEItemDefinition> spatialCell128();

	Optional<AEItemDefinition> facade();

	Optional<AEItemDefinition> crystalSeed();

	// rv1
	Optional<AEItemDefinition> encodedPattern();

	Optional<AEItemDefinition> colorApplicator();

	Optional<AEColoredItemDefinition> coloredPaintBall();

	Optional<AEColoredItemDefinition> coloredLumenPaintBall();
}
