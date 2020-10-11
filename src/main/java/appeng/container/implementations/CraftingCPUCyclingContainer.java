package appeng.container.implementations;

/**
 * Implemented on screens that show information about a crafting CPU and allow the CPU to be cycled. Is triggered by
 * receiving a config value packet with name <code>Terminal.Cpu</code>.
 */
public interface CraftingCPUCyclingContainer {

    void cycleSelectedCPU(boolean forward);

}
