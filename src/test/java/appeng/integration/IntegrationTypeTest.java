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

package appeng.integration;


import static org.junit.Assert.assertNotNull;

import org.junit.Test;


public class IntegrationTypeTest
{

	@Test
	public void testCreateInstanceWithoutExceptions()
	{
		for( IntegrationType type : IntegrationType.values() )
		{
			try
			{
				assertNotNull( type.createInstance() );
			}
			// We actually only care about this specific type not being thrown.
			catch( MissingIntegrationModuleException e )
			{
				throw e;
			}
			// Throw everything else away as these might be caused due to missing dependencies during testing.
			catch( Throwable e )
			{
				// ignore
			}
		}
	}

}
