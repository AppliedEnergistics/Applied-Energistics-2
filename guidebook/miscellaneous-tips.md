---
navigation:
  title: Miscellaneous Tips
---

### How Items are Placed

Items entering the network will start at the highest priority storage, as
their first destination, in the case of two storages have the same priority,
if one already contains the item, they will prefer that storage over any
other. Any Whitelisted cells will be treated as already containing the item
when in the same priority group as other storages.

### Upgrading Storage Cells

If you have an EMPTY Storage cell any tier you can remove the
Cell/Segment/Block/Cluster from the housing by shift + right clicking with it
in your hand, so you can store it or use it to make bigger cells. it also
gives you an empty storage cell housing to re-insert a cell into.

### Colored Terminals / Monitors

When you place a <ItemLink id="terminal"/> or
other monitors on a cable, they take on the color of that cable, so if the cable is
blue, so will the screen of the placed part.

### One Way Network Connections

You can hook up a Storage Bus to a interface on a seperate network, to provide
a one way connection, allowing you to create public / private networks. This
requires that the Interface be unconfigured, if the interface is configured to
store items, it will instead see the items in the inventory.

### Rotating Blocks

You can rotate most blocks by using a Buildcraft Compatible Wrench, such as
the <ItemLink id="certus_quartz_wrench"/>.

### Setting Priority

You can set Storage Priorities on <ItemLink
id="chest"/>, <ItemLink
id="drive"/> or <ItemLink
id="storage_bus"/> in the Priority Tab on the
right top side. Higher Priorities are more imporant then lower ones and by
default all storages are set to 0.

### Removing Blocks / Parts

You can Shift + Rightclick with a Buildcraft Compatible Wrench and it will
dismantle the AE Block or Part and dropping it for you, this is most useful
with Parts as if you use a pick it will drop any cable, and parts in the
block, using a wrench lets you only take off a single part.
