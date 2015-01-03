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
 * A list of all blocks in AE
 */
public interface IBlocks
{
	/*
	 * world gen
	 */
	AEItemDefinition quartzOre();

	AEItemDefinition quartzOreCharged();

	AEItemDefinition matrixFrame();

	/*
	 * decorative
	 */
	AEItemDefinition quartz();

	AEItemDefinition quartzPillar();

	AEItemDefinition quartzChiseled();

	AEItemDefinition quartzGlass();

	AEItemDefinition quartzVibrantGlass();

	AEItemDefinition quartzTorch();

	AEItemDefinition fluix();

	AEItemDefinition skyStone();

	AEItemDefinition skyChest();

	AEItemDefinition skyCompass();

	AEItemDefinition skyStoneStair();

	AEItemDefinition skyStoneBlockStair();

	AEItemDefinition skyStoneBrickStair();

	AEItemDefinition skyStoneSmallBrickStair();

	AEItemDefinition fluixStair();

	AEItemDefinition quartzStair();

	AEItemDefinition chiseledQuartzStair();

	AEItemDefinition quartzPillarStair();

	/*
	 * misc
	 */
	AEItemDefinition grindStone();

	AEItemDefinition crankHandle();

	AEItemDefinition inscriber();

	AEItemDefinition wireless();

	AEItemDefinition charger();

	AEItemDefinition tinyTNT();

	AEItemDefinition security();

	/*
	 * quantum Network Bridge
	 */
	AEItemDefinition quantumRing();

	AEItemDefinition quantumLink();

	/*
	 * spatial iO
	 */
	AEItemDefinition spatialPylon();

	AEItemDefinition spatialIOPort();

	/*
	 * Bus / cables
	 */
	AEItemDefinition multiPart();

	/*
	 * machines
	 */
	AEItemDefinition controller();

	AEItemDefinition drive();

	AEItemDefinition chest();

	AEItemDefinition iface();

	AEItemDefinition cellWorkbench();

	AEItemDefinition iOPort();

	AEItemDefinition condenser();

	AEItemDefinition energyAcceptor();

	AEItemDefinition vibrationChamber();

	AEItemDefinition quartzGrowthAccelerator();

	AEItemDefinition energyCell();

	AEItemDefinition energyCellDense();

	AEItemDefinition energyCellCreative();

	// rv1
	AEItemDefinition craftingUnit();

	AEItemDefinition craftingAccelerator();

	AEItemDefinition craftingStorage1k();

	AEItemDefinition craftingStorage4k();

	AEItemDefinition craftingStorage16k();

	AEItemDefinition craftingStorage64k();

	AEItemDefinition craftingMonitor();

	AEItemDefinition molecularAssembler();

	AEItemDefinition lightDetector();

	AEItemDefinition paint();
}
