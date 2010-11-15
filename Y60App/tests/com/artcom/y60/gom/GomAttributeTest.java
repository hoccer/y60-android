package com.artcom.y60.gom;

import android.test.AndroidTestCase;

import com.artcom.y60.Constants;

public class GomAttributeTest extends AndroidTestCase {

    static final String BASE_PATH = GomTestConstants.FIXTURES + "gom_attribute_test";

    private GomAttribute mTestAttr;
    private GomReference mTestRef;
    private String mValue;

    @Override
    public void setUp() throws Exception {
        GomProxyHelper helper = new GomProxyHelper(getContext(), null);
        for (int i = 0; i < 200; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ix) {
            }
        }
        mTestRef = new GomReference(Constants.Gom.URI + BASE_PATH + "/" + getName() + ":"
                        + System.currentTimeMillis());
        mValue = mTestRef.path();
        GomHttpWrapper.updateOrCreateAttribute(mTestRef.url().toString(), mValue);
        mTestAttr = helper.getAttribute(mTestRef.path());
    }

    public void testGetValue() {
        assertEquals(mValue, mTestAttr.getValue());
    }

    public void testPutValue() throws Exception {
        mTestAttr.putValue("changed value");
        assertEquals("changed value", mTestAttr.getValue());
        mTestAttr.putValue(mValue);
        assertEquals(mValue, mTestAttr.getValue());
    }

    public void testGetPath() {

        assertEquals(mTestRef.path(), mTestAttr.getPath());
    }

    public void testGetName() {

        assertEquals(mTestRef.name(), mTestAttr.getName());
    }

    public void testResolveReference() throws GomException {

        GomEntry entry = mTestAttr.resolveReference();

        assertEquals(mValue, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals(mValue, ((GomAttribute) entry).getValue());
    }
}
