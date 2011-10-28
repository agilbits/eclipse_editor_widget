package br.com.agilbits.swt.extension.paginator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.custom.PageInformation;
import org.eclipse.swt.custom.PageInformationFactory;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.agilbits.swt.extension.paginator.LogicalLine;
import br.com.agilbits.swt.extension.paginator.Paginator;

public class PaginatorContinuousLocationTest {
    private PageInformation pageInfo;
    private Paginator paginator;
    private IExtendedStyledTextContent content;
    private Font font;

    @Before
    public void setUp() throws Exception {
        pageInfo = PageInformationFactory.getLetterPage();
        pageInfo.setDrawPageBreaks(false);
        paginator = new Paginator(pageInfo);

        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(20);
        when(content.getParagraphSpacing(anyInt())).thenReturn(0);
        when(content.getLine(anyInt())).thenReturn("a");
        when(content.getRightMargin(anyInt())).thenReturn(100);
        when(content.getBreakDecorationSize(anyInt())).thenReturn(0);
        when(content.shouldMergeWithNext(anyInt())).thenReturn(false);
        when(content.canBreak(anyInt())).thenReturn(true);
        when(content.canBreakAfter(anyInt())).thenReturn(true);
        when(content.getMinimumLinesToFit(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);

        font = new Font(Display.getDefault(), "Courier", 12, SWT.NORMAL);
        paginator.setFont(font);
    }

    @After
    public void tearDown() throws Exception {
        font.dispose();
    }

    @Test
    public void shouldHaveHeightOfOneLinePlusExternalMarginsPlusHalfOfGivenArgumentForEmptyDocument()
            throws Exception {
        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(1);
        when(content.getParagraphSpacing(anyInt())).thenReturn(0);
        when(content.getLine(anyInt())).thenReturn("");
        when(content.getRightMargin(anyInt())).thenReturn(100);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getHeight(22), is(12 + 2 * Paginator.EXTERNAL_MARGIN + 11));
    }

    @Test
    public void shouldHaveHeightOfOneLinePlusExternalMarginsForContentWithOneLine()
            throws Exception {
        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(1);
        when(content.getParagraphSpacing(0)).thenReturn(2);
        when(content.getLine(anyInt())).thenReturn("a");
        when(content.getRightMargin(anyInt())).thenReturn(100);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getHeight(0), is(36 + 2 * Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldHaveHeightOfTwoLinesPlusExternalMarginsForContentWithTwoLines()
            throws Exception {
        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(2);
        when(content.getParagraphSpacing(0)).thenReturn(2);
        when(content.getParagraphSpacing(1)).thenReturn(1);
        when(content.getLine(anyInt())).thenReturn("a");
        when(content.getRightMargin(anyInt())).thenReturn(100);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getHeight(0), is(36 + 24 + 2 * Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldHaveHeightOfContentLinesPlusExternalMargins() throws Exception {
        assertThat(paginator.getHeight(0), is(20 * 12 + 2 * Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldHaveHeightOfContentLinesWithParagraphSpacingPlusExternalMargins()
            throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.layoutChanged();

        assertThat(paginator.getHeight(0), is(20 * 24 + 2 * Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldHaveHeightOfContentLinesWithParagraphSpacingEvenForFirstContentOfOtherPages()
            throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(3);
        paginator.layoutChanged();

        assertThat(paginator.getHeight(0), is(20 * 48 + 2 * Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldReturnFirstTextLayoutAtCorrectLocation() throws Exception {
        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN);
        assertThat(getLocation(0), is(upperLeftCorner));
    }

    @Test
    public void shouldReturnFirstTextLayoutAtCorrectLocationWithParagraphSpacing() throws Exception {
        when(content.getParagraphSpacing(0)).thenReturn(1);
        paginator.layoutChanged();

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 12);
        assertThat(getLocation(0), is(upperLeftCorner));
    }

    @Test
    public void shouldReturnSecondTextLayoutAtCorrectLocation() throws Exception {
        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 12);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldReturnFirstTextLayoutAtCorrectLocationRegardlessOfPageMargins()
            throws Exception {
        when(content.getLeftMargin(0)).thenReturn(20);
        pageInfo.setTopMargin(1.0);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN + 20, Paginator.EXTERNAL_MARGIN);
        assertThat(getLocation(0), is(upperLeftCorner));
    }

    @Test
    public void shouldNotConsiderPageBreakForContentOnSecondPage() throws Exception {
        pageInfo = new PageInformation(new Point(72, 12), 8, 1);

        when(content.getLineCount()).thenReturn(2);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 12);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldNotUseFullPageHeightForContentOnSecondPage() throws Exception {
        pageInfo = new PageInformation(new Point(72, 16), 8, 1);

        when(content.getLineCount()).thenReturn(2);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 12);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldNotConsiderTopMarginForContentOnSecondPage() throws Exception {
        when(content.getLineCount()).thenReturn(2);

        pageInfo = new PageInformation(new Point(72, 16), 8, 2);
        pageInfo.setTopMargin(1.0);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 12);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldReturnTextLayoutLocationOnGetLinePixel() throws Exception {
        pageInfo.setTopMargin(1.0);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.getLinePixel(0), is(Paginator.EXTERNAL_MARGIN));
        assertThat(paginator.getLinePixel(1), is(Paginator.EXTERNAL_MARGIN + 12));
    }

    @Test
    public void shouldReturnBottomPixelOfLastLineOnGetLinePixelWithLineCount() throws Exception {
        pageInfo.setTopMargin(1.0);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.getLinePixel(20), is(10 + 20 * 12));
    }

    private Point getLocation(int lineIndex) {
        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
        return paginator.getLocation(logicalLine);
    }
}
