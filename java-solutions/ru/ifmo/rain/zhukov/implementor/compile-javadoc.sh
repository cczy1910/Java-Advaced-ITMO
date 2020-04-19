rm -rf _javadoc

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"

ROOT=${WD}/../../../../../../

BASIC_PATH=${ROOT}/../java-advanced-2020

BASIC_PACKAGE="modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor"

javadoc \
    -private \
    -link https://docs.oracle.com/en/java/javase/11/docs/api/ \
    -d _javadoc \
    -cp : ${WD}/*.java \
    ${BASIC_PATH}/${BASIC_PACKAGE}/Impler.java \
    ${BASIC_PATH}/${BASIC_PACKAGE}/JarImpler.java \
    ${BASIC_PATH}/${BASIC_PACKAGE}/ImplerException.java