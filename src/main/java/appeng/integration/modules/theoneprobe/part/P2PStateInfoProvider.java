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

package appeng.integration.modules.theoneprobe.part;

import com.google.common.collect.Iterators;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.parts.IPart;
import appeng.integration.modules.theoneprobe.TheOneProbeText;
import appeng.me.GridAccessException;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.Platform;

public class P2PStateInfoProvider implements IPartProbInfoProvider {

    private static final int STATE_UNLINKED = 0;
    private static final int STATE_OUTPUT = 1;
    private static final int STATE_INPUT = 2;

    @Override
    public void addProbeInfo(IPart part, ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world,
            BlockState blockState, IProbeHitData data) {
        if (part instanceof P2PTunnelPart) {
            final P2PTunnelPart tunnel = (P2PTunnelPart) part;

            if (!tunnel.isPowered()) {
                return;
            }

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
                final P2PTunnelPart input = tunnel.getInput();
                if (input != null) {
                    state = STATE_OUTPUT;
                }
            }

            switch (state) {
                case STATE_UNLINKED:
                    probeInfo.text(TheOneProbeText.P2P_UNLINKED.getTranslationComponent());
                    break;
                case STATE_OUTPUT:
                    probeInfo.text(TheOneProbeText.P2P_OUTPUT.getTranslationComponent());
                    break;
                case STATE_INPUT:
                    probeInfo.text(getOutputText(outputCount));
                    break;
            }

            final short freq = tunnel.getFrequency();
            final ITextComponent freqTooltip = new StringTextComponent(Platform.p2p().toHexString(freq));

            probeInfo.text(freqTooltip);
        }
    }

    private static int getOutputCount(P2PTunnelPart tunnel) {
        try {
            return Iterators.size(tunnel.getOutputs().iterator());
        } catch (GridAccessException e) {
            // Well... unknown size it is!
            return 0;
        }
    }

    private static ITextComponent getOutputText(int outputs) {
        if (outputs <= 1) {
            return TheOneProbeText.P2P_INPUT_ONE_OUTPUT.getTranslationComponent();
        } else {
            return TheOneProbeText.P2P_INPUT_MANY_OUTPUTS.getTranslationComponent(outputs);
        }
    }

}
