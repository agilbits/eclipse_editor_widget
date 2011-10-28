package br.com.agilbits.sample;

import org.eclipse.swt.custom.DecorationRenderer;
import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

public class SamplePageInformationRenderer implements DecorationRenderer {
//    private ISourceViewer sourceViewer;
//
//    public SamplePageInformationRenderer(ExtendedRenderer renderer, ISourceViewer iSourceViewer) {
//        sourceViewer = iSourceViewer;
//    }
//
//    protected String getPageHeader(PageInformation pageInformation, int pageIndex, int pageOffset) {
//        String result = "Page " + Integer.toString(pageIndex + 1);
//        try {
//            IDocument document = sourceViewer.getDocument();
//            int length = Math.min(15, document.getLength() - pageOffset);
//            result += " starting with \"" + document.get(pageOffset, length).trim() + "\"";
//        }
//        catch (BadLocationException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    protected String getPageFooter(PageInformation pageInformation, int pageIndex, int pageOffset) {
//        String result = "Page " + Integer.toString(pageIndex + 1);
//        try {
//            IDocument document = sourceViewer.getDocument();
//            int length = Math.min(15, document.getLength() - pageOffset);
//            result += " starting with \"" + document.get(pageOffset, length).trim() + "\"";
//        }
//        catch (BadLocationException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    public String getPageHeader(int pageIndex, int pageOffset) {
//        return null;
//    }
//
//    public String getPageFooter(int pageIndex, int pageOffset) {
//        return null;
//    }

	public void drawHeader(GC gc, ExtendedStyledText styledText, int offset,
			int pageStartY, Rectangle trim) {}

	public void decorateLine(GC gc, int layoutOffset, Point layoutLocation,
			TextLayout layout, Rectangle trim) {}

	public void drawFooter(GC gc, ExtendedStyledText styledText, int offset,
			int pageEndY, Rectangle trim) {}
}
