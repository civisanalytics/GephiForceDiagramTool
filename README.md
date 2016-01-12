# GephiForceDiagramTool

A lightweight command-line utility for generating Gephi diagrams using the
force-directed layout algorithm.

This utility is designed to simplify the process of creating attractive force-directed graph
diagrams using [Gephi](http://gephi.org).  Currently, Gephi provides very flexibly functionality
for diagram creation, but it can only be accessed via the GUI, which can involve tedious manual work,
or programmatically via the GephiToolkit Java library, for which documentation is limited.

GephiForceDiagramTool packages an important subset of Gephi's force diagram creation functionality in a
simple and portable wrapper.

## Installation

### Compilation

To build the project with the gradle build manager:

1. Clone the repository
2. Execute `./gradlew build` in the top-level directory of the repository in order to comple the source and create the jar file `build/libs/GephiForceDiagramTool.jar`

### Running Tests

#### David Copperfield

GephiForceDiagramTool is configured to generate test diagrams using a set of adjective-noun adjacency relationships
from the novel David Copperfield, [distributed by M. E. J. Newman](http://www-personal.umich.edu/~mejn/netdata/).
In order to run GephiForceDiagramTool on these test datasets, run either

```
./gradlew runFATest
```

or

```
./gradlew runFA2Test
```

at the command line.

These tasks will automatically download the test data from the web (so you should be connected to the internet), and will
create the files `data/adjnoun_fa.png` and `data/adjnoun_fa2.png`, respectively.  The two network diagrams differ in the choice
of layout algorithm (ForceAtlas vs. ForceAtlas2) and the color scheme.

#### Twitter Climate Change

A third test dataset, consisting of the graph of friend-follower
relationships between 4906 Twitter accounts active in the discussion of
climate change, is also included in the repository.  To obtain this data and generate a sample graph:

1. Execute `git lfs pull` in the top-level directory of the repository, in order to get the GML file `data/climatechange.gml` from git Large File Storage.  (You will need the [git-lfs client extension](https://git-lfs.github.com/))
2. Execute `./gradlew runClimateTest` to generate the diagram `data/climatechange.png`

### Running at the command line

Examples of command line usage can be found in the script `scripts/gml2gephi.sh`, and in the gradle `runFATest` task defined in `settings.gradle`.

To run the `gml2gephi.sh` script or another script that invokes CreateGephiForceDiagram by means of the java
executable, first unpack the GephiForceDiagram JAR file and its dependencies from the `build/distributions/GephiForceDiagramTool.tar` file:

```
mkdir runtime
tar xf build/distributions/GephiForceDiagramTool.tar -C runtime
```

Then, modify the `CLASSPATH` in `gml2gephi.sh` appropriately
(so that it includes `GephiForceDiagramTool.jar` as well as the supporting libraries
for `jColorbrewer`, `argparse4j`, `commons-math3` and `gephi-toolkit`)
before running `bash gml2gephi.sh <INFILE> <OUTFILE>`

## Options

### Program help

* `--help` (`-h`): Show help message and exit

### Input/Output

* `--gml_input_file` (`-gml`): Specify input file in GML format

* `--png_output_file` (`-png`): Specify output file in PNG format

### General Layout Options

* `--figure_height` (`-fight`): Height of output figure in pixels

* `--figure_width` (`figwd`): Width of output figure in pixels

* `--min_label_size` (`-minls`): Minimum size for labels

* `--max_label_size` (`-maxls`): Maximum size for labels

* `--min_node_size` (`-minns`): Minimum size for nodes

* `--max_node_size` (`-maxns`): Maximum size for nodes

* `--label_adjust` (`-ladj`): Whether to adjust  layout  to  prevent overlapping labels

* `--label_adjust_time_seconds` (`-lat`): Number of seconds to spend on label adjust

* `--edge_opacity` (`-eo`): Edge opacity for image rendering

### ForceAtlas (force-directed layout) options

* `--layout_algorithm` (`-la`): Layout algorithm to use (force_atlas or force_atlas2)

* `--layout_time_seconds` (`-t`): Number of seconds to spend on force-directed layout

* `--gravity` (`-g`): Gravity parameter for force_atlas/force_atlas2

* `--scaling_ratio` (`-sr`): Scaling ratio parameter for force_atlas/force_atlas2

* `--jitter_tolerance` (`-jt`): Jitter tolerance parameter for force_atlas2

* `--inertia` (`-i`): Inertia parameter for force_atlas

* `--speed` (`-s`): Speed parameter for force_atlas

### Data selection and filtering options

* `--node_size_column` (`-nscol`): Column of data to use for sizing nodes

* `--node_color_column` (`-ncc`): Column of data to use for assigning color to nodes

* `--node_color_type` (`-nct`): Whether to assign colors to discrete groups (partition) or using a continuous scale (ranking)

* `--node_label_column` (`-nlc`): Column of data to use for node labels

* `--label_percentile` (`-labpct`): Percentile cutoff in size ranking for nodes to be labled

* `--degree_filter` (`-df`): Minimum number of connections (degree) for a node not to be filtered out of network

### Color palette options

* `--color_palette_source` (`-cps`): Source of the color palette to be used for the diagram.
  (One of "Gephi" or "Colorbrewer")

* `--color_palette_type` (`-cpt`): Type of color palette. (One of "qualitative",
  "diverging" or "sequential")

* `--color_palette_number` (`-cpn`): (Zero-indexed) number of palette in the given series.

* `--num_colors` (`-nc`): Number of colors to use in constructing the desired palette.

Available color palettes are listed below.  More information on Colorbrewer palettes
is available from the maintainers of the [jColorbrewer](https://github.com/rcsb/colorbrewer) package.

Source       | Type        | Number | Max num colors | Color-blind safe?
------------ | ----------- | ------ | -------------- | -----------------
Gephi        | Qualitative | 0      | 9              | ??
Gephi        | Qualitative | 1      | 9              | ??
Gephi        | Qualitative | 2      | 9              | ??
Gephi        | Qualitative | 3      | 9              | ??
Gephi        | Diverging   | 0      | 5              | ??
Gephi        | Diverging   | 1      | 5              | ??
Gephi        | Diverging   | 2      | 5              | ??
Gephi        | Diverging   | 3      | 5              | ??
Gephi        | Diverging   | 4      | 5              | ??
Gephi        | Sequential  | 0      | 5              | ??
Gephi        | Sequential  | 1      | 5              | ??
Gephi        | Sequential  | 2      | 5              | ??
Gephi        | Sequential  | 3      | 5              | ??
Gephi        | Sequential  | 4      | 5              | ??
Gephi        | Sequential  | 5      | 5              | ??
Gephi        | Sequential  | 6      | 5              | ??
Gephi        | Sequential  | 7      | 5              | ??
Colorbrewer  | Qualitative | 0      | N/A            | No
Colorbrewer  | Qualitative | 1      | N/A            | No
Colorbrewer  | Qualitative | 2      | N/A            | Yes
Colorbrewer  | Qualitative | 3      | N/A            | No
Colorbrewer  | Qualitative | 4      | N/A            | No
Colorbrewer  | Qualitative | 5      | N/A            | No
Colorbrewer  | Qualitative | 6      | N/A            | No
Colorbrewer  | Qualitative | 7      | N/A            | No
Colorbrewer  | Diverging   | 0      | N/A            | Yes
Colorbrewer  | Diverging   | 1      | N/A            | Yes
Colorbrewer  | Diverging   | 2      | N/A            | Yes
Colorbrewer  | Diverging   | 3      | N/A            | Yes
Colorbrewer  | Diverging   | 4      | N/A            | Yes
Colorbrewer  | Diverging   | 5      | N/A            | No
Colorbrewer  | Diverging   | 6      | N/A            | Yes
Colorbrewer  | Diverging   | 7      | N/A            | No
Colorbrewer  | Diverging   | 8      | N/A            | No
Colorbrewer  | Diverging   | 9      | N/A            | Yes
Colorbrewer  | Diverging   | 10     | N/A            | Yes
Colorbrewer  | Sequential  | 0      | N/A            | Yes
Colorbrewer  | Sequential  | 1      | N/A            | Yes
Colorbrewer  | Sequential  | 2      | N/A            | Yes
Colorbrewer  | Sequential  | 3      | N/A            | Yes
Colorbrewer  | Sequential  | 4      | N/A            | Yes
Colorbrewer  | Sequential  | 5      | N/A            | Yes
Colorbrewer  | Sequential  | 6      | N/A            | Yes
Colorbrewer  | Sequential  | 7      | N/A            | Yes
Colorbrewer  | Sequential  | 8      | N/A            | Yes
Colorbrewer  | Sequential  | 9      | N/A            | Yes
Colorbrewer  | Sequential  | 10     | N/A            | Yes
Colorbrewer  | Sequential  | 11     | N/A            | Yes
Colorbrewer  | Sequential  | 12     | N/A            | Yes
Colorbrewer  | Sequential  | 13     | N/A            | Yes
Colorbrewer  | Sequential  | 14     | N/A            | Yes
Colorbrewer  | Sequential  | 15     | N/A            | Yes
Colorbrewer  | Sequential  | 16     | N/A            | Yes
Colorbrewer  | Sequential  | 17     | N/A            | Yes

## Dependencies

GephiForceDiagramTool is compatible with Java 1.7 and later.

GephiForceDiagramTool uses the following external packages:

* [jColorbrewer](https://github.com/rcsb/colorbrewer)
* [argparse4j](https://argparse4j.github.io/)
* [commons-math3](https://commons.apache.org/proper/commons-math/)
* [gephi-toolkit](https://gephi.org/toolkit/)

## License

GephiForceDiagramTool is released under the [BSD 3-Clause License](LICENSE.txt)

GephiForceDiagramTool is configured with the [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html),
distributed under the [Apaghe License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
