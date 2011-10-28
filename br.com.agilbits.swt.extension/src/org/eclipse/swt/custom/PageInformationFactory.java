package org.eclipse.swt.custom;


public class PageInformationFactory {
    public static PageInformation getLetterPage() {
        PageInformation pageInformation = new PageInformation(8.5, 11);
        pageInformation.setDrawPageBreaks(true);
        return pageInformation;
    }

    public static PageInformation getA4Page() {
        PageInformation pageInformation = new PageInformation(8.3, 11.7);
        pageInformation.setDrawPageBreaks(true);
        return pageInformation;
    }
}
