---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация зарядки
  icon: charger
---

# Автоматизация зарядки

Обратите внимание, что поскольку здесь используется <ItemLink id="pattern_provider" />, он предназначен для интеграции в вашу установку [автоматического создания](../ae2-mechanics/autocrafting.md).
Если вы просто хотите автоматизировать автономный <ItemLink id="charger" /> , используйте воронки, сундуки и прочее.

Автоматизация <ItemLink id="charger" />а довольно проста. <ItemLink id="pattern_provider" /> отправляет ингредиент в зарядник, затем [подсеть канала](pipe-subnet.md)
или другой канал предметов отправляет результат обратно поставщику.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/charger_automation.snbt" />

<BoxAnnotation color="#dddddd" min="1 0 0" max="2 1 1">
        (1) Поставщик шаблонов: в стандартной конфигурации, с соответствующими шаблонами обработки. Также обеспечивает зарядник питанием.

        ![Charger Pattern](../assets/diagrams/charger_pattern_small.png)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 0" max="1 1.3 1">
        (2) ME Шина импорта: в конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 1.3 1">
        (3) ME Шина хранения: в конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) находится в конфигурации по умолчанию с соответствующим <ItemLink id="processing_pattern" />.
  Он также обеспечивает <ItemLink id="charger" /> [энергией](../ae2-mechanics/energy.md), поскольку действует как [кабель](../items-blocks-machines/cables.md).
  
    ![Charger Pattern](../assets/diagrams/charger_pattern.png)

* <ItemLink id="import_bus" /> (2) имеет конфигурацию по умолчанию.
* <ItemLink id="storage_bus" /> (3) имеет конфигурацию по умолчанию.

## Как это работает

1. <ItemLink id="pattern_provider" /> помещает ингредиенты в <ItemLink id="charger" />.
2. Зарядник выполняет свою функцию.
3. <ItemLink id="import_bus" /> в зелёной подсети извлекает результат из зарядника и пытается сохранить его в
   [сетевом хранилище](../ae2-mechanics/import-export-storage.md).
4. Единственным хранилищем в зелёной подсети является <ItemLink id="storage_bus" />, которая сохраняет полученные предметы в поставщике шаблонов, возвращая их в основную сеть.
