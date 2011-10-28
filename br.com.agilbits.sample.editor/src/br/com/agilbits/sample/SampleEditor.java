package br.com.agilbits.sample;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.PageInformation;
import org.eclipse.swt.custom.PageInformationFactory;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;

import br.com.agilbits.swt.extension.zoom.FixedZoom;

public class SampleEditor extends TextEditor {
    public final static String ID = "ExtendedStyledTextRCP.editor1";

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        return new SampleSourceViewer(parent, ruler, styles);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        FontData fontData = new FontData("Courier", 12, SWT.NONE); //$NON-NLS-1$
        Font defaultFont = new Font(parent.getDisplay(), fontData);
        setFont(defaultFont);
        
        PageInformation pageInformation = PageInformationFactory.getA4Page();
       
        ExtendedStyledText styledText = (ExtendedStyledText) getTextWidget();
		int caretOffset = styledText.getCaretOffset();

        pageInformation.setTopMargin(1.5);
        pageInformation.setBottomMargin(1.5);
        pageInformation.setDrawPageBreaks(true);

        SamplePageInformationRenderer pageRenderer = new SamplePageInformationRenderer();
        
        styledText.setPageInformation(pageInformation, pageRenderer);
        styledText.scrollToTop();
        styledText.setCaretOffset(caretOffset);
        
        styledText.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent event) {
			}
		}); 
    }

    public void setFont(Font font) {
        getTextWidget().setFont(font);
    }

    public Font getFont() {
        return getTextWidget().getFont();
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    public IDocument getDocument() {
        return getSourceViewer().getDocument();
    }

    public StyledText getTextWidget() {
        return getSourceViewer().getTextWidget();
    }

    public int getCaretOffset() {
        return getTextWidget().getCaretOffset();
    }

    /**
     * Creates action entries for all SWT StyledText actions as defined in
     * <code>org.eclipse.swt.custom.ST</code>. Overwrites and extends the list of these actions
     * afterwards.
     * <p>
     * Subclasses may extend.
     * </p>
     * 
     * @since 2.0
     */
    protected void createNavigationActions() {
        super.createNavigationActions();
//        sourceViewer.getTextWidget().setKeyBinding(SWT.DEL, ST.DELETE_NEXT);
    }

    public void changeZoom() {
        double newZoom = 1 + Math.random();
        ((ExtendedStyledText)getTextWidget()).setScale(new FixedZoom(newZoom));
    }
}
