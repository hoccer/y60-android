package com.artcom.y60.gom;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONObject;

public class GomTestObserver implements GomObserver {

    private int              mCreateCount = 0;
    private int              mUpdateCount = 0;
    private int              mDeleteCount = 0;
    public LinkedList<Event> mEvents      = new LinkedList<Event>();
    private TestCase         mTest;

    public GomTestObserver(TestCase pTest) {
        mTest = pTest;
    }

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
        mTest.assertTrue("create not called", mCreateCount > 0);
    }

    public void assertDeleteCalled() {
        mTest.assertTrue("delete not called", mDeleteCount > 0);
    }

    public void assertUpdateCalled() {
        mTest.assertTrue("update not called", mUpdateCount > 0);
    }

    public void assertCreateNotCalled() {
        mTest.assertTrue("create called", mCreateCount == 0);
    }

    public void assertDeleteNotCalled() {
        mTest.assertTrue("delete called", mDeleteCount == 0);
    }

    public void assertUpdateNotCalled() {
        mTest.assertTrue("update called", mUpdateCount == 0);
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
        public String     path;

        public Event(JSONObject pData, String pPath) {

            data = pData;
            path = pPath;
        }
    }
}