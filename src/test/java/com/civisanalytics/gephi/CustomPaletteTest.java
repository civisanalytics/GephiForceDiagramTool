package com.civisanalytics.gephi;

import java.util.Arrays;
import java.awt.Color;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class CustomPaletteTest {

    CustomPalette palette;

    public CustomPaletteTest() throws GephiForceDiagramException {
        palette = new CustomPalette();
    }

    /** Test parsing of valid {@code PaletteSource} strings */
    @Test
    public void testSourceStringParsing() throws GephiForceDiagramException {
        String[] colorSchemeStrings = {"Gephi", "GEPHI", "Colorbrewer", "COLORBREWER"};
        PaletteSource[] colorSchemeEnum = {PaletteSource.GEPHI,
                                           PaletteSource.GEPHI,
                                           PaletteSource.COLORBREWER,
                                           PaletteSource.COLORBREWER};
        for (int i = 0; i < colorSchemeStrings.length; i++) {
            assertEquals("Color scheme source parsed incorrectly: " + colorSchemeStrings[i],
                         CustomPalette.parseSourceString(colorSchemeStrings[i]),
                         colorSchemeEnum[i]);
        }
    }

    /** Test that invalid {@code PaletteSource} string
     *  results in {@code GephiForceDiagramException}
     */
    @Test(expected = GephiForceDiagramException.class)
    public void testSourceStringException() throws GephiForceDiagramException {
        CustomPalette.parseSourceString("foo");
    }

    /** Test parsing of valid {@code PaletteType} strings */
    @Test
    public void testTypeStringParsing() throws GephiForceDiagramException {
        String[] colorSchemeStrings = {"Sequential", "SEQUENTIAL",
                                       "Diverging", "DIVERGING",
                                       "Qualitative", "QUALITATIVE"};
        PaletteType[] colorSchemeEnum = {PaletteType.SEQUENTIAL,
                                           PaletteType.SEQUENTIAL,
                                           PaletteType.DIVERGING,
                                           PaletteType.DIVERGING,
                                           PaletteType.QUALITATIVE,
                                           PaletteType.QUALITATIVE};
        for (int i = 0; i < colorSchemeStrings.length; i++) {
            assertEquals("Color scheme type parsed incorrectly: " + colorSchemeStrings[i],
                         CustomPalette.parseTypeString(colorSchemeStrings[i]),
                         colorSchemeEnum[i]);
        }
    }

    /** Test that invalid {@code PaletteType} string
     *  results in {@code GephiForceDiagramException}
     */
    @Test(expected = GephiForceDiagramException.class)
    public void testTypeStringException() throws GephiForceDiagramException {
        CustomPalette.parseTypeString("foo");
    }

    /** Test that palettes are generated as expected,
     *  given valid inputs
     */
    @Test
    public void testSetColorScheme() throws GephiForceDiagramException {
        PaletteSource[] sources = {PaletteSource.GEPHI,
                                   PaletteSource.GEPHI,
                                   PaletteSource.GEPHI,
                                   PaletteSource.COLORBREWER,
                                   PaletteSource.COLORBREWER,
                                   PaletteSource.COLORBREWER};
        PaletteType[] types = {PaletteType.SEQUENTIAL,
                               PaletteType.DIVERGING,
                               PaletteType.QUALITATIVE,
                               PaletteType.SEQUENTIAL,
                               PaletteType.DIVERGING,
                               PaletteType.QUALITATIVE};
        int[] numbers = {7, 4, 3, 17, 10, 7};
        int[] numColors = {4, 5, 8, 3, 5, 7};
        Color[][] targetColors = {
            {new Color(255, 255, 212), new Color(254, 217, 142),
             new Color(254, 153, 41), new Color(217, 95, 14)},
            {new Color(215, 25, 28), new Color(253, 174, 97),
             new Color(255, 255, 191), new Color(171, 217, 233),
             new Color(44, 123, 182)},
            {new Color(141, 211, 199), new Color(255, 255, 179),
             new Color(190, 186, 218), new Color(251, 128, 114),
             new Color(128, 177, 211), new Color(253, 180, 98),
             new Color(179, 222, 105), new Color(252, 205, 229)},
            {new Color(255, 237, 160), new Color(254, 178, 76),
             new Color(240, 59, 32)},
            {new Color(0, 255, 255), new Color(128, 255, 255),
             new Color(255, 255, 255), new Color(255, 128, 255),
             new Color(255, 0, 255)},
            {new Color(141, 211, 199), new Color(255, 255, 179),
             new Color(190, 186, 218), new Color(251, 128, 114),
             new Color(128, 177, 211), new Color(253, 180, 98),
             new Color(179, 222, 105)}
        };

        for (int i = 0; i < sources.length; i++) {
            palette.setColorScheme(sources[i], types[i], numbers[i], numColors[i]);
            Color[] calculatedColors = palette.getColors();
            String errorMessage = String.format("Incorrect colors calculated "
                                                + "for palette source %s, type %s, "
                                                + "number %d, with %d colors\n"
                                                + "Expected %s, got %s",
                                                sources[i], types[i],
                                                numbers[i], numColors[i],
                                                Arrays.toString(targetColors[i]),
                                                Arrays.toString(calculatedColors));
            assertArrayEquals(errorMessage, calculatedColors, targetColors[i]);
        }
    }

    /** Test that null {@code PaletteSource}
     *  results in {@code GephiForceDiagramException}
     */
    @Test(expected = GephiForceDiagramException.class)
    public void testUnsupportedSource() throws GephiForceDiagramException {
        palette.setColorScheme(null, PaletteType.DIVERGING, 0, 3);
    }

    /** Test that null {@code PaletteType}
     *  results in {@code GephiForceDiagramException}
     */
    @Test(expected = GephiForceDiagramException.class)
    public void testUnsupportedType() throws GephiForceDiagramException {
        palette.setColorScheme(PaletteSource.GEPHI, null, 0, 3);
    }

    /** Test that palette number outside allowed bounds
     *  results in {@code GephiForceDiagramException}
     */
    @Test(expected = GephiForceDiagramException.class)
    public void testUnknownPalette() throws GephiForceDiagramException {
        palette.setColorScheme(PaletteSource.GEPHI, PaletteType.DIVERGING, 100, 3);
    }

    /** Test that number of colors outside allowed bounds
     *  results in {@code GephiForceDiagramException}
     */
    @Test(expected = GephiForceDiagramException.class)
    public void testTooManyColors() throws GephiForceDiagramException {
        palette.setColorScheme(PaletteSource.GEPHI, PaletteType.DIVERGING, 0, 100);
    }

}
