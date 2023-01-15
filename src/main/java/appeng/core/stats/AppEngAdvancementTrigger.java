/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.core.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class AppEngAdvancementTrigger
        implements CriterionTrigger<AppEngAdvancementTrigger.Instance>, IAdvancementTrigger {
    private final Map<PlayerAdvancements, Listeners> listeners = new HashMap<>();

    public AppEngAdvancementTrigger() {
        super();
    }

    public Criterion<Instance> criterion() {
        return new Criterion<>(this, instance());
    }

    public Instance instance() {
        return new Instance();
    }

    @Override
    public void addPlayerListener(PlayerAdvancements playerAdvancementsIn,
            Listener<Instance> listener) {
        Listeners l = this.listeners.get(playerAdvancementsIn);

        if (l == null) {
            l = new Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, l);
        }

        l.add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements playerAdvancementsIn,
            Listener<Instance> listener) {
        Listeners l = this.listeners.get(playerAdvancementsIn);

        if (l != null) {
            l.remove(listener);

            if (l.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Instance createInstance(JsonObject object, DeserializationContext conditions) {
        return new Instance();
    }

    @Override
    public void trigger(ServerPlayer parPlayer) {
        Listeners l = this.listeners.get(parPlayer.getAdvancements());

        if (l != null) {
            l.trigger(parPlayer);
        }
    }

    public static class Instance implements CriterionTriggerInstance {
        public boolean test() {
            return true;
        }

        @Override
        public JsonObject serializeToJson() {
            return new JsonObject();
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = new HashSet<>();

        Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(Listener<Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Listener<Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(Player player) {
            List<Listener<Instance>> list = null;

            for (Listener<Instance> listener : this.listeners) {
                if (listener.trigger().test()) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (Listener<Instance> l : list) {
                    l.run(this.playerAdvancements);
                }
            }
        }
    }
}
