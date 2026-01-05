---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: ME Порт ввода/вывода
  icon: io_port
  position: 210
categories:
- devices
item_ids:
- ae2:io_port
---

# ME Порт ввода/вывода

<BlockImage id="io_port" p:powered="true" scale="8" />

ME Порт ввода/вывода позволяет быстро заполнять и опустошать [ячейки хранения](../items-blocks-machines/storage_cells.md) в или из
[сетевого хранилища](../ae2-mechanics/import-export-storage.md).

Его можно повернуть с помощью <ItemLink id="certus_quartz_wrench" />.

## Настройки

*   ME Порт ввода/вывода можно настроить на перемещение ячейки в выходные слоты, когда ячейка пуста, заполнена или когда работа выполнена.
*   Если вставлена <ItemLink id="redstone_card" />, будут доступны различные условия для редстоуна.
*   В центре GUI находится стрелка, указывающая направление перемещения предметов: из ячейки в [сетевое хранилище](../ae2-mechanics/import-export-storage.md),
    или из хранилища в ячейку.

## Улучшения

ME Порт ввода/вывода поддерживает следующие [улучшения](upgrade_cards.md):

*   <ItemLink id="speed_card" /> увеличивает количество перемещаемых предметов за одну операцию.
*   <ItemLink id="redstone_card" /> добавляет управление редстоуном, позволяя активировать его при высоком уровне сигнала, низком уровне сигнала или один раз за импульс.

## Рецепт

<RecipeFor id="io_port" />
