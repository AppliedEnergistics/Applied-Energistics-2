---
navigation:
  parent: example-setups/example-setups-index.md
  title: Auto-Regulated Cobblestone Generator
  icon: minecraft:cobblestone
---

# Auto-Regulated Cobblestone Generator

Automation of a cobblestone generator is simple, just face an <ItemLink id="annihilation_plane" /> into a standard vanilla
manual cobblestone generator. However, doing this will eventually jam your network full of cobblestone, so some regulation
is desired.

Due to how annihilation planes work (they act like <ItemLink id="import_bus" />ses), we cannot simply put a <ItemLink id="level_emitter" />
facing an <ItemLink id="export_bus" /> with a <ItemLink id="redstone_card" /> (since you cannot go directly import to export
with no storage in between). We have to be a bit more roundabout.

<ItemLink id="toggle_bus" />ses allow you to connect and disconnect parts of your network with redstone signals, but they cause
the network to reboot whenever they do this. There is a simple workaround: put the toggle bus on a [subnetwork](../ae2-mechanics/subnetworks.md)
such that it only reboots the subnet.

We can have a self-contained <ItemLink id="annihilation_plane" /> and <ItemLink id="storage_bus" /> [subnetwork](../ae2-mechanics/subnetworks.md)
push into an <ItemLink id="interface" /> on the main network. The toggle bus will connect and disconnect the subnet from a
<ItemLink id="quartz_fiber" />, cutting power to the planes.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/regulated_cobble_gen.snbt" />

<BoxAnnotation color="#dddddd" min="3 2 2" max="7 2.3 3">
        (1) Annihilation Planes: No GUI to configure, but can be enchanted with Efficiency and Unbreaking to reduce power draw.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 2 2" max="2.3 3 3">
        (2) Storage Bus: In its default configuration.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.3 2.3 2" max="2.7 2.7 2.3">
        (3) Toggle Bus: Very important that the toggle bus is on the
        subnetwork, and not the main network.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.3 3 2.3" max="2.7 3.3 2.7">
        (4) Level Emitter: Configured with cobblestone and the desired quantity, set to "Emit when levels are below limit".
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 2 3" max="2 3 2">
        (5) Interface: In its default configuration.
  </BoxAnnotation>

<DiamondAnnotation pos="0 2.5 1.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

<DiamondAnnotation pos="5 1.5 3.5" color="#00ff00">
        Waterlogged stairs keep the water from flowing and turning the lava into obsidian.
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

* The <ItemLink id="annihilation_plane" />s (1) Have no GUI to configure, but can be enchanted with Efficiency and Unbreaking to reduce power draw.
* The <ItemLink id="storage_bus" /> (2) is in its default configuration.
* The <ItemLink id="toggle_bus" /> (3) must be on the subnetwork side of the quartz fiber, not the main network, or the main
  network will reboot every time it toggles.
* The <ItemLink id="level_emitter" /> (4) is configured with the desired item and quantity, and set to "Emit when levels are below limit".
* The <ItemLink id="interface" /> (5) is in its default configuration.

## How It Works

1. The cobblestone generator makes some cobblestone.
2. The <ItemLink id="annihilation_plane" />s break the cobblestone. 
3. The <ItemLink id="storage_bus" /> stores the cobblestone in the <ItemLink id="interface" />, sending it into the main network.
4. When the amount of cobblestone in the main network exceeds the set amount, the <ItemLink id="level_emitter" /> stops
   sending a signal, turning off the <ItemLink id="toggle_bus" />.
5. This cuts power to the subnetwork, stopping the annihilation planes from working.
