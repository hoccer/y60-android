package com.artcom.y60.gom;

import junit.framework.TestCase;
import android.net.Uri;

public class GomReferenceTest extends TestCase {

    public void testNodeReference() {

        String baseUrl = "http://testmich";
        String parentPath = "/bla";
        String nodeName = "blubb";
        String nodePath = parentPath + "/" + nodeName;
        String nodeUrl = baseUrl + nodePath;

        GomReference node = new GomReference(nodeUrl);
        assertEquals(baseUrl, node.baseUrl().toString());
        assertFalse(node.isAttribute());
        assertTrue(node.isNode());
        assertFalse(node.isRoot());
        assertEquals(nodeName, node.name());
        assertEquals(parentPath, node.parent().path());
        assertEquals(baseUrl + parentPath, node.parent().url().toString());
        assertEquals(nodePath, node.path());
        assertEquals(nodeUrl, node.toString());
        assertEquals(nodeUrl, node.url().toString());
    }

    public void testAttributeReference() {

        String baseUrl = "http://testmich";
        String parentPath = "/bla";
        String attrName = "blubb";
        String attrPath = parentPath + ":" + attrName;
        String attrUrl = baseUrl + attrPath;

        GomReference attr = new GomReference(attrUrl);
        assertEquals(baseUrl, attr.baseUrl().toString());
        assertTrue(attr.isAttribute());
        assertFalse(attr.isNode());
        assertFalse(attr.isRoot());
        assertEquals(attrName, attr.name());
        assertEquals(parentPath, attr.parent().path());
        assertEquals(baseUrl + parentPath, attr.parent().url().toString());
        assertEquals(attrPath, attr.path());
        assertEquals(attrUrl, attr.toString());
        assertEquals(attrUrl, attr.url().toString());
    }

    public void testRootReference() {

        String baseUrl = "http://testmich";
        String rootPath = "/";
        String rootUrl = baseUrl + rootPath;

        GomReference root = new GomReference(rootUrl);
        assertEquals(baseUrl, root.baseUrl().toString());
        assertFalse(root.isAttribute());
        assertTrue(root.isNode());
        assertTrue(root.isRoot());
        assertEquals(rootPath, root.name());
        assertNull(root.parent());
        assertEquals(rootPath, root.path());
        assertEquals(rootUrl, root.toString());
        assertEquals(rootUrl, root.url().toString());
    }

    public void testConstructFromSegments() {

        GomReference expected = new GomReference("http://some/really/very/long/url");
        GomReference actual = new GomReference(Uri.parse("http://some"), "really", "very", "long",
                        "url");

        assertEquals(expected, actual);
    }
}
