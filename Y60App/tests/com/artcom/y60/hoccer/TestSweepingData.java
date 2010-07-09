package com.artcom.y60.hoccer;

import com.artcom.y60.TestHelper;
import com.artcom.y60.data.GenericStreamableContent;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.data.StreamableString;
import com.artcom.y60.http.HttpHelper;

public class TestSweepingData extends HocEventTestCase {

    private static final String LOG_TAG = "TestSweepingData";
    private HocEvent            mEvent;

    public void testLonelySweepOutEvent() throws Exception {
        SweepOutEvent hocEvent = getPeer().sweepOut(new StreamableString("my hocced data"));
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        hocEvent.addCallback(eventCallback);
        blockUntilEventIsAlive("sweepOut", hocEvent);

        TestHelper.assertMatches("event shuld have a valid resource location", getPeer()
                .getRemoteServer()
                + "/events/\\w*", hocEvent.getResourceLocation());

        double lifetime = hocEvent.getRemainingLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        blockUntilLifetimeDecreases(hocEvent, lifetime);

        blockUntilEventIsExpired("sweepOut", hocEvent);
        assertEquals("lifetime should be down to zero", 0.0, hocEvent.getRemainingLifetime());
        blockUntilDataHasBeenUploaded(hocEvent);

        assertTrue("should have got error callback", eventCallback.hadError);
        assertPollingHasStopped(hocEvent);
    }

    public void testAbortingLonelySweepOutEvent() throws Exception {
        final SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        sweepOut.addCallback(eventCallback);

        blockUntilEventIsAlive("sweepOut", sweepOut);

        Thread.sleep(200);
        assertEquals("event should exist on server", 202, HttpHelper.getStatusCode(sweepOut
                .getResourceLocation()));
        sweepOut.abort();

        TestHelper.blockUntilEquals("event should be removed from server", 2000, 410,
                new TestHelper.Measurement() {

                    @Override
                    public Object getActualValue() throws Exception {
                        return HttpHelper.getStatusCode(sweepOut.getResourceLocation());
                    }
                });
        assertPollingHasStopped(sweepOut);
        assertTrue("should have been notified about error", eventCallback.hadError);
    }

    public void testLonelySweepInEvent() throws Exception {
        mEvent = getPeer().sweepIn();
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        mEvent.addCallback(eventCallback);
        blockUntilEventIsAlive("sweepIn", mEvent);

        TestHelper.assertMatches("event shuld have a valid resource location", getPeer()
                .getRemoteServer()
                + "/events/\\w*", mEvent.getResourceLocation());

        double lifetime = mEvent.getRemainingLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        blockUntilLifetimeDecreases(mEvent, lifetime);

        blockUntilEventIsExpired("sweepIn", mEvent);
        assertEquals("lifetime should be down to zero", 0.0, mEvent.getRemainingLifetime());
        assertTrue("should have got error callback", eventCallback.hadError);
    }

    public void testTransferingStringBetweenSweepInAndOutEvents() throws Exception {

        GenericStreamableContent content = new GenericStreamableContent();
        content.setContentType("text/plain");
        content.setFilename("thedemofilename.txt");
        content.openOutputStream().write("my hocced text".getBytes(), 0, "my hocced text".length());

        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        blockUntilEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        blockUntilEventIsAlive("sweepIn", sweepIn);

        blockUntilEventHasNumberOfPeers(sweepOut, 1);
        blockUntilEventHasNumberOfPeers(sweepIn, 1);

        blockUntilEventIsLinked(sweepOut);
        blockUntilEventIsLinked(sweepIn);

        blockUntilDataHasBeenUploaded(sweepOut);
        blockUntilDataHasBeenDownloaded(sweepIn, "my hocced text");
        assertEquals("mime type should be as expected", "text/plain", sweepIn.getData()
                .getContentType());
        assertEquals("filename should be as expected", "thedemofilename.txt", sweepIn.getData()
                .getFilename());
        assertPollingHasStopped(sweepIn);
        assertPollingHasStopped(sweepOut);
    }

    public void testTransferingImageBetweenSweepInAndOutEvents() throws Exception {

        GenericStreamableContent content = new GenericStreamableContent();
        content.setContentType("image/png");
        content.setFilename("thedemofilename.png");

        byte[] imageData = HttpHelper
                .getAsByteArray("http://www.artcom.de/templates/artcom/css/images/artcom_rgb_screen_193x22.png");
        content.openOutputStream().write(imageData, 0, imageData.length);

        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        blockUntilEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        blockUntilEventIsAlive("sweepIn", sweepIn);

        blockUntilEventHasNumberOfPeers(sweepOut, 1);
        blockUntilEventHasNumberOfPeers(sweepIn, 1);

        blockUntilEventIsLinked(sweepOut);
        blockUntilEventIsLinked(sweepIn);

        blockUntilDataHasBeenUploaded(sweepOut);
        blockUntilDataHasBeenDownloaded(sweepIn, imageData);

        assertEquals("mime type of sweep out should be as expected", "image/png", sweepOut
                .getData().getContentType());
        assertEquals("mime type should be as expected", "image/png", sweepIn.getData()
                .getContentType());
        assertEquals("received filename should be as expected", "thedemofilename.png", sweepIn
                .getData().getFilename());
    }

    public void testOnSuccessNotificationWhenUploadIsDoneBeforLinkEstablished() throws Exception {

        StreamableContent content = new StreamableString("hocci");
        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        sweepOut.addCallback(eventCallback);

        blockUntilEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        blockUntilEventIsAlive("sweepIn", sweepIn);

        blockUntilDataHasBeenUploaded(sweepOut);
        assertEventIsNotLinked(sweepOut);
        assertFalse("event should not be successful right now", eventCallback.wasSuccessful);

        blockUntilEventIsLinked(sweepOut);
        assertTrue("event should now be successful", eventCallback.wasSuccessful);
    }

    public void testOnSuccessNotificationWhenUploadIsDoneAfterLinkEstablished() throws Exception {

        RetainStreamableString content = new RetainStreamableString("hocci");
        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        sweepOut.addCallback(eventCallback);

        blockUntilEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        blockUntilEventIsAlive("sweepIn", sweepIn);

        blockUntilEventIsLinked(sweepOut);
        assertDataHasNotBeenUploaded(sweepOut);
        assertFalse("event should not be successful right now", eventCallback.wasSuccessful);

        content.releaseInputStream();
        blockUntilDataHasBeenUploaded(sweepOut);
        blockUntilEventIsSuccessful(eventCallback, 1000);

    }

    public void testOnSuccessNotificationWhenDownloadIsDoneAndLinkEstablished() throws Exception {

        StreamableContent content = new StreamableString("hocci");
        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        blockUntilEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        sweepIn.addCallback(eventCallback);
        blockUntilEventIsAlive("sweepIn", sweepIn);

        assertDataHasNotBeenDownloaded(sweepIn);
        assertFalse("event should not be successful right now", eventCallback.wasSuccessful);
        assertEventIsNotLinked(sweepIn);
        assertFalse("event should not be successful right now", eventCallback.wasSuccessful);

        blockUntilEventIsLinked(sweepIn);
        assertDataHasNotBeenDownloaded(sweepIn);
        assertFalse("event should not be successful right now", eventCallback.wasSuccessful);

        blockUntilDataHasBeenDownloaded(sweepIn, "hocci");
        Thread.sleep(2000);
        assertTrue("event should now be successful", eventCallback.wasSuccessful);
    }
}
