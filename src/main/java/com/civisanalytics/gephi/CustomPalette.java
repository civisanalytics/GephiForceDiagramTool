package com.civisanalytics.gephi;

import java.awt.Color;
import java.util.Arrays;
import org.gephi.utils.PaletteUtils;
import org.jcolorbrewer.ColorBrewer;

enum PaletteSource { GEPHI, COLORBREWER };
enum PaletteType { SEQUENTIAL, DIVERGING, QUALITATIVE };

/**
 * Color schemes for use in creating Gephi force-directed graphs.
 * <p>
 * Includes all of the palettes in {@code org.gephi.utils.PaletteUtils},
 * and also some popular palettes from
 * <a href="http://colorbrewer2.org">Colorbrewer</a>.
 */
public class CustomPalette {

    private static final Color DEFAULT_COLOR = Color.BLACK;
    public static final PaletteSource DEFAULT_SOURCE = PaletteSource.GEPHI;
    public static final PaletteType DEFAULT_TYPE = PaletteType.QUALITATIVE;

    private Color[] colors;

    public CustomPalette(final PaletteSource paletteSource,
                         final PaletteType paletteType,
                         final int paletteNumber,
                         final int numColors) throws GephiForceDiagramException {
        setColorScheme(paletteSource, paletteType, paletteNumber, numColors);
    }

    /**
     * Default palette is first qualitative palette from Gephi,
     * with nine colors
     */
    public CustomPalette() throws GephiForceDiagramException {
        setColorScheme(DEFAULT_SOURCE, DEFAULT_TYPE, 0, 9);
    }

    public Color[] getColors() {
        return colors;
    }

    /**
     * Select a new palette with the chosen parameters
     * <p>
     * Note that each Gephi palette has a maximum number of colors as indicated
     * in the README
     *
     * @param paletteSource  Whether palette is Gephi built-in or from Colorbrewer
     * @param paletteType  Qualitative, Diverging or Sequential
     * @param paletteNumber  Number of palette in chosen set
     * @param numColors  Number of colors to assign to palette
     */
    public void setColorScheme(final PaletteSource paletteSource,
                               final PaletteType paletteType,
                               final int paletteNumber,
                               final int numColors) throws GephiForceDiagramException {
        if (paletteSource == PaletteSource.GEPHI) {
            if (paletteType == PaletteType.QUALITATIVE) {
                try {
                    colors = PaletteUtils.getQualitativePalettes()[paletteNumber].getColors();
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new GephiForceDiagramException("Gephi qualitative palette "
                                                         + paletteNumber + " not found.");
                }
            } else if (paletteType == PaletteType.DIVERGING) {
                try {
                    colors = PaletteUtils.getDivergingPalettes()[paletteNumber].getColors();
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new GephiForceDiagramException("Gephi diverging palette "
                                        + paletteNumber + " not found.");
                }
            } else if (paletteType == PaletteType.SEQUENTIAL) {
                try {
                    // sic
                    colors = PaletteUtils.getSequencialPalettes()[paletteNumber].getColors();
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new GephiForceDiagramException("Gephi sequential palette "
                                        + paletteNumber + " not found.");
                }
            } else {
                throw new GephiForceDiagramException("Unsupported Gephi color palette type: "
                                    + paletteType);
            }

            if (colors.length < numColors) {
                throw new GephiForceDiagramException("Too many colors (" + numColors
                                    + ") requested from palette (with only "
                                    + colors.length + " colors)");
            }
            colors = Arrays.copyOfRange(colors, 0, numColors);

        } else if (paletteSource == PaletteSource.COLORBREWER) {
            boolean colorblindSafe = false;
            if (paletteType == PaletteType.QUALITATIVE) {
                try {
                    ColorBrewer brwr = ColorBrewer
                        .getQualitativeColorPalettes(colorblindSafe)[paletteNumber];
                    colors = brwr.getColorPalette(numColors);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new GephiForceDiagramException("Colorbrewer qualitative palette "
                                        + paletteNumber + " not found.");
                }
            } else if (paletteType == PaletteType.DIVERGING) {
                try {
                    ColorBrewer brwr = ColorBrewer
                        .getDivergingColorPalettes(colorblindSafe)[paletteNumber];
                    colors = brwr.getColorPalette(numColors);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new GephiForceDiagramException("Colorbrewer diverging palette "
                                        + paletteNumber + " not found.");
                }
            } else if (paletteType == PaletteType.SEQUENTIAL) {
                try {
                    ColorBrewer brwr = ColorBrewer
                        .getSequentialColorPalettes(colorblindSafe)[paletteNumber];
                    colors = brwr.getColorPalette(numColors);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new GephiForceDiagramException("Colorbrewer sequential palette "
                                        + paletteNumber + " not found.");
                }
            } else {
                throw new GephiForceDiagramException("Unsupported Colorbrewer color palette type: "
                                    + paletteType);
            }

        } else {
            throw new GephiForceDiagramException("Unsupported color palette source: "
                                                 + paletteSource);
        }

    }

    /**
     * Translate string to static constant representing source of color palette
     */
    public static PaletteSource parseSourceString(final String s)
        throws GephiForceDiagramException {
        if (s.toLowerCase().equals("gephi")) {
            return PaletteSource.GEPHI;
        } else if (s.toLowerCase().equals("colorbrewer")) {
            return PaletteSource.COLORBREWER;
        } else {
            throw new GephiForceDiagramException("Unknown palette source: " + s);
        }
    }

     /**
     * Translate string to static constant representing type of color palette
     */
    public static PaletteType parseTypeString(final String s)
        throws GephiForceDiagramException {
        if (s.toLowerCase().equals("sequential")) {
            return PaletteType.SEQUENTIAL;
        } else if (s.toLowerCase().equals("diverging")) {
            return PaletteType.DIVERGING;
    } else if (s.toLowerCase().equals("qualitative")) {
            return PaletteType.QUALITATIVE;
        } else {
            throw new GephiForceDiagramException("Unknown palette type: " + s);
        }
    }
}
