#!/bin/bash

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

ROOT=${WD}/../../../../../
LIB=${WD}/junit-platform-console-standalone-1.7.0-M1.jar
OUT=${WD}/_build/production/bank

javac -cp ${ROOT}:${LIB} -d ${OUT} ${WD}/*.java

