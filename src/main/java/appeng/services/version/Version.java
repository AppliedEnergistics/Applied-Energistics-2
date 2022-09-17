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


/**
 * Stores version information, which are easily compared
 */
public interface Version {
    /**
     * @return revision of this version
     */
    int revision();

    /**
     * @return channel of this version
     */
    Channel channel();

    /**
     * @return build of this version
     */
    int build();

    /**
     * A version is never if these criteria are met:
     * if the current revision is higher than the compared revision OR
     * if revision are equal and the current channel is higher than the compared channel (Stable > Beta > Alpha) OR
     * if revision, channel are equal and the build is higher than the compared build
     *
     * @return true if criteria are met
     */
    boolean isNewerAs(Version maybeOlder);

    /**
     * Prints the revision, channel and build into a common displayed way
     * <p>
     * rv2-beta-8
     *
     * @return formatted version
     */
    String formatted();
}
