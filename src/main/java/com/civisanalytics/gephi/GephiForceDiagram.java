package com.civisanalytics.gephi;

import static com.civisanalytics.gephi.GephiUtils.getFloatAttributeValue;
import static com.civisanalytics.gephi.GephiUtils.getStringAttributeValue;
import static com.civisanalytics.gephi.GephiUtils.getNumericColumn;
import static com.civisanalytics.gephi.GephiUtils.getNodeRankingFunction;
import static com.civisanalytics.gephi.GephiUtils.getNodePartitionFunction;
import static com.civisanalytics.gephi.GephiUtils.getDynamicProperty;
import static com.civisanalytics.gephi.GephiUtils.exportFile;
import static com.civisanalytics.gephi.GephiUtils.validateColumn;
import static com.civisanalytics.gephi.GephiUtils.hackLabels;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import java.util.ArrayList;
import java.util.List;

import org.openide.util.Lookup;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.project.api.Workspace;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.labelAdjust.LabelAdjust;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;

import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;

import org.gephi.layout.spi.Layout;

import org.gephi.graph.api.Node;
import org.gephi.graph.api.Column;

/**
 * Module for creating a force-directed graph image
 * from a GML-format graph using the
 * <a href="https://gephi.org/toolkit/">gephi-toolkit</a>
 */
public class GephiForceDiagram {

    /**
     * Number of seconds to spend on force-directed layout
     */
    private int layoutTime = 1;
    public int getLayoutTime() { return layoutTime; }
    public void setLayoutTime(final int t) { layoutTime = t; }

    /**
     * Number of seconds to spend adjusting layout for layout readability
     */
    private int labelAdjustTime = 20;
    public int getLabelAdjustTime() { return labelAdjustTime; }
    public void setLabelAdjustTime(final int t) { labelAdjustTime = t; }

    /**
     * Minimum degree for node to be retained in layout
     */
    private int degreeFilter = 0;
    public int getDegreeFilter() { return degreeFilter; }
    public void setDegreeFilter(final int t) { degreeFilter = t; }

    /**
     * Height of figure in pixels
     */
    private int figureHeight = 4096;
    public int getFigureHeight() { return figureHeight; }
    public void setFigureHeight(final int t) { figureHeight = t; }

    /**
     * Width of figure in pixels
     */
    private int figureWidth = 4096;
    public int getFigureWidth() { return figureWidth; }
    public void setFigureWidth(final int t) { figureWidth = t; }

    /**
     * Whether palette is from Gephi or Colorbrewer
     */
    private PaletteSource colorPaletteSource = CustomPalette.DEFAULT_SOURCE;
    public PaletteSource getColorPaletteSource() { return colorPaletteSource; }
    public void setColorPaletteSource(final PaletteSource c) { colorPaletteSource = c; }

    /**
     * Whether palette colors are sequential, diverging, or qualitative
     */
    private PaletteType colorPaletteType = CustomPalette.DEFAULT_TYPE;
    public PaletteType getColorPaletteType() { return colorPaletteType; }
    public void setColorPaletteType(final PaletteType c) { colorPaletteType = c; }

    /**
     * Index of palette in selected set
     */
    private int colorPaletteNumber = 0;
    public int getColorPaletteNumber() { return colorPaletteNumber; }
    public void setColorPaletteNumber(final int c) { colorPaletteNumber = c; }

    /**
     * Number of colors in desired palette
     */
    private int numColors = 9;
    public int getNumColors() { return numColors; }
    public void setNumColors(final int c) { numColors = c; }

    /**
     * Node attribute to use for sizing
     */
    private String nodeSizeColumn = "centrality";
    public String getNodeSizeColumn() { return nodeSizeColumn; }
    public void setNodeSizeColumn(final String c) { nodeSizeColumn = c; }

    /**
     * Node attribute to use for label
     */
    private String nodeLabelColumn = "name";
    public String getNodeLabelColumn() { return nodeLabelColumn; }
    public void setNodeLabelColumn(final String c) { nodeLabelColumn = c; }

