#!/usr/bin/env bash

cd ../../../../../../../

ROOT=${PWD}

BASIC_PATH=${ROOT}/java-advanced-2020
SOLUTION_PATH=${ROOT}/java-advanced-2020-solutions

MODULE_NAME=ru.ifmo.rain.zhukov
MODULE_PATH=ru/ifmo/rain/zhukov/implementor

OUT_PATH=${SOLUTION_PATH}/java-solutions/${MODULE_PATH}/_build/production/${MODULE_NAME}
REQ_PATH=${BASIC_PATH}/lib:${BASIC_PATH}/artifacts
SRC_PATH=${SOLUTION_PATH}/java-solutions
JAR_PATH=${SOLUTION_PATH}/java-solutions/${MODULE_PATH}

rm -rf ${OUT_PATH}

javac --module-path ${REQ_PATH} ${SRC_PATH}/module-info.java ${SRC_PATH}/${MODULE_PATH}/*.java -d ${OUT_PATH}

jar -c --file ${JAR_PATH}/_implementor.jar --main-class=${MODULE_NAME}.implementor.JarImplementor --module-path ${REQ_PATH} -C ${OUT_PATH} .