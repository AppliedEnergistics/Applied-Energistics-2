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

import appeng.api.util.AEItemDefinition;


/**
 * All access is optional due to the ability to disable certain functionality via a configuration file.
 * This needs to be caught on the used side.
 */
public interface IBlocks
{
	/*
	 * world gen
	 */
	Optional<AEItemDefinition> quartzOre();

	Optional<AEItemDefinition> quartzOreCharged();

	Optional<AEItemDefinition> matrixFrame();

	/*
	 * decorative
	 */
	Optional<AEItemDefinition> quartz();

	Optional<AEItemDefinition> quartzPillar();

	Optional<AEItemDefinition> quartzChiseled();

	Optional<AEItemDefinition> quartzGlass();

	Optional<AEItemDefinition> quartzVibrantGlass();

	Optional<AEItemDefinition> quartzTorch();

	Optional<AEItemDefinition> fluix();

	Optional<AEItemDefinition> skyStone();

	Optional<AEItemDefinition> skyChest();

	Optional<AEItemDefinition> skyCompass();

	Optional<AEItemDefinition> skyStoneStair();

	Optional<AEItemDefinition> skyStoneBlockStair();

	Optional<AEItemDefinition> skyStoneBrickStair();

	Optional<AEItemDefinition> skyStoneSmallBrickStair();

	Optional<AEItemDefinition> fluixStair();

	Optional<AEItemDefinition> quartzStair();

	Optional<AEItemDefinition> chiseledQuartzStair();

	Optional<AEItemDefinition> quartzPillarStair();

	/*
	 * misc
	 */
	Optional<AEItemDefinition> grindStone();

	Optional<AEItemDefinition> crankHandle();

	Optional<AEItemDefinition> inscriber();

	Optional<AEItemDefinition> wireless();

	Optional<AEItemDefinition> charger();

	Optional<AEItemDefinition> tinyTNT();

	Optional<AEItemDefinition> security();

	/*
	 * quantum Network Bridge
	 */
	Optional<AEItemDefinition> quantumRing();

	Optional<AEItemDefinition> quantumLink();

	/*
	 * spatial iO
	 */
	Optional<AEItemDefinition> spatialPylon();

	Optional<AEItemDefinition> spatialIOPort();

	/*
	 * Bus / cables
	 */
	Optional<AEItemDefinition> multiPart();

	/*
	 * machines
	 */
	Optional<AEItemDefinition> controller();

	Optional<AEItemDefinition> drive();

	Optional<AEItemDefinition> chest();

	Optional<AEItemDefinition> iface();

	Optional<AEItemDefinition> cellWorkbench();

	Optional<AEItemDefinition> iOPort();

	Optional<AEItemDefinition> condenser();

	Optional<AEItemDefinition> energyAcceptor();

	Optional<AEItemDefinition> vibrationChamber();

	Optional<AEItemDefinition> quartzGrowthAccelerator();

	Optional<AEItemDefinition> energyCell();

	Optional<AEItemDefinition> energyCellDense();

	Optional<AEItemDefinition> energyCellCreative();

	// rv1
	Optional<AEItemDefinition> craftingUnit();

	Optional<AEItemDefinition> craftingAccelerator();

	Optional<AEItemDefinition> craftingStorage1k();

	Optional<AEItemDefinition> craftingStorage4k();

	Optional<AEItemDefinition> craftingStorage16k();

	Optional<AEItemDefinition> craftingStorage64k();

	Optional<AEItemDefinition> craftingMonitor();

	Optional<AEItemDefinition> molecularAssembler();

	Optional<AEItemDefinition> lightDetector();

	Optional<AEItemDefinition> paint();
}
