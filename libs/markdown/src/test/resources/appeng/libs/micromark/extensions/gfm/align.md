# Align

## An empty initial cell

| | a|c|
|--|:----:|:---|
|a|b|c|
|a|b|c|

## Missing alignment characters

| a | b | c |
|   |---|---|
| d | e | f |

* * *

| a | b | c |
|---|---|   |
| d | e | f |

## Incorrect characters

| a | b | c |
|---|-*-|---|
| d | e | f |

## Two alignments

|a|
|::|

|a|
|:-:|

## Two at the start or end

|a|
|::-|

|a|
|-::|

## In the middle

|a|
|-:-|

## A space in the middle

|a|
|- -|

## No pipe

a
:-:

a
:-

a
-:

## A single colon

|a|
|:|

a
:

## Alignment on empty cells

| a | b | c | d | e |
| - | - | :- | -: | :-: |
| f |
