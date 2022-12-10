---
categories:
  - Upgrades
item_ids:
  - ae2:fuzzy_card
navigation:
  parent: website/index.md
  title: Fuzzy Card
---

Used to add fuzzy behavior to <ItemLink
id="formation_plane"/>, <ItemLink
id="export_bus"/>, <ItemLink
id="import_bus"/>, <ItemLink
id="level_emitter"/>, <ItemLink
id="storage_bus"/>, <ItemLink
id="view_cell"/> and <ItemLink
id="item_storage_cell_1k"/> as well as other non
spatial [storage cells](../me-network/storage-cells.md).

### Fuzzy Comparison Details

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
