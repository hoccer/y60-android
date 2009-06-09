package com.artcom.y60.gom;

import java.util.HashMap;

import junit.framework.TestCase;
import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;

public class HttpGomWrapperTest extends TestCase {

    private static final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/gom_http_wrapper_test";

    private static final String ATTR_URL = Constants.Gom.URI + TEST_BASE_PATH
            + ":show_me_the_value";

    private static final String ATTR_VALUE = "tralala";

    public HttpGomWrapperTest() {

        HttpHelper.putXML(ATTR_URL, "<attribute>" + ATTR_VALUE + "</attribute>");
    }

    public void testGetAttributeValue() {

        String value = GomHttpWrapper.getAttributeValue(Uri.parse(ATTR_URL));

        assertEquals("attribute value wasn't as expected", ATTR_VALUE, value);
    }

    public void testCreateNodeWithAttributes() {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_create_node_with_attributes";
        String nodePath = testPath + "/" + timestamp;
        String nodeUrl = Constants.Gom.URI + nodePath;

        String attr1 = "attribute_1";

        try {

            HttpHelper.get(nodeUrl);
            fail("Expected a 404 on observer " + nodeUrl + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("attribute_1", "mangosalat");

        GomHttpWrapper.createNodeWithAttributes(nodeUrl, data);

        assertNotNull("attribute should be in gom", HttpHelper.get(nodeUrl + ":" + attr1));

    }
}
