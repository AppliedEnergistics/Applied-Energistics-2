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
 * A list of all blocks in AE
 */
public interface IBlocks
{
	/*
	 * world gen
	 */
	IBlockDefinition quartzOre();

	IBlockDefinition quartzOreCharged();

	IBlockDefinition matrixFrame();

	/*
	 * decorative
	 */
	IBlockDefinition quartz();

	IBlockDefinition quartzPillar();

	IBlockDefinition quartzChiseled();

	IBlockDefinition quartzGlass();

	IBlockDefinition quartzVibrantGlass();

	IBlockDefinition quartzTorch();

	IBlockDefinition fluix();

	IBlockDefinition skyStone();

	IBlockDefinition skyChest();

	IBlockDefinition skyCompass();

	IBlockDefinition skyStoneStair();

	IBlockDefinition skyStoneBlockStair();

	IBlockDefinition skyStoneBrickStair();

	IBlockDefinition skyStoneSmallBrickStair();

	IBlockDefinition fluixStair();

	IBlockDefinition quartzStair();

	IBlockDefinition chiseledQuartzStair();

	IBlockDefinition quartzPillarStair();

	/*
	 * misc
	 */
	ITileDefinition grindStone();

	ITileDefinition crankHandle();

	ITileDefinition inscriber();

	ITileDefinition wireless();

	ITileDefinition charger();

	ITileDefinition tinyTNT();

	ITileDefinition security();

	/*
	 * quantum Network Bridge
	 */
	ITileDefinition quantumRing();

	ITileDefinition quantumLink();

	/*
	 * spatial iO
	 */
	ITileDefinition spatialPylon();

	ITileDefinition spatialIOPort();

	/*
	 * Bus / cables
	 */
	ITileDefinition multiPart();

	/*
	 * machines
	 */
	ITileDefinition controller();

	ITileDefinition drive();

	ITileDefinition chest();

	ITileDefinition iface();

	ITileDefinition cellWorkbench();

	ITileDefinition iOPort();

	ITileDefinition condenser();

	ITileDefinition energyAcceptor();

	ITileDefinition vibrationChamber();

	ITileDefinition quartzGrowthAccelerator();

	ITileDefinition energyCell();

	ITileDefinition energyCellDense();

	ITileDefinition energyCellCreative();

	// rv1
	ITileDefinition craftingUnit();

	ITileDefinition craftingAccelerator();

	ITileDefinition craftingStorage1k();

	ITileDefinition craftingStorage4k();

	ITileDefinition craftingStorage16k();

	ITileDefinition craftingStorage64k();

	ITileDefinition craftingMonitor();

	ITileDefinition molecularAssembler();

	ITileDefinition lightDetector();

	ITileDefinition paint();
}
