---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Upgrade Cards
  icon: speed_card
  position: 410
item_ids:
- ae2:basic_card
- ae2:advanced_card
- ae2:redstone_card
- ae2:capacity_card
- ae2:void_card
- ae2:fuzzy_card
- ae2:speed_card
- ae2:inverter_card
- ae2:crafting_card
- ae2:equal_distribution_card
- ae2:energy_card
---
# Upgrade Cards
<Row>
<ItemImage id="redstone_card" scale="2" /><ItemImage id="capacity_card" scale="2" /><ItemImage id="void_card" scale="2" />
<ItemImage id="fuzzy_card" scale="2" /><ItemImage id="speed_card" scale="2" /><ItemImage id="inverter_card" scale="2" />
<ItemImage id="crafting_card" scale="2" /><ItemImage id="equal_distribution_card" scale="2" /><ItemImage id="energy_card" scale="2" />
</Row>

Upgrade cards change the behavior of AE2 [devices](../ae2-mechanics/devices.md) and machines, increasing their speed, improving their
filter capacity, enabling redstone control, etc.

---

# Card Components

<Row><ItemImage id="basic_card" scale="2" /><ItemImage id="advanced_card" scale="2" /></Row>

Cards are crafted with either basic or advanced card bases

<Row><RecipeFor id="basic_card" /><RecipeFor id="advanced_card" /></Row>

---

# Redstone Card

<ItemImage id="redstone_card" scale="2" />

Redstone cards add redstone control, adding a toggle button in the device's GUI to swap between various redstone conditions.

<RecipeFor id="redstone_card" />

---

# Capacity Card

<ItemImage id="capacity_card" scale="2" />

Capacity cards increase the amount of filter slots in import, export, and storage busses, and formation planes.

<RecipeFor id="capacity_card" />

---

# Overflow Destruction Card

<ItemImage id="void_card" scale="2" />

Overflow destruction cards can be applied to [storage cells](storage_cells.md) in a <ItemLink id="cell_workbench"/>
and will delete incoming items if the cell is full. (make sure to [partition](cell_workbench.md) your cells!) Combined with an equal distribution card,
items will be voided if that specific item's section of the cell is full, even if other items' sections are empty.

<RecipeFor id="void_card" />

---

# Fuzzy Card

<ItemImage id="fuzzy_card" scale="2" />

Fuzzy cards let devices and tools with filters filter by damage level and/or ignore item NBT, allowing you to export
all iron axes no matter the damage level and enchantments, or only export damaged diamond swords, not fully repaired ones.

Below is an example of how Fuzzy Damage comparison mods work, left side is the
bus config, top is the compared item.

| 25%                    | 10% Damaged Pickaxe | 30% Damaged Pickaxe | 80% Damaged Pickaxe | Full Repair Pickaxe |
| ---------------------- | ------------------- | ------------------- | ------------------- | ------------------- |
| Nearly Broken Pickaxe  | ✅                  | \*\*\*\*            | \*\*\*\*            | \*\*\*\*            |
| Fully Repaired Pickaxe | \*\*\*\*            | ✅                  | ✅                  | ✅                  |

| 50%                    | 10% Damaged Pickaxe | 30% Damaged Pickaxe | 80% Damaged Pickaxe | Full Repair Pickaxe |
| ---------------------- | ------------------- | ------------------- | ------------------- | ------------------- |
| Nearly Broken Pickaxe  | ✅                  | ✅                  | \*\*\*\*            | \*\*\*\*            |
| Fully Repaired Pickaxe | \*\*\*\*            | \*\*\*\*            | ✅                  | ✅                  |

| 75%                    | 10% Damaged Pickaxe | 30% Damaged Pickaxe | 80% Damaged Pickaxe | Full Repair Pickaxe |
| ---------------------- | ------------------- | ------------------- | ------------------- | ------------------- |
| Nearly Broken Pickaxe  | ✅                  | ✅                  | \*\*\*\*            | \*\*\*\*            |
| Fully Repaired Pickaxe | \*\*\*\*            |                     | ✅                  | ✅                  |

| 99%                    | 10% Damaged Pickaxe | 30% Damaged Pickaxe | 80% Damaged Pickaxe | Full Repair Pickaxe |
| ---------------------- | ------------------- | ------------------- | ------------------- | ------------------- |
| Nearly Broken Pickaxe  | ✅                  | ✅                  | ✅                  | \*\*\*\*            |
| Fully Repaired Pickaxe | \*\*\*\*            | \*\*\*\*            | \*\*\*\*            | ✅                  |

| Ignore                 | 10% Damaged Pickaxe | 30% Damaged Pickaxe | 80% Damaged Pickaxe | Full Repair Pickaxe |
| ---------------------- | ------------------- | ------------------- | ------------------- | ------------------- |
| Nearly Broken Pickaxe  | ✅                  | ✅                  | ✅                  | **✅**              |
| Fully Repaired Pickaxe | **✅**              | **✅**              | **✅**              | ✅                  |

<RecipeFor id="fuzzy_card" />

---

# Acceleration Card

<ItemImage id="speed_card" scale="2" />

Acceleration cards make stuff go faster, making import and export busses move more items per operation, and making inscribers
and assemblers work faster.

<RecipeFor id="speed_card" />

---

# Inverter Card

<ItemImage id="inverter_card" scale="2" />

Inverter cards swap filters in devices and tools from whitelist to blacklist.

<RecipeFor id="inverter_card" />

---

# Crafting Card

<ItemImage id="inverter_card" scale="2" />

Crafting cards let the device send crafting requests to your [autocrafting](../ae2-mechanics/autocrafting.md)
system to get the items it desires.

<RecipeFor id="inverter_card" />

---

# Equal Distribution Card

<ItemImage id="equal_distribution_card" scale="2" />

Equal distribution cards can be applied to [storage cells](storage_cells.md) in a <ItemLink id="cell_workbench"/> and
split the cell into equally-sized sections based on what the card is [partitioned](cell_workbench.md) to. This prevents one item type from completely
filling the cell.

<RecipeFor id="equal_distribution_card" />

---

# Energy Card

<ItemImage id="energy_card" scale="2" />

Energy cards add more energy storage to certain tools like portable terminals.

<RecipeFor id="energy_card" />