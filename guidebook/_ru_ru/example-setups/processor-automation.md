---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация процессора
  icon: inscriber
---

# Автоматизация производства процессоров

Существует множество способов автоматизации [процессоров](../items-blocks-machines/processors.md), и это один из них.

Эту общую схему можно реализовать с любым типом умного кабеля, плотного кабеля, канала или как там это называется в моде, если
только вы сможете её фильтровать.

![The Process FLow Diagram](../assets/diagrams/processor_flow_diagram.png)

Ниже подробно описано, как это сделать только с помощью мода «Прикладная энергетика 2», используя [подсеть кабеля](pipe-subnet.md).

Обратите внимание: поскольку здесь используется <ItemLink id="pattern_provider" />, он предназначен для интеграции в вашу систему [автоматического создания](../ae2-mechanics/autocrafting.md).
Если вы просто хотите автоматизировать процесс обработки в автономном режиме, замените поставщик шаблонов на другой контейнер и непосредственно загружайте ингредиенты в верх контейнера.

Это обеспечивает обратную совместимость
с предыдущими версиями мода «Прикладная энергетика 2», поскольку, даже если <ItemLink id="inscriber" /> является сторонним, подсеть каналов всё равно вставляется и
извлекается из правильных сторон.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/processor_automation.snbt" />

  <BoxAnnotation color="#dddddd" min="5 1 0" max="6 2 1" thickness=".05">
        (1) Поставщик шаблонов: в конфигурации по умолчанию, с соответствующими шаблонами обработки.

        <Row>
            ![Logic Pattern](../assets/diagrams/logic_pattern_small.png)
            ![Calculation Pattern](../assets/diagrams/calculation_pattern_small.png)
            ![Engineering Pattern](../assets/diagrams/engineering_pattern_small.png)
        </Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4.7 2 0" max="5 3 1" thickness=".05">
        (2) ME Шина хранения #1: в конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 1 0" max="4.3 2 1" thickness=".05">
        (3) ME Шина экспорта #1: фильтруется по кремнию, имеет 2 карты ускорения.
        <Row><ItemImage id="silicon" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 4 0" max="4.3 3 1" thickness=".05">
        (4) ME Шина экспорта #2: фильтруется по золотому слитку, имеет 2 карты ускорения.
        <Row><ItemImage id="minecraft:gold_ingot" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 5 0" max="4.3 4 1" thickness=".05">
        (5) ME Шина экспорта #3: фильтруется по кристаллу истинного кварца, имеет 2 карты ускорения.
        <Row><ItemImage id="certus_quartz_crystal" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 6 0" max="4.3 5 1" thickness=".05">
        (6) ME Шина экспорта #4: фильтруется по алмазу, имеет 2 карты ускорения.
        <Row><ItemImage id="minecraft:diamond" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.3 3 0" max="2 2 1" thickness=".05">
        (7) ME Шина экспорта #5: фильтруется по редстоуновой пыли, имеет 2 карты ускорения.
        <Row><ItemImage id="minecraft:redstone" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 1 0" max="3 2 1" thickness=".05">
        (8) Высекатель #1: в конфигурации по умолчанию, кремниевая печать и 4 карты ускорения.
        <Row><ItemImage id="silicon_press" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 3 0" max="3 4 1" thickness=".05">
        (9) Высекатель #2: в конфигурации по умолчанию, логическая печать и 4 карты ускорения.
        <Row><ItemImage id="logic_processor_press" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 4 0" max="3 5 1" thickness=".05">
        (10) Высекатель #3: в конфигурации по умолчанию, вычислительная печать и 4 карты ускорения.
        <Row><ItemImage id="calculation_processor_press" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 5 0" max="3 6 1" thickness=".05">
        (11) Высекатель #4: в конфигурации по умолчанию, инженерная печать и 4 карты ускорения.
        <Row><ItemImage id="engineering_processor_press" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 2 0" max="1 3 1" thickness=".05">
        (12) Высекатель #5: в конфигурации по умолчанию, имеет 4 карты ускорения.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 2 0" max="3 1 1" thickness=".05">
        (13) ME Шина импорта #1: в конфигурации по умолчанию, имеет 2 карты ускорения.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 4 0" max="3 3 1" thickness=".05">
        (14) ME Шина импорта #2: в конфигурации по умолчанию, имеет 2 карты ускорения.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 5 0" max="3 4 1" thickness=".05">
        (15) ME Шина импорта #3: в конфигурации по умолчанию, имеет 2 карты ускорения.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 6 0" max="3 5 1" thickness=".05">
        (16) ME Шина импорта #4: в конфигурации по умолчанию, имеет 2 карты ускорения.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 3 0" max="1 3.3 1" thickness=".05">
        (17) ME Шина хранения #2: в конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 1.7 0" max="1 2 1" thickness=".05">
        (18) ME Шина хранения #3: в конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 2 0" max="0.7 3 1" thickness=".05">
        (19) ME Шина импорта #5: в конфигурации по умолчанию, имеет 2 карты ускорения.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="5 0.7 0" max="6 1 1" thickness=".05">
        (20) ME Шина хранения #4: в конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.3 2.7 0.3" max="3.7 3 0.7" thickness=".05">
        Кварцевое волокно питает все три высекателя, поскольку они действуют как кабели и, таким образом, передают энергию.
  </BoxAnnotation>

