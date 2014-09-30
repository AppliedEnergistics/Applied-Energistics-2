package appeng.items.tools.powered.powersink;

/*
 @Interface(iface = "universalelectricity.core.item.IItemElectric", modid = "IC2")
 public class UniversalElectricity extends ThermalExpansion implements IItemElectric
 {
 * 
 * public UniversalElectricity(Class c, String subName) { super( c, subName ); }
 * 
 * @Override public float recharge(ItemStack is, float energy, boolean
 * doRecharge) { return (float) (energy - injectExternalPower( PowerUnits.KJ,
 * is, energy, !doRecharge )); }
 * 
 * @Override public float discharge(ItemStack is, float energy, boolean
 * doDischarge) { return 0; }
 * 
 * @Override public float getElectricityStored(ItemStack is) { return (int)
 * PowerUnits.AE.convertTo( PowerUnits.KJ, getAECurrentPower( is ) ); }
 * 
 * @Override public float getMaxElectricityStored(ItemStack is) { return (int)
 * PowerUnits.AE.convertTo( PowerUnits.KJ, getAEMaxPower( is ) ); }
 * 
 * @Override public void setElectricity(ItemStack is, float joules) { double
 * currentPower = getAECurrentPower( is ); double targetPower =
 * PowerUnits.KJ.convertTo( PowerUnits.AE, joules ); if ( targetPower >
 * currentPower ) injectAEPower( is, targetPower - currentPower ); else
 * extractAEPower( is, currentPower - targetPower ); }
 * 
 * @Override public float getTransfer(ItemStack is) { return (float)
 * PowerUnits.AE.convertTo( PowerUnits.KJ, getAEMaxPower( is ) -
 * getAECurrentPower( is ) ); }
 * 
 * @Override public float getVoltage(ItemStack itemStack) { return 120; }

 }
 */

