package com.artcom.y60.hoccer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.data.StreamableString;

import android.test.suitebuilder.annotation.Suppress;

public class TestDropPickData extends HocEventTestCase {

    private static String LOG_TAG = "TestDropPickData";

    public void testDrop() throws Exception {

        DropEvent hoc = getPeer().dropIt(new StreamableString("my hocced data"), 20);
        blockUntilEventIsAlive("drop", hoc);

        TestHelper.assertMatches("event should have a valid resource location", getPeer()
                .getRemoteServer()
                + "/events/\\w*", hoc.getResourceLocation());

        double lifetime = hoc.getRemainingLifetime();
        TestHelper.assertGreater("lifetime should be fine", 18, lifetime);
        blockUntilLifetimeDecreases(hoc, lifetime);

        blockUntilDataHasBeenUploaded(hoc);
        assertPollingHasStopped(hoc);
    }

    @Suppress
    public void testEmptyPick() throws Exception {

        PickEvent hoc = getPeer().pickIt();
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        hoc.addCallback(eventCallback);
        blockUntilEventIsExpired("pick", hoc);

        TestHelper.assertMatches("event should have a valid resource location", getPeer()
                .getRemoteServer()
                + "/events/\\w*", hoc.getResourceLocation());

        blockUntilLifetimeIsDownTo(hoc, 0);
        assertEquals("lifetime should be down to zero", 0.0, hoc.getRemainingLifetime());
        assertTrue("should have got error", hoc.hasError());
        assertEquals("status message", "Nothing to pick up from this location", hoc.getMessage());
        assertTrue("should have got error callback", eventCallback.hadError);
        assertPollingHasStopped(hoc);
    }

    public void testDropAndPick() throws Exception {

        DropEvent drop = getPeer().dropIt(new StreamableString("the dropped data"), 5);
        blockUntilEventIsAlive("drop", drop);
        blockUntilDataHasBeenUploaded(drop);
        TestHelper.assertGreater("lifetime should be fine", 4, drop.getRemainingLifetime());

        PickEvent pick = getPeer().pickIt();
        blockUntilEventIsExpired("pick", pick);
        blockUntilEventIsLinked(pick);

        TestHelper.assertGreater("lifetime should be fine", 2, drop.getRemainingLifetime());
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

        DropEvent drop = getPeer().dropIt(new StreamableString("dropped data"), 10);
        blockUntilDataHasBeenUploaded(drop);

        final PickEvent pick = getPeer().pickIt();
        PickSecondFileCallback callback = new PickSecondFileCallback(pick);
        pick.addCallback(callback);

        blockUntilEventIsExpired("pick", pick);
        blockUntilEventIsLinked(pick);
        assertPollingHasStopped(pick);
        assertTrue("'data downlaoded' should have been called", callback.hasDataBeenDownloaded);
        assertEquals("dropped data", pick.getData().toString());
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
                JSONObject droppedObject = mEvent.getListOfPieces().getJSONObject(0);
                assertEquals("content type", droppedObject.get("content_type"), "text/plain");
                assertEquals("content type", droppedObject.get("filename"), "data.txt");
                Logger.v(LOG_TAG, "start donwload");
                mEvent.downloadDataFrom(droppedObject.getString("uri"));
            } catch (JSONException e) {
                hasDataBeenDownloaded = false;
                Logger.e(LOG_TAG, e);
            }

        }

        @Override
        public void onDataExchanged(HocEvent hoc) {
            Logger.v(LOG_TAG, "pick completed");
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

        @Override
        public void onAbort(HocEvent hoc) {
            // TODO Auto-generated method stub

        }
    }
}
