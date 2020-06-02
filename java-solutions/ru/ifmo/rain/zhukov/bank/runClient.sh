#!/bin/bash

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

ROOT=${WD}/../../../../../../
LIB=${ROOT}/../java-advanced-2020/lib
OUT=${WD}/_build/production/bank

java -cp ${LIB}/*:${OUT} ru.ifmo.rain.zhukov.bank.Client $@
