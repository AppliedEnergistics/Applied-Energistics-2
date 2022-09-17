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

package appeng.core.api;


import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IPartHelper;
import appeng.api.parts.LayerBase;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.parts.PartPlacement;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;


public class ApiPart implements IPartHelper {

    private final LoadingCache<CacheKey, Class<? extends AEBaseTile>> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<CacheKey, Class<? extends AEBaseTile>>() {
                @Override
                public Class<? extends AEBaseTile> load(CacheKey key) throws Exception {
                    return ApiPart.this.generateCombinedClass(key);
                }
            });

    private final Map<Class<?>, String> interfaces2Layer = new HashMap<>();
    private final List<String> desc = new ArrayList<>();

    /**
     * Conceptually this method will build a new class hierarchy that is rooted at the given base class, and includes a
     * chain of all registered layers.
     * <p/>
     * To accomplish this, it takes the first registered layer, replaces it's inheritance from LayerBase with an
     * inheritance from the given baseClass,
     * and uses the resulting class as the parent class for the next registered layer, for which it repeats this
     * process. This process is then repeated
     * until a class hierarchy of all layers is formed. While janking out the inheritance from LayerBase, it'll make
     * also sure that calls to that
     * classes method will instead be forwarded to the superclass that was inserted as part of the described process.
     * <p/>
     * Example: If layers A and B are registered, and TileCableBus is passed in as the baseClass, a synthetic class
     * A_B_TileCableBus should be returned,
     * which has A_B_TileCableBus -extends-> B_TileCableBus -extends-> TileCableBus as it's class hierarchy, where
     * A_B_TileCableBus has been generated
     * from A, and B_TileCableBus has been generated from B.
     */
    public Class<? extends AEBaseTile> getCombinedInstance(final Class<? extends AEBaseTile> baseClass) {
        if (this.desc.isEmpty()) {
            // No layers registered...
            return baseClass;
        }

        return this.cache.getUnchecked(new CacheKey(baseClass, this.desc));
    }

    private Class<? extends AEBaseTile> generateCombinedClass(CacheKey cacheKey) {
        final Class<? extends AEBaseTile> parentClass;

        // Get the list of interfaces that still need to be implemented beyond the current one
        List<String> remainingInterfaces = cacheKey.getInterfaces().subList(1, cacheKey.getInterfaces().size());

        // We are not at the root of the class hierarchy yet
        if (!remainingInterfaces.isEmpty()) {
            CacheKey parentKey = new CacheKey(cacheKey.getBaseClass(), remainingInterfaces);
            parentClass = this.cache.getUnchecked(parentKey);
        } else {
            parentClass = cacheKey.getBaseClass();
        }

        // Which interface should be implemented in this layer?
        String interfaceName = cacheKey.getInterfaces().get(0);

        try {
            // This is the particular interface that this layer was registered for. Loading the class may fail if i.e.
            // an API is broken or not present
            // and in this case, the layer will be skipped!
            Class<?> interfaceClass = Class.forName(interfaceName);
            String layerImpl = this.interfaces2Layer.get(interfaceClass);

            return this.getClassByDesc(parentClass, layerImpl);
        } catch (final Throwable t) {
            AELog.warn("Error loading " + interfaceName);
            AELog.debug(t);
            return parentClass;
        }

    }

    @SuppressWarnings("unchecked")
    private Class<? extends AEBaseTile> getClassByDesc(Class<? extends AEBaseTile> baseClass, final String next) {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassNode n = this.getReader(next);
        final String originalName = n.name;

        try {
            n.name = n.name + '_' + baseClass.getSimpleName();
            n.superName = baseClass.getName().replace('.', '/');
        } catch (final Throwable t) {
            AELog.debug(t);
        }

        for (final MethodNode mn : n.methods) {
            final Iterator<AbstractInsnNode> i = mn.instructions.iterator();
            while (i.hasNext()) {
                this.processNode(i.next(), n.superName);
            }
        }

        final DefaultPackageClassNameRemapper remapper = new DefaultPackageClassNameRemapper();
        remapper.inputOutput.put("appeng/api/parts/LayerBase", n.superName);
        remapper.inputOutput.put(originalName, n.name);
        n.accept(new RemappingClassAdapter(cw, remapper));
        // n.accept( cw );

        // n.accept( new TraceClassVisitor( new PrintWriter( System.out ) ) );
        final byte[] byteArray = cw.toByteArray();
        final int size = byteArray.length;
        final Class clazz = this.loadClass(n.name.replace("/", "."), byteArray);

        try {
            final Object fish = clazz.newInstance();

            boolean hasError = false;

            if (!baseClass.isInstance(fish)) {
                hasError = true;
                AELog.error("Error, Expected layer to implement " + baseClass + " did not.");
            }

            if (fish instanceof LayerBase) {
                hasError = true;
                AELog.error("Error, Expected layer to NOT implement LayerBase but it DID.");
            }

            if (!(fish instanceof TileCableBus)) {
                hasError = true;
                AELog.error("Error, Expected layer to implement TileCableBus did not.");
            }

            if (!(fish instanceof TileEntity)) {
                hasError = true;
                AELog.error("Error, Expected layer to implement TileEntity did not.");
            }

            if (!hasError) {
                AELog.info("Layer: " + n.name + " loaded successfully - " + size + " bytes");
            }
        } catch (final Throwable t) {
            AELog.error("Layer: " + n.name + " Failed.");
            AELog.debug(t);
        }

        return clazz;
    }

    private ClassNode getReader(final String name) {
        final String path = '/' + name.replace(".", "/") + ".class";
        final InputStream is = this.getClass().getResourceAsStream(path);
        try {
            final ClassReader cr = new ClassReader(is);

            final ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.EXPAND_FRAMES);

            return cn;
        } catch (final IOException e) {
            throw new IllegalStateException("Error loading " + name, e);
        }
    }

    private void processNode(final AbstractInsnNode next, final String nePar) {
        if (next instanceof MethodInsnNode) {
            final MethodInsnNode min = (MethodInsnNode) next;
            if (min.owner.equals("appeng/api/parts/LayerBase")) {
                min.owner = nePar;
            }
        }
    }

    private Class loadClass(final String name, byte[] b) {
        // override classDefine (as it is protected) and define the class.
        Class clazz = null;
        try {
            final ClassLoader loader = this.getClass().getClassLoader();// ClassLoader.getSystemClassLoader();
            final Class<ClassLoader> root = ClassLoader.class;
            final Class<? extends ClassLoader> cls = loader.getClass();
            final Method defineClassMethod = root.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            final Method runTransformersMethod = cls.getDeclaredMethod("runTransformers", String.class, String.class, byte[].class);

            runTransformersMethod.setAccessible(true);
            defineClassMethod.setAccessible(true);
            try {
                final Object[] argsA = {
                        name,
                        name,
                        b
                };
                b = (byte[]) runTransformersMethod.invoke(loader, argsA);

                final Object[] args = {
                        name,
                        b,
                        0,
                        b.length
                };
                clazz = (Class) defineClassMethod.invoke(loader, args);
            } finally {
                runTransformersMethod.setAccessible(false);
                defineClassMethod.setAccessible(false);
            }
        } catch (final Exception e) {
            AELog.debug(e);
            throw new IllegalStateException("Unable to manage part API.", e);
        }
        return clazz;
    }

    @Override
    public boolean registerNewLayer(final String layer, final String layerInterface) {
        try {
            final Class<?> layerInterfaceClass = Class.forName(layerInterface);
            if (this.interfaces2Layer.get(layerInterfaceClass) == null) {
                this.interfaces2Layer.put(layerInterfaceClass, layer);
                this.desc.add(layerInterface);
                return true;
            } else {
                AELog.info("Layer " + layer + " not registered, " + layerInterface + " already has a layer.");
            }
        } catch (final Throwable ignored) {
        }

        return false;
    }

    @Override
    public EnumActionResult placeBus(final ItemStack is, final BlockPos pos, final EnumFacing side, final EntityPlayer player, final EnumHand hand, final World w) {
        return PartPlacement.place(is, pos, side, player, hand, w, PartPlacement.PlaceType.PLACE_ITEM, 0);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        return AppEng.proxy.getRenderMode();
    }

    private static class DefaultPackageClassNameRemapper extends Remapper {

        private final HashMap<String, String> inputOutput = new HashMap<>();

        @Override
        public String map(final String typeName) {
            final String o = this.inputOutput.get(typeName);
            if (o == null) {
                return typeName;
            }
            return o;
        }
    }

    private static class CacheKey {
        private final Class<? extends AEBaseTile> baseClass;

        private final List<String> interfaces;

        private CacheKey(Class<? extends AEBaseTile> baseClass, List<String> interfaces) {
            this.baseClass = baseClass;
            this.interfaces = ImmutableList.copyOf(interfaces);
        }

        private Class<? extends AEBaseTile> getBaseClass() {
            return this.baseClass;
        }

        private List<String> getInterfaces() {
            return this.interfaces;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;

            return this.baseClass.equals(cacheKey.baseClass) && this.interfaces.equals(cacheKey.interfaces);
        }

        @Override
        public int hashCode() {
            int result = this.baseClass.hashCode();
            result = 31 * result + this.interfaces.hashCode();
            return result;
        }
    }
}
