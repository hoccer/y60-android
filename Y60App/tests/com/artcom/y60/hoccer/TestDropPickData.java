package com.artcom.y60.hoccer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.data.StreamableString;

public class TestDropPickData extends HocEventTestCase {

    private static String LOG_TAG = "TestDropPickData";

    public void testDrop() throws Exception {

        DropEvent hoc = getPeer().drop(new StreamableString("my hocced data"), 30);
        blockUntilEventIsAlive("drop", hoc);

        TestHelper.assertMatches("event should have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", hoc.getResourceLocation());

        double lifetime = hoc.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 18, lifetime);
        blockUntilLifetimeDecreases(hoc, lifetime);

        blockUntilDataHasBeenUploaded(hoc);
        assertPollingHasStopped(hoc);
    }

    public void testEmptyPick() throws Exception {

        PickEvent hoc = getPeer().pick();
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        hoc.addCallback(eventCallback);
        blockUntilEventIsExpired("pick", hoc);

        TestHelper.assertMatches("event should have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", hoc.getResourceLocation());

        assertEquals("lifetime should be down to zero", 0.0, hoc.getLifetime());
        assertTrue("should have got error", hoc.hasError());
        assertEquals("status message", "Nothing to pick up from this location", hoc.getMessage());
        assertTrue("should have got error callback", eventCallback.hadError);
        assertPollingHasStopped(hoc);
    }

    public void testDropPickExampleFlow() throws Exception {

        DropEvent drop = getPeer().drop(new StreamableString("the dropped data"), 10);
        blockUntilEventIsAlive("drop", drop);
        blockUntilDataHasBeenUploaded(drop);
        TestHelper.assertGreater("lifetime should be fine", 8, drop.getLifetime());

        PickEvent pick = getPeer().pick();
        blockUntilEventIsExpired("pick", pick);
        blockUntilEventIsLinked(pick);

        TestHelper.assertGreater("lifetime should be fine", 2, drop.getLifetime());
        blockUntilEventIsExpired("drop", drop);

        JSONArray pieces = pick.getListOfPieces();
        assertNotNull(pieces);
        assertEquals("size", 1, pieces.length());
        JSONObject droppedObject = pieces.getJSONObject(0);
        assertEquals("content type", droppedObject.get("content_type"), "text/plain");
        assertEquals("content type", droppedObject.get("filename"), "data.txt");

        pick.downloadDataFrom(droppedObject.getString("uri"));
        blockUntilDataHasBeenDownloaded(pick, "the dropped data");

        assertPollingHasStopped(drop);
        assertPollingHasStopped(pick);
    }

    public void testPickingFileWithinEventListenerCallback() throws Exception {

        DropEvent drop = getPeer().drop(new StreamableString("first dropped data"), 10);
        blockUntilDataHasBeenUploaded(drop);

        final PickEvent pick = getPeer().pick();
        PickSecondFileCallback callback = new PickSecondFileCallback(pick);
        pick.addCallback(callback);

        blockUntilEventIsExpired("pick", pick);
        blockUntilEventIsLinked(pick);
        assertPollingHasStopped(pick);
        assertTrue("data downlaoded callback should have been called",
                callback.hasDataBeenDownloaded);
    }

    private class PickSecondFileCallback implements HocEventListener {
        public boolean          hasDataBeenDownloaded = false;
        private final PickEvent mEvent;

        PickSecondFileCallback(PickEvent event) {
            mEvent = event;
        }

        @Override
        public void onLinkEstablished() {
            Logger.v(LOG_TAG, "picking link is established");
            try {
                JSONObject droppedObject = mEvent.getListOfPieces().getJSONObject(1);
                assertEquals("content type", droppedObject.get("content_type"), "text/plain");
                assertEquals("content type", droppedObject.get("filename"), "data.txt");
                mEvent.downloadDataFrom(droppedObject.getString("uri"));
            } catch (JSONException e) {
                hasDataBeenDownloaded = false;
            }

        }

        @Override
        public void onDataExchanged(HocEvent hoc) {
            assertEquals("second dropped data", hoc.getData().toString());
            hasDataBeenDownloaded = true;
        }

        @Override
        public void onTransferProgress(double progress) {
        }

        @Override
        public void onFeedback(String message) {
        }

        @Override
        public void onError(HocEventException e) {
        }
    }
}
