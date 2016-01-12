package com.civisanalytics.gephi;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentType;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Command-line script for creating force-directed graphs with Gephi
 */
public class CreateGephiForceDiagram {

    /**
     * Validator for numeric command-line arguments that must lie in
     * specified range
     */
    protected static class BoundedNumericArgument<T extends Number & Comparable<T>>
        implements ArgumentType<T> {
        T minimum;
        T maximum;
        boolean includesMinimum;
        boolean includesMaximum;

        protected BoundedNumericArgument(final T min, final T max,
                                       final boolean includesMin, final boolean includesMax) {
            minimum = min;
            maximum = max;
            includesMinimum = includesMin;
            includesMaximum = includesMax;
        }

        @Override
        public T convert(final ArgumentParser parser, final Argument arg, final String value)
            throws ArgumentParserException {
            try {
                T n = null;
                // Ensure conversion to correct type
                if (minimum instanceof Float || maximum instanceof Float) {
                    n = (T) new Float(Float.parseFloat(value));
                } else if (minimum instanceof Double || maximum instanceof Double) {
                    n = (T) new Double(Double.parseDouble(value));
                } else if (minimum instanceof Integer || maximum instanceof Integer) {
                    n = (T) new Integer(Integer.parseInt(value));
                } else if (minimum instanceof Long || maximum instanceof Long) {
                    n = (T) new Long(Long.parseLong(value));
                } else if (minimum instanceof Short || maximum instanceof Short) {
                    n = (T) new Short(Short.parseShort(value));
                }

                // Enforce bounds
                if (maximum != null && n.compareTo(maximum) >= 0 && !includesMaximum) {
                    throw new ArgumentParserException(String
                                                      .format("Invalid value %s for argument %s; "
                                                              + "must be less than %s",
                                                              value, arg.textualName(), maximum),
                                                      parser);
                } else if (minimum != null && n.compareTo(minimum) <= 0 && !includesMinimum) {
                    throw new ArgumentParserException(String
                                                      .format("Invalid value %s for argument %s; "
                                                              + "must be greater than %s",
                                                              value, arg.textualName(), minimum),
                                                      parser);
                } else if (maximum != null && n.compareTo(maximum) > 0) {
                    throw new ArgumentParserException(String
                                                      .format("Invalid value %s for argument %s; "
                                                              + "must not be greater than %s",
                                                              value, arg.textualName(), maximum),
                                                      parser);
                } else if (minimum != null && n.compareTo(minimum) < 0) {
                    throw new ArgumentParserException(String
                                                      .format("Invalid value %s for argument %s; "
                                                              + "must not be less than %s",
                                                              value, arg.textualName(), minimum),
                                                      parser);
                }
                return n;
            } catch (NumberFormatException e) {
                throw new ArgumentParserException(String.format("Could not parse value %s for "
                                                                + "argument %s as appropriate "
                                                                + "numeric type",
                                                                value, arg.textualName()),
                                                  parser);
            }
        }
    }

