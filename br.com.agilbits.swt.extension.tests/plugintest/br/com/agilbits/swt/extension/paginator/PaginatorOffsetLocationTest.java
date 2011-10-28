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
import org.junit.Before;
import org.junit.Test;

import br.com.agilbits.swt.extension.paginator.Paginator;

public class PaginatorOffsetLocationTest {
    static final int CARET_BEFORE = 0;
    static final int CARET_AFTER = 1;

    private Paginator paginator;
    private PageInformation pageInfo;
    private IExtendedStyledTextContent content;
    private Font font;

    @Before
    public void setUp() throws Exception {
        pageInfo = PageInformationFactory.getLetterPage();
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator = new Paginator(pageInfo);

        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(40);
        when(content.getParagraphSpacing(anyInt())).thenReturn(0);
        when(content.getLine(anyInt())).thenReturn("a a a a a");
        when(content.getRightMargin(anyInt())).thenReturn(100);
        when(content.canBreak(anyInt())).thenReturn(true);
        when(content.canBreakAfter(anyInt())).thenReturn(true);
        when(content.getMinimumLinesToFit(anyInt())).thenReturn(1);

        paginator.setContent(Display.getDefault(), content);

        font = new Font(Display.getDefault(), "Courier", 12, SWT.NORMAL);
        paginator.setFont(font);
    }
    
    @Test
    public void shouldGetLocationForFirstOffsetOfEmptyContent() throws Exception {
        when(content.getCharCount()).thenReturn(0);
        when(content.getLineCount()).thenReturn(1);
        when(content.getLine(0)).thenReturn("");
        paginator.setContent(Display.getDefault(), content);
        
        assertThat(paginator.getLocation(0, 0, false), is(new Point(10, 10 + 72)));
    }

    @Test
    public void shouldGetLocationForFirstOffset() throws Exception {
        assertThat(paginator.getLocation(0, 0, true), is(new Point(10, 10 + 72)));
        assertThat(paginator.getLocation(0, 0, false), is(new Point(10, 10 + 72)));
    }

    @Test
    public void shouldGetLocationForSecondOffset() throws Exception {
        assertThat(paginator.getLocation(0, 1, true), is(new Point(10 + 7, 10 + 72)));
        assertThat(paginator.getLocation(0, 1, false), is(new Point(10 + 7, 10 + 72)));
    }

    @Test
    public void shouldGetLocationForLastOffset() throws Exception {
        assertThat(paginator.getLocation(0, 8, true), is(new Point(10 + 56, 10 + 72)));
        assertThat(paginator.getLocation(0, 8, false), is(new Point(10 + 56, 10 + 72)));
    }

    @Test
    public void shouldGetLocationAfterLastOffset() throws Exception {
        assertThat(paginator.getLocation(0, 9, true), is(new Point(10 + 63, 10 + 72)));
        assertThat(paginator.getLocation(0, 9, false), is(new Point(10 + 63, 10 + 72)));
    }

