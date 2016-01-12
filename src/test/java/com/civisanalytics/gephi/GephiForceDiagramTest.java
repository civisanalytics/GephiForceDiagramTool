package com.civisanalytics.gephi;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.ArgumentCaptor;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Table;
import org.gephi.appearance.api.Function;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import static com.civisanalytics.gephi.GephiUtils.getNodeRankingFunction;
import static com.civisanalytics.gephi.GephiUtils.getNodePartitionFunction;
import static com.civisanalytics.gephi.GephiUtils.getFloatAttributeValue;
import static com.civisanalytics.gephi.GephiUtils.getStringAttributeValue;
import static com.civisanalytics.gephi.GephiUtils.getNumericColumn;

public class GephiForceDiagramTest {
    int numConnectedNodes = 100;
    int numOutliers = 10;

    GephiForceDiagram gephiDiagram;
    Lookup lookup;
    Graph graph;
    GraphModel graphModel;
    Table attrTable;

    /**
     * Create a sample {@code GephiForceDiagram} object for use in testing
     * each of its methods.
     * <p>
     * Sample graph contains 100 nodes that are fully-
     * connected to one another, and an additional 10 "outlier" nodes that
     * are only connected to three nodes in the first set.
     */
    public GephiForceDiagramTest() {
        // Initialize object
        gephiDiagram = new GephiForceDiagram();

        // Get Gephi globals
        lookup = Lookup.getDefault();
        ProjectController projectController = lookup.lookup(ProjectController.class);
        projectController.newProject();
        GraphController graphController = lookup.lookup(GraphController.class);

        // Set up graph for tests
        graphModel = graphController.getGraphModel();
        graph = graphModel.getGraph();
        graph.clear();

        // Add node attributes
        AttributeColumnsController attrColController
            = lookup.lookup(AttributeColumnsController.class);
        attrTable = graphModel.getNodeTable();
        attrColController.addAttributeColumn(attrTable, "float_column", Float.class);
        attrColController.addAttributeColumn(attrTable, "string_column", String.class);

        // Set up fully-connected directed graph
        GraphFactory graphFactory = graphModel.factory();
        Node[] nodes = new Node[numConnectedNodes];
        for (int i = 0; i < numConnectedNodes; i++) {
            String id = String.valueOf(i);
            nodes[i] = graphFactory.newNode(id);
            nodes[i].setAttribute("float_column", (float) i);
            nodes[i].setAttribute("string_column", id);
            graph.addNode(nodes[i]);
        }
        for (int i = 0; i < numConnectedNodes; i++) {
            for (int j = 0; j < numConnectedNodes; j++) {
                if (i != j) {
                    graph.addEdge(graphFactory.newEdge(nodes[i], nodes[j]));
                }
            }
        }

        // Add outliers to filter by degree
        Node[] outliers = new Node[numOutliers];
        for (int i = 0; i < numOutliers; i++) {
            String id = String.valueOf(i + numConnectedNodes);
            outliers[i] = graphFactory.newNode(id);
            outliers[i].setAttribute("float_column", (float) i
                                     + numConnectedNodes);
            outliers[i].setAttribute("string_column", id);
            graph.addNode(outliers[i]);
        }
        // Add to graph with degree 3
        for (int i = 0; i < numOutliers; i++) {
            for (int j = 0; j < 3; j++) {
                graph.addEdge(graphFactory.newEdge(outliers[i], nodes[j]));
                graph.addEdge(graphFactory.newEdge(nodes[j], outliers[i]));
            }
        }

        // Set layout configuration variables
        gephiDiagram.setLayoutTime(2);
        gephiDiagram.setLayoutAlgorithm("force_atlas2");
        gephiDiagram.setLabelAdjust(true);
        gephiDiagram.setLabelAdjustTime(1);
        gephiDiagram.setNodeSizeColumn("float_column");
        gephiDiagram.setNodeLabelColumn("string_column");
        gephiDiagram.setNodeColorColumn("string_column");
        gephiDiagram.setNodeColorType("partition");
        gephiDiagram.setLabelPercentile(50.0);
        gephiDiagram.setGravity(2.0);
        gephiDiagram.setScalingRatio(10.0);
        gephiDiagram.setJitterTolerance(2.0);
        gephiDiagram.setNumColors(8);

        // Initialize graph state
        gephiDiagram.getGephiState().initializeGraphModel();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test that string and numeric attributes of nodes can be accessed
     */
    @Test
    public void testGetAttributeValues() throws GephiForceDiagramException {
        float delta = 0.00001f;
        for (Node n : graph.getNodes()) {
            float f = getFloatAttributeValue(n, "float_column");
            assertEquals("Incorrect float value returned",
                         f, Float.parseFloat((String) n.getId()), delta);
            String s = getStringAttributeValue(n, "string_column");
            assertEquals("Incorrect string value returned",
                         s, n.getId());
        }
    }

    /**
     * Test that Gephi ranking functions for attributes can be accessed
     */
    @Test
    public void testGetNodeRankingFunction() {
        gephiDiagram.getGephiState().initializeGraphModel();

        Column floatCol = attrTable.getColumn("float_column");
        Column strCol = attrTable.getColumn("string_column");

        Function rankFunc = getNodeRankingFunction(gephiDiagram.getGephiState(),
                                                   floatCol,
                                                   RankingNodeSizeTransformer.class);
        assertTrue("Failed to create ranking function for float column",
                   rankFunc != null);

        rankFunc = getNodeRankingFunction(gephiDiagram.getGephiState(),
                                          strCol,
                                          RankingNodeSizeTransformer.class);
        assertTrue("Failed to create ranking function for string column",
                   rankFunc != null);
    }

    /**
     * Test that Gephi partition functions for attributes can be accessed
     */
    @Test
    public void testGetNodePartitionFunction() {
        gephiDiagram.getGephiState().initializeGraphModel();

        Column floatCol = attrTable.getColumn("float_column");
        Column strCol = attrTable.getColumn("string_column");

        Function rankFunc = getNodePartitionFunction(gephiDiagram.getGephiState(),
                                                     floatCol,
                                                     PartitionElementColorTransformer.class);
        assertTrue("Failed to create partition function for float column",
                   rankFunc != null);

        rankFunc = getNodePartitionFunction(gephiDiagram.getGephiState(),
                                            strCol,
                                            PartitionElementColorTransformer.class);
        assertTrue("Failed to create partition function for string column",
                   rankFunc != null);
    }

    /**
     * Test conversion of non-numeric columns to numeric
     */
    @Test
    public void testGetNumericColumn() {
        gephiDiagram.getGephiState().initializeGraphModel();

        Column floatCol = attrTable.getColumn("float_column");
        Column floatToFloat = getNumericColumn(gephiDiagram.getGephiState(),
                                               floatCol, Float.class);
        assertEquals("Numeric column unnecessarily modified",
                     floatCol, floatToFloat);

        Column strCol = attrTable.getColumn("string_column");
        Column strToFloat = getNumericColumn(gephiDiagram.getGephiState(),
                                             strCol, Float.class);
        assertEquals("Converted column has incorrect name",
                     strCol.getTitle() + "_numeric", strToFloat.getTitle());
        assertTrue("String column not converted to float", strToFloat.isNumber());

        Column strToFloat2 = getNumericColumn(gephiDiagram.getGephiState(),
                                              strCol, Float.class);
        assertEquals("Column converted twice",
                     strToFloat, strToFloat2);
    }

    /**
     * Test filtering of graph by node degree.
     * <p>
     * Outlier nodes should be filtered out, since they have fewer connections.
     */
    @Test
    public void testDegreeFilter() {
        gephiDiagram.setDegreeFilter(10);
        assertEquals("Error setting degree filter parameter",
                     10, gephiDiagram.getDegreeFilter());

        gephiDiagram.getGephiState().initializeGraphModel();

        // Initial graph size
        assertEquals("Unexpected graph size for tests", 110, graph.getNodeCount());

        // Filtered graph size
        gephiDiagram.degreeFilterNodes();
        assertEquals("Error filtering graph by node degree",
                     100, graph.getNodeCount());
    }

    /**
     * Test validation of columns
     */
    @Test
    public void testValidateColumns() throws GephiForceDiagramException {
        gephiDiagram.getGephiState().initializeGraphModel();

        // No exception
        gephiDiagram.setNodeSizeColumn("float_column");
        gephiDiagram.setNodeColorColumn("float_column");
        gephiDiagram.setNodeLabelColumn("string_column");
        gephiDiagram.validateColumns();

        // Exception
        thrown.expect(GephiForceDiagramException.class);
        thrown.expectMessage("Could not access column foo in graph");
        gephiDiagram.setNodeSizeColumn("foo");
        gephiDiagram.validateColumns();
    }

    /**
     * Test that call to force-directed layout algorithm is made correctly
     */
    @Test
    public void testAutoLayout() throws GephiForceDiagramException {
        AutoLayout autoLayout = spy(new AutoLayout(gephiDiagram.getLayoutTime(),
                                                   TimeUnit.SECONDS));
        autoLayout.setGraphModel(graphModel);
        gephiDiagram.initializeLayout(autoLayout);

        // Verify called with ForceAtlas2 parameters
        ArgumentCaptor<AutoLayout.DynamicProperty[]> dplistArg
            = ArgumentCaptor.forClass(AutoLayout.DynamicProperty[].class);
        verify(autoLayout).addLayout(any(Layout.class), eq(1.0f), dplistArg.capture());
        String[] targetNames = new String[] {
            "ForceAtlas2.adjustSizes.name",
            "ForceAtlas2.gravity.name",
            "ForceAtlas2.scalingRatio.name",
            "ForceAtlas2.jitterTolerance.name"
        };
        Object[] targetVals = new Object[] {true, 2.0, 10.0, 2.0};
        for (int i = 0; i < 4; i++) {
            AutoLayout.DynamicProperty dp = dplistArg.getValue()[i];
            assertEquals(targetNames[i], dp.getCanonicalName());
            assertEquals(targetVals[i], dp.getValue(0.0f));
        }
    }

    /**
     * Test that {@code resizeNodes} results in node sizes that increase
     * as the corresponding attribute increases.
     */
    @Test
    public void testResizeNodes() {
        gephiDiagram.resizeNodes();

        // Assert node sizes monotonically increasing
        double prevSize = 0.0;
        for (Node n : graph.getNodes()) {
            double nodeSize = n.size();
            assertTrue("Error resizing nodes", nodeSize > prevSize);
            prevSize = nodeSize;
        }
    }

    /**
     * Test that the eight colors of the chosen palette are all
     * assigned to nodes in the test graph.
     */
    @Test
    public void testColorNodes() throws GephiForceDiagramException {
        gephiDiagram.colorNodes();
        Set colorsSeen = new HashSet<List<Double>>();
        // Assert exactly 8 different colors assigned
        for (Node n : graph.getNodes()) {
            List l = new ArrayList<Double>();
            l.add(n.r());
            l.add(n.g());
            l.add(n.b());
            colorsSeen.add(l);
        }
        assertEquals("Error coloring nodes", 8, colorsSeen.size());
    }

    /**
     * Test that half of all nodes are given a visible label
     * (as specified by {@code labelPercentile})
     */
    @Test
    public void testLabelNodes()
        throws GephiForceDiagramException {
        gephiDiagram.labelNodes();
        // Assert number of labeled nodes = (numConnectedNodes + numOutliers) / 2
        int numLabeled = 0;
        for (Node n : graph.getNodes()) {
            String label = n.getLabel();
            if (label != "") {
                numLabeled++;
            }
        }
        assertEquals("Error assigning node labels",
                     (numConnectedNodes + numOutliers) / 2,
                     numLabeled);
    }

    /**
     * Test that force-directed layout algorithm is working properly, in that
     * less strongly-connected nodes end up closer to the periphery of the figure.
     */
    @Test
    public void testDoLayout() throws GephiForceDiagramException {
        AutoLayout autoLayout = new AutoLayout(gephiDiagram.getLayoutTime(),
                                               TimeUnit.SECONDS);
        autoLayout.setGraphModel(graphModel);
        gephiDiagram.initializeLayout(autoLayout);

        Random rand = new Random();
        // Map all data to circle around origin
        for (Node n : graph.getNodes()) {
            float x = rand.nextFloat() - 0.5f;
            float y = rand.nextFloat() - 0.5f;
            float dist = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            n.setX(10.0f * x / dist);
            n.setY(10.0f * y / dist);
        }

        autoLayout.execute();

        // Assert that fully-connected nodes are closer to origin than outliers
        double delta = 100;
        int i = 0;
        double connectedDistance = 0;
        double outlierDistance = 0;
        for (Node n : graph.getNodes()) {
            float x = n.x();
            float y = n.y();
            if (i < numConnectedNodes) {
                connectedDistance += Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            } else {
                outlierDistance += Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            }
            i++;
        }
        connectedDistance = connectedDistance / numConnectedNodes;
        outlierDistance = outlierDistance / numOutliers;
        assertTrue("Outlier points too close to center after layout",
                   (connectedDistance + delta) < outlierDistance);
    }

    /**
     * Get the smallest layout distance from {@code n0} to any other
     * node in the graph.
     *
     * @param  n0  node from which distance is measured
     * @return shortest distance
     */
    private double getMinDistanceToNode(final Node n0) {
        double minDist = Double.MAX_VALUE;
        float x0 = n0.x();
        float y0 = n0.y();
        for (Node n : graph.getNodes()) {
            float x = n.x();
            float y = n.y();
            double dist = Math.sqrt(Math.pow(x - x0, 2) + Math.pow(y - y0, 2));
            if (dist < minDist && n != n0) {
                minDist = dist;
            }
        }
        return minDist;
    }

    /**
     * Test that {@code adjustLabels} results in more space around
     * a chosen labeled node.
     */
    @Test
    public void testAdjustLabels() throws GephiForceDiagramException {
        // Map all data to line
        float x = 1.0f;
        for (Node n : graph.getNodes()) {
            n.setX(x);
            n.setY(x);
            x++;
        }

        // Get mean distance to largest fully-connected node
        Node n0 = graph.getNode(String.valueOf(numConnectedNodes - 1));
        Node n1 = graph.getNode(String.valueOf(numConnectedNodes - 2));
        double minDist = getMinDistanceToNode(n0);
        assertEquals("Error forcing nodes to overlap",
                     1.4, minDist, 0.1);

        gephiDiagram.labelNodes();
        gephiDiagram.adjustLabels();

        // Assert mean distance increased (to avoid label overlap)
        double delta = 20;
        assertTrue("Label adjust should increase space around labeled nodes",
                   getMinDistanceToNode(n0) > minDist + delta);
    }

}
