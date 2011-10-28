package br.com.agilbits.swt.extension.paginator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.IExtendedStyledTextContent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

import br.com.agilbits.swt.extension.paginator.ExtendedTextLayout;
import br.com.agilbits.swt.extension.paginator.LogicalLine;

public class ExtendedTextLayoutTest {
    private IExtendedStyledTextContent content;
    private ExtendedTextLayout layout;
    private Device device;
    private Font font;

    @Before
    public void setUp() throws Exception {
        content = mock(IExtendedStyledTextContent.class);
        when(content.getLineCount()).thenReturn(2);
        when(content.getLine(anyInt())).thenReturn("a");
        when(content.getRightMargin(anyInt())).thenReturn(100);

        device = Display.getDefault();
        font = new Font(device, "Courier", 12, SWT.NORMAL);

        TextLayout textLayout = createTextLayout();
        LogicalLine line = createMockLine(textLayout, 0, 0);
        Point location = new Point(10, 22);
        layout = new ExtendedTextLayout(line, "a", location, false, 2);
    }

    private TextLayout createTextLayout() {
        TextLayout textLayout = new TextLayout(device);
        textLayout.setText("a");
        textLayout.setWidth(100);
        textLayout.setFont(font);
        return textLayout;
    }

    @Test
    public void shouldHaveTextLayoutWithCorrectText() throws Exception {
        assertThat(layout.getText(), is("a"));
    }

    @Test
    public void shouldHaveBaseHeightForSecondLine() throws Exception {
        assertThat(layout.getHeight(), is(12));
    }

    @Test
    public void shouldHaveHeightBasedOnFont() throws Exception {
        Font biggerFont = new Font(device, "Courier", 16, SWT.NORMAL);
        TextLayout textLayout = createTextLayout();
        textLayout.setFont(biggerFont);

        LogicalLine line = createMockLine(textLayout, 0, 0);
        Point location = new Point(10, 42);
        layout = new ExtendedTextLayout(line, "a", location, false, 2);
        assertThat(layout.getHeight(), is(16));
    }

    @Test
    public void shouldAnswerLocationBasedOnLineLocation() throws Exception {
        TextLayout textLayout = createTextLayout();
        LogicalLine line = createMockLine(textLayout, 5, 3);
        Point location = new Point(10, 22);
        layout = new ExtendedTextLayout(line, "a", location, false, 2);

        assertThat(layout.getLocation(), is(new Point(15, 25)));
    }

    @Test
    public void shouldNotMaintainCapitalizedTextWhenStyleIsRemoved() throws Exception {
        TextLayout textLayout = createTextLayout();
        LogicalLine line = createMockLine(textLayout, 0, 0);
        Point location = new Point(10, 22);
        layout = new ExtendedTextLayout(line, "a", location, false, 2);
        layout.getLayout().setText("A");
        assertThat(layout.getLayout().getText(), is("a"));
    }

    private LogicalLine createMockLine(final TextLayout textLayout, final int x, final int y) {
        return new LogicalLine(device, content, 0) {
            @Override
            public TextLayout getTextLayout() {
                return textLayout;
            }

            @Override
            protected Point getSpacedBaseLocation() {
                return new Point(x, y);
            }
        };
    }
}
