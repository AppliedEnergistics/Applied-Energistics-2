---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Энергохранилища
  icon: energy_cell
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:energy_cell
- ae2:dense_energy_cell
- ae2:creative_energy_cell
---

# Энергохранилища

<Row gap="20">
  <BlockImage id="energy_cell" scale="8" p:fullness="4" />

  <BlockImage id="dense_energy_cell" scale="8" p:fullness="4" />

  <BlockImage id="creative_energy_cell" scale="8" />
</Row>

Энергохранилища обеспечивают сети больший запас [энергии](../ae2-mechanics/energy.md). Небольшой энергетический буфер помогает сгладить
скачки энергопотребления при установке или извлечении большого количества предметов, а больший объём энергохранилища
позволяет сети работать, когда энергия не вырабатывается (например, ночью с помощью солнечных панелей), или справляться с массовым мгновенным
потреблением энергии [пространственного хранилища](../ae2-mechanics/spatial-io.md).

## Заполнение полос

<Row>
<BlockImage id="energy_cell" scale="4" p:fullness="0" />
<BlockImage id="energy_cell" scale="4" p:fullness="1" />
<BlockImage id="energy_cell" scale="4" p:fullness="2" />
<BlockImage id="energy_cell" scale="4" p:fullness="3" />
<BlockImage id="energy_cell" scale="4" p:fullness="4" />
</Row>

Полосы на боковой стороне энергохранилища соответствуют его уровню энергии.

* 0 — уровень заряда ниже 25%
* 1 — уровень заряда от 25% до 50%
* 2 — уровень заряда от 50% до 75%
* 3 — уровень заряда от 75% до 99%
* 4 — уровень заряда выше 99%

## Типы энергохранилищ

*   <ItemLink id="energy_cell" /> может хранить 200 тыс. AE, и одного будет достаточно для большинства случаев использования, легко справляясь со скачками напряжения
    при обычном использовании сети.
*   <ItemLink id="dense_energy_cell" /> может хранить 1,6 млн. AE и предназначен для случаев, когда вы хотите запустить сеть на накопленной энергии или
    справиться с огромным мгновенным потреблением энергии большим [пространственным хранилищем](../ae2-mechanics/spatial-io.md).
*   <ItemLink id="creative_energy_cell" /> — это креативный предмет для тестирования, предоставляющий НЕОГРАНИЧЕННУЮ МОЩНОСТЬ или что-то в этом роде.

## Рецепты

<Row>
  <RecipeFor id="energy_cell" />

  <RecipeFor id="dense_energy_cell" />
</Row>
