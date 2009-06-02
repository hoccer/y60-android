/**
 * 
 */
package com.artcom.y60.gom;

import org.json.JSONObject;

class GomTestObserver implements GomObserver {

    private boolean mCreateCalled = false;
    private boolean mUpdateCalled = false;
    private boolean mDeleteCalled = false;
    private JSONObject mData;
    private String mPath;

    public void reset(){
        mCreateCalled = false;
        mUpdateCalled = false;
        mDeleteCalled = false;
    }
    
    @Override
    public void onEntryCreated(String pPath, JSONObject pData) {

        mCreateCalled = true;
        mData = pData;
        mPath = pPath;
    }

    @Override
    public void onEntryDeleted(String pPath, JSONObject pData) {

        mDeleteCalled = true;
        mData = pData;
        mPath = pPath;
    }

    @Override
    public void onEntryUpdated(String pPath, JSONObject pData) {

        mUpdateCalled = true;
        mData = pData;
        mPath = pPath;
    }

    public void assertCreateCalled() {

        GomNotificationHelperTest.assertTrue("create not called", mCreateCalled);
    }

    public void assertDeleteCalled() {

        GomNotificationHelperTest.assertTrue("delete not called", mDeleteCalled);
    }

    public void assertUpdateCalled() {

        GomNotificationHelperTest.assertTrue("update not called", mUpdateCalled);
    }

    public void assertCreateNotCalled() {

        GomNotificationHelperTest.assertTrue("create called", !mCreateCalled);
    }

    public void assertDeleteNotCalled() {

        GomNotificationHelperTest.assertTrue("delete called", !mDeleteCalled);
    }

    public void assertUpdateNotCalled() {

        GomNotificationHelperTest.assertTrue("update called", !mUpdateCalled);
    }

    public String getPath() {

        return mPath;
    }

    public JSONObject getData() {

        return mData;
    }
}