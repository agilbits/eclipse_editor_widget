package br.com.agilbits.swt.extension.paginator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.com.agilbits.swt.extension.paginator.ExtendedTextLayout;
import br.com.agilbits.swt.extension.paginator.LogicalLine;

public class LogicalLineTest {
    private IExtendedStyledTextContent content;
    private LogicalLine layout;
    private Device device;
    private Font font;

    @Before
    public void setUp() throws Exception {
        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(2);
        when(content.getLine(anyInt())).thenReturn("a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a");
        when(content.getRightMargin(anyInt())).thenReturn(100);
        when(content.getOffsetAtLine(anyInt())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Integer line = (Integer) invocation.getArguments()[0];
                return line * 59;
            }
        });

        device = Display.getDefault();
        font = new Font(device, "Courier", 12, SWT.NORMAL);

        layout = new LogicalLine(device, content, 1);
        layout.setFont(font);
        layout.setBaseLocation(new Point(10, 10));
    }

    @Test
    public void shouldProvideSingleTextLayoutForLayoutWithoutBreak() throws Exception {
        layout.computeChildren(false, 400, 0, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(1));
        assertThat(layouts.get(0).getText(),
                   is("a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a"));
    }

    @Test
    public void shouldNotHaveBreakOnFirstChildOfLayoutWithoutBreak() throws Exception {
        layout.computeChildren(false, 300, 0, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(1));
        assertThat(layouts.get(0).hasPageBreak(), is(false));
    }

    @Test
    public void shouldProvideSinglePushedTextLayoutForLayoutWithBreak() throws Exception {
        layout.computeChildren(false, 0, 15, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(1));
        assertThat(layouts.get(0).getText(),
                   is("a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a"));
        assertThat(layouts.get(0).getLocation(), is(new Point(10, 10 + 15)));
    }

    @Test
    public void shouldHaveBreakOnFirstChildOfPushedTextLayout() throws Exception {
        layout.computeChildren(false, 0, 15, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(1));
        assertThat(layouts.get(0).hasPageBreak(), is(true));
    }

    @Test
    public void shouldChangeLayoutFromPushedToNotPushedAndUpdatePageBreak() throws Exception {
        layout.computeChildren(false, 0, 15, 400, 1, 2);
        assertThat(layout.getChildren().get(0).hasPageBreak(), is(true));

        layout.computeChildren(false, 300, 15, 400, 1, 2);
        assertThat(layout.getChildren().get(0).hasPageBreak(), is(false));
    }

    @Test
    public void shouldProvideSinglePushedTextLayoutForLayoutWithBreakBeforeParagraphSpacing()
            throws Exception {
        when(content.getParagraphSpacing(1)).thenReturn(1);

        layout.computeChildren(false, 0, 15, 400, 2, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(1));
        assertThat(layouts.get(0).getText(),
                   is("a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a"));
        assertThat(layouts.get(0).getLocation(), is(new Point(10, 10 + 15)));
    }

    @Test
    public void shouldProvideSinglePushedTextLayoutForLayoutWithBreakBeforeParagraphSpacingWithCorrectOffset()
            throws Exception {
        when(content.getParagraphSpacing(1)).thenReturn(1);

        layout.computeChildren(false, 0, 15, 400, 2, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(1));
        assertThat(layouts.get(0).getOffset(), is(0));
    }

    @Test
    public void shouldProvideSinglePushedTextLayoutForLayoutWithBreakAfterParagraphSpacingButBeforeEndOfText()
            throws Exception {
        when(content.getParagraphSpacing(1)).thenReturn(1);

        layout.computeChildren(false, 20, 15, 400, 2, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(1));
        assertThat(layouts.get(0).getText(),
                   is("a a a a a a a a a a a a a a a a a a a a a a a a a a a a a a"));
        assertThat(layouts.get(0).getLocation(), is(new Point(10, 20 + 10 + 15)));
    }

    @Test
    public void shouldProvideTwoTextLayoutsForLayoutWithBreak() throws Exception {
        layout.computeChildren(false, 12, 0, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(2));
        assertThat(layouts.get(0).getText(), is("a a a a a a a "));
        assertThat(layouts.get(1).getText(), is("a a a a a a a a a a a a a a a a a a a a a a a"));
    }

    @Test
    public void shouldProvideTwoTextLayoutsWithCorrectOffsetsForLayoutWithBreak() throws Exception {
        layout.computeChildren(false, 12, 0, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(2));
        assertThat(layouts.get(0).getOffset(), is(0));
        assertThat(layouts.get(1).getOffset(), is(14));
    }

    @Test
    public void shouldKnowWhichChildHasBreakWithTwoChildren() throws Exception {
        layout.computeChildren(false, 12, 0, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(2));
        assertThat(layouts.get(0).hasPageBreak(), is(false));
        assertThat(layouts.get(1).hasPageBreak(), is(true));
    }

    @Test
    public void shouldProvideTwoTextLayoutsDependingOnBreakPosition() throws Exception {
        layout.computeChildren(false, 24, 0, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(2));
        assertThat(layouts.get(0).getText(), is("a a a a a a a a a a a a a a "));
        assertThat(layouts.get(1).getText(), is("a a a a a a a a a a a a a a a a"));
    }

    @Test
    public void shouldProvideThreeTextLayoutsGivenASmallPage() throws Exception {
        layout.computeChildren(false, 24, 0, 24, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(3));
        assertThat(layouts.get(0).getText(), is("a a a a a a a a a a a a a a "));
        assertThat(layouts.get(1).getText(), is("a a a a a a a a a a a a a a "));
        assertThat(layouts.get(2).getText(), is("a a"));
    }

    @Test
    public void shouldKnowWhichChildHasBreakWithThreeChildren() throws Exception {
        layout.computeChildren(false, 20, 0, 24, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(3));
        assertThat(layouts.get(0).hasPageBreak(), is(false));
        assertThat(layouts.get(1).hasPageBreak(), is(true));
        assertThat(layouts.get(2).hasPageBreak(), is(true));
    }

    @Test
    public void shouldConsiderParagraphSpacingForFirstLine() throws Exception {
        when(content.getParagraphSpacing(1)).thenReturn(1);

        layout.computeChildren(false, 24, 0, 24, 2, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(3));
        assertThat(layouts.get(0).getText(), is("a a a a a a a "));
        assertThat(layouts.get(1).getText(), is("a a a a a a a a a a a a a a "));
        assertThat(layouts.get(2).getText(), is("a a a a a a a a a"));
    }

    @Test
    public void shouldProvideChildrenWithLocationWithoutParagraphSpacing() throws Exception {
        layout.computeChildren(false, 24, 12, 24, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(3));
        assertThat(layouts.get(0).getLocation(), is(new Point(10, 10)));
        assertThat(layouts.get(1).getLocation(), is(new Point(10, 10 + 24 + 12)));
        assertThat(layouts.get(2).getLocation(), is(new Point(10, 10 + (24 + 12) * 2)));
    }

    @Test
    public void shouldProvideChildrenWithLocationWithParagraphSpacing() throws Exception {
        when(content.getParagraphSpacing(1)).thenReturn(1);

        layout.computeChildren(false, 24, 12, 24, 2, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(3));
        assertThat(layouts.get(0).getLocation(), is(new Point(10, 22)));
        assertThat(layouts.get(1).getLocation(), is(new Point(10, 10 + 24 + 12)));
        assertThat(layouts.get(2).getLocation(), is(new Point(10, 10 + (24 + 12) * 2)));
    }

    @Test
    public void shouldConsiderPageBreaksOnGetFullHeight() throws Exception {
        when(content.getParagraphSpacing(1)).thenReturn(1);

        layout.computeChildren(false, 24, 12, 24, 2, 2);
        assertThat(layout.getFullHeight(), is((24 + 12) * 2 + 24));
    }

    @Test
    public void shouldProvideChildrenWithCorrectLocationWhenHalfLineFits() throws Exception {
        layout.computeChildren(false, 18, 20, 400, 1, 2);
        List<ExtendedTextLayout> layouts = layout.getChildren();
        assertThat(layouts.size(), is(2));
        assertThat(layouts.get(0).getLocation(), is(new Point(10, 10)));
        assertThat(layouts.get(1).getLocation(), is(new Point(10, 10 + 12 + 6 + 20)));
    }
    
    @Test
    public void shouldDisposeElegantlyWhenNeverRetrievedChildren() throws Exception {
        try {
            layout.dispose();
        }
        catch (Exception e) {
            fail("Shouldn't throw any exception");
        }
    }
}
