package com.artcom.y60.gom;

import android.net.Uri;
import android.os.RemoteException;
import android.test.AndroidTestCase;

import com.artcom.y60.Logger;

public class GomAttributeTest extends AndroidTestCase {

    // Constants ---------------------------------------------------------

    static final String NAME = "attribute";

    static final String PATH = GomTestConstants.FIXTURES + "gom_attribute_test:" + NAME;

    static final String VALUE = PATH;

    // Instance Variables ------------------------------------------------

    private GomAttribute mTestAttr;

    // Public Instance Methods -------------------------------------------

    public void setUp() {

        GomProxyHelper helper = new GomProxyHelper(getContext(), null);

        for (int i = 0; i < 200; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ix) {
            }
        }

        mTestAttr = helper.getAttribute(PATH);
    }

    public void testGetValue() {

        assertEquals(VALUE, mTestAttr.getValue());
    }

    public void testPutValue() {

        mTestAttr.putValue("changed value");
        assertEquals("changed value", mTestAttr.getValue());
        mTestAttr.putValue(VALUE);
        assertEquals(VALUE, mTestAttr.getValue());
    }

    public void testPutOrCreateValue() throws RemoteException {

        String attrName = "not_existing_attribute";
        GomNode parent = mTestAttr.getNode();

        if (parent.hasAttribute(attrName)) {
            parent.deleteAttribute(attrName);
        }
        parent.refresh();
        assertTrue("attribute should not exist", !parent.hasAttribute(attrName));

        Uri attrUri = Uri.parse(parent.getUri() + ":" + attrName);
        GomHttpWrapper.updateOrCreateAttribute(attrUri, "the putted value");
        parent.refresh();
        assertTrue("attribute should exist", parent.hasAttribute(attrName));
        assertEquals("the putted value", parent.getAttribute(attrName).getValue());

        parent.deleteAttribute(attrName);
        parent.refresh();
        assertTrue("attribute should again not exist", !parent.hasAttribute(attrName));
    }

    public void testGetPath() {

        assertEquals(PATH, mTestAttr.getPath());
    }

    public void testGetName() {

        assertEquals(NAME, mTestAttr.getName());
    }

    public void testResolveReference() {

        GomEntry entry = mTestAttr.resolveReference();

        assertNotNull(entry);

        Logger.v("GomAttributeTest", "resolveReference got entry with path ", entry.getPath());

        assertEquals(VALUE, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals(VALUE, ((GomAttribute) entry).getValue());
    }
}
