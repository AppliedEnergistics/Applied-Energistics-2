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

import java.util.List;

import com.google.common.collect.Iterators;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;

import appeng.api.parts.IPart;
import appeng.core.localization.WailaText;
import appeng.me.GridAccessException;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.Platform;

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
     * @param part     part with state
     * @param tooltip  to be added to tooltip
     * @param accessor wrapper for various information
     * @param config   config settings
     */
    @Override
    public void appendBody(final IPart part, final List<ITextComponent> tooltip, final IDataAccessor accessor,
            final IPluginConfig config) {
        if (part instanceof P2PTunnelPart) {
            CompoundNBT nbtData = accessor.getServerData();
            if (nbtData.contains(TAG_P2P_STATE)) {
                int[] stateArr = nbtData.getIntArray(TAG_P2P_STATE);
                if (stateArr.length == 2) {
                    int state = stateArr[0];
                    int outputs = stateArr[1];

                    switch (state) {
                        case STATE_UNLINKED:
                            tooltip.add(WailaText.P2PUnlinked.textComponent());
                            break;
                        case STATE_OUTPUT:
                            tooltip.add(WailaText.P2POutput.textComponent());
                            break;
                        case STATE_INPUT:
                            tooltip.add(getOutputText(outputs));
                            break;
                    }
                }

                final short freq = nbtData.getShort(TAG_P2P_FREQUENCY);
                final String freqTooltip = Platform.p2p().toHexString(freq);
                tooltip.add(new TranslationTextComponent("gui.tooltips.appliedenergistics2.P2PFrequency", freqTooltip));
            }
        }
    }

    @Override
    public void appendServerData(ServerPlayerEntity player, IPart part, TileEntity te, CompoundNBT tag, World world,
            BlockPos pos) {
        if (part instanceof P2PTunnelPart) {
            final P2PTunnelPart<?> tunnel = (P2PTunnelPart<?>) part;

            if (!tunnel.isPowered()) {
                return;
            }

            // Frquency
            final short frequency = tunnel.getFrequency();
            tag.putShort(TAG_P2P_FREQUENCY, frequency);

            // The default state
            int state = STATE_UNLINKED;
            int outputCount = 0;

            if (!tunnel.isOutput()) {
                outputCount = getOutputCount(tunnel);
                if (outputCount > 0) {
                    // Only set it to INPUT if we know there are any outputs
                    state = STATE_INPUT;
                }
            } else {
                P2PTunnelPart<?> input = tunnel.getInput();
                if (input != null) {
                    state = STATE_OUTPUT;
                }
            }

            tag.putIntArray(TAG_P2P_STATE, new int[] { state, outputCount });

        }
    }

    private static int getOutputCount(P2PTunnelPart<?> tunnel) {
        try {
            return Iterators.size(tunnel.getOutputs().iterator());
        } catch (GridAccessException e) {
            // Well... unknown size it is!
            return 0;
        }
    }

    private static ITextComponent getOutputText(int outputs) {
        if (outputs <= 1) {
            return WailaText.P2PInputOneOutput.textComponent();
        } else {
            return WailaText.P2PInputManyOutputs.textComponent(outputs);
        }
    }

}