    /**
     * Node attribute to use for color
     */
    private String nodeColorColumn = "community";
    public String getNodeColorColumn() { return nodeColorColumn; }
    public void setNodeColorColumn(final String c) { nodeColorColumn = c; }

    /**
     * Whether node coloring is scalar ("ranking") or categorical ("partition")
     */
    private String nodeColorType = "partition";
    public String getNodeColorType() { return nodeColorType; }
    public void setNodeColorType(final String c) { nodeColorType = c; }

    /**
     * Gephi algorithm to use for layout ("force_atlas" | "force_atlas2")
     */
    private String layoutAlgorithm = "force_atlas2";
    public String getayoutAlgorithm() { return layoutAlgorithm; }
    public void setLayoutAlgorithm(final String c) { layoutAlgorithm = c; }

    /**
     * Whether to adjust layout for label readability
     */
    private boolean labelAdjust = false;
    public boolean getLabelAdjust() { return labelAdjust; }
    public void setLabelAdjust(final boolean b) { labelAdjust = b; }

    /**
     * Gravity parameter of force_atlas / force_atlas2
     */
    private double gravity = 1.0;
    public double getGravity() { return gravity; }
    public void setGravity(final double t) { gravity = t; }

    /**
     * Scaling ratio parameter of force_atlas / force_atlas2
     */
    private double scalingRatio = 5.0;
    public double getScalingRatio() { return scalingRatio; }
    public void setScalingRatio(final double t) { scalingRatio = t; }

    /**
     * Jitter tolerance parameter of force_atlas2
     */
    private double jitterTolerance = 1.0;
    public double getJitterTolerance() { return jitterTolerance; }
    public void setJitterTolerance(final double t) { jitterTolerance = t; }

    /**
     * Inertia parameter of force_atlas
     */
    private double inertia = 0.1;
    public double getInertia() { return inertia; }
    public void setInertia(final double t) { inertia = t; }

    /**
     * Speed parameter of force_atlas
     */
    private double speed = 1.0;
    public double getSpeed() { return speed; }
    public void setSpeed(final double t) { speed = t; }

    /**
     * Opacity of edges connecting nodes (in percent)
     */
    private double edgeOpacity = 10;
    public double getEdgeOpacity() { return edgeOpacity; }
    public void setEdgeOpacity(final double t) { edgeOpacity = t; }

    /**
     * Minimum percentile (in node sizing column) for a node to be labeled
     */
    private double labelPercentile = 98.0;
    public double getLabelPercentile() { return labelPercentile; }
    public void setLabelPercentile(final double t) { labelPercentile = t; }

    /**
     * Minimum size for node labels
     */
    private float minLabelSize = 0.0f;
    public float getMinLabelSize() { return minLabelSize; }
    public void setMinLabelSize(final float t) { minLabelSize = t; }

    /**
     * Maximum size for node labels (will be auto-sized if set to zero)
     */
    private float maxLabelSize = 0.0f;
    public float getMaxLabelSize() { return maxLabelSize; }
    public void setMaxLabelSize(final float t) { maxLabelSize = t; }

    /**
     * Minimum size for nodes
     */
    private int minNodeSize = 3;
    public float getMinNodeSize() { return minNodeSize; }
    public void setMinNodeSize(final int t) { minNodeSize = t; }

    /**
     * Maximum size for nodes
     */
    private int maxNodeSize = 50;
    public float getMaxNodeSize() { return maxNodeSize; }
    public void setMaxNodeSize(final int t) { maxNodeSize = t; }

    /**
     * State of associated Gephi project
     */
    private GephiState gephiState;
    public GephiState getGephiState() { return gephiState; }
    public void setGephiState(final GephiState gs) { gephiState = gs; }

