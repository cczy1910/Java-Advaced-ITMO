#!/bin/bash

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

OUT=${WD}/_build/production/bank

java -cp ${OUT} ru.ifmo.rain.zhukov.bank.Client $@
