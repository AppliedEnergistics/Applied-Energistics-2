/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.parts.IPart;
import appeng.core.localization.InGameTooltip;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.Platform;

/**
 * Provides information about a P2P tunnel to WAILA.
 */
public final class P2PStateDataProvider implements IPartDataProvider {

    private static final byte STATE_UNLINKED = 0;
    private static final byte STATE_OUTPUT = 1;
    private static final byte STATE_INPUT = 2;
    public static final String TAG_P2P_STATE = "p2pState";
    public static final String TAG_P2P_OUTPUTS = "p2pOutputs";
    public static final String TAG_P2P_FREQUENCY = "p2pFrequency";

    @Override
    public void appendBody(IPart part, CompoundTag partTag, List<Component> tooltip) {
        if (partTag.contains(TAG_P2P_STATE, Tag.TAG_BYTE)) {
            var state = partTag.getByte(TAG_P2P_STATE);
            var outputs = partTag.getInt(TAG_P2P_OUTPUTS);

            switch (state) {
                case STATE_UNLINKED -> tooltip.add(InGameTooltip.P2PUnlinked.textComponent());
                case STATE_OUTPUT -> tooltip.add(InGameTooltip.P2POutput.textComponent());
                case STATE_INPUT -> tooltip.add(getOutputText(outputs));
            }

            var freq = partTag.getShort(TAG_P2P_FREQUENCY);
            var freqTooltip = Platform.p2p().toHexString(freq);
            tooltip.add(new TranslatableComponent("gui.tooltips.ae2.P2PFrequency", freqTooltip));
        }
    }

    @Override
    public void appendServerData(ServerPlayer player, IPart part, CompoundTag partTag) {
        if (part instanceof P2PTunnelPart<?>p2pTunnel) {
            if (!p2pTunnel.isPowered()) {
                return;
            }

            // Frequency
            partTag.putShort(TAG_P2P_FREQUENCY, p2pTunnel.getFrequency());

            // The default state
            byte state = STATE_UNLINKED;
            if (!p2pTunnel.isOutput()) {
                var outputCount = p2pTunnel.getOutputs().size();
                if (outputCount > 0) {
                    // Only set it to INPUT if we know there are any outputs
                    state = STATE_INPUT;
                    partTag.putInt(TAG_P2P_OUTPUTS, outputCount);
                }
            } else {
                if (p2pTunnel.getInput() != null) {
                    state = STATE_OUTPUT;
                }
            }

            partTag.putByte(TAG_P2P_STATE, state);
        }
    }

    private static Component getOutputText(int outputs) {
        if (outputs <= 1) {
            return InGameTooltip.P2PInputOneOutput.textComponent();
        } else {
            return InGameTooltip.P2PInputManyOutputs.textComponent(outputs);
        }
    }

}
