/**
 * 
 */
package com.artcom.y60.gom;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

class GomTestObserver implements GomObserver {

    private int mCreateCount = 0;
    private int mUpdateCount = 0;
    private int mDeleteCount = 0;
    public LinkedList<Event> mEvents = new LinkedList<Event>();

    public void reset() {
        mCreateCount = 0;
        mUpdateCount = 0;
        mDeleteCount = 0;
        mEvents = new LinkedList<Event>();
    }

    @Override
    public void onEntryCreated(String pPath, JSONObject pData) {

        mCreateCount += 1;
        mEvents.add(new Event(pData, pPath));
    }

    @Override
    public void onEntryDeleted(String pPath, JSONObject pData) {

        mDeleteCount += 1;
        mEvents.add(new Event(pData, pPath));
    }

    @Override
    public void onEntryUpdated(String pPath, JSONObject pData) {

        mUpdateCount += 1;
        mEvents.add(new Event(pData, pPath));
    }

    public void assertCreateCalled() {

        GomNotificationHelperTest.assertTrue("create not called", mCreateCount > 0);
    }

    public void assertDeleteCalled() {

        GomNotificationHelperTest.assertTrue("delete not called", mDeleteCount > 0);
    }

    public void assertUpdateCalled() {

        GomNotificationHelperTest.assertTrue("update not called", mUpdateCount > 0);
    }

    public void assertCreateNotCalled() {

        GomNotificationHelperTest.assertTrue("create called", mCreateCount == 0);
    }

    public void assertDeleteNotCalled() {

        GomNotificationHelperTest.assertTrue("delete called", mDeleteCount == 0);
    }

    public void assertUpdateNotCalled() {

        GomNotificationHelperTest.assertTrue("update called", mUpdateCount == 0);
    }

    public int getUpdateCount() {

        return mUpdateCount;
    }

    public int getDeleteCount() {

        return mDeleteCount;
    }

    public int getCreateCount() {

        return mCreateCount;
    }

    public String getPath() {

        return mEvents.getLast().path;
    }

    public JSONObject getData() {

        return mEvents.getLast().data;
    }

    public List<Event> getEvents() {

        return mEvents;
    }

    public class Event {

        public JSONObject data;
        public String path;

        public Event(JSONObject pData, String pPath) {

            data = pData;
            path = pPath;
        }
    }
}