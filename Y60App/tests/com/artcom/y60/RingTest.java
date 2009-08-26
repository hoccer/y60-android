package com.artcom.y60;

import android.test.AndroidTestCase;

public class RingTest extends AndroidTestCase {

    private class DemoRing extends Ring<StringBuffer> {

        @Override
        protected void onChange(StringBuffer pOldActiveMember, StringBuffer pNextActiveMember) {
            getCurrent().append("!");
        }
    }

    public void testGettingCurrentInOnChange() throws Exception {

        DemoRing ring = new DemoRing();
        ring.add(new StringBuffer("null"));
        ring.add(new StringBuffer("one"));
        ring.add(new StringBuffer("two"));

        ring.jumpTo(0);
        assertEquals("null!", ring.getCurrent().toString());

        ring.stepForward();
        assertEquals("one!", ring.getCurrent().toString());

        ring.stepForward();
        assertEquals("two!", ring.getCurrent().toString());

        ring.stepForward();
        assertEquals("null!!", ring.getCurrent().toString());

        ring.stepBackward();
        assertEquals("two!!", ring.getCurrent().toString());
    }

    public void testNoChangeCallbackWhenAlreadyActive() {
        DemoRing ring = new DemoRing();
        ring.add(new StringBuffer("null"));
        ring.add(new StringBuffer("one"));
        ring.add(new StringBuffer("two"));

        assertEquals("null", ring.getCurrent().toString());
        ring.jumpTo(0);
        assertEquals("null", ring.getCurrent().toString());

        ring.jumpTo(3);
        assertEquals("null", ring.getCurrent().toString());

        ring = new DemoRing();
        ring.add(new StringBuffer("null"));

        assertEquals("null", ring.getCurrent().toString());
        ring.stepForward();
        assertEquals("null", ring.getCurrent().toString());

    }

}
