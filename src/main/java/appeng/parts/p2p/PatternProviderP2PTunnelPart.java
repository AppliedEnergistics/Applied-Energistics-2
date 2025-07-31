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

package appeng.parts.p2p;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import appeng.api.config.Actionable;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.core.AppEng;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;

/**
 * P2P tunnel for {@link PatternProviderTarget} connections.
 */
public class PatternProviderP2PTunnelPart extends P2PTunnelPart<PatternProviderP2PTunnelPart> {

    private static final P2PModels MODELS = new P2PModels(AppEng.makeId("part/p2p/p2p_tunnel_pattern_provider"));

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final PatternProviderTarget inputHandler = new InputHandler();
    private final PatternProviderTarget outputHandler = new OutputHandler();
    private final PatternProviderTarget emptyHandler = new EmptyHandler();

    public PatternProviderP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    /**
     * Returns the PatternProviderTarget exposed to adjacent blocks.
     */
    public PatternProviderTarget getExposedTarget() {
        return isOutput() ? outputHandler : inputHandler;
    }

    /**
     * Returns the target connected to the input side of this tunnel or an empty handler if none exists.
     */
    PatternProviderTarget getInputTarget() {
        var input = getInput();
        if (input == null) {
            return emptyHandler;
        }
        var target = input.getAdjacentTarget();
        return target == null ? emptyHandler : target;
    }

    /**
     * Returns the target connected to this tunnel's side or {@code null} if none exists.
     */
    private PatternProviderTarget getAdjacentTarget() {
        if (!(getLevel() instanceof ServerLevel server)) {
            return null;
        }

        BlockPos neighbor = getBlockEntity().getBlockPos().relative(getSide());
        var be = server.getBlockEntity(neighbor);
        return PatternProviderTarget.get(server, neighbor, be, getSide().getOpposite(), new MachineSource(this));
    }

    @Override
    public Stream<PatternProviderP2PTunnelPart> getOutputStream() {
        return super.getOutputStream().limit(32);
    }

    private class InputHandler implements PatternProviderTarget {
        @Override
        public long insert(AEKey what, long amount, Actionable mode) {
            var outputs = getOutputs();
            int count = outputs.size();
            if (count == 0 || amount == 0) {
                return 0;
            }

            long amountPerOutput = amount / count;
            long overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;
            long insertedTotal = 0;

            for (PatternProviderP2PTunnelPart out : outputs) {
                var target = out.getAdjacentTarget();
                if (target == null) {
                    continue;
                }

                long toSend = amountPerOutput + overflow;
                long inserted = target.insert(what, toSend, mode);
                overflow = toSend - inserted;
                insertedTotal += inserted;
            }

            if (mode == Actionable.MODULATE) {
                deductTransportCost(insertedTotal, what.getType());
            }

            return insertedTotal;
        }

        @Override
        public boolean containsPatternInput(Set<AEKey> inputs) {
            for (PatternProviderP2PTunnelPart out : getOutputs()) {
                var target = out.getAdjacentTarget();
                if (target != null && target.containsPatternInput(inputs)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class OutputHandler implements PatternProviderTarget {
        @Override
        public long insert(AEKey what, long amount, Actionable mode) {
            var target = getInputTarget();
            long inserted = target.insert(what, amount, mode);
            if (mode == Actionable.MODULATE) {
                deductTransportCost(inserted, what.getType());
            }
            return inserted;
        }

        @Override
        public boolean containsPatternInput(Set<AEKey> inputs) {
            return getInputTarget().containsPatternInput(inputs);
        }
    }

    private static class EmptyHandler implements PatternProviderTarget {
        @Override
        public long insert(AEKey what, long amount, Actionable mode) {
            return 0;
        }

        @Override
        public boolean containsPatternInput(Set<AEKey> inputs) {
            return false;
        }
    }
}
