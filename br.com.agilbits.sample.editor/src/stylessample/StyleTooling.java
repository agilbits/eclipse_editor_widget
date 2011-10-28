package stylessample;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import br.com.agilbits.sample.app.Activator;


public class StyleTooling {
    private static final String CAPITALIZE = "CAPITALIZE";

    public class SimpleExtendedModifyListener implements ExtendedModifyListener {

        public void modifyText(ExtendedModifyEvent event) {
            if (event.length == 0)
                return;
            StyleRange style;
            if (event.length == 1
                    || text.getTextRange(event.start, event.length).equals(text.getLineDelimiter())) {
                // Have the new text take on the style of the text to its right
                // (during
                // typing) if no style information is active.
                int caretOffset = text.getCaretOffset();
                style = null;
                if (caretOffset < text.getCharCount())
                    style = text.getStyleRangeAtOffset(caretOffset);
                if (style != null) {
                    style = (StyleRange) style.clone();
                    style.start = event.start;
                    style.length = event.length;
                }
                else {
                    style = new StyleRange(event.start, event.length, null, null, SWT.NORMAL);
                }
                style.underline = underlineButton.getSelection();
                style.strikeout = strikeoutButton.getSelection();
                if (!style.isUnstyled())
                    text.setStyleRange(style);
            }
            else {
                // paste occurring, have text take on the styles it had when it was
                // cut/copied
                for (int i = 0; i < cachedStyles.size(); i++) {
                    style = (StyleRange) cachedStyles.elementAt(i);
                    StyleRange newStyle = (StyleRange) style.clone();
                    newStyle.start = style.start + event.start;
                    text.setStyleRange(newStyle);
                }
            }

        }

    }

    private ToolBar toolBar = null;

    StyledText text;

    @SuppressWarnings("rawtypes")
    Vector cachedStyles = new Vector();

    Color RED = null;

    Color BLUE = null;

    Color GREEN = null;

    Font font = null;

    Images images = new Images();

    ToolItem boldButton, italicButton, underlineButton, strikeoutButton, capitalizeButton;

    /*
     * public StyleTooling(StyledText text) { this.text = text; }
     */

