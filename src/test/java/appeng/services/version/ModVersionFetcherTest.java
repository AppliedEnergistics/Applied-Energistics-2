/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.services.version;


import org.junit.Assert;
import org.junit.Test;


/**
 * @author thatsIch
 * @version rv3 - 16.05.2015
 * @since rv3 16.05.2015
 */
public class ModVersionFetcherTest
{
	//	private static final ModVersionFetcher FETCHER = new ModVersionFetcher(  )

	private final ModVersionFetcher indev;
	private final ModVersionFetcher pullRequest;
	private final ModVersionFetcher working;

	public ModVersionFetcherTest()
	{
		final VersionParser parser = new VersionParser();

		this.indev = new ModVersionFetcher( "@version@", parser );
		this.pullRequest = new ModVersionFetcher( "pr", parser );
		this.working = new ModVersionFetcher( "rv2-beta-8", parser );
	}

	@Test
	public void testInDev() throws Exception
	{
		Assert.assertEquals( this.indev.get(), new DoNotCheckVersion() );
	}

	@Test
	public void testPR() throws Exception
	{
		Assert.assertEquals( this.pullRequest.get(), new DoNotCheckVersion() );
	}

	@Test
	public void testWorking() throws Exception
	{
		Assert.assertEquals( this.working.get(), new DefaultVersion( 2, Channel.Beta, 8 ) );
	}
}
