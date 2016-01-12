#!/usr/bin/env bash
# Script to create Gephi network diagram in png format, given gml network as input

JAVA=java
BASEDIR=..
LIBDIR=$BASEDIR/runtime/GephiForceDiagramTool/lib
CLASSPATH="$LIBDIR/GephiForceDiagramTool.jar:$LIBDIR/jcolorbrewer-5.2.jar:$LIBDIR/argparse4j-0.4.4.jar:$LIBDIR/commons-math3-3.4.1.jar:$LIBDIR/gephi-toolkit-0.9.1-all.jar"

$JAVA -cp $CLASSPATH com.civisanalytics.gephi.CreateGephiForceDiagram \
    --gml_input_file "$1" \
    --png_output_file "$2" \
    --layout_algorithm force_atlas2 \
    --gravity 0.5 \
    --scaling_ratio 5.0 \
    --jitter_tolerance 1.5 \
    --layout_time_minutes 1 \
    --edge_opacity 10 \
    --label_adjust \
    --label_percentile 98
