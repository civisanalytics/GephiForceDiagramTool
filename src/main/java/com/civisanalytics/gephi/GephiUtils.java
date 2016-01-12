package com.civisanalytics.gephi;

import java.io.File;
import java.io.IOException;
import org.gephi.project.api.Workspace;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Column;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.appearance.api.Function;
import org.gephi.graph.api.TextProperties;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.graph.api.Graph;

/**
 * Static methods for interfacing with Gephi resources
 */
public abstract class GephiUtils {

    /**
     * Utility function to get value of float attribute in node table
     *
     * @param n     node with attribute
     * @param attr  name of attribute
     * @return value of attribute for node
     */
    protected static float getFloatAttributeValue(final Node n, final String attr)
        throws GephiForceDiagramException {
        try {
            return (float) n.getAttribute(attr);
        } catch (RuntimeException e) {
            throw new GephiForceDiagramException("Could not extract column "
                                                 + attr + " as float");
        }
    }

    /**
     * Utility function to get value of String attribute in node table
     *
     * @param n     node with attribute
     * @param attr  name of attribute
     * @return value of attribute for node
     */
    protected static String getStringAttributeValue(final Node n, final String attr)
        throws GephiForceDiagramException {
        try {
            return (String) n.getAttribute(attr);
        } catch (RuntimeException e) {
            throw new GephiForceDiagramException("Could not extract column "
                                                 + attr + " as string");
        }
    }

    /**
     * Get a numeric version of a column of interest
     * <p>
     * If column is already numeric and of class {@code c},
     * original column {@code col} is returned.  Otherwise, a
     * new column will be created, coercing the values of
     * {@code col} to the desired type.
     *
     * @param gephiState Gephi state for project
     * @param col        original column
     * @param c          numeric class desired for column
     * @return numeric version of column
     */
    protected static Column getNumericColumn(final GephiState gephiState, final Column col,
                                             final Class c) {
        // Do nothing if column is already of desired type
        if (col.getTypeClass() == c) {
            return col;
        }

        // Check if column has already been converted to numeric
        Column numColumn = gephiState.attrTable.getColumn(col.getTitle() + "_numeric");

        if (numColumn == null) {
            numColumn = gephiState.attrColController.duplicateColumn(gephiState.attrTable,
                                                                     col,
                                                                     col.getTitle() + "_numeric",
                                                                     c);
        }
        return numColumn;
    }

    /**
     * Get ranking function for selected Column
     *
     * @param gephiState Gephi state for project
     * @param col        column to be used for ranking
     * @param c          class of desired ranking function
     * @return ranking function to pass to {@code AppearanceController.transform}
     */
    protected static Function
        getNodeRankingFunction(final GephiState gephiState, final Column col,
                               final Class c) {
        // Gephi sometimes imports numeric columns as strings
        Column rankColumn = getNumericColumn(gephiState, col, Float.class);
        gephiState.appearanceController.forceRankingFunction(new FunctionStub(rankColumn));
        Function rankFunction = gephiState.appearanceModel.getNodeFunction(gephiState.graph,
                                                                           rankColumn, c);
        return rankFunction;
    }

    /**
     * Get partition function for selected Column
     *
     * @param gephiState Gephi state for project
     * @param col        column to be used for partitioning
     * @param c          class of desired partition function
     * @return partition function to pass to {@code AppearanceController.transform}
     */
    protected static Function
        getNodePartitionFunction(final GephiState gephiState,
                                 final Column col, final Class c) {
        gephiState.appearanceController.forcePartitionFunction(new FunctionStub(col));
        Function partitionFunction = gephiState.appearanceModel.getNodeFunction(gephiState.graph,
                                                                                col, c);
        return partitionFunction;
    }

    /**
     * Utility function to get Gephi Dynamic Property
     *
     * @param name   name of property to set
     * @param value  value to assign to property
     * @return Gephi {@code AutoLayout.DynamicProperty} mapping name to value
     */
    protected static AutoLayout.DynamicProperty getDynamicProperty(final String name,
                                                                   final Object value) {
        return AutoLayout.createDynamicProperty(name, value, 0f);
    }

    /**
     * Export rendered graph to PNG file
     *
     * @param gephiState Gephi state for project
     * @param workspace  reference to Gephi Workspace object
     * @param outfile    output file
     */
    protected static void exportFile(final GephiState gephiState,
                                     final int figureWidth,
                                     final int figureHeight,
                                     final Workspace workspace,
                                     final File outfile) throws IOException {
        PNGExporter pngExporter = (PNGExporter) gephiState.exportController.getExporter("png");
        pngExporter.setWorkspace(workspace);
        pngExporter.setHeight(figureHeight);
        pngExporter.setWidth(figureWidth);
        gephiState.exportController.exportFile(outfile, pngExporter);
    }

    /**
     * Ensure that column can be accessed
     *
     * @param gephiState Gephi state for project
     * @param colName    Name of column to be validated
     */
    protected static void validateColumn(final GephiState gephiState,
                                         final String colName) throws GephiForceDiagramException {
        Column c = gephiState.attrTable.getColumn(colName);
        if (c == null) {
            throw new GephiForceDiagramException("Could not access column "
                                                 + colName + " in graph");
        }
    }

    /**
     * Hack to fix LabelAdjust.  See:
     * https://github.com/gephi/gephi/issues/564
     */
    protected static void hackLabels(final Graph g) {
        for (Node n : g.getNodes()) {
            TextProperties tp = n.getTextProperties();
            String label = n.getLabel();
            int labelSize = Math.round(tp.getSize() * 8f);
            tp.setText(label);
            if (!label.equals("")) {
                tp.setDimensions(label.length() * labelSize, labelSize);
            }
        }
    }

}
