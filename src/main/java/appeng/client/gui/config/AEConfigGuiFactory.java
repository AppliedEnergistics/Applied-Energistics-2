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

package appeng.client.gui.config;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;


public class AEConfigGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(final Minecraft minecraftInstance) {

    }

    /**
     * If this method returns false, the config button in the mod list will be disabled
     *
     * @return true if this object provides a config gui screen, false otherwise
     */
    @Override
    public boolean hasConfigGui() {
        return false;
    }

    /**
     * Return an initialized {@link GuiScreen}. This screen will be displayed
     * when the "config" button is pressed in the mod list. It will
     * have a single argument constructor - the "parent" screen, the same as all
     * Minecraft GUIs. The expected behaviour is that this screen will replace the
     * "mod list" screen completely, and will return to the mod list screen through
     * the parent link, once the appropriate action is taken from the config screen.
     * <p>
     * This config GUI is anticipated to provide configuration to the mod in a friendly
     * visual way. It should not be abused to set internals such as IDs (they're gonna
     * keep disappearing anyway), but rather, interesting behaviours. This config GUI
     * is never run when a server game is running, and should be used to configure
     * desired behaviours that affect server state. Costs, mod game modes, stuff like that
     * can be changed here.
     *
     * @param parentScreen The screen to which must be returned when closing the
     *                     returned screen.
     * @return A class that will be instantiated on clicks on the config button
     * or null if no GUI is desired.
     */
    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new AEConfigGui(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

}