    public static void main(final String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("CreateGephiForceDiagram")
            .defaultHelp(true)
            .description("Create Gephi Force Diagram for Network");

        ArgumentGroup ioGroup = parser.addArgumentGroup("I/O Options");
        ioGroup.addArgument("-gml", "--gml_input_file")
            .required(true)
            .type(Arguments.fileType().verifyCanRead())
            .help("Specify input file in GML format");
        ioGroup.addArgument("-png", "--png_output_file")
            .required(true)
            .type(Arguments.fileType())
            .help("Specify output file in PNG format");

        ArgumentGroup layoutGroup = parser.addArgumentGroup("General Layout Options");
        layoutGroup.addArgument("-fight", "--figure_height")
            .type(new BoundedNumericArgument<Integer>(0, null, false, true)).setDefault(4096)
            .help("Height of output figure in pixels");
        layoutGroup.addArgument("-figwd", "--figure_width")
            .type(new BoundedNumericArgument<Integer>(0, null, false, true)).setDefault(4096)
            .help("Width of output figure in pixels");
        layoutGroup.addArgument("-minls", "--min_label_size")
            .type(new BoundedNumericArgument<Float>(0f, null, true, true)).setDefault(0.0f)
            .help("Minimum size for labels");
        layoutGroup.addArgument("-maxls", "--max_label_size")
            .type(new BoundedNumericArgument<Float>(0f, null, true, true)).setDefault(0.0f)
            .help("Maximum size for labels");
        layoutGroup.addArgument("-minns", "--min_node_size")
            .type(new BoundedNumericArgument<Integer>(0, null, true, true)).setDefault(3)
            .help("Minimum size for nodes");
        layoutGroup.addArgument("-maxns", "--max_node_size")
            .type(new BoundedNumericArgument<Integer>(0, null, true, true)).setDefault(50)
            .help("Maximum size for nodes");
        layoutGroup.addArgument("-ladj", "--label_adjust")
            .action(Arguments.storeTrue())
            .help("Whether to adjust layout to prevent overlapping labels");
        layoutGroup.addArgument("-lat", "--label_adjust_time_seconds")
            .type(new BoundedNumericArgument<Integer>(0, null, true, true)).setDefault(20)
            .help("Number of seconds to spend on label adjust");
        layoutGroup.addArgument("-eo", "--edge_opacity")
            .type(new BoundedNumericArgument<Double>(0.0, 100.0, true, true)).setDefault(10.0)
            .help("Edge opacity for image rendering");

        ArgumentGroup faGroup = parser.addArgumentGroup("ForceAtlas Options");
        faGroup.addArgument("-la", "--layout_algorithm")
            .setDefault("force_atlas2")
            .choices("force_atlas", "force_atlas2")
            .help("Name of layout algorithm to use");
        faGroup.addArgument("-t", "--layout_time_seconds")
            .type(new BoundedNumericArgument<Integer>(0, null, true, true)).setDefault(60)
            .help("Number of seconds to spend doing force-directed layout");
        faGroup.addArgument("-g", "--gravity")
            .type(new BoundedNumericArgument<Double>(0.0, null, true, true)).setDefault(1.0)
            .help("Gravity parameter for force_atlas/force_atlas2");
        faGroup.addArgument("-sr", "--scaling_ratio")
            .type(new BoundedNumericArgument<Double>(0.0, null, true, true)).setDefault(5.0)
            .help("Scaling ratio parameter for force_atlas/force_atlas2");
        faGroup.addArgument("-jt", "--jitter_tolerance")
            .type(new BoundedNumericArgument<Double>(0.0, null, true, true)).setDefault(1.0)
            .help("Jitter tolerance parameter for force_atlas2");
        faGroup.addArgument("-i", "--inertia")
            .type(new BoundedNumericArgument<Double>(0.0, null, true, true)).setDefault(0.1)
            .help("Inertia parameter for force_atlas");
        faGroup.addArgument("-s", "--speed")
            .type(new BoundedNumericArgument<Double>(0.0, null, true, true)).setDefault(1.0)
            .help("Speed parameter for force_atlas");

        ArgumentGroup fieldGroup = parser.addArgumentGroup("Data Selection and Filtering Options");
        fieldGroup.addArgument("-nscol", "--node_size_column")
            .setDefault("centrality")
            .help("Real-valued column of data to use for sizing nodes");
        fieldGroup.addArgument("-ncc", "--node_color_column")
            .setDefault("community")
            .help("Name of column to color nodes");
        fieldGroup.addArgument("-nct", "--node_color_type")
            .setDefault("partition")
            .choices("partition", "ranking")
            .help("Choose either partition or ranking for node color type");
        fieldGroup.addArgument("-nlc", "--node_label_column")
            .setDefault("name")
            .help("Name of column to label nodes");
        fieldGroup.addArgument("-labpct", "--label_percentile")
            .type(new BoundedNumericArgument<Double>(0.0, 100.0, true, true)).setDefault(98.0)
            .help("Percentile cutoff in ranking for nodes to be labeled");
        fieldGroup.addArgument("-df", "--degree_filter")
            .type(new BoundedNumericArgument<Integer>(0, null, true, true)).setDefault(0)
            .help("Minimum number of connections for a node not to be filtered out of network");

        ArgumentGroup colorGroup = parser.addArgumentGroup("Color Palette Options");
        colorGroup.addArgument("-cps", "--color_palette_source")
            .setDefault("gephi")
            .choices("gephi", "colorbrewer")
            .help("Source of color palette");
        colorGroup.addArgument("-cpt", "--color_palette_type")
            .setDefault("qualitative")
            .choices("qualitative", "diverging", "sequential")
            .help("Type of color palette");
        colorGroup.addArgument("-cpn", "--color_palette_number")
            .type(new BoundedNumericArgument<Integer>(0, null, true, true)).setDefault(0)
            .help("Number of color palette in set");
        colorGroup.addArgument("-nc", "--num_colors")
            .type(new BoundedNumericArgument<Integer>(0, null, true, true)).setDefault(9)
            .help("Number of colors in palette");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        Set<String> hashArgs = new HashSet<String>(Arrays.asList(args));
        if (ns.getString("layout_algorithm").equals("force_atlas")) {
            if (hashArgs.contains("--jitter_tolerance") || hashArgs.contains("-jt")) {
                System.err.println("Parameter --jitter_tolerance may not be specified "
                                   + "for force_atlas algorithm");
                System.exit(1);
            }
        } else if (ns.getString("layout_algorithm").equals("force_atlas2")) {
            if (hashArgs.contains("--inertia") || hashArgs.contains("-i")) {
                System.err.println("Parameter --inertia may not be specified "
                                   + "for force_atlas2 algorithm");
                System.exit(1);
            } else if (hashArgs.contains("--speed") || hashArgs.contains("-s")) {
                System.err.println("Parameter --speed may not be specified "
                                   + "for force_atlas2 algorithm");
                System.exit(1);
            }
        }

        GephiForceDiagram fd = initDiagram(ns);
        try {
            fd.run((File) ns.get("gml_input_file"),
                   (File) ns.get("png_output_file"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        // Force exit to work around FA2 destructor hang
        System.err.println("Exiting...");
        System.exit(0);
    }

    /**
     * Initialize GephiForceDiagram with command-line parameters
     *
     * @param ns  Namespace object returned by argparse4j
     * @return initialized GephiForceDiagram object, on which
     *         `run` can be called
     */
    private static GephiForceDiagram initDiagram(final Namespace ns) {

        GephiForceDiagram fd = new GephiForceDiagram();

        fd.setLayoutTime(ns.getInt("layout_time_seconds"));
        fd.setLabelAdjustTime(ns.getInt("label_adjust_time_seconds"));
        fd.setDegreeFilter(ns.getInt("degree_filter"));
        fd.setFigureHeight(ns.getInt("figure_height"));
        fd.setFigureWidth(ns.getInt("figure_width"));
        fd.setNodeSizeColumn(ns.getString("node_size_column"));
        fd.setNodeColorType(ns.getString("node_color_type"));
        fd.setNodeColorColumn(ns.getString("node_color_column"));
        fd.setNodeLabelColumn(ns.getString("node_label_column"));
        fd.setLayoutAlgorithm(ns.getString("layout_algorithm"));
        fd.setLabelPercentile(ns.getDouble("label_percentile"));
        fd.setGravity(ns.getDouble("gravity"));
        fd.setScalingRatio(ns.getDouble("scaling_ratio"));
        fd.setJitterTolerance(ns.getDouble("jitter_tolerance"));
        fd.setSpeed(ns.getDouble("speed"));
        fd.setInertia(ns.getDouble("inertia"));
        fd.setEdgeOpacity(ns.getDouble("edge_opacity"));
        fd.setMinLabelSize(ns.getFloat("min_label_size"));
        fd.setMaxLabelSize(ns.getFloat("max_label_size"));
        fd.setMinNodeSize(ns.getInt("min_node_size"));
        fd.setMaxNodeSize(ns.getInt("max_node_size"));
        fd.setLabelAdjust(ns.getBoolean("label_adjust"));

        try {
            fd.setColorPaletteSource(CustomPalette
                                     .parseSourceString(ns.getString("color_palette_source")));
            fd.setColorPaletteType(CustomPalette
                                   .parseTypeString(ns.getString("color_palette_type")));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        fd.setColorPaletteNumber(ns.getInt("color_palette_number"));
        fd.setNumColors(ns.getInt("num_colors"));

        return fd;
    }

}
