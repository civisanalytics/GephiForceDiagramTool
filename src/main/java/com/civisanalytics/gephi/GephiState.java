package com.civisanalytics.gephi;

import org.openide.util.Lookup;
import org.gephi.project.api.ProjectController;
import org.gephi.graph.api.GraphController;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.preview.api.PreviewController;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;

/**
 * Data structure for all state associated with Gephi project
 */
public class GephiState {
    protected ProjectController projectController;
    protected ImportController importController;
    protected ExportController exportController;
    protected PreviewController previewController;
    protected GraphController graphController;
    protected AttributeColumnsController attrColController;
    protected AppearanceController appearanceController;
    protected AppearanceModel appearanceModel;
    protected GraphModel graphModel;
    protected Graph graph;
    protected Table attrTable;

    /**
     * Initialize Gephi controllers, given a Lookup object
     */
    public GephiState(final Lookup l) {
        projectController = l.lookup(ProjectController.class);
        importController = l.lookup(ImportController.class);
        exportController = l.lookup(ExportController.class);
        previewController = l.lookup(PreviewController.class);
        graphController = l.lookup(GraphController.class);
        attrColController = l.lookup(AttributeColumnsController.class);
        appearanceController = l.lookup(AppearanceController.class);
    }

    /**
     * Initialize state variables for graph and node attributes.
     * <p>
     * This should not be done until data has been loaded into the graph
     * (and may be called repeatedly)
     */
    protected void initializeGraphModel() {
        graphModel = graphController.getGraphModel();
        graph = graphModel.getGraph();
        appearanceModel = appearanceController.getModel();
        attrTable = graphModel.getNodeTable();
    }

}
