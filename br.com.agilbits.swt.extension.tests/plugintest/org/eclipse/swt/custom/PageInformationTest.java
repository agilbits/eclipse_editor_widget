package org.eclipse.swt.custom;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.graphics.Point;
import org.junit.Before;
import org.junit.Test;

public class PageInformationTest {
    private PageInformation pageInformation;

    @Before
    public void setUp() {
        pageInformation = new PageInformation(new Point(20, 21), 8.5, 11);
    }

    @Test
    public void shouldCalculatePageWidthBasedOnDPI() throws Exception {
        assertEquals(170, pageInformation.getPageWidth());
    }

    @Test
    public void shouldCalculatePageHeightBasedOnDPI() throws Exception {
        assertEquals(231, pageInformation.getPageHeight());
    }
}
