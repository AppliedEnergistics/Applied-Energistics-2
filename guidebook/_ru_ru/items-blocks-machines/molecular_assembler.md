---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Молекулярный сборщик
  icon: molecular_assembler
  position: 310
categories:
- machines
item_ids:
- ae2:molecular_assembler
---

# Молекулярный сборщик

<BlockImage id="molecular_assembler" scale="8" />

Молекулярный сборщик принимает на вход предметы и выполняет операцию, заданную смежным <ItemLink id="pattern_provider" />,
или вставленными <ItemLink id="crafting_pattern" />, <ItemLink id="smithing_table_pattern" />, или <ItemLink id="stonecutting_pattern" />,
а затем помещает результат в смежные инвентари.

В этом сборщике используется шаблон создания, который определяет рецепт: 1 Дубовое бревно = 4 Дубовые доски. Когда дубовые брёвна помещаются в верхнюю воронку,
сборщик обрабатывает и извлекает дубовые доски в нижнюю воронку.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/standalone_assembler.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Основное применение молекулярного сборщика

Однако они в основном используются рядом с <ItemLink id="pattern_provider" />. Поставщики шаблонов ведут себя в этом случае особым образом
и отправляют информацию о соответствующем шаблоне вместе с ингредиентами смежным сборщикам. Поскольку сборщики автоматически выгружают результаты
созданий в смежные инвентари (и, следовательно, в слоты возврата поставщика шаблонов), сборщик на поставщике шаблонов
— это всё, что нужно для автоматизации создания шаблонов.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/assembler_tower.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Улучшения

Молекулярный сборщик поддерживает следующие [улучшения](upgrade_cards.md):

*   <ItemLink id="speed_card" /> увеличивает количество перемещаемых предметов за одну операцию.

## Рецепт

<RecipeFor id="molecular_assembler" />

## Примечание

Мод «Optifine» нарушает функцию «перемещения в соседние инвентари», поэтому большинство схем создания с использованием сборщиков не будут работать.