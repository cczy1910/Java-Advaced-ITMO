#!/bin/bash

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

OUT=${WD}/_build/production/bank

export CLASSPATH="${OUT}"

rmiregistry 2> /dev/null &

java  ru.ifmo.rain.zhukov.bank.Server