    /**
     * Number of times to process the graph, removing nodes with
     * degree < {@code degreeFilter}
     */
    private static final int NUM_DEGREE_FILTER_ITERATIONS = 4;

    public GephiForceDiagram() {
        // Initialize controllers
        gephiState = new GephiState(Lookup.getDefault());
    }

    /**
     * Top-level function for creating a force-directed graph diagram
     *
     * @param infile  GML-format input File
     * @param outfile  PNG-format output File
     */
    public void run(final File infile, final File outfile)
        throws GephiForceDiagramException, FileNotFoundException,
               IOException {

        // Initialize project workspace
        gephiState.projectController.newProject();
        Workspace workspace = gephiState.projectController.getCurrentWorkspace();

        // Import file
        Container container = gephiState.importController.importFile(infile);
        if (container == null) {
            throw new FileNotFoundException("Could not open file " + infile);
        }
        container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);

        // Append imported data to GraphAPI
        gephiState.importController.process(container, new DefaultProcessor(), workspace);

        // Get handle to graph
        gephiState.initializeGraphModel();

        // Validate columns needed for layout
        validateColumns();

        // Filter out nodes with very few connections
        degreeFilterNodes();

        // Do layout
        executeLayout();

        // Do label adjust
        if (labelAdjust) {
            adjustLabels();
        }

