---
categories:
  - ME Network/Network Functions
item_ids:
  - ae2:interface
  - ae2:cable_interface
related:
  - Possible Upgrades
navigation:
  parent: website/index.md
  title: ME Interface
---

![A picture of a Interface Block.](../../../assets/large/interface.png)![A picture
of a Interface Part.](../../../assets/large/interface_module.png)The <ItemLink
id="interface"/> is the only component which can
be used as a part, or as a Block. Crafting an ME interface in either form by
itself produces the other form. The thin form is useful if you want to provide
several different interfaces in a single block of physical space, but each
will need its own channel. The block form lets multiple other blocks connect
to a single ME interface, using only one channel for the interface.

<ItemGrid>
  <ItemIcon id="interface" />
  <ItemIcon id="cable_interface" />
</ItemGrid>

The <ItemLink id="interface"/> acts as an in
between when working with pipes, tubes, networks, or machines from other mods.

You can configure certain items to be exported from the [ME Network](../../me-network.md) into the <ItemLink
id="interface"/> for use with other mods. Or use
other mods to insert into any <ItemLink
id="interface"/>. as long as it isn't full of
exported materials it will add any added items into the [ME Network](../../me-network.md).

The interface normally functions like a chest, however with one exception, if
you place a storage bus on an interface, you essentially include the entire
network instead, this allows networks to share huge sets of contents and to be
chained together in a very effective manner. In addition to this mode, if you
you configure your interface to explicilty provide specific materials, the
storage bus will behave as if the interface was a standard chest, disabling
this advanced feature. (As of this writing, autocrafting in another network
won't reliably use the items in a configured interface.)

The <ItemLink id="interface"/> require a
[channel](../channels.md) to function.

<RecipeFor id="interface" />
