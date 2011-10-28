package br.com.agilbits.swt.extension.paginator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.custom.PageInformation;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

import br.com.agilbits.swt.extension.paginator.LogicalLine;
import br.com.agilbits.swt.extension.paginator.Paginator;

public class PaginatorMoreContTest {
    private PageInformation pageInfo;
    private Paginator paginator;
    private IExtendedStyledTextContent content;

    @Before
    public void setUp() throws Exception {
        Font font = new Font(Display.getDefault(), "Courier", 12, SWT.NORMAL);
        pageInfo = new PageInformation(new Point(7, 12), 10, 6);
        pageInfo.setDrawPageBreaks(true);
        paginator = new Paginator(pageInfo);
        paginator.setFont(font);

        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(3);
        when(content.getRightMargin(anyInt())).thenReturn(70);

        when(content.shouldMergeWithNext(anyInt())).thenReturn(false);
        when(content.canBreak(anyInt())).thenReturn(true);
        when(content.canBreakAfter(anyInt())).thenReturn(true);
        when(content.getMinimumLinesToFit(anyInt())).thenReturn(1);

        when(content.getParagraphSpacing(1)).thenReturn(1);
        when(content.getLine(1)).thenReturn("NANO");
        when(content.shouldMergeWithNext(1)).thenReturn(true);
        when(content.canBreak(1)).thenReturn(false);
        when(content.canBreakAfter(1)).thenReturn(false);

        when(content.getBreakDecorationSize(2)).thenReturn(1);
    }
    
