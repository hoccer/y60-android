package com.artcom.y60.dc;




import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.test.ServiceTestCase;

import com.artcom.y60.Constants;
import com.artcom.y60.IntentExtraKeys;

public class DeviceControllerHandlerTest extends ServiceTestCase<DeviceControllerService> {

    public DeviceControllerHandlerTest() {
        super(DeviceControllerService.class);
        // TODO Auto-generated constructor stub
    }
    
    public void testHandleGomNotificationCreate() throws Exception {
    
        checkBroadcastIntent("create");
    }
    
    public void testHandleGomNotificationUpdate() throws Exception {
        
        checkBroadcastIntent("update");
    }
    
    public void testHandleGomNotificationDelete() throws Exception {
        
        checkBroadcastIntent("delete");
    }
    
    private void checkBroadcastIntent(String pOperation) throws IOException, JSONException, HandlerException{ 
    
        String timestamp = String.valueOf(System.currentTimeMillis());
              
        MockService             mockService = new MockService();
        DeviceControllerHandler handler     = new DeviceControllerHandler(mockService);
        
        String                  nodePath    = "/hans/peter";
        String                  jsonStr     = createJsonContent(pOperation, nodePath, timestamp);
        MockHttpServletRequest  mockRequest = new MockHttpServletRequest(Constants.Network.GNP_TARGET, jsonStr);
        handler.handle(Constants.Network.GNP_TARGET, mockRequest, null, 0);
        
        Intent intent = mockService.getBroadcastIntent();
        
        assertNotNull("no broadcast was sent", intent);
        
        String actualOp = intent.getStringExtra(IntentExtraKeys.NOTIFICATION_OPERATION);
        assertEquals("operation is not: " + actualOp, pOperation, actualOp);
        
        String expectedPath = nodePath + ":" + timestamp;
        String actualPath   = intent.getStringExtra(IntentExtraKeys.NOTIFICATION_PATH);
        assertEquals("path is not as expected: "+actualPath, expectedPath, actualPath );
        
        JSONObject jsonData     = new JSONObject(jsonStr);
        JSONObject jsonOp       = jsonData.getJSONObject(pOperation);
        String     actualOpData = intent.getStringExtra(IntentExtraKeys.NOTIFICATION_DATA_STRING);
        assertEquals("unexpected JSON content: "+actualOpData, jsonOp.toString(), actualOpData);
    }
    
    private String createJsonContent(String pOperation, String pNode, String pAttributeName ){
        
        JSONObject jsonAttr = new JSONObject();
        JSONObject jsonOperation = new JSONObject();
        JSONObject jsonTmp = new JSONObject();
        
        
        try {
            jsonAttr.put("name", pAttributeName);
            jsonAttr.put("node", pNode);
            jsonAttr.put("value", "mango");
            jsonAttr.put("type", "string");
            
            jsonOperation.put("attribute", jsonAttr);                        
            
            jsonTmp.put("uri", pNode + ":" + pAttributeName);
            jsonTmp.put(pOperation, jsonOperation);
            
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        
        return jsonTmp.toString();
        
    }

}
