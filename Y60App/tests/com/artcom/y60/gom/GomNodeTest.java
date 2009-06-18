package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.TestHelper;

public class GomNodeTest extends AndroidTestCase {

    // Constants ---------------------------------------------------------

    static final String NAME       = "node";

    static final String PATH       = GomTestConstants.FIXTURES + "gom_node_test/" + NAME;

    static final Uri    NODE_URL   = Uri.parse(Constants.Gom.URI + PATH);

    static final String ATTR_NAME  = "attribute";

    static final String ATTR_VALUE = "value";

    static final String ATTR_PATH  = PATH + ":" + ATTR_NAME;

    static final Uri    ATTR_URL   = Uri.parse(Constants.Gom.URI + ATTR_PATH);

    static final String CHILD_NAME = "child";

    static final String CHILD_PATH = PATH + "/" + CHILD_NAME;

    static final Uri    CHILD_URL  = Uri.parse(Constants.Gom.URI + CHILD_PATH);

    // Instance Variables ------------------------------------------------

    private GomNode     mTestNode;

    // Constructors ------------------------------------------------------

    public GomNodeTest() {

        // write fixtures to the GOM:
        HttpHelper.postXML(CHILD_URL.toString(), "<node/>");
        HttpHelper.putXML(ATTR_URL.toString(), "<attribute>" + ATTR_VALUE + "</attribute>");
    }

    // Public Instance Methods -------------------------------------------

    public void setUp() throws GomException {

        final GomProxyHelper helper = new GomProxyHelper(getContext(), null);

        TestHelper.blockUntilTrue("GOM not bound", 2000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return helper.isBound();
            }
        });

        mTestNode = helper.getNode(PATH);
    }

    public void testGetName() {

        assertEquals(NAME, mTestNode.getName());
    }

    public void testGetPath() {

        assertEquals(PATH, mTestNode.getPath());
    }

    public void testGetChildNode() throws Exception {

        GomEntry entry = mTestNode.getNode(CHILD_NAME);

        assertNotNull(entry);
        assertEquals(CHILD_NAME, entry.getName());
        assertEquals(CHILD_PATH, entry.getPath());
        assertTrue(entry instanceof GomNode);
    }

    public void testGetAttribute() throws Exception {

        GomEntry entry = mTestNode.getEntry(ATTR_NAME);

        assertNotNull(entry);
        assertEquals(ATTR_NAME, entry.getName());
        assertEquals(ATTR_PATH, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals(ATTR_VALUE, ((GomAttribute) entry).getValue());
    }

    public void testGetEntry() throws GomException {

        GomEntry entry = mTestNode.getEntry(CHILD_NAME);
        assertNotNull(entry);
        assertEquals(CHILD_NAME, entry.getName());
        assertEquals(CHILD_PATH, entry.getPath());
        assertTrue(entry instanceof GomNode);

        entry = mTestNode.getEntry(ATTR_NAME);
        assertNotNull(entry);
        assertEquals(ATTR_NAME, entry.getName());
        assertEquals(ATTR_PATH, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals(ATTR_VALUE, ((GomAttribute) entry).getValue());
    }

    public void testAttributes() throws GomException {

        Set<GomAttribute> attrs = mTestNode.attributes();

        Map<String, String> expected = new HashMap<String, String>();
        expected.put(ATTR_NAME, ATTR_VALUE);

        assertEquals(expected.size(), attrs.size());

        for (GomAttribute attr : attrs) {

            assertTrue(expected.containsKey(attr.getName()));
            assertEquals(expected.get(attr.getName()), attr.getValue());
        }
    }

    public void testEntries() throws GomException {

        Set<GomEntry> entries = mTestNode.entries();

        Map<String, String> expAtts = new HashMap<String, String>();
        expAtts.put(ATTR_NAME, ATTR_VALUE);
        Set<String> expNodes = new HashSet<String>();
        expNodes.add(CHILD_NAME);

        assertEquals(expAtts.size() + expNodes.size(), entries.size());

        for (GomEntry entry : entries) {

            if (entry instanceof GomAttribute) {

                GomAttribute attr = (GomAttribute) entry;
                assertTrue(expAtts.containsKey(attr.getName()));
                assertEquals(expAtts.get(attr.getName()), attr.getValue());
            }
        }

        for (GomEntry entry : entries) {

            if (entry instanceof GomNode) {

                GomNode node = (GomNode) entry;
                assertTrue(expNodes.contains(node.getName()));
            }
        }
    }

    public void testKeys() throws GomException {

        Set<String> keys = mTestNode.entryNames();

        Set<String> expected = new HashSet<String>();
        expected.add(ATTR_NAME);
        expected.add(CHILD_NAME);

        assertEquals(expected.size(), keys.size());

        for (String key : keys) {

            assertTrue(expected.contains(key));
        }
    }

    public void testNodes() throws GomException {

        Set<GomNode> nodes = mTestNode.nodes();

        Set<String> expected = new HashSet<String>();
        expected.add(CHILD_NAME);

        assertEquals(expected.size(), nodes.size());

        for (GomNode node : nodes) {

            assertTrue(expected.contains(node.getName()));
        }
    }
}
