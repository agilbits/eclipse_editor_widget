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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

import br.com.agilbits.swt.extension.paginator.Paginator;

public class PaginatorBoundsTest {
    private PageInformation pageInfo;
    private Paginator paginator;
    private IExtendedStyledTextContent content;

    @Before
    public void setUp() throws Exception {
        pageInfo = PageInformationFactory.getLetterPage();
        pageInfo.setDrawPageBreaks(true);
        paginator = new Paginator(pageInfo);

        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(20);
        when(content.getParagraphSpacing(anyInt())).thenReturn(0);
        when(content.getLine(anyInt())).thenReturn("a a a a a");
        when(content.getRightMargin(anyInt())).thenReturn(100);
        when(content.canBreak(anyInt())).thenReturn(true);
        when(content.canBreakAfter(anyInt())).thenReturn(true);
        when(content.getMinimumLinesToFit(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);

        Font font = new Font(Display.getDefault(), "Courier", 12, SWT.NORMAL);
        paginator.setFont(font);
    }

    @Test
    public void shouldGetLineBoundsForContinuousFirstUnwrappedLine() throws Exception {
        pageInfo.setDrawPageBreaks(false);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.getLineBounds(0, 0), is(new Rectangle(0, 0, 7 * 9, 12)));
    }

    @Test
    public void shouldGetLineBoundsForContinuousFirstWrappedLine() throws Exception {
        pageInfo.setDrawPageBreaks(false);
        paginator.setPageInformation(pageInfo);

        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getLineBounds(0, 0), is(new Rectangle(0, 0, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(0, 1), is(new Rectangle(0, 12, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(0, 2), is(new Rectangle(0, 24, 7, 12)));
    }

    @Test
    public void shouldGetLineBoundsForContinuousLastWrappedLine() throws Exception {
        pageInfo.setDrawPageBreaks(false);
        paginator.setPageInformation(pageInfo);

        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getLineBounds(19, 0), is(new Rectangle(0, 0, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(19, 1), is(new Rectangle(0, 12, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(19, 2), is(new Rectangle(0, 24, 7, 12)));
    }

    @Test
    public void shouldGetLineBoundsForFirstUnwrappedLine() throws Exception {
        assertThat(paginator.getLineBounds(0, 0), is(new Rectangle(0, 0, 7 * 9, 12)));
    }

    @Test
    public void shouldGetLineBoundsForFirstWrappedLine() throws Exception {
        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getLineBounds(0, 0), is(new Rectangle(0, 0, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(0, 1), is(new Rectangle(0, 12, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(0, 2), is(new Rectangle(0, 24, 7, 12)));
    }

    @Test
    public void shouldGetLineBoundsForLastWrappedLine() throws Exception {
        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getLineBounds(19, 0), is(new Rectangle(0, 0, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(19, 1), is(new Rectangle(0, 12, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(19, 2), is(new Rectangle(0, 24, 7, 12)));
    }

    @Test
    public void shouldGetLineBoundsForPushedLine() throws Exception {
        pageInfo = new PageInformation(new Point(7, 18), 4, 4.5);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getLineBounds(1, 0), is(new Rectangle(0, 0, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(1, 1), is(new Rectangle(0, 12, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(1, 2), is(new Rectangle(0, 24, 7, 12)));
    }

    @Test
    public void shouldGetLineBoundsForPushedLineOnContinuousMode() throws Exception {
        pageInfo = new PageInformation(new Point(7, 18), 4, 4.5);
        pageInfo.setDrawPageBreaks(false);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getLineBounds(1, 0), is(new Rectangle(0, 0, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(1, 1), is(new Rectangle(0, 12, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(1, 2), is(new Rectangle(0, 24, 7, 12)));
    }

    @Test
    public void shouldGetLineBoundsForBrokenLine() throws Exception {
        pageInfo = new PageInformation(new Point(7, 18), 4, 3);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getLineBounds(0, 0), is(new Rectangle(0, 0, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(0, 1),
                   is(new Rectangle(0, 18 + 18 + 10 + 18, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(0, 2), is(new Rectangle(0, (18 + 18 + 10 + 18) * 2, 7,
                                                                   12)));
    }

    @Test
    public void shouldGetLineBoundsForBrokenLineOnContinuousMode() throws Exception {
        pageInfo = new PageInformation(new Point(7, 18), 4, 3);
        pageInfo.setDrawPageBreaks(false);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getLineBounds(0, 0), is(new Rectangle(0, 0, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(0, 1), is(new Rectangle(0, 12, 7 * 4, 12)));
        assertThat(paginator.getLineBounds(0, 2), is(new Rectangle(0, 12 * 2, 7, 12)));
    }

    @Test
    public void shouldGetBoundsForOffsetInFirstUnwrappedLine() throws Exception {
        assertThat(paginator.getBounds(0, 0), is(new Rectangle(0, 0, 7, 12)));
        assertThat(paginator.getBounds(0, 1), is(new Rectangle(7, 0, 7, 12)));
    }

    @Test
    public void shouldGetBoundsForOffsetInFirstUnwrappedLineOnContinuousMode() throws Exception {
        pageInfo.setDrawPageBreaks(false);
        paginator.setPageInformation(pageInfo);

        assertThat(paginator.getBounds(0, 0), is(new Rectangle(0, 0, 7, 12)));
        assertThat(paginator.getBounds(0, 1), is(new Rectangle(7, 0, 7, 12)));
    }

    @Test
    public void shouldGetBoundsForOffsetInFirstWrappedLine() throws Exception {
        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getBounds(0, 0), is(new Rectangle(0, 0, 7, 12)));
        assertThat(paginator.getBounds(0, 4), is(new Rectangle(0, 12, 7, 12)));
        assertThat(paginator.getBounds(0, 8), is(new Rectangle(0, 24, 7, 12)));
    }

    @Test
    public void shouldGetBoundsForOffsetInFirstWrappedLineOnContinuousMode() throws Exception {
        pageInfo.setDrawPageBreaks(false);
        paginator.setPageInformation(pageInfo);
        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getBounds(0, 0), is(new Rectangle(0, 0, 7, 12)));
        assertThat(paginator.getBounds(0, 4), is(new Rectangle(0, 12, 7, 12)));
        assertThat(paginator.getBounds(0, 8), is(new Rectangle(0, 24, 7, 12)));
    }

    @Test
    public void shouldGetBoundsForOffsetInBrokenLine() throws Exception {
        pageInfo = new PageInformation(new Point(7, 18), 4, 3);
        pageInfo.setDrawPageBreaks(true);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getBounds(0, 0), is(new Rectangle(0, 0, 7, 12)));
        assertThat(paginator.getBounds(0, 4), is(new Rectangle(0, 3 * 18 + 10, 7, 12)));
        assertThat(paginator.getBounds(0, 8),
                   is(new Rectangle(0, 3 * 18 + 10 + 3 * 18 + 10, 7, 12)));
    }

    @Test
    public void shouldGetBoundsForOffsetInBrokenLineOnContinuousMode() throws Exception {
        pageInfo = new PageInformation(new Point(7, 18), 4, 3);
        pageInfo.setDrawPageBreaks(false);
        pageInfo.setTopMargin(1.0);
        pageInfo.setBottomMargin(1.0);
        paginator.setPageInformation(pageInfo);

        when(content.getRightMargin(anyInt())).thenReturn(7 * 4);
        paginator.layoutChanged();

        assertThat(paginator.getBounds(0, 0), is(new Rectangle(0, 0, 7, 12)));
        assertThat(paginator.getBounds(0, 4), is(new Rectangle(0, 12, 7, 12)));
        assertThat(paginator.getBounds(0, 8), is(new Rectangle(0, 24, 7, 12)));
    }
}
