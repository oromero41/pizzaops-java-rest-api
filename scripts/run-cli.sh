#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p out
javac -encoding UTF-8 -d out src/main/java/com/pizzaops/{PizzaCatalog,PizzaOrder,PizzaService,PizzaStatistics,PizzaOrderSystem}.java
java -cp out com.pizzaops.PizzaOrderSystem
