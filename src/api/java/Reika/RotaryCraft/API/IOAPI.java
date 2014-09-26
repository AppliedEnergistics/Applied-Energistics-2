/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.API;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.tileentity.TileEntity;

public class IOAPI {

	private static Class io;
	private static Method render;

	static {
		try {
			io = Class.forName("Reika.RotaryCraft.Auxiliary.IORenderer", false, IOAPI.class.getClassLoader());
			render = io.getMethod("renderIO", TileEntity.class, double.class, double.class, double.class);
		}
		catch (ClassNotFoundException e) {
			System.out.println("RotaryCraft IORenderer class not found!");
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			System.out.println("Could not find renderIO method in IORenderer class!");
			e.printStackTrace();
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	/** Call this to run the RotaryCraft I/O Renderer on your TileEntity.
	 * 
	 * You must call this from inside your TE renderer's "renderTileEntityAt" method.
	 * Calling it on pass 1 only is strongly recommended to
	 * prevent visual glitches caused by OpenGL limitations.
	 * @param machine Your TileEntity as either a ShaftPowerEmitter or ShaftPowerReceiver
	 * @param par2 The "par2" passed in the "renderTileEntityAt"; related to x-displacement
	 * @param par4 The "par4" passed in the "renderTileEntityAt"; related to y-displacement
	 * @param par6 The "par6" passed in the "renderTileEntityAt"; related to z-displacement
	 */
	public static void renderIO(ShaftMachine machine, double par2, double par4, double par6) {
		try {
			render.invoke(null, machine, par2, par4, par6);
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		catch (NullPointerException e) { //If init failed
			e.printStackTrace();
		}
	}
}
