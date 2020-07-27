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

import java.util.*;

import com.google.gson.JsonObject;

import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import appeng.core.AppEng;

public class AppEngAdvancementTrigger implements Criterion<AppEngAdvancementTrigger.Instance>, IAdvancementTrigger {
    private final Identifier ID;
    private final Map<PlayerAdvancementTracker, AppEngAdvancementTrigger.Listeners> listeners = new HashMap<>();

    public AppEngAdvancementTrigger(String parString) {
        super();
        this.ID = new Identifier(AppEng.MOD_ID, parString);
    }

    @Override
    public Identifier getId() {
        return this.ID;
    }

    @Override
    public void beginTrackingCondition(PlayerAdvancementTracker playerAdvancementsIn,
            Criterion.ConditionsContainer<AppEngAdvancementTrigger.Instance> listener) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(playerAdvancementsIn);

        if (l == null) {
            l = new AppEngAdvancementTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, l);
        }

        l.add(listener);
    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker playerAdvancementsIn,
            Criterion.ConditionsContainer<AppEngAdvancementTrigger.Instance> listener) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(playerAdvancementsIn);

        if (l != null) {
            l.remove(listener);

            if (l.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void endTracking(PlayerAdvancementTracker playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public AppEngAdvancementTrigger.Instance conditionsFromJson(JsonObject json,
            AdvancementEntityPredicateDeserializer context) {
        return new AppEngAdvancementTrigger.Instance(this.getId());
    }

    @Override
    public void trigger(ServerPlayerEntity parPlayer) {
        AppEngAdvancementTrigger.Listeners l = this.listeners.get(parPlayer.getAdvancementTracker());

        if (l != null) {
            l.trigger(parPlayer);
        }
    }

    public static class Instance implements CriterionConditions {
        private final Identifier id;

        public Instance(Identifier id) {
            this.id = id;
        }

        public boolean test() {
            return true;
        }

        @Override
        public Identifier getId() {
            return id;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            return new JsonObject();
        }
    }

    static class Listeners {
        private final PlayerAdvancementTracker playerAdvancements;
        private final Set<Criterion.ConditionsContainer<AppEngAdvancementTrigger.Instance>> listeners = new HashSet<>();

        Listeners(PlayerAdvancementTracker playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(Criterion.ConditionsContainer<AppEngAdvancementTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Criterion.ConditionsContainer<AppEngAdvancementTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(PlayerEntity player) {
            List<Criterion.ConditionsContainer<AppEngAdvancementTrigger.Instance>> list = null;

            for (Criterion.ConditionsContainer<AppEngAdvancementTrigger.Instance> listener : this.listeners) {
                if (listener.getConditions().test()) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (Criterion.ConditionsContainer<AppEngAdvancementTrigger.Instance> l : list) {
                    l.grant(this.playerAdvancements);
                }
            }
        }
    }
}