    public ToolBar createToolBar(Composite parent) {

        toolBar = new ToolBar(parent, SWT.NULL);
        SelectionAdapter listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setStyle(event.widget);
            }
        };
        images.loadAll(parent.getDisplay());
        initColors(parent.getDisplay());
        boldButton = new ToolItem(toolBar, SWT.CHECK);
        boldButton.setImage(images.Bold);
        boldButton.setToolTipText("Bold");
        boldButton.addSelectionListener(listener);
        italicButton = new ToolItem(toolBar, SWT.CHECK);
        italicButton.setImage(images.Italic);
        italicButton.setToolTipText("Italic");
        italicButton.addSelectionListener(listener);
        underlineButton = new ToolItem(toolBar, SWT.CHECK);
        underlineButton.setImage(images.Underline);
        underlineButton.setToolTipText("Underline");
        underlineButton.addSelectionListener(listener);
        capitalizeButton = new ToolItem(toolBar, SWT.CHECK);
        capitalizeButton.setImage(images.Capitalize);
        capitalizeButton.setToolTipText("Capitalize");
        capitalizeButton.addSelectionListener(listener);
        strikeoutButton = new ToolItem(toolBar, SWT.CHECK);
        strikeoutButton.setImage(images.Strikeout);
        strikeoutButton.setToolTipText("Strikeout");
        strikeoutButton.addSelectionListener(listener);

        ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);
        item = new ToolItem(toolBar, SWT.PUSH);
        item.setImage(images.Red);
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                fgColor(RED);
            }
        });
        item = new ToolItem(toolBar, SWT.PUSH);
        item.setImage(images.Green);
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                fgColor(GREEN);
            }
        });
        item = new ToolItem(toolBar, SWT.PUSH);
        item.setImage(images.Blue);
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                fgColor(BLUE);
            }
        });
        item = new ToolItem(toolBar, SWT.SEPARATOR);
        item = new ToolItem(toolBar, SWT.PUSH);
        item.setImage(images.Erase);
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                clear();
            }
        });
        return toolBar;
    }

    protected void initColors(Display display) {
        RED = new Color(display, new RGB(255, 0, 0));
        GREEN = new Color(display, new RGB(0, 255, 0));
        BLUE = new Color(display, new RGB(0, 0, 255));
    }

    void setStyle(Widget widget) {
        Point sel = text.getSelectionRange();
        if ((sel == null) || (sel.y == 0))
            return;
        StyleRange style;
        for (int i = sel.x; i < sel.x + sel.y; i++) {
            final StyleRange range = text.getStyleRangeAtOffset(i);
            if (range != null) {
                style = (StyleRange) range.clone();
                style.start = i;
                style.length = 1;
            }
            else {
                style = new StyleRange(i, 1, null, null, SWT.NORMAL);
            }
            if (widget == boldButton) {
                style.fontStyle |= SWT.BOLD;
            }
            else if (widget == italicButton) {
                style.fontStyle |= SWT.ITALIC;
            }
            else if (widget == underlineButton) {
                style.underline = !style.underline;
            }
            else if (widget == strikeoutButton) {
                style.strikeout = !style.strikeout;
            }
            else if (widget == capitalizeButton) {
                if (!CAPITALIZE.equals(style.data)) {
                    style.data = CAPITALIZE;
                    style.foreground = text.getForeground();
                    // A little hack. We need this, because otherwise our range will be
                    // considered "unstyled", and null StyleRangle will be set instead of it
                }
                else
                    style.data = null;
            }
            text.setStyleRange(style);
        }
        text.setSelectionRange(sel.x + sel.y, 0);
    }

    void clear() {
        Point sel = text.getSelectionRange();
        if ((sel != null) && (sel.y != 0)) {
            StyleRange style;
            style = new StyleRange(sel.x, sel.y, null, null, SWT.NORMAL);
            text.setStyleRange(style);
        }
        text.setSelectionRange(sel.x + sel.y, 0);
    }

    void fgColor(Color fg) {
        Point sel = text.getSelectionRange();
        if ((sel == null) || (sel.y == 0))
            return;
        StyleRange style, range;
        for (int i = sel.x; i < sel.x + sel.y; i++) {
            range = text.getStyleRangeAtOffset(i);
            if (range != null) {
                style = (StyleRange) range.clone();
                style.start = i;
                style.length = 1;
                style.foreground = fg;
            }
            else {
                style = new StyleRange(i, 1, fg, null, SWT.NORMAL);
            }
            text.setStyleRange(style);
        }
        text.setSelectionRange(sel.x + sel.y, 0);
    }

    public StyledText getStyledText() {
        return text;
    }

    public void setStyledText(final StyledText text) {
        this.text = text;
        text.addExtendedModifyListener(new SimpleExtendedModifyListener());
        text.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                System.out.println(e.x + " " + e.y);

            }

            public void widgetDefaultSelected(SelectionEvent e) {
            // TODO Auto-generated method stub

            }
        });
        text.addCaretListener(new CaretListener() {

            public void caretMoved(CaretEvent event) {
                int offset = event.caretOffset;
                if (offset < 0 || offset >= text.getCharCount())
                    return;
                StyleRange range = text.getStyleRangeAtOffset(offset);
                if (range != null) {
                    boldButton.setSelection(((range.fontStyle & SWT.BOLD) > 0));
                    italicButton.setSelection((range.fontStyle & SWT.ITALIC) > 0);
                    strikeoutButton.setSelection(range.strikeout);
                    underlineButton.setSelection(range.underline);
                    capitalizeButton.setSelection(CAPITALIZE.equals(range.data));
                }
                else {
                    boldButton.setSelection(false);
                    italicButton.setSelection(false);
                    strikeoutButton.setSelection(false);
                    underlineButton.setSelection(false);
                    capitalizeButton.setSelection(false);
                }
            }
        });
    }

}

class Images {

    // Bitmap Images
    public Image Bold;

    public Image Italic;

    public Image Underline;

    public Image Strikeout;

    public Image Red;

    public Image Green;

    public Image Blue;

    public Image Capitalize;

    public Image Erase;

    Image[] AllBitmaps;

    Images() {}

    public void freeAll() {
        for (int i = 0; i < AllBitmaps.length; i++)
            AllBitmaps[i].dispose();
        AllBitmaps = null;
    }

    Image createBitmapImage(Display display, String fileName) {
        return Activator.getImageDescriptor(fileName).createImage();
    }

    public void loadAll(Display display) {
        // Bitmap Images
        Bold = createBitmapImage(display, "bold");
        Italic = createBitmapImage(display, "italic");
        Underline = createBitmapImage(display, "underline");
        Strikeout = createBitmapImage(display, "strikeout");
        Red = createBitmapImage(display, "red");
        Green = createBitmapImage(display, "green");
        Blue = createBitmapImage(display, "blue");
        Capitalize = createBitmapImage(display, "capitalize");
        Erase = createBitmapImage(display, "delete");

        AllBitmaps = new Image[] { Bold, Italic, Underline, Strikeout, Red, Green, Blue, Erase, };
    }
}
