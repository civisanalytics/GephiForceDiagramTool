package com.civisanalytics.gephi;
import org.gephi.appearance.api.AttributeFunction;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Element;

/**
 * Minimal implementation of the AttributeFunction interface,
 * including only the Column member variable
 * <p>
 * This class is necessary because the {@code forceRanking}
 * and {@code forcePartition} methods of {@code AppearanceController}
 * require a Function as an argument (even though they only use the
 * information about the {@code Column} to which the
 * {@code Function} applies.
 */
public class FunctionStub implements AttributeFunction {
    Column column;

    public FunctionStub(final Column c) {
        column = c;
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public Class getElementClass() {
        return null;
    }

    @Override
    public Graph getGraph() {
        return null;
    }

    @Override
    public TransformerUI getUI() {
        return null;
    }

    @Override
    public boolean isAttribute() {
        return true;
    }

    @Override
    public boolean isRanking() {
        return false;
    }

    @Override
    public boolean isPartition() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public void transform(final Element e, final Graph g) {
    }

    @Override
    public Transformer getTransformer() {
        return null;
    }
}
