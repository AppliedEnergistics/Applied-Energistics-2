/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package appeng;

import cpw.mods.modlauncher.Launcher;

import java.util.Arrays;
import java.util.Locale;

public class TestLauncher {
    public static void main(String... args) throws InterruptedException {
        final String markerselection = System.getProperty("forge.logging.markers", "");
        Arrays.stream(markerselection.split(",")).forEach(marker -> System.setProperty("forge.logging.marker." + marker.toLowerCase(Locale.ROOT), "ACCEPT"));
        System.setProperty("fml.earlyprogresswindow", "false");

        Launcher.main(
                "--gameDir", ".",
                "--launchTarget", "junit",
                "--fml.mcpVersion", System.getenv("MCP_VERSION"),
                "--fml.mcVersion", System.getenv("MC_VERSION"),
                "--fml.forgeGroup", System.getenv("FORGE_GROUP"),
                "--fml.forgeVersion", System.getenv("FORGE_VERSION")
        );
    }

}
