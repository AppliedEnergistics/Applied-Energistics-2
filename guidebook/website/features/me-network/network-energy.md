---
navigation:
  parent: website/index.md
  title: Network Energy
  icon: energy_cell
item_ids:
  - ae2:energy_acceptor
  - ae2:cable_energy_acceptor
  - ae2:creative_energy_cell
  - ae2:energy_cell
  - ae2:dense_energy_cell
  - ae2:vibration_chamber
  - ae2:quartz_fiber
---

The ME Network needs energy to function. This energy is measured in AE per tick.

To power your network, you can either connect a <ItemLink id="vibration_chamber" /> directly,
or use an <ItemLink id="energy_acceptor" /> to connect energy sources from compatible mods.

Your network will have some inherent energy storage, which can be increased by connecting
energy cells.

To see the current energy statistics for your network, right-click any part of it with a <ItemLink id="network_tool" />.

## Energy Acceptor

![Picture of a Energy Accepter.](../../assets/large/energy_accepter.png)

The <ItemLink id="energy_acceptor" /> converts energy from external
systems into AE and stores it in the network.

The following energy systems are supported:

| Energy System                | Conversion Rate |
| ---------------------------- | --------------- |
| Forge Energy / Redstone Flux | 2 FE = 1 AE     |

<RecipeFor id="energy_acceptor" />
<RecipeFor id="cable_energy_acceptor" />

## Energy Storage

![A picture of a uncharged, and charged energy cell.](../../assets/large/energy_cell.png)

Stores up to 200,000 AE. They do not accept power directly, but are used to add
additional power storage to an already existing [ME Network](../me-network.md).

<RecipeFor id="energy_cell" />

![A picture of a uncharged, and charged energy cell.](../../assets/large/dense_energy_cell.png)

store AE energy up to 1.6 million units. They do not accept power directly but
are used to add additional power storage to an already existing [ME Network](../me-network.md).

<RecipeFor id="dense_energy_cell" />

<ItemLink id="creative_energy_cell" /> contain infinite AE energy and can be used
to provide power without needing to generate it.

They can only be spawned in **Creative Mode**.

### Vibration Chamber

![A picture of a Vibration Chamber.](../../assets/large/vibration_chamber.png)

A modified furnace capable of generating AE Power instead of smelting ores. When
placed on an [ME Network](../me-network.md) it will charge <ItemLink id="energy_cell"/> or
power other Network Devices.

The <ItemLink id="vibration_chamber"/> will burn
almost any solid burnable fuel for power. It will slow, or accelerate the burn
depending on how much power it is able to store vs what is wasted. Generates
between 1 and 10 AE/t depending on its burn speed.

<RecipeFor id="vibration_chamber" />

### Sharing Power Between Networks

A part designed to share energy between two [ME Network](../me-network.md)s without sharing anything else, also
used to craft <ItemLink id="fluix_glass_cable" />.

<RecipeFor id="quartz_fiber" />
