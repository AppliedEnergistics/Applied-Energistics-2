package appeng.integration.modules.gregtech;

import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ToolClass {
    private static Class<?> GTToolClass;

    static {
        try {
            GTToolClass = Class.forName("gregtech.api.items.IToolItem", false, Launch.classLoader);
        } catch (ClassNotFoundException ignored) {
            try {
                GTToolClass = Class.forName("gregtech.api.items.toolitem.IGTTool", false, Launch.classLoader);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final Enum<Interfaces> GTToolInterface = getGTToolInterface();
    private static final Method getMaxItemDamage = ReflectionHelper.findMethod(GTToolClass, "getMaxItemDamage", null, ItemStack.class);
    private static final Method getItemDamage = ReflectionHelper.findMethod(GTToolClass, "getItemDamage", null, ItemStack.class);

    public static Class<?> getGTToolClass() {
        return GTToolClass;
    }

    public static Enum<Interfaces> getGTToolInterface() {
        if (GTToolClass.getName().equals("IToolItem")) {
            return Interfaces.ITOOLITEM;
        } else {
            return Interfaces.IGTTOOL;
        }
    }

    public static int getGTMaxDamage(ItemStack itemStack) {
        if (GTToolInterface == Interfaces.ITOOLITEM) {
            try {
                return (int) getMaxItemDamage.invoke(itemStack.getItem(), itemStack);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            return itemStack.getMaxDamage();
        }
    }

    public static int getGTitemDamage(ItemStack itemStack) {
        if (GTToolInterface == Interfaces.ITOOLITEM) {
            try {
                return (int) getItemDamage.invoke(itemStack.getItem(), itemStack);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            return itemStack.getMaxDamage();
        }
    }

    enum Interfaces {
        ITOOLITEM,
        IGTTOOL
    }
}
