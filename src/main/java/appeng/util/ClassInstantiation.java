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

package appeng.util;


import appeng.core.AELog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;


public class ClassInstantiation<T> {
    private final Class<? extends T> template;
    private final Object[] args;

    public ClassInstantiation(final Class<? extends T> template, final Object... args) {
        this.template = template;
        this.args = args;
    }

    public Optional<T> get() {
        @SuppressWarnings("unchecked") final Constructor<T>[] constructors = (Constructor<T>[]) this.template.getConstructors();

        for (final Constructor<T> constructor : constructors) {
            final Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length == this.args.length) {
                boolean valid = true;

                for (int idx = 0; idx < paramTypes.length; idx++) {
                    final Class<?> cz = this.args[idx].getClass();
                    if (!this.isClassMatch(paramTypes[idx], cz, this.args[idx])) {
                        valid = false;
                    }
                }

                if (valid) {
                    try {
                        return Optional.of(constructor.newInstance(this.args));
                    } catch (final InstantiationException e) {
                        e.printStackTrace();
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (final InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        return Optional.empty();
    }

    private boolean isClassMatch(Class<?> expected, Class<?> got, final Object value) {
        if (value == null && !expected.isPrimitive()) {
            return true;
        }

        expected = this.condense(expected, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class);
        got = this.condense(got, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class);

        return expected == got || expected.isAssignableFrom(got);
    }

    private Class<?> condense(final Class<?> expected, final Class<?>... wrappers) {
        if (expected.isPrimitive()) {
            for (final Class clz : wrappers) {
                try {
                    if (expected == clz.getField("TYPE").get(null)) {
                        return clz;
                    }
                } catch (final Throwable t) {
                    AELog.debug(t);
                }
            }
        }
        return expected;
    }
}
