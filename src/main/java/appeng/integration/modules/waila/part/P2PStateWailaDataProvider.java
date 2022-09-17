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

package appeng.integration.modules.waila.part;


import appeng.api.parts.IPart;
import appeng.core.localization.WailaText;
import appeng.me.GridAccessException;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.util.Platform;
import com.google.common.collect.Iterators;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.List;


/**
 * Provides information about a P2P tunnel to WAILA.
 */
public final class P2PStateWailaDataProvider extends BasePartWailaDataProvider {

    private static final int STATE_UNLINKED = 0;
    private static final int STATE_OUTPUT = 1;
    private static final int STATE_INPUT = 2;
    public static final String TAG_P2P_STATE = "p2p_state";
    public static final String TAG_P2P_FREQUENCY = "p2p_frequency";

    /**
     * Adds state to the tooltip
     *
     * @param part           part with state
     * @param currentToolTip to be added to tooltip
     * @param accessor       wrapper for various information
     * @param config         config settings
     * @return modified tooltip
     */
    @Override
    public List<String> getWailaBody(final IPart part, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        if (part instanceof PartP2PTunnel) {
            NBTTagCompound nbtData = accessor.getNBTData();
            if (nbtData.hasKey(TAG_P2P_STATE)) {
                int[] stateArr = nbtData.getIntArray(TAG_P2P_STATE);
                if (stateArr.length == 2) {
                    int state = stateArr[0];
                    int outputs = stateArr[1];

                    switch (state) {
                        case STATE_UNLINKED:
                            currentToolTip.add(WailaText.P2PUnlinked.getLocal());
                            break;
                        case STATE_OUTPUT:
                            currentToolTip.add(WailaText.P2POutput.getLocal());
                            break;
                        case STATE_INPUT:
                            currentToolTip.add(getOutputText(outputs));
                            break;
                    }
                }

                final short freq = nbtData.getShort(TAG_P2P_FREQUENCY);
                final String freqTooltip = Platform.p2p().toHexString(freq);
                currentToolTip.add(I18n.translateToLocalFormatted("gui.tooltips.appliedenergistics2.P2PFrequency", freqTooltip));
            }
        }

        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, IPart part, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (part instanceof PartP2PTunnel) {
            final PartP2PTunnel tunnel = (PartP2PTunnel) part;

            if (!tunnel.isPowered()) {
                return tag;
            }

            // Frquency
            final short frequency = tunnel.getFrequency();
            tag.setShort(TAG_P2P_FREQUENCY, frequency);

            // The default state
            int state = STATE_UNLINKED;
            int outputCount = getOutputCount(tunnel);
            int inputCount = getInputCount(tunnel);

            if (!tunnel.isOutput()) {
                if (outputCount > 0) {
                    // Only set it to INPUT if we know there are any outputs
                    state = STATE_INPUT;
                }
            } else {
                if (inputCount > 0) {
                    state = STATE_OUTPUT;
                }
            }

            tag.setIntArray(TAG_P2P_STATE, new int[]{
                    state,
                    outputCount
            });

        }

        return tag;
    }

    private static int getOutputCount(PartP2PTunnel tunnel) {
        try {
            return Iterators.size(tunnel.getOutputs().iterator());
        } catch (GridAccessException e) {
            // Well... unknown size it is!
            return 0;
        }
    }

    private static int getInputCount(PartP2PTunnel tunnel) {
        try {
            return Iterators.size(tunnel.getInputs().iterator());
        } catch (GridAccessException e) {
            // Well... unknown size it is!
            return 0;
        }
    }

    private static String getOutputText(int outputs) {
        if (outputs <= 1) {
            return WailaText.P2P_INPUT_ONE_OUTPUT.getLocal();
        } else {
            return String.format(WailaText.P2P_INPUT_MANY_OUTPUTS.getLocal(), outputs);
        }
    }

    private static String getInputText(int inputs) {
        if (inputs <= 1) {
            return WailaText.P2P_OUTPUT_ONE_INPUT.getLocal();
        } else {
            return String.format(WailaText.P2P_OUTPUT_MANY_INPUTS.getLocal(), inputs);
        }
    }

}