        // Output image file
        exportFile(gephiState, figureWidth, figureHeight, workspace, outfile);
    }

    /**
     * Transform node attributes to set sizes, colors, and labels;
     * apply force-directed layout algorithm
     */
    protected void executeLayout()
        throws GephiForceDiagramException {

        // Layout for N seconds
        AutoLayout autoLayout = new AutoLayout(layoutTime, TimeUnit.SECONDS);
        autoLayout.setGraphModel(gephiState.graphModel);

        // Initialize ForceAtlas layout
        initializeLayout(autoLayout);

        // Set node sizes
        resizeNodes();

        // Assign node colors
        colorNodes();

        // Insert node labels
        labelNodes();

        autoLayout.execute();
    }

    /**
     * Set parameters of layout algorithm to be applied to graph
     *
     * @param autoLayout  Gephi {@code AutoLayout} to be applied in
     *                    laying out graph
     */
    protected void initializeLayout(final AutoLayout autoLayout)
        throws GephiForceDiagramException {

        if (layoutAlgorithm.equals("force_atlas2")) {
            Layout faLayout = new ForceAtlas2Builder().buildLayout();

            // AdjustSizes always enabled
            List<AutoLayout.DynamicProperty> properties
                = new ArrayList<AutoLayout.DynamicProperty>();
            properties.add(getDynamicProperty("ForceAtlas2.adjustSizes.name", Boolean.TRUE));

            // Set other parameters with selected values
            double[] params = {gravity, scalingRatio, jitterTolerance};
            String[] paramNames = {"ForceAtlas2.gravity.name",
                                   "ForceAtlas2.scalingRatio.name",
                                   "ForceAtlas2.jitterTolerance.name"};
            for (int i = 0; i < params.length; i++) {
                if (params[i] > 0) {
                    properties.add(getDynamicProperty(paramNames[i], params[i]));
                }
            }
            autoLayout.addLayout(faLayout, 1.0f,
                                 properties.toArray(new AutoLayout
                                                    .DynamicProperty[properties.size()]));

        } else if (layoutAlgorithm.equals("force_atlas")) {
            Layout faLayout = new ForceAtlasLayout(null);

            // AdjustSizes always enabled
            List<AutoLayout.DynamicProperty> properties
                = new ArrayList<AutoLayout.DynamicProperty>();
            properties.add(getDynamicProperty("ForceAtlas.adjustSizes.name", Boolean.TRUE));

            // Reimplement scaling ratio for ForceAtlas: set attraction strength to default,
            //   and repulsion strength to a multiple of that
            double attraction = 10.0;
            properties.add(getDynamicProperty("ForceAtlas.attractionStrength.name",
                                               attraction));
            double repulsion = attraction * 10.0 * scalingRatio;
            properties.add(getDynamicProperty("ForceAtlas.repulsionStrength.name",
                                               repulsion));

            // Set other parameters with selected values
            double[] params = {gravity, speed, inertia};
            String[] paramNames = {"ForceAtlas.gravity.name",
                                   "ForceAtlas.speed.name",
                                   "ForceAtlas.inertia.name"};
            for (int i = 0; i < params.length; i++) {
                if (params[i] > 0) {
                    properties.add(getDynamicProperty(paramNames[i], params[i]));
                }
            }
            autoLayout.addLayout(faLayout, 1.0f,
                                 properties.toArray(new AutoLayout
                                                    .DynamicProperty[properties.size()]));

        } else {
            throw new GephiForceDiagramException("Unknown layout algorithm: " + layoutAlgorithm);
        }
    }

    /**
     * Assign node attributes for labeling
     */
    protected void labelNodes()
        throws GephiForceDiagramException {
        // Find top N nodes to label, and assign labels
        Column sizeColumn = gephiState.attrTable.getColumn(nodeSizeColumn);
        String nodeSizeColumnAttr = getNumericColumn(gephiState,
                                                     sizeColumn,
                                                     Float.class).getTitle();
        double cutoff = Double.NEGATIVE_INFINITY;
        if (labelPercentile > 0) {
            double[] rankvals = new double[gephiState.graph.getNodeCount()];
            int k = 0;
            for (Node n : gephiState.graph.getNodes()) {
                rankvals[k] = getFloatAttributeValue(n, nodeSizeColumnAttr);
                k++;
            }
            cutoff = new Percentile().evaluate(rankvals, labelPercentile);
        }

        for (Node n : gephiState.graph.getNodes()) {
            String label = getStringAttributeValue(n, getNodeLabelColumn());
                if (getFloatAttributeValue(n, nodeSizeColumnAttr) > cutoff
                && label != null) {
                n.setLabel(label);
            } else {
                n.setLabel("");
            }
        }

        // If label size not set, size proportionally with nodes
        float effectiveMinLabelSize = minLabelSize;
        float effectiveMaxLabelSize = maxLabelSize;
        if (minLabelSize <= 0 || maxLabelSize <= 0) {
            effectiveMinLabelSize = minNodeSize / 5f;
            effectiveMaxLabelSize = maxNodeSize / 5f;
        }

        // Rank label size
        Function labelSizeRanking = getNodeRankingFunction(gephiState, sizeColumn,
                                                           RankingLabelSizeTransformer.class);
        RankingLabelSizeTransformer labelSizeTransformer
            = (RankingLabelSizeTransformer) labelSizeRanking.getTransformer();
        labelSizeTransformer.setMinSize(effectiveMinLabelSize);
        labelSizeTransformer.setMaxSize(effectiveMaxLabelSize);
        gephiState.appearanceController.transform(labelSizeRanking);

        // Gephi has some weird bug such that label sizing will not work without this function
        // (which sets bounding box for label text to non-zero
        hackLabels(gephiState.graph);

        // Set 'show labels' option in Preview - and disable node size influence on text size
        PreviewModel previewModel = gephiState.previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, edgeOpacity);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE,
                                              Boolean.FALSE);
    }

    /**
     * Assign node attributes for sizing
     */
    protected void resizeNodes() {
        Column sizeColumn = gephiState.attrTable.getColumn(nodeSizeColumn);
        Function sizeRanking = getNodeRankingFunction(gephiState, sizeColumn,
                                                      RankingNodeSizeTransformer.class);

        RankingNodeSizeTransformer sizeTransformer
            = (RankingNodeSizeTransformer) sizeRanking.getTransformer();
        sizeTransformer.setMinSize(minNodeSize);
        sizeTransformer.setMaxSize(maxNodeSize);
        gephiState.appearanceController.transform(sizeRanking);
    }

    /**
     * Assign node attributes for color
     */
    protected void colorNodes()
        throws GephiForceDiagramException {
        // Node Color - Partition or Ranking
        CustomPalette categorialPalette;
        categorialPalette = new CustomPalette();
        categorialPalette.setColorScheme(colorPaletteSource,
                                         colorPaletteType,
                                         colorPaletteNumber,
                                         numColors);
        Color[] colors = categorialPalette.getColors();

        Column nodeColorAttrColumn = gephiState.attrTable.getColumn(nodeColorColumn);
        if (nodeColorType.toLowerCase().equals("ranking")) {
            // Rank by some chosen column and assign colors
            Function continuousFunction
                = getNodeRankingFunction(gephiState, nodeColorAttrColumn,
                                         RankingElementColorTransformer.class);

            // Select colors as extremes of range
            RankingElementColorTransformer rankingColorTransformer =
                (RankingElementColorTransformer) continuousFunction.getTransformer();

            rankingColorTransformer.setColors(new Color[]{colors[0],
                                                          colors[colors.length - 1]});
            gephiState.appearanceController.transform(continuousFunction);
        } else if (nodeColorType.toLowerCase().equals("partition")) {
            // Partition by group and assign colors
            Function categoricalFunction
                = getNodePartitionFunction(gephiState, nodeColorAttrColumn,
                                           PartitionElementColorTransformer.class);
            Partition partition = ((PartitionFunction) categoricalFunction).getPartition();
            if (partition.size() > colors.length) {
                System.err.println("Only " + colors.length + " colors in colormap for "
                                   + partition.size() + " groups; some colors will be re-used.");
            }
            Color[] rotateColors = new Color[partition.size()];
            for (int i = 0; i < partition.size(); i++) {
                rotateColors[i] = colors[i % colors.length];
            }
            partition.setColors(rotateColors);
            gephiState.appearanceController.transform(categoricalFunction);
        } else {
            throw new GephiForceDiagramException("nodeColorType must be specified as "
                                                 + "either 'partition' or 'ranking'");
        }
    }

    /**
     * Filter graph to remove nodes with degree <
     * {@code degreeFilter}
     */
    protected void degreeFilterNodes() {
        if (degreeFilter < 1) {
            System.err.println("Skipping filtering based on degree");
            return;
        }

        System.err.println("Before filtering based on degree:");
        System.err.println("  Nodes: " + gephiState.graph.getNodeCount()
                           + " Edges: " + gephiState.graph.getEdgeCount());

        for (int k = 0; k < NUM_DEGREE_FILTER_ITERATIONS; k++) {
            List<Node> toRemove = new ArrayList<Node>();
            for (Node n : gephiState.graph.getNodes()) {
                if (gephiState.graph.getDegree(n) < degreeFilter) {
                    toRemove.add(n);
                }
            }
            if (toRemove.size() == 0) {
                break;
            }
            for (Node n : toRemove) {
                gephiState.graph.removeNode(n);
            }
        }

        System.err.println("After filtering:");
        System.err.println("  Nodes: " + gephiState.graph.getNodeCount()
                           + " Edges: " + gephiState.graph.getEdgeCount());
    }

    /**
     * Adjust layout to make labels more readable
     */
    protected void adjustLabels() {
        AutoLayout autoLayout = new AutoLayout(labelAdjustTime, TimeUnit.SECONDS);
        autoLayout.setGraphModel(gephiState.graphModel);
        LabelAdjust laLayout = new LabelAdjust(null);
        autoLayout.addLayout(laLayout, 1.0f);
        autoLayout.execute();
    }

    /**
     * Ensure that all necessary columns can be accessed
     */
    protected void validateColumns() throws GephiForceDiagramException {
        validateColumn(gephiState, nodeSizeColumn);
        validateColumn(gephiState, nodeColorColumn);
        validateColumn(gephiState, nodeLabelColumn);
    }

}
