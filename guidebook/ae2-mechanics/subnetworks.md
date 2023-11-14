---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Subnetworks
---

# Subnetworks

<GameScene zoom="4" interactive={true}>
<ImportStructure src="../assets/assemblies/subnet_demonstration.snbt" />

<DiamondAnnotation pos="6.5 2.5 0.5" color="#00ff00">
        Item Pipe Subnet
    </DiamondAnnotation>

<DiamondAnnotation pos="5.5 2.5 0.5" color="#00ff00">
        Fluid Pipe Subnet
    </DiamondAnnotation>

<DiamondAnnotation pos="4.5 2.5 0.5" color="#00ff00">
        Filtered Annihilation Plane
    </DiamondAnnotation>

<DiamondAnnotation pos="3.5 2.5 0.5" color="#00ff00">
        Formation Plane Subnet
    </DiamondAnnotation>

<DiamondAnnotation pos="2.5 2.5 0.5" color="#00ff00">
        Subnet using the Interface-Storage Bus interaction to act as a local sub-storage that the main
network can access
    </DiamondAnnotation>

<DiamondAnnotation pos="1.5 1.5 0.5" color="#00ff00">
        Another item pipe subnet, to return the charged items to the Pattern Provider
    </DiamondAnnotation>

<IsometricCamera yaw="195" pitch="30" />
</GameScene>

"Subnetwork" is a rather loosely-defined term, but one might say that a subnetwork is any [network](../ae2-mechanics/me-network-connections.md) that supports your
main network or does some small task. They are typically small enough to not require controllers. Their main 2 uses tend to be:

*   To restrict what [devices](../ae2-mechanics/devices.md) have access to what storage (you don't want the import bus on a "pipe" subnet to have access to your main net
    storage, or it will put the items in your storage cells instead of in the destination inventory).
*   To save channels on your main network, like having a pattern provider output to an interface connected to several storage
    busses on several machines, using 1 channel, instead of putting a pattern provider on each machine, using several channels.

Very important in making a subnet is keeping track of the [network connections](../ae2-mechanics/me-network-connections.md).
Often, people put together some jumble of interfaces and busses and stuff and expect it to be a subnet when
all the devices are still connected to the main network through various fullblock devices.

Cables with different colors have nothing to do with making a subnetwork other than that they won't connect to each other.

They can be

*   an import bus and storage bus set up to transfer items or fluids from one container to another like an item or fluid pipe
*   an annihilation plane and storage bus, so that the only place the annihilation plane can put what it breaks is the storage bus, allowing you to filter the plane
*   an interface and formation plane, so that whatever is inserted into the interface gets pushed to the formation plane and placed/dropped in the world
*   a setup to automatically make certus quartz, regulated and controlled by a <ItemLink id="level_emitter" /> on the main network
*   a specialized storage system accessible from the main network via the special storage-bus-on-interface interaction, in order to store the output of a farm without endlessly overflowing your main storage
*   and so on

Very useful for making subnetworks is the <ItemLink id="quartz_fiber" />. It transfers power between networks without
connecting them, allowing you to power subnets without needing to put energy acceptors and power cables everywhere.
