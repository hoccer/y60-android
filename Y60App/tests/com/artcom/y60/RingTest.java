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

        assertEquals("null!", ring.getCurrent().toString());
        ring.jumpTo(0);
        assertEquals("null!", ring.getCurrent().toString());

        ring.jumpTo(3);
        assertEquals("null!", ring.getCurrent().toString());

        ring = new DemoRing();
        ring.add(new StringBuffer("null"));

        assertEquals("null!", ring.getCurrent().toString());
        ring.stepForward();
        assertEquals("null!", ring.getCurrent().toString());

    }

    public void testInsertingNullObjects() {
        Ring ring = new Ring<String>();

        ring.add("one");
        ring.add(null);
        ring.add("two");

        assertEquals("one", ring.getCurrent());
        ring.stepForward();
        assertNull(ring.getCurrent());
        ring.stepForward();
        assertEquals("two", ring.getCurrent());
        ring.jumpTo(1);
        assertNull(ring.getCurrent());
    }
}
