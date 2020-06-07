#!/bin/bash

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

LIB=${WD}/junit-platform-console-standalone-1.7.0-M1.jar
OUT=${WD}/_build/production/bank

java -cp ${LIB}:${OUT} org.junit.runner.JUnitCore  ru.ifmo.rain.zhukov.bank.BankTest