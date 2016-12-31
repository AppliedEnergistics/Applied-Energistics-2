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

package appeng.coremod;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class AppEngCoreTest
{
	private final static String EXPECTED_CONTAINER_CLASS_NAME = appeng.coremod.AppEngCore.class.getName();
	private final static String EXPECTED_CONTAINER_MOD_ID = "appliedenergistics2-core";
	private final static String[] EXPECTED_TRANSFORMERS = new String[] {
			appeng.coremod.transformer.ASMIntegration.class.getName()
	};

	private AppEngCore coreModContainer = new AppEngCore();

	@Test
	public void testTransformerStringsMatchActualClasses()
	{
		assertArrayEquals( EXPECTED_TRANSFORMERS, coreModContainer.getASMTransformerClass() );
	}

	@Test
	public void testContainerClassExists()
	{
		assertEquals( EXPECTED_CONTAINER_CLASS_NAME, coreModContainer.getModContainerClass() );
	}

	@Test
	public void testContainerModId()
	{
		assertEquals( EXPECTED_CONTAINER_MOD_ID, coreModContainer.getModId() );
	}

}