    @Test
    public void shouldNotPushSpeechWithoutContentIfSpeechFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.shouldMergeWithNext(1)).thenReturn(false);
        when(content.canBreak(1)).thenReturn(false);
        when(content.canBreakAfter(1)).thenReturn(true);
        when(content.getLine(2)).thenReturn("lalala");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 60)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10)));
    }

    @Test
    public void shouldPushSpeechWithoutContentIfSpeechDoesntFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789 123456789");
        when(content.shouldMergeWithNext(1)).thenReturn(false);
        when(content.canBreak(1)).thenReturn(false);
        when(content.canBreakAfter(1)).thenReturn(true);
        when(content.getLine(2)).thenReturn("lalala");
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }
    
    @Test
    public void shouldPushSpeechWith1LineDialogueIf0LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("lalala");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldNotPushSpeechWith1LineDialogueIf1LineOfDialogueFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("lalala");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 48)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 60)));
    }

    @Test
    public void shouldPushSpeechWith2LinesDialogueIf0LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldPushSpeechWith2LinesDialogueIf1LineOfDialogueFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldNotPushSpeechWith2LinesDialogueIf2LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 36)));
        LogicalLine logicalLine = paginator.getLogicalLine(2);
        assertThat(logicalLine.getChildren().size(), is(1));
        assertThat(logicalLine.getChild(0).getLocation(), is(new Point(10, 10 + 48)));
    }

    @Test
    public void shouldPushSpeechWith3LinesDialogueIf0LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldPushSpeechWith3LinesDialogueIf1LineOfDialogueFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldPushSpeechWith3LinesDialogueIf2LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldNotPushSpeechWith3LinesDialogueIf3LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        LogicalLine logicalLine = paginator.getLogicalLine(2);
        assertThat(logicalLine.getChildren().size(), is(1));
        assertThat(logicalLine.getChild(0).getLocation(), is(new Point(10, 10 + 36)));
    }

    @Test
    public void shouldPushSpeechWith4LinesDialogueIf0LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldPushSpeechWith4LinesDialogueIf1LineOfDialogueFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldPushSpeechWith4LinesDialogueIf2LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldBreakSpeechWith4LinesDialogueIf3LinesOfDialogueFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        LogicalLine logicalLine = paginator.getLogicalLine(2);
        assertThat(logicalLine.getChildren().size(), is(2));
        assertThat(logicalLine.getChild(0).getLocation(), is(new Point(10, 10 + 36)));
        assertThat(logicalLine.getChild(0).getHeight(), is(24));
        assertThat(logicalLine.getChild(21).getLocation(), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(logicalLine.getChild(21).getHeight(), is(24));
    }

    @Test
    public void shouldNotPushSpeechWith4LinesDialogueIf4LinesOfDialogueFit() throws Exception {
        pageInfo = new PageInformation(new Point(7, 12), 10, 7);
        pageInfo.setDrawPageBreaks(true);
        paginator.setPageInformation(pageInfo);

        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        LogicalLine logicalLine = paginator.getLogicalLine(2);
        assertThat(logicalLine.getChildren().size(), is(1));
        assertThat(logicalLine.getChild(0).getLocation(), is(new Point(10, 10 + 36)));
    }

    @Test
    public void shouldBreakSpeechWith5LinesDialogueIf4LinesOfDialogueFit() throws Exception {
        pageInfo = new PageInformation(new Point(7, 12), 10, 7);
        pageInfo.setDrawPageBreaks(true);
        paginator.setPageInformation(pageInfo);

        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        LogicalLine logicalLine = paginator.getLogicalLine(2);
        assertThat(logicalLine.getChildren().size(), is(2));
        assertThat(logicalLine.getChild(0).getLocation(), is(new Point(10, 10 + 36)));
        assertThat(logicalLine.getChild(0).getHeight(), is(36));
        assertThat(logicalLine.getChild(31).getLocation(), is(new Point(10, 10 + 84 + 10 + 12)));
        assertThat(logicalLine.getChild(31).getHeight(), is(24));
    }

    @Test
    public void whenSpeechHas2DialoguesWith2LinesEachAnd3LinesOfDialogueFitShouldPushSecondDialogue()
            throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.shouldMergeWithNext(2)).thenReturn(true);
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        when(content.getLine(3)).thenReturn("123456789 123456789");
        when(content.getMinimumLinesToFit(3)).thenReturn(2);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        LogicalLine firstDialogue = paginator.getLogicalLine(2);
        assertThat(firstDialogue.getChildren().size(), is(1));
        assertThat(firstDialogue.getChild(0).getLocation(), is(new Point(10, 10 + 36)));
        assertThat(firstDialogue.getChild(0).getHeight(), is(24));

        LogicalLine secondDialogue = paginator.getLogicalLine(3);
        assertThat(secondDialogue.getChildren().size(), is(1));
        assertThat(secondDialogue.getChild(0).getLocation(), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(secondDialogue.getChild(0).getHeight(), is(24));
    }

    @Test
    public void whenSpeechHas2DialoguesWith2LinesEachAnd2LinesOfDialogueFitShouldPushEverything()
            throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        when(content.shouldMergeWithNext(2)).thenReturn(true);
        when(content.getLine(3)).thenReturn("123456789 123456789");
        when(content.getMinimumLinesToFit(3)).thenReturn(2);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(paginator.getLocation(3, 0, true), is(new Point(10, 10 + 72 + 10 + 36)));
    }

    @Test
    public void whenSpeechHas2DialoguesWith1And3LinesAnd3LinesOfDialogueFitShouldBreakSecondDialogue()
            throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        when(content.shouldMergeWithNext(2)).thenReturn(true);
        when(content.getLine(3)).thenReturn("123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(3)).thenReturn(2);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        LogicalLine firstDialogue = paginator.getLogicalLine(2);
        assertThat(firstDialogue.getChildren().size(), is(1));
        assertThat(firstDialogue.getChild(0).getLocation(), is(new Point(10, 10 + 36)));
        assertThat(firstDialogue.getChild(0).getHeight(), is(12));

        LogicalLine secondDialogue = paginator.getLogicalLine(3);
        assertThat(secondDialogue.getChildren().size(), is(2));
        assertThat(secondDialogue.getChild(0).getLocation(), is(new Point(10, 10 + 36 + 12)));
        assertThat(secondDialogue.getChild(0).getHeight(), is(12));
        assertThat(secondDialogue.getChild(11).getLocation(), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(secondDialogue.getChild(11).getHeight(), is(24));
    }

    @Test
    public void whenSpeechHas2DialoguesWith3And1LinesAnd3LinesOfDialogueFitShouldBreakFirstDialogue()
            throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789");
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        when(content.shouldMergeWithNext(2)).thenReturn(true);
        when(content.getLine(3)).thenReturn("123456789");
        when(content.getMinimumLinesToFit(3)).thenReturn(2);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        LogicalLine firstDialogue = paginator.getLogicalLine(2);
        assertThat(firstDialogue.getChildren().size(), is(2));
        assertThat(firstDialogue.getChild(0).getLocation(), is(new Point(10, 10 + 36)));
        assertThat(firstDialogue.getChild(0).getHeight(), is(24));
        assertThat(firstDialogue.getChild(31).getLocation(), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(firstDialogue.getChild(31).getHeight(), is(12));

        LogicalLine secondDialogue = paginator.getLogicalLine(3);
        assertThat(secondDialogue.getChildren().size(), is(1));
        assertThat(secondDialogue.getChild(0).getLocation(), is(new Point(10, 10 + 72 + 10 + 24)));
        assertThat(secondDialogue.getChild(0).getHeight(), is(12));
    }
    
    @Test
    public void shouldPushSpeechWith1LineParenthesisIf0LinesOfParenthesisFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("(lalala)");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldNotPushSpeechWith1LineParenthesisIf1LineOfParenthesisFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("lalala");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 48)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 60)));
    }

    @Test
    public void shouldPushSpeechWith2LinesParenthesisIf0LinesOfParenthesisFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldPushSpeechWith2LinesParenthesisIf1LineOfParenthesisFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldNotPushSpeechWith2LinesParenthesisIf2LinesOfParenthesisFit() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 36)));
        LogicalLine logicalLine = paginator.getLogicalLine(2);
        assertThat(logicalLine.getChildren().size(), is(1));
        assertThat(logicalLine.getChild(0).getLocation(), is(new Point(10, 10 + 48)));
    }

    @Test
    public void shouldPushSpeechWith2ParenthesisOf1LineIf0LinesOfParenthesisFit() throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(false);
        when(content.getLine(3)).thenReturn("123456789");
        when(content.canBreak(3)).thenReturn(false);
        when(content.canBreakAfter(3)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(paginator.getLocation(3, 0, true), is(new Point(10, 10 + 72 + 10 + 12 + 12)));
    }

    @Test
    public void shouldPushSpeechWith2ParenthesisOf1LineIf1LineOfParenthesisFits() throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(false);
        when(content.getLine(3)).thenReturn("123456789");
        when(content.canBreak(3)).thenReturn(false);
        when(content.canBreakAfter(3)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(paginator.getLocation(3, 0, true), is(new Point(10, 10 + 72 + 10 + 12 + 12)));
    }

    @Test
    public void shouldNotPushSpeechWith2ParenthesisOf1LineIf2LinesOfParenthesisFit() throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(false);
        when(content.getLine(3)).thenReturn("123456789");
        when(content.canBreak(3)).thenReturn(false);
        when(content.canBreakAfter(3)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 36)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 48)));
        assertThat(paginator.getLocation(3, 0, true), is(new Point(10, 10 + 60)));
    }

    @Test
    public void shouldPushSpeechWith2ParenthesisOf2LinesIf3LinesOfParenthesisFit() throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(false);
        when(content.getLine(3)).thenReturn("123456789 123456789");
        when(content.canBreak(3)).thenReturn(false);
        when(content.canBreakAfter(3)).thenReturn(true);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(paginator.getLocation(3, 0, true), is(new Point(10, 10 + 72 + 10 + 12 + 24)));
    }

    @Test
    public void shouldPushSpeechWith1LineParenthesisAnd1LineDialogueIf1LinesAfterSpeechFit() throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(false);
        when(content.getLine(3)).thenReturn("123456789");
        when(content.canBreak(3)).thenReturn(true);
        when(content.canBreakAfter(3)).thenReturn(true);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        when(content.getMinimumLinesToFit(3)).thenReturn(2);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(paginator.getLocation(3, 0, true), is(new Point(10, 10 + 72 + 10 + 12 + 12)));
    }

    @Test
    public void shouldNotPushSpeechWith1LineParenthesisAnd1LineDialogueIf2LinesAfterSpeechFit() throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.getLine(2)).thenReturn("123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(false);
        when(content.getLine(3)).thenReturn("123456789");
        when(content.canBreak(3)).thenReturn(true);
        when(content.canBreakAfter(3)).thenReturn(true);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        when(content.getMinimumLinesToFit(3)).thenReturn(2);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 36)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 36 + 12)));
        assertThat(paginator.getLocation(3, 0, true), is(new Point(10, 10 + 36 + 12 + 12)));
    }

    @Test
    public void shouldBreakSpeechWith1LineParenthesisAnd3LinesDialogueIf3LinesAfterSpeechFit() throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(false);
        when(content.getLine(3)).thenReturn("123456789 123456789 123456789");
        when(content.canBreak(3)).thenReturn(true);
        when(content.canBreakAfter(3)).thenReturn(true);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        when(content.getMinimumLinesToFit(3)).thenReturn(2);
        
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 24 + 12)));
        LogicalLine logicalLine = paginator.getLogicalLine(3);
        assertThat(logicalLine.getChildren().size(), is(2));
        assertThat(logicalLine.getChildren().get(0).getLocation(), is(new Point(10, 10 + 24 + 12 + 12)));
        assertThat(logicalLine.getChildren().get(0).getHeight(), is(12));
        assertThat(logicalLine.getChildren().get(1).getLocation(), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(logicalLine.getChildren().get(1).getHeight(), is(24));
    }

    @Test
    public void shouldPushSpeechWith2LineParenthesisAnd2LinesDialogueIf3LinesAfterSpeechFit() throws Exception {
        when(content.getLineCount()).thenReturn(4);
        when(content.getLine(0)).thenReturn("123456789");
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.canBreak(2)).thenReturn(false);
        when(content.canBreakAfter(2)).thenReturn(false);
        when(content.getLine(3)).thenReturn("123456789 123456789");
        when(content.canBreak(3)).thenReturn(true);
        when(content.canBreakAfter(3)).thenReturn(true);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        when(content.getMinimumLinesToFit(3)).thenReturn(2);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 72 + 10)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(paginator.getLocation(3, 0, true), is(new Point(10, 10 + 72 + 10 + 12 + 24)));
    }

    @Test
    public void shouldPushDialogueWith2LinesIf1LineFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789 123456789");
        when(content.canBreak(1)).thenReturn(true);
        when(content.canBreakAfter(1)).thenReturn(true);
        when(content.getMinimumLinesToFit(1)).thenReturn(1);
        when(content.shouldMergeWithNext(1)).thenReturn(false);
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.canBreak(2)).thenReturn(true);
        when(content.canBreakAfter(2)).thenReturn(true);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 48)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldNotPushDialogueWith2LinesIf2LineFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.canBreak(1)).thenReturn(true);
        when(content.canBreakAfter(1)).thenReturn(true);
        when(content.getMinimumLinesToFit(1)).thenReturn(1);
        when(content.shouldMergeWithNext(1)).thenReturn(false);
        when(content.getLine(2)).thenReturn("123456789 123456789");
        when(content.canBreak(2)).thenReturn(true);
        when(content.canBreakAfter(2)).thenReturn(true);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 36)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 36 + 12)));
    }

    @Test
    public void shouldPushDialogueWith3LinesIf2LineFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789 123456789");
        when(content.canBreak(1)).thenReturn(true);
        when(content.canBreakAfter(1)).thenReturn(true);
        when(content.getMinimumLinesToFit(1)).thenReturn(1);
        when(content.shouldMergeWithNext(1)).thenReturn(false);
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789");
        when(content.canBreak(2)).thenReturn(true);
        when(content.canBreakAfter(2)).thenReturn(true);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 36)));
        assertThat(paginator.getLocation(2, 0, true), is(new Point(10, 10 + 72 + 10 + 12)));
    }

    @Test
    public void shouldBreakDialogueWith4LinesIf3LineFits() throws Exception {
        when(content.getLine(0)).thenReturn("123456789");
        when(content.canBreak(1)).thenReturn(true);
        when(content.canBreakAfter(1)).thenReturn(true);
        when(content.getMinimumLinesToFit(1)).thenReturn(1);
        when(content.shouldMergeWithNext(1)).thenReturn(false);
        when(content.getLine(2)).thenReturn("123456789 123456789 123456789 123456789");
        when(content.canBreak(2)).thenReturn(true);
        when(content.canBreakAfter(2)).thenReturn(true);
        when(content.getBreakDecorationSize(3)).thenReturn(1);
        when(content.getMinimumLinesToFit(2)).thenReturn(3);
        paginator.setContent(Display.getDefault(), content);

        assertThat(paginator.getLocation(1, 0, true), is(new Point(10, 10 + 24)));
        LogicalLine logicalLine = paginator.getLogicalLine(2);
        assertThat(logicalLine.getChildren().size(), is(2));
        assertThat(logicalLine.getChildren().get(0).getLocation(), is(new Point(10, 10 + 24 + 12)));
        assertThat(logicalLine.getChildren().get(0).getHeight(), is(24));
        assertThat(logicalLine.getChildren().get(1).getLocation(), is(new Point(10, 10 + 72 + 10 + 12)));
        assertThat(logicalLine.getChildren().get(1).getHeight(), is(24));
    }
}
