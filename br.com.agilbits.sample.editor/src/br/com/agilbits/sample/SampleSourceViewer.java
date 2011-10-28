package br.com.agilbits.sample;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class SampleSourceViewer extends SourceViewer {

    public SampleSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		super(parent, ruler, styles);
	}

	boolean firstReveal = true;

    @Override
    protected StyledText createTextWidget(Composite parent, int styles) {
        ExtendedStyledText styledText = new ExtendedStyledText(parent, styles | SWT.WRAP);
        styledText.setExternalBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        
        return styledText;
    }

    @Override
    protected IDocumentAdapter createDocumentAdapter() {
        return new SampleContent();
    }

    @Override
    public void setDocument(IDocument document, IAnnotationModel annotationModel) {
        for (int line = 0; line < document.getNumberOfLines(); line += 6) {
            try {
                Position position = new Position(document.getLineOffset(line));
                annotationModel.addAnnotation(new Annotation("sample", false, ""), position);
            }
            catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        super.setDocument(document, annotationModel);
    }
}
