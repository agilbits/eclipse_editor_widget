package br.com.agilbits.swt.extension.paginator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.custom.PageInformation;
import org.eclipse.swt.custom.PageInformationFactory;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.agilbits.swt.extension.paginator.Paginator;

public class PaginatorLineIndexTest {
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
    public void shouldReturnFirstLineForAllPixelsUntilItsEnd() throws Exception {
        for (int i = 0; i <= 22; i++)
            assertThat("Wrong for pixel " + i, paginator.getLineIndex(i), is(0));
        assertThat(paginator.getLineIndex(23), is(not(0)));
    }

    @Test
    public void shouldReturnSecondLineForAllPixelsAfterTheFirstAndUntilItsEnd() throws Exception {
        for (int i = 23; i <= 34; i++)
            assertThat("Wrong for pixel " + i, paginator.getLineIndex(i), is(1));
        assertThat(paginator.getLineIndex(35), is(not(1)));
    }

    @Test
    public void shouldReturnFirstLineForAllPixelsOfMarginAndLineSinceThereNoSpaceForFirstLine()
            throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);
        for (int i = 0; i <= 22; i++)
            assertThat("Wrong for pixel " + i, paginator.getLineIndex(i), is(0));
        assertThat(paginator.getLineIndex(23), is(not(0)));
    }

    @Test
    public void shouldReturnLineForAllPixelsToThem() throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);
        for (int line = 1; line < 32; line++) {
            for (int i = 1; i < 24; i++) {
                int y = 22 + 24 * (line - 1) + i;
                assertThat("Wrong for pixel " + y, paginator.getLineIndex(y), is(line));
            }
        }
    }

    @Test
    public void shouldReturnLastLineOfFirstPageForAllPixelsBeforeSecondPageAndUpUntilItsStart()
            throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);
        for (int i = 767; i <= 802; i++)
            assertThat("Wrong for pixel " + i, paginator.getLineIndex(i), is(32));
        assertThat(paginator.getLineIndex(803), is(not(32)));
    }

    @Test
    public void shouldReturnFirstLineOnSecondPageForAllPixelsAfterTheEndOfFirstPageAndUntilItsEnd()
            throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);
        for (int i = 803; i < 803 + 10 + 12; i++)
            assertThat("Wrong for pixel " + i, paginator.getLineIndex(i), is(33));
        assertThat(paginator.getLineIndex(825), is(not(33)));
    }
    
    @Test
    public void shouldReturnBrokenLineForAllPixelsFromEndOfPageToEndOfContentOnNextPage()
            throws Exception {
        when(content.getParagraphSpacing(anyInt())).thenReturn(1);
        when(content.getLine(32)).thenReturn("a a a a a a a a a a a a a a a a a a a a a a");
        paginator.setContent(Display.getDefault(), content);
        for (int i = 767; i < 803 + 10 + 24; i++)
            assertThat("Wrong for pixel " + i, paginator.getLineIndex(i), is(32));
        assertThat(paginator.getLineIndex(803+10+24+1), is(not(32)));
    }

    @Test
    public void shouldReturnLastContentOnLastPageForAllPixelsAfterIt() throws Exception {
        for (int i = 491; i <= 792; i++)
            assertThat("Wrong for pixel " + i, paginator.getLineIndex(i), is(39));
    }
}