    @Test
    public void shouldGetLocationForFirstOffsetOfMiddleWrappedLine() throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 0, true), is(new Point(10, 10 + 384 + 72)));
        assertThat(paginator.getLocation(32, 0, false), is(new Point(10, 10 + 384 + 72)));
    }

    @Test
    public void shouldGetLocationForSecondOffsetOfMiddleWrappedLine() throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 1, true), is(new Point(10 + 7, 10 + 384 + 72)));
        assertThat(paginator.getLocation(32, 1, false), is(new Point(10 + 7, 10 + 384 + 72)));
    }

    @Test
    public void shouldGetLocationForLastOffsetOfFirstVisualLineOfMiddleWrappedLine()
            throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 13, true), is(new Point(10 + 91, 10 + 384 + 72)));
        assertThat(paginator.getLocation(32, 13, false), is(new Point(10 + 91, 10 + 384 + 72)));
    }

    @Test
    public void shouldGetLocationForFirstOffsetOfSecondVisualLineOfMiddleWrappedLine()
            throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 14, false), is(new Point(10 + 98, 10 + 384 + 72)));
        assertThat(paginator.getLocation(32, 14, true), is(new Point(10, 10 + 384 + 12 + 72)));
    }

    @Test
    public void shouldGetLocationForSecondOffsetOfSecondVisualLineOfMiddleWrappedLine()
            throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 15, true), is(new Point(10 + 7, 10 + 384 + 12 + 72)));
        assertThat(paginator.getLocation(32, 15, false), is(new Point(10 + 7, 10 + 384 + 12 + 72)));
    }

    @Test
    public void shouldGetLocationForThirdOffsetOfSecondVisualLineOfMiddleWrappedLine()
            throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 16, true), is(new Point(10 + 14, 10 + 384 + 12 + 72)));
        assertThat(paginator.getLocation(32, 16, false), is(new Point(10 + 14, 10 + 384 + 12 + 72)));
    }

    @Test
    public void shouldGetLocationForFirstOffsetOfLastVisualLineOfMiddleWrappedLine()
            throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 42, false), is(new Point(10 + 98, 10 + 384 + 24 + 72)));
        assertThat(paginator.getLocation(32, 42, true), is(new Point(10, 10 + 384 + 36 + 72)));
    }

    @Test
    public void shouldGetLocationForSecondOffsetOfLastVisualLineOfMiddleWrappedLine()
            throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 43, true), is(new Point(10 + 7, 10 + 384 + 36 + 72)));
        assertThat(paginator.getLocation(32, 43, false), is(new Point(10 + 7, 10 + 384 + 36 + 72)));
    }

    @Test
    public void shouldGetLocationForLastOffsetOfLastVisualLineOfMiddleWrappedLine()
            throws Exception {
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(32, 52, true), is(new Point(10 + 70, 10 + 384 + 36 + 72)));
        assertThat(paginator.getLocation(32, 52, false), is(new Point(10 + 70, 10 + 384 + 36 + 72)));
        assertThat(paginator.getLocation(32, 53, true), is(new Point(10 + 77, 10 + 384 + 36 + 72)));
        assertThat(paginator.getLocation(32, 53, false), is(new Point(10 + 77, 10 + 384 + 36 + 72)));
    }

    @Test
    public void shouldGetLocationForFirstOffsetOfLastLine() throws Exception {
        assertThat(paginator.getLocation(39, 0, true), is(new Point(10, 10 + 468 + 72)));
        assertThat(paginator.getLocation(39, 0, false), is(new Point(10, 10 + 468 + 72)));
    }

    @Test
    public void shouldGetLocationForSecondOffsetOfLastLine() throws Exception {
        assertThat(paginator.getLocation(39, 1, true), is(new Point(10 + 7, 10 + 468 + 72)));
        assertThat(paginator.getLocation(39, 1, false), is(new Point(10 + 7, 10 + 468 + 72)));
    }

    @Test
    public void shouldGetLocationForLastOffsetOfLastLine() throws Exception {
        assertThat(paginator.getLocation(39, 8, true), is(new Point(10 + 56, 10 + 468 + 72)));
        assertThat(paginator.getLocation(39, 8, false), is(new Point(10 + 56, 10 + 468 + 72)));
    }

    @Test
    public void shouldGetLocationAfterLastOffsetOfLastLine() throws Exception {
        assertThat(paginator.getLocation(39, 9, true), is(new Point(10 + 63, 10 + 468 + 72)));
        assertThat(paginator.getLocation(39, 9, false), is(new Point(10 + 63, 10 + 468 + 72)));
    }

    @Test
    public void shouldGetLocationOfBrokenContent() throws Exception {
        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        pageInfo = new PageInformation(new Point(7, 18), 4, 3);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        int firstPage = 10 + 18;
        assertThat(paginator.getLocation(0, 0, true), is(new Point(10, firstPage)));
        assertThat(paginator.getLocation(0, 0, false), is(new Point(10, firstPage)));
        assertThat(paginator.getLocation(0, 3, true), is(new Point(10 + 21, firstPage)));
        assertThat(paginator.getLocation(0, 3, false), is(new Point(10 + 21, firstPage)));
        assertThat(paginator.getLocation(0, 4, false), is(new Point(10 + 28, firstPage)));

        int secondPage = 10 + 18 * 3 + 10 + 18;
        assertThat(paginator.getLocation(0, 4, true), is(new Point(10, secondPage)));
        assertThat(paginator.getLocation(0, 8, false), is(new Point(10 + 28, secondPage)));

        int thirdPage = (10 + 18 * 3) * 2 + 10 + 18;
        assertThat(paginator.getLocation(0, 8, true), is(new Point(10, thirdPage)));
        assertThat(paginator.getLocation(0, 9, true), is(new Point(10 + 7, thirdPage)));
        assertThat(paginator.getLocation(0, 9, false), is(new Point(10 + 7, thirdPage)));
    }

    @Test
    public void shouldGetOffsetForCoordinatesBeforeStart() throws Exception {
        assertOffsetAndTrailingForLocation(0, 37, -100, 0, CARET_BEFORE);
    }
    
    @Test
    public void shouldGetOffsetForCoordinatesAtStart() throws Exception {
        assertOffsetAndTrailingForLocation(0, 0, 0, 0, CARET_BEFORE);
    }

    @Test
    public void shouldGetOffsetForCoordinatesAtY0() throws Exception {
        assertOffsetAndTrailingForLocation(0, 20, 0, 2, CARET_AFTER);
    }

    @Test
    public void shouldGetOffsetForCoordinatesWithinFirstCharacterOnFirstLine() throws Exception {
        assertOffsetAndTrailingForLocation(0, 3, 11, 0, CARET_BEFORE);
        assertOffsetAndTrailingForLocation(0, 6, 11, 0, CARET_AFTER);
        assertOffsetAndTrailingForLocation(0, 3, 12, 8, CARET_AFTER);
    }

    @Test
    public void shouldGetOffsetForCoordinatesAfterLastCharacterOnFirstLine() throws Exception {
        assertOffsetAndTrailingForLocation(0, 100, 6, 8, CARET_AFTER);
    }

    @Test
    public void shouldGetOffsetForCoordinatesWithiLastCharacterOnLastLine() throws Exception {
        assertOffsetAndTrailingForLocation(39, 54, 11, 7, CARET_AFTER);
        assertOffsetAndTrailingForLocation(39, 56, 6, 8, CARET_BEFORE);
    }

    @Test
    public void shouldGetOffsetForCoordinatesAfterLastCharacterOnLastLine() throws Exception {
        assertOffsetAndTrailingForLocation(39, 100, 6, 8, CARET_AFTER);
    }

    @Test
    public void shouldGetOffsetForCoordinatesAfterLastLine() throws Exception {
        assertOffsetAndTrailingForLocation(39, 2, 30, 8, CARET_AFTER);
        assertOffsetAndTrailingForLocation(39, 100, 30, 8, CARET_AFTER);
    }

    @Test
    public void shouldGetOffsetForCoordinatesAfterLastLineOnEmptyDocument() throws Exception {
        when(content.getLine(0)).thenReturn("");
        paginator.setContent(Display.getDefault(), content);
        
        assertOffsetAndTrailingForLocation(0, 100, 30, 0, CARET_BEFORE);
    }

    @Test
    public void shouldGetOffsetForCoordinatesOnBrokenContent() throws Exception {
        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        pageInfo = new PageInformation(new Point(7, 18), 4, 3);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        assertOffsetAndTrailingForLocation(0, 100, 6, 3, CARET_AFTER);
        assertOffsetAndTrailingForLocation(0, 1, 18 + 18 + 10 + 18 + 6, 4, CARET_BEFORE);
        assertOffsetAndTrailingForLocation(0, 4, 18 + 18 + 10 + 18 + 6, 4, CARET_AFTER);
        assertOffsetAndTrailingForLocation(0, 6, 18 + 18 + 10 + 18 + 6, 4, CARET_AFTER);
        assertOffsetAndTrailingForLocation(0, 100, 18 + 18 + 10 + 18 + 6, 7, CARET_AFTER);
        assertOffsetAndTrailingForLocation(0, 2, 18 + 18 + 10 + 18 + 18, 7, CARET_AFTER);
        assertOffsetAndTrailingForLocation(0, 2, 18 + 18 + 10 + 18 * 3 + 10 + 6, 8,
                                           CARET_BEFORE);
        assertOffsetAndTrailingForLocation(0, 2, 18 + 18 + 10 + 18 * 3 + 10 + 18 + 6, 8,
                                           CARET_BEFORE);
        assertOffsetAndTrailingForLocation(0, 100, 18 + 18 + 10 + 18 * 3 + 10 + 18 + 6, 8,
                                           CARET_AFTER);
    }

    private void assertOffsetAndTrailingForLocation(int lineIndex, int x, int y,
            int expectedOffset, int expectedTrailing) {
        int[] trailing = new int[1];
        assertThat("Offset was wrong", paginator.getOffset(lineIndex, x, y, trailing),
                   is(expectedOffset));
        assertThat("Trailing was wrong", trailing[0], is(expectedTrailing));
    }
}
