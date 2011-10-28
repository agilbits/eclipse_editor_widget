package br.com.agilbits.swt.extension.paginator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PageInformation;
import org.eclipse.swt.custom.PageInformationFactory;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

import br.com.agilbits.swt.extension.paginator.ExtendedTextLayout;
import br.com.agilbits.swt.extension.paginator.LogicalLine;
import br.com.agilbits.swt.extension.paginator.Paginator;

public class PaginatorTextChangeTest {
    private Paginator paginator;
    private StubContent content;
    private PageInformation pageInfo;

    @Before
    public void setUp() throws Exception {
        pageInfo = PageInformationFactory.getLetterPage();
        paginator = new Paginator(pageInfo);

        content = new StubContent(4, "a");
        content.setParagraphSpacing(1, 1);
        content.setParagraphSpacing(2, 2);
        content.setParagraphSpacing(3, 3);
        paginator.setContent(Display.getDefault(), content);

        Font font = new Font(Display.getDefault(), "Courier", 12, SWT.NORMAL);
        paginator.setFont(font);
    }

    @Test
    public void whenTextIsRemovedLocationsShouldBeUpdated() throws Exception {
        TextChangingEvent event = createEvent(0, 1, 0, null, 0);
        paginator.textChanging(event);

        content.changeTextForLine(0, "");

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 22 + 12)));
        assertThat(getLocation(2), is(new Point(10, 22 + 24 + 24)));
        assertThat(getLocation(3), is(new Point(10, 22 + 24 + 36 + 36)));

        assertThat(getText(0), is(""));
        assertThat(getChildText(0, 0), is(""));
        assertThat(getText(1), is("a"));
        assertThat(getChildText(1, 0), is("a"));
        assertThat(getText(2), is("a"));
        assertThat(getChildText(2, 0), is("a"));
        assertThat(getText(3), is("a"));
        assertThat(getChildText(3, 0), is("a"));
    }

    @Test
    public void whenLineIsRemovedLocationsShouldBeUpdated() throws Exception {
        TextChangingEvent event = createEvent(1, 1, 1, null, 0);
        paginator.textChanging(event);

        content.changeTextForLine(0, "aa");
        content.removeLine(1);

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 10 + 12 + 24)));
        assertThat(getLocation(2), is(new Point(10, 10 + 12 + 36 + 36)));

        assertThat(getText(0), is("aa"));
        assertThat(getChildText(0, 0), is("aa"));
        assertThat(getText(1), is("a"));
        assertThat(getChildText(1, 0), is("a"));
        assertThat(getText(2), is("a"));
        assertThat(getChildText(2, 0), is("a"));
    }

    @Test
    public void whenSeveralLinesAreRemovedLocationsShouldBeUpdated() throws Exception {
        TextChangingEvent event = createEvent(1, 4, 2, null, 0);
        paginator.textChanging(event);

        content.removeLine(2);
        content.removeLine(1);

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 10 + 12 + 36)));
        assertThat(getText(0), is("a"));
        assertThat(getChildText(0, 0), is("a"));
        assertThat(getText(1), is("a"));
        assertThat(getChildText(1, 0), is("a"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void whenSeveralLinesAreRemovedTextLayoutsShouldBeRemoved() throws Exception {
        TextChangingEvent event = createEvent(1, 4, 2, null, 0);
        paginator.textChanging(event);

        content.removeLine(2);
        content.removeLine(1);

        paginator.getLogicalLine(2);
    }

    @Test
    public void whenTextIsInsertedOnSameLineLocationsShouldStayTheSame() throws Exception {
        TextChangingEvent event = createEvent(3, 0, 0, "aa", 0);
        paginator.textChanging(event);

        content.changeTextForLine(1, "aaa");

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 34)));
        assertThat(getLocation(2), is(new Point(10, 70)));

        assertThat(getText(0), is("a"));
        assertThat(getChildText(0, 0), is("a"));
        assertThat(getText(1), is("aaa"));
        assertThat(getChildText(1, 0), is("aaa"));
        assertThat(getText(2), is("a"));
        assertThat(getChildText(2, 0), is("a"));
    }

    @Test
    public void whenTextIsInsertedAndWrapedOnSameLineLocationsShouldBeUpdated() throws Exception {
        TextChangingEvent event = createEvent(3, 0, 0, "aa aaa aaa aaa aaa aaa", 0);
        paginator.textChanging(event);

        content.changeTextForLine(1, "aaa aaa aaa aaa aaa aaa");

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 22 + 12)));
        assertThat(getLocation(2), is(new Point(10, 22 + 36 + 24)));

        assertThat(getText(0), is("a"));
        assertThat(getChildText(0, 0), is("a"));
        assertThat(getText(1), is("aaa aaa aaa aaa aaa aaa"));
        assertThat(paginator.getLogicalLine(1).getChildren().size(), is(1));
        assertThat(getChildText(1, 0), is("aaa aaa aaa aaa aaa aaa"));
        assertThat(getText(2), is("a"));
        assertThat(getChildText(2, 0), is("a"));
    }

    @Test
    public void whenLineIsInsertedLocationsShouldBeUpdated() throws Exception {
        TextChangingEvent event = createEvent(1, 0, 0, "\nb", 1);
        paginator.textChanging(event);

        content.addLine(1, "b");

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 22)));
        assertThat(getLocation(2), is(new Point(10, 22 + 12 + 12)));
        assertThat(getLocation(3), is(new Point(10, 22 + 12 + 24 + 24)));
        assertThat(getLocation(4), is(new Point(10, 22 + 12 + 24 + 36 + 36)));

        assertThat(getText(0), is("a"));
        assertThat(getChildText(0, 0), is("a"));
        assertThat(getText(1), is("b"));
        assertThat(getChildText(1, 0), is("b"));
        assertThat(getText(2), is("a"));
        assertThat(getChildText(2, 0), is("a"));
        assertThat(getText(3), is("a"));
        assertThat(getChildText(3, 0), is("a"));
        assertThat(getText(4), is("a"));
        assertThat(getChildText(4, 0), is("a"));
    }

    @Test
    public void whenSeveralLinesAreInsertedLocationsShouldBeUpdated() throws Exception {
        TextChangingEvent event = createEvent(1, 0, 0, "\nb\nc\nd", 3);
        paginator.textChanging(event);

        content.addLine(1, "b");
        content.addLine(2, "c");
        content.addLine(3, "d");

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 22)));
        assertThat(getLocation(2), is(new Point(10, 22 + 12)));
        assertThat(getLocation(3), is(new Point(10, 22 + 12 + 12)));
        assertThat(getLocation(4), is(new Point(10, 22 + 12 + 12 + 12 + 12)));
        assertThat(getLocation(5), is(new Point(10, 22 + 12 + 12 + 12 + 24 + 24)));
        assertThat(getLocation(6), is(new Point(10, 22 + 12 + 12 + 12 + 24 + 36 + 36)));

        assertThat(getText(0), is("a"));
        assertThat(getChildText(0, 0), is("a"));
        assertThat(getText(1), is("b"));
        assertThat(getChildText(1, 0), is("b"));
        assertThat(getText(2), is("c"));
        assertThat(getChildText(2, 0), is("c"));
        assertThat(getText(3), is("d"));
        assertThat(getChildText(3, 0), is("d"));
        assertThat(getText(4), is("a"));
        assertThat(getChildText(4, 0), is("a"));
        assertThat(getText(5), is("a"));
        assertThat(getChildText(5, 0), is("a"));
        assertThat(getText(6), is("a"));
        assertThat(getChildText(6, 0), is("a"));
    }

    @Test
    public void whenTextIsReplacedOnSameLineLocationsShouldStayTheSame() throws Exception {
        TextChangingEvent event = createEvent(0, 1, 0, "b", 0);
        paginator.textChanging(event);

        content.changeTextForLine(0, "b");

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 34)));
        assertThat(getLocation(2), is(new Point(10, 70)));

        assertThat(getText(0), is("b"));
        assertThat(getChildText(0, 0), is("b"));
        assertThat(getText(1), is("a"));
        assertThat(getChildText(1, 0), is("a"));
        assertThat(getText(2), is("a"));
        assertThat(getChildText(2, 0), is("a"));
    }

    @Test
    public void whenLineIsReplacedLocationsShouldBeUpdated() throws Exception {
        TextChangingEvent event = createEvent(2, 2, 1, "b\n", 1);
        paginator.textChanging(event);

        content.removeLine(1);
        content.addLine(1, "b");

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 22)));
        assertThat(getLocation(2), is(new Point(10, 22 + 12 + 24)));

        assertThat(getText(0), is("a"));
        assertThat(getChildText(0, 0), is("a"));
        assertThat(getText(1), is("b"));
        assertThat(getChildText(1, 0), is("b"));
        assertThat(getText(2), is("a"));
        assertThat(getChildText(2, 0), is("a"));
    }

    @Test
    public void whenSeveralLinesAreReplacedLocationsShouldBeUpdated() throws Exception {
        content.removeLine(2);
        content.removeLine(1);
        content.addLine(1, "b");
        content.addLine(2, "c");
        content.addLine(3, "d");

        TextChangingEvent event = createEvent(2, 4, 2, "b\nc\nd\n", 3);
        paginator.textChanging(event);

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 22)));
        assertThat(getLocation(2), is(new Point(10, 22 + 12)));
        assertThat(getLocation(3), is(new Point(10, 22 + 12 + 12)));
        assertThat(getLocation(4), is(new Point(10, 22 + 12 + 12 + 12 + 36)));

        assertThat(getText(0), is("a"));
        assertThat(getChildText(0, 0), is("a"));
        assertThat(getText(1), is("b"));
        assertThat(getChildText(1, 0), is("b"));
        assertThat(getText(2), is("c"));
        assertThat(getChildText(2, 0), is("c"));
        assertThat(getText(3), is("d"));
        assertThat(getChildText(3, 0), is("d"));
        assertThat(getText(4), is("a"));
        assertThat(getChildText(4, 0), is("a"));
    }

    @Test
    public void whenTextIsChangedOnSecondPagePageBreaksShouldRemainCorrect() throws Exception {
        pageInfo.setTopMargin(0.1);
        content = new StubContent(80, "a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.hasPageBreakWithinLine(65), is(true));

        TextChangingEvent event = createEvent(140, 0, 0, "b\n", 1);
        paginator.textChanging(event);

        content.addLine(70, "b");

        assertThat(getLocation(71), is(new Point(10, 812 + 7 + 6 * 12)));
        assertThat(paginator.hasPageBreakWithinLine(65), is(true));

        assertThat(paginator.hasPageBreakWithinLine(64), is(false));
        assertThat(paginator.hasPageBreakWithinLine(66), is(false));
    }

    @Test
    public void whenTextIsChangedOnLastContentOfFirstPageItShouldBePushedToSecondPage()
            throws Exception {
        pageInfo.setTopMargin(0.1);
        content = new StubContent(80, "a");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.hasPageBreakWithinLine(65), is(true));

        TextChangingEvent event = createEvent(128, 0, 0,
                                              "bbb bbb bbb bb" +
                                              "b bbb bbb bbb " +
                                              "bbb bbb bbb bb" +
                                              "b bbb", 0);
        paginator.textChanging(event);
        content.changeTextForLine(64, "bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbba");

        assertThat(getLocation(64), is(new Point(10, 10 + 64 * 12 + 7)));
        assertThat(paginator.hasPageBreakWithinLine(64), is(true));

        assertThat(paginator.hasPageBreakWithinLine(63), is(false));
        assertThat(getLocation(65), is(new Point(10, 10 + 11*72 + 10 + 7 + 3*12)));
        assertThat(paginator.hasPageBreakWithinLine(65), is(false));
    }

    @Test
    public void whenTextIsChangedOnLastContentOfFirstPageTheNextContentShouldConsiderItsParagraphSpacing()
            throws Exception {
        pageInfo.setTopMargin(0.1);
        content = new StubContent(80, "a");
        content.setParagraphSpacing(65, 1);

        paginator.setContent(Display.getDefault(), content);

        assertThat(getLocation(65), is(new Point(10, 10 + 11*72 + 10 + 7)));

        TextChangingEvent event = createEvent(128, 0, 0,
                                              "bbb bbb bbb bb" +
                                              "b bbb bbb bbb " +
                                              "bbb bbb bbb bb" +
                                              "b bbb", 0);
        paginator.textChanging(event);
        content.changeTextForLine(64, "bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbba");

        LogicalLine logicalLine = paginator.getLogicalLine(65);
        assertThat(getLocation(65), is(new Point(10, 10 + 11*72 + 10 + 7 + 36 + 12)));
        assertThat(logicalLine.getHeight(), is(24));
    }

    @Test
    public void whenTextIsRemovedInBlockLocationsOfBlockShouldBeUpdated() throws Exception {
        content.setParagraphSpacing(2, 0);
        content.setParagraphSpacing(3, 0);
        content.setCanBreak(1, false);
        content.setCanBreakAfter(1, false);
        content.setMergeWithNext(1, true);
        content.setCanBreak(2, false);
        content.setCanBreakAfter(2, false);
        content.setMergeWithNext(2, true);
        content.setMinimumLines(3, 2);
        
        pageInfo = new PageInformation(new Point(7, 12), 10, 4);
        pageInfo.setDrawPageBreaks(true);
        paginator.setPageInformation(pageInfo);
        
        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 10 + 48 + 10)));
        assertThat(getLocation(2), is(new Point(10, 10 + 48 + 10 + 12)));
        assertThat(getLocation(3), is(new Point(10, 10 + 48 + 10 + 12 + 12)));
        
        TextChangingEvent event = createEvent(4, 2, 1, null, 0);
        paginator.textChanging(event);

        content.removeLine(2);
        content.setMinimumLines(2, 3);

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 22 + 12)));
        assertThat(getLocation(2), is(new Point(10, 22 + 12 + 12)));
    }

    @Test
    public void whenTextIsAddedInBlockLocationsOfBlockShouldBeUpdated() throws Exception {
        content.removeLine(2);
        content.setParagraphSpacing(2, 0);
        content.setCanBreak(1, false);
        content.setCanBreakAfter(1, false);
        content.setMergeWithNext(1, true);
        content.setMinimumLines(2, 2);
        paginator.setContent(Display.getDefault(), content);
        
        pageInfo = new PageInformation(new Point(7, 12), 10, 4);
        pageInfo.setDrawPageBreaks(true);
        paginator.setPageInformation(pageInfo);

        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 22 + 12)));
        assertThat(getLocation(2), is(new Point(10, 22 + 12 + 12)));
        
        TextChangingEvent event = createEvent(4, 0, 0, "bcdefgh 123456789", 0);
        paginator.textChanging(event);

        content.changeTextForLine(2, "abcdefgh 123456789");
        
        assertThat(getLocation(0), is(new Point(10, 10)));
        assertThat(getLocation(1), is(new Point(10, 10 + 48 + 10)));
        assertThat(getLocation(2), is(new Point(10, 10 + 48 + 10 + 12)));
    }

    private TextChangingEvent createEvent(int start, int replaceCharCount, int replaceLineCount,
            String newText, int newLineCount) {
        TextChangingEvent event = new TextChangingEvent(content);
        event.start = start;
        event.replaceCharCount = replaceCharCount;
        event.replaceLineCount = replaceLineCount;
        event.newText = newText;
        event.newCharCount = newText == null ? 0 : newText.length();
        event.newLineCount = newLineCount;
        return event;
    }

    private Point getLocation(int lineIndex) {
        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
        return paginator.getLocation(logicalLine);
    }

    private String getText(int lineIndex) {
        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
        return logicalLine.getTextLayout().getText();
    }

    private String getChildText(int lineIndex, int childIndex) {
        LogicalLine logicalLine = paginator.getLogicalLine(lineIndex);
        ExtendedTextLayout child = logicalLine.getChildren().get(childIndex);
        return child.getLayout().getText();
    }
}
