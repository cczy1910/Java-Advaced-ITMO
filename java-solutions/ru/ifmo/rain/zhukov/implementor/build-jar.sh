#!/usr/bin/env bash

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

ROOT=${WD}/../../../../../../

BASIC_PATH=${ROOT}/../java-advanced-2020
SOLUTION_PATH=${ROOT}

MODULE_NAME=ru.ifmo.rain.zhukov
MODULE_PATH=ru/ifmo/rain/zhukov/implementor

OUT_PATH=${SOLUTION_PATH}/java-solutions/${MODULE_PATH}/_build/production/${MODULE_NAME}
REQ_PATH=${BASIC_PATH}/lib:${BASIC_PATH}/artifacts
SRC_PATH=${SOLUTION_PATH}/java-solutions
JAR_PATH=${SOLUTION_PATH}/java-solutions/${MODULE_PATH}

rm -rf ${OUT_PATH}

javac --module-path ${REQ_PATH} -d ${OUT_PATH} ${SRC_PATH}/module-info.java ${WD}/*.java

jar -c --file ${JAR_PATH}/_implementor.jar --main-class=${MODULE_NAME}.implementor.JarImplementor --module-path ${REQ_PATH} -C ${OUT_PATH} .