<DiamondAnnotation pos="7 1.5 0.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="185" pitch="5" />
</GameScene>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) находится в конфигурации по умолчанию с соответствующим <ItemLink id="processing_pattern" />.

  ![Logic Pattern](../assets/diagrams/logic_pattern.png)
  ![Calculation Pattern](../assets/diagrams/calculation_pattern.png)
  ![Engineering Pattern](../assets/diagrams/engineering_pattern.png)

* <ItemLink id="storage_bus" /> (2, 17, 18, 20) находятся в конфигурации по умолчанию.
* <ItemLink id="export_bus" /> (3-7) фильтруются по соответствующему ингредиенту. Они имеют 2 <ItemLink id="speed_card" />.
    <Row>
      <ItemImage id="silicon" scale="2" />
      <ItemImage id="minecraft:gold_ingot" scale="2" />
      <ItemImage id="certus_quartz_crystal" scale="2" />
      <ItemImage id="minecraft:diamond" scale="2" />
      <ItemImage id="minecraft:redstone" scale="2" />
    </Row>
* <ItemLink id="import_bus" /> (13-16, 19) находятся в конфигурации по умолчанию. У них есть 2 <ItemLink id="speed_card" />.
* <ItemLink id="inscriber" /> находятся в конфигурации по умолчанию. У них есть соответствующая [печать](../items-blocks-machines/presses.md),
   и 4 <ItemLink id="speed_card" />.
   <Row>
     <ItemImage id="silicon_press" scale="2" />
     <ItemImage id="logic_processor_press" scale="2" />
     <ItemImage id="calculation_processor_press" scale="2" />
     <ItemImage id="engineering_processor_press" scale="2" />
   </Row>

## Как это работает

1. <ItemLink id="pattern_provider" /> помещает ингредиенты в бочку.
2. Первая [подсеть кабеля](pipe-subnet.md) (оранжевая) извлекает кремний, редстоуновую пыль и соответствующий ингредиент процессора
(золотой слиток, кристалл истинного кварца или алмаз) из бочки и помещает их в соответствующий  <ItemLink id="inscriber" />.
3. Первые четыре <ItemLink id="inscriber" /> собирают <ItemLink id="printed_silicon" />, и <ItemLink id="printed_logic_processor" />,
   <ItemLink id="printed_calculation_processor" />, или <ItemLink id="printed_engineering_processor" />.
4. Вторая и третья [подсеть кабеля](pipe-subnet.md) (зелёные) извлекают печатные платы из первых четырёх <ItemLink id="inscriber" />
    и помещают их в пятый, последний <ItemLink id="inscriber" />.
5. Пятый <ItemLink id="inscriber" /> собирает [процессор](../items-blocks-machines/processors.md).
6. Четвёртая [подсеть кабеля](pipe-subnet.md) (фиолетовая) помещает процессор в поставщик шаблонов, возвращая его в основную сеть.