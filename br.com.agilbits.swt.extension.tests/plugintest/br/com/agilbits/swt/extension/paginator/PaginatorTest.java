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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.com.agilbits.swt.extension.paginator.LogicalLine;
import br.com.agilbits.swt.extension.paginator.Paginator;

public class PaginatorTest {
    private Paginator paginator;
    private PageInformation pageInfo;
    private IExtendedStyledTextContent content;
    private Font font;

    @Before
    public void setUp() throws Exception {
        pageInfo = PageInformationFactory.getLetterPage();
        paginator = new Paginator(pageInfo);

        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(40);
        when(content.getParagraphSpacing(anyInt())).thenReturn(0);
        when(content.getLine(anyInt())).thenReturn("a");
        when(content.getRightMargin(anyInt())).thenReturn(100);
        when(content.getLineAtOffset(anyInt())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Integer offset = (Integer) invocation.getArguments()[0];
                if (offset < 0)
                    throw new Exception("Couldn't get line at offset " + offset);
                else if (offset == 0)
                    return 0;
                else if (offset == 79)
                    return 39;
                else
                    return 1 + (offset - 1) / 2;
            }
        });
        when(content.getCharCount()).thenReturn(79);
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
    public void shouldHaveOnePageForEmptyContent() throws Exception {
        when(content.getCharCount()).thenReturn(0);
        when(content.getLineCount()).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getPageCount(), is(1));
    }

    @Test
    public void shouldHaveOnePageForOnePageContent() throws Exception {
        assertThat(paginator.getPageCount(), is(1));
    }

    @Test
    public void shouldHaveOneLetterPageHeightPlusExternalMarginsForEmptyDocumentAndIgnoreExtraArgument()
            throws Exception {
        assertThat(paginator.getHeight(1432), is(11 * 72 + 2 * Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldHaveOneA4PageHeightPlusExternalMarginsForEmptyDocument() throws Exception {
        PageInformation pageInfo = PageInformationFactory.getA4Page();
        paginator.setPageInformation(pageInfo);
        assertThat(paginator.getHeight(0), is((int) (11.7 * 72 + 2 * Paginator.EXTERNAL_MARGIN)));
    }

    @Test
    public void shouldHaveOneLetterPageWidthPlusExternalMarginsForEmptyDocument() throws Exception {
        assertThat(paginator.getWidth(), is((int) Math.round(8.5 * 72) + 2
                * Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldHaveOneA4PageWidthPlusExternalMarginsForEmptyDocument() throws Exception {
        PageInformation pageInfo = PageInformationFactory.getA4Page();
        paginator.setPageInformation(pageInfo);
        assertThat(paginator.getWidth(), is((int) Math.round(8.3 * 72) + 2
                * Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldHaveTwoPagesForTwoPageContent() throws Exception {
        when(content.getLineCount()).thenReturn(80);
        when(content.getCharCount()).thenReturn(159);
        when(content.getLineAtOffset(anyInt())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Integer offset = (Integer) invocation.getArguments()[0];
                if (offset < 0)
                    throw new Exception("Couldn't get line at offset " + offset);
                else if (offset == 0)
                    return 0;
                else if (offset == 159)
                    return 79;
                else
                    return 1 + (offset - 1) / 2;
            }
        });
        paginator.setContent(Display.getDefault(), content);
        assertThat(paginator.getPageCount(), is(2));
    }

    @Test
    public void shouldHaveTwoPagesForOnePageContentPlusEmptyLineOnSecondPage() throws Exception {
        when(content.getLineCount()).thenReturn(67);
        when(content.getLine(anyInt())).thenReturn("");
        when(content.getCharCount()).thenReturn(66);
        when(content.getLineAtOffset(anyInt())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Integer offset = (Integer) invocation.getArguments()[0];
                if (offset < 0)
                    throw new Exception("Couldn't get line at offset " + offset);
                else
                    return offset;
            }
        });
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getPageCount(), is(2));
    }

    @Test
    public void shouldHaveTwoLetterPagesHeightsPlusExternalMarginForTwoPageContent()
            throws Exception {
        when(content.getLineCount()).thenReturn(80);
        when(content.getCharCount()).thenReturn(159);
        when(content.getLineAtOffset(anyInt())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Integer offset = (Integer) invocation.getArguments()[0];
                if (offset < 0)
                    throw new Exception("Couldn't get line at offset " + offset);
                else if (offset == 0)
                    return 0;
                else if (offset == 159)
                    return 79;
                else
                    return 1 + (offset - 1) / 2;
            }
        });
        paginator.setContent(Display.getDefault(), content);
        assertThat(paginator.getHeight(0), is(2 * (11 * 72 + Paginator.EXTERNAL_MARGIN)
                + Paginator.EXTERNAL_MARGIN));
    }

    @Test
    public void shouldHaveTwoPagesForOnePageContentPlusParagraphSpacing() throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);
        assertThat(paginator.getPageCount(), is(2));
    }

    @Test
    public void shouldConsiderPushedContentsForPageCount() throws Exception {
        pageInfo = new PageInformation(new Point(72, 20), 8.5, 11);
        paginator.setPageInformation(pageInfo);

        when(content.getLineCount()).thenReturn(486);
        when(content.getCharCount()).thenReturn(485 * 2 + 1);
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        when(content.getLineAtOffset(anyInt())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Integer offset = (Integer) invocation.getArguments()[0];
                if (offset < 0)
                    throw new Exception("Couldn't get line at offset " + offset);
                else if (offset == 0)
                    return 0;
                else if (offset == 485 * 2 + 1)
                    return 485;
                else
                    return 1 + (offset - 1) / 2;
            }
        });
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getPageCount(), is(54));
    }

    @Test
    public void shouldHaveTwoPagesForOnePageContentPlusBigTopMargin() throws Exception {
        pageInfo.setTopMargin(6.0);
        paginator.setPageInformation(pageInfo);
        assertThat(paginator.getPageCount(), is(2));
    }

    @Test
    public void shouldHaveTwoPagesForOnePageContentPlusBigBottomMargin() throws Exception {
        pageInfo.setBottomMargin(6.0);
        paginator.setPageInformation(pageInfo);
        assertThat(paginator.getPageCount(), is(2));
    }

    @Test
    public void shouldHaveTwoPagesForOneWrappedPageContent() throws Exception {
        when(content.getLine(anyInt())).thenReturn("a a");
        when(content.getLeftMargin(anyInt())).thenReturn(0);
        when(content.getRightMargin(anyInt())).thenReturn(20);
        paginator.setContent(Display.getDefault(), content);
        assertThat(paginator.getPageCount(), is(2));
    }

    @Test
    public void shouldReturnATextLayoutForALineIndex() throws Exception {
        when(content.getLineCount()).thenReturn(2);
        when(content.getLine(0)).thenReturn("a a");
        when(content.getLine(1)).thenReturn("a");
        paginator.setContent(Display.getDefault(), content);
        assertThat(paginator.getLogicalLine(0).getTextLayout().getText(), is("a a"));
        assertThat(paginator.getLogicalLine(1).getTextLayout().getText(), is("a"));
    }

    @Test
    public void shouldReturnFirstTextLayoutAtCorrectBaseLocation() throws Exception {
        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN);
        assertThat(getLocation(0), is(upperLeftCorner));
    }

    @Test
    public void shouldReturnSecondTextLayoutAtCorrectBaseLocation() throws Exception {
        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 12);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldReturnFirstTextLayoutAtCorrectBaseLocationWithMargins() throws Exception {
        when(content.getLeftMargin(0)).thenReturn(20);
        pageInfo.setTopMargin(1.0);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN + 20,
                                          Paginator.EXTERNAL_MARGIN + 72);
        assertThat(getLocation(0), is(upperLeftCorner));
    }

    @Test
    public void shouldReturnSecondTextLayoutAtCorrectLocationWithMargins() throws Exception {
        when(content.getLeftMargin(1)).thenReturn(20);
        when(content.getParagraphSpacing(1)).thenReturn(1);
        pageInfo.setTopMargin(1.0);
        paginator.setPageInformation(pageInfo);
        paginator.layoutChanged();

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN + 20,
                                          Paginator.EXTERNAL_MARGIN + 72 + 12 + 12);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldPushTextLayoutToNextPageIfDoesntFitInPage() throws Exception {
        pageInfo = new PageInformation(new Point(72, 12), 8, 1);
        pageInfo.setDrawPageBreaks(true);

        when(content.getLineCount()).thenReturn(2);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 12
                + Paginator.EXTERNAL_MARGIN);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldNotPushTextLayoutToNextPageIfTextFitsInPageWithBottomMargin()
            throws Exception {
        pageInfo = new PageInformation(new Point(72, 12), 8, 3);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setBottomMargin(1);

        when(content.getLineCount()).thenReturn(6);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, 2
                * (Paginator.EXTERNAL_MARGIN + 36) + Paginator.EXTERNAL_MARGIN);
        assertThat(getLocation(4), is(upperLeftCorner));
    }

    @Test
    public void shouldUseFullPageHeightIfPushedContent() throws Exception {
        pageInfo = new PageInformation(new Point(72, 16), 8, 1);
        pageInfo.setDrawPageBreaks(true);

        when(content.getLineCount()).thenReturn(2);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 16
                + Paginator.EXTERNAL_MARGIN);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldPushAfterTopMargin() throws Exception {
        pageInfo = new PageInformation(new Point(72, 16), 8, 2);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);

        when(content.getLineCount()).thenReturn(2);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 32
                + Paginator.EXTERNAL_MARGIN + 16);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldUseFullPageHeightConsideringBottomMargin() throws Exception {
        pageInfo = new PageInformation(new Point(72, 16), 8, 2);
        pageInfo.setBottomMargin(1.0);
        pageInfo.setDrawPageBreaks(true);

        when(content.getLineCount()).thenReturn(2);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 32
                + Paginator.EXTERNAL_MARGIN);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldPushContentConsideringBottomMargin() throws Exception {
        pageInfo = new PageInformation(new Point(72, 12), 8, 2);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setBottomMargin(1.0);

        when(content.getLineCount()).thenReturn(2);
        paginator.setPageInformation(pageInfo);

        Point upperLeftCorner = new Point(Paginator.EXTERNAL_MARGIN, Paginator.EXTERNAL_MARGIN + 24
                + Paginator.EXTERNAL_MARGIN);
        assertThat(getLocation(1), is(upperLeftCorner));
    }

    @Test
    public void shouldTellLinesThatContainPageBreaks() throws Exception {
        pageInfo = new PageInformation(new Point(72, 12), 8, 1);

        when(content.getLineCount()).thenReturn(2);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.hasPageBreakWithinLine(0), is(true));
        assertThat(paginator.hasPageBreakWithinLine(1), is(true));
    }

    @Test
    public void shouldTellLinesThatDontContainPageBreaks() throws Exception {
        assertThat(paginator.hasPageBreakWithinLine(0), is(true));
        for (int i = 1; i < 40; i++)
            assertThat(paginator.hasPageBreakWithinLine(i), is(false));
    }

    @Test
    public void shouldTellBrokenLinesThatContainPageBreak() throws Exception {
        pageInfo = new PageInformation(new Point(12, 18), 8, 1);
        pageInfo.setDrawPageBreaks(true);

        when(content.getLineCount()).thenReturn(2);
        when(content.getLine(0)).thenReturn("a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.hasPageBreakWithinLine(0), is(true));
        assertThat(paginator.hasPageBreakWithinLine(1), is(true));
    }

    @Test
    public void shouldReturnTextLayoutLocationOnGetLinePixel() throws Exception {
        assertThat(paginator.getLinePixel(0), is(Paginator.EXTERNAL_MARGIN));
        assertThat(paginator.getLinePixel(1), is(Paginator.EXTERNAL_MARGIN + 12));
    }

    @Test
    public void shouldReturnBottomPixelOfLastLineOnGetLinePixelWithLineCount() throws Exception {
        assertThat(paginator.getLinePixel(40), is(10 + 40 * 12));
    }

    @Test
    public void shouldReturnPage0ForAllContentsInSinglePage() throws Exception {
        for (int offset = 0; offset < 79; offset++)
            assertThat("Wrong page for line " + offset, paginator.getPage(offset), is(0));
    }

    @Test
    public void shouldReturnPage0ForAllContentsInFirstPage() throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);
        for (int offset = 0; offset < 65; offset++)
            assertThat("Wrong page for line " + offset, paginator.getPage(offset), is(0));
    }

    @Test
    public void shouldReturnPage1ForAllContentsInSecondPage() throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);
        for (int offset = 65; offset < 79; offset++)
            assertThat("Wrong page for line " + offset, paginator.getPage(offset), is(1));
    }

    @Test
    public void shouldReturnOffset0ForPage0() throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);
        assertThat(paginator.getOffsetForPage(0), is(0));
    }
    
    @Test
    public void shouldReturnOffset65ForPage1() throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        when(content.getOffsetAtLine(33)).thenReturn(65);
        paginator.setContent(Display.getDefault(), content);
        assertThat(paginator.getOffsetForPage(1), is(65));
    }
    
    @Test
    public void shouldReturnOffset14ForPage1WhenOnBrokenContent() throws Exception {
        pageInfo = new PageInformation(new Point(12, 18), 8, 1);
        pageInfo.setDrawPageBreaks(true);

        when(content.getLineCount()).thenReturn(2);
        when(content.getOffsetAtLine(0)).thenReturn(0);
        when(content.getLine(0)).thenReturn("a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);
        paginator.setPageInformation(pageInfo);
        
        assertThat(paginator.getOffsetForPage(1), is(14));
    }

    @Test
    public void shouldKnowLocationOfLayoutWithLeftMargin() throws Exception {
        when(content.getLeftMargin(0)).thenReturn(20);
        paginator.layoutChanged();
        assertThat(getLocation(0), is(new Point(30, 10)));
    }

    @Test
    public void shouldKnowLocationOfLayoutWithParagraphSpacing() throws Exception {
        when(content.getParagraphSpacing(1)).thenReturn(1);
        paginator.layoutChanged();
        assertThat(getLocation(1), is(new Point(10, 34)));
    }

    @Test
    public void shouldIgnoreExternalAndTopMarginsOfFirstPageForModelLocation() throws Exception {
        pageInfo.setTopMargin(1.0);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.getModelLocation(0), is(0));
        assertThat(paginator.getModelLocation(1), is(12));
    }

    @Test
    public void shouldConsiderParagraphSpacingForModelLocation() throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.layoutChanged();
        pageInfo.setTopMargin(1.0);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.getModelLocation(0), is(0));
        assertThat(paginator.getModelLocation(1), is(12));
        assertThat(paginator.getModelLocation(2), is(12));
        assertThat(paginator.getModelLocation(3), is(36));
        assertThat(paginator.getModelLocation(4), is(36));
    }

    @Test
    public void shouldIgnoreExternalTopAndBottomMarginsOfSecondPageForModelLocation()
            throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.layoutChanged();
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.hasPageBreakWithinLine(27), is(true));
        assertThat(paginator.getModelLocation(1 + 25 * 2), is(12 + 25 * 24));
        assertThat(paginator.getModelLocation(1 + 25 * 2 + 1), is(12 + 25 * 24));
        assertThat(paginator.getModelLocation(1 + 26 * 2), is(648));
        assertThat(paginator.getModelLocation(1 + 26 * 2 + 1), is(648));
        assertThat(paginator.getModelLocation(1 + 27 * 2), is(648 + 12));
        assertThat(paginator.getModelLocation(1 + 27 * 2 + 1), is(648 + 12));
    }

    @Test
    public void modelLocationShouldWorkWithBrokenContent() throws Exception {
        pageInfo = new PageInformation(new Point(12, 18), 8, 3);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        when(content.getLine(0)).thenReturn("a a a a a a a a a");
        when(content.getCharCount()).thenReturn(16 + 78);
        when(content.getLineAtOffset(anyInt())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Integer offset = (Integer) invocation.getArguments()[0];
                if (offset < 16)
                    return 0;
                else
                    return 1 + (offset - 16) / 2;
            }
        });
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.hasPageBreakWithinLine(0), is(true));
        for (int i = 0; i < 14; i++)
            assertThat("Model location for offset " + i, paginator.getModelLocation(i), is(0));
        for (int i = 14; i < 16; i++)
            assertThat("Model location for offset " + i, paginator.getModelLocation(i), is(18));

        assertThat(paginator.hasPageBreakWithinLine(1), is(true));
        assertThat(paginator.getModelLocation(16), is(18 * 2));
        assertThat(paginator.getModelLocation(17), is(18 * 2));
    }
    
    @Test
    public void modelLocationShouldWorkWithMultiLinePageAndBrokenContent() throws Exception {
        pageInfo = new PageInformation(new Point(7, 12), 12, 4);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        when(content.getLine(1)).thenReturn("a a a a a a a a a");
        when(content.getCharCount()).thenReturn(16 + 78);
        when(content.getLineAtOffset(anyInt())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Integer offset = (Integer) invocation.getArguments()[0];
                if (offset == 0)
                    return 0;
                else if (1 <= offset && offset < 17)
                    return 1;
                else
                    return 2 + (offset - 17) / 2;
            }
        });
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.hasPageBreakWithinLine(0), is(true));
        assertThat(paginator.getModelLocation(0), is(0));
        
        assertThat(paginator.hasPageBreakWithinLine(1), is(true));
        for (int i = 1; i < 14; i++)
            assertThat("Model location for offset " + i, paginator.getModelLocation(i), is(12));
        for (int i = 14; i < 17; i++)
            assertThat("Model location for offset " + i, paginator.getModelLocation(i), is(24));

        assertThat(paginator.hasPageBreakWithinLine(2), is(false));
        assertThat(paginator.getModelLocation(17), is(12 * 3));
        assertThat(paginator.getModelLocation(18), is(12 * 3));
    }

    @Test
    public void shouldCountTotalModelLengthWithoutPageBreaks() throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.layoutChanged();
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.getModelLength(), is(648 + 12 + 12 * 24));
    }

    @Test
    public void shouldCountModelLengthForEmptyDocument() throws Exception {
        when(content.getCharCount()).thenReturn(0);
        when(content.getLineCount()).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getModelLength(), is(12));
    }

    private Point getLocation(int lineIndex) {
        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
        return paginator.getLocation(logicalLine);
    }
}
