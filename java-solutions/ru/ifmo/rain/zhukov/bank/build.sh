#!/bin/bash

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

ROOT=${WD}/../../../../../../
LIB=${ROOT}/../java-advanced-2020/lib
OUT=${WD}/_build/production/bank

javac -cp ${LIB}/*:${ROOT}/java-solutions -d ${OUT} ${WD}/*.java
