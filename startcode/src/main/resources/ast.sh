#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

GRAMMAR_DIR="$REPO_ROOT/src/main/antlr4/nl/han/ica/icss/parser"
GRAMMAR_FILE="$GRAMMAR_DIR/ICSS.g4"
PACKAGE_NAME="nl.han.ica.icss.parser"
START_RULE="stylesheet"

OUT_DIR="$REPO_ROOT/target/antlr-manual"
CLASSES_DIR="$OUT_DIR/classes"
INPUT_FILE="level0.icss"

ANTLR_JAR="/home/atakan/Documents/HBO-ICT/antlr-4.13.2-complete.jar"

mkdir -p "$OUT_DIR" "$CLASSES_DIR"
rm -f "$OUT_DIR"/*.java "$OUT_DIR"/*.tokens "$OUT_DIR"/*.interp

java -jar "$ANTLR_JAR" -Xexact-output-dir -o "$OUT_DIR" -package "$PACKAGE_NAME" "$GRAMMAR_FILE"
javac -cp "$ANTLR_JAR" -d "$CLASSES_DIR" "$OUT_DIR"/*.java

java -cp "$CLASSES_DIR:$ANTLR_JAR" org.antlr.v4.gui.TestRig "$PACKAGE_NAME.ICSS" "$START_RULE" -gui "$INPUT_FILE"
