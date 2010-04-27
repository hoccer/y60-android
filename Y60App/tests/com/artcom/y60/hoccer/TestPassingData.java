package com.artcom.y60.hoccer;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.data.GenericStreamableContent;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.data.StreamableString;
import com.artcom.y60.http.AsyncHttpRequestWithBody;
import com.artcom.y60.http.HttpHelper;

public class TestPassingData extends HocEventTestCase {

    private static final String LOG_TAG = "TestPassingData";
    private HocEvent            mEvent;

    public void testLonelySweepOutEvent() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        sweepOut.addCallback(eventCallback);
        assertEventIsAlive("sweepOut", sweepOut);

        TestHelper.assertMatches("event shuld have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", sweepOut.getResourceLocation());

        double lifetime = sweepOut.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        assertLifetimeDecreases(sweepOut, lifetime);

        assertEventIsExpired("sweepOut", sweepOut);
        assertEquals("lifetime should be down to zero", 0.0, sweepOut.getLifetime());
        assertDataHasBeenUploaded(sweepOut);
        assertTrue("should have got error callback", eventCallback.hadError);
    }

    public void testAbortingLonelySweepOutEvent() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        assertEventIsAlive("sweepOut", sweepOut);

        Thread.sleep(200);
        assertEquals("event should exist on server", 202, HttpHelper.getStatusCode(sweepOut
                .getResourceLocation()));
        sweepOut.abort();
        assertEquals("event should be removed from server", 404, HttpHelper.getStatusCode(sweepOut
                .getResourceLocation()));
    }

    public void testLonelySweepInEvent() throws Exception {
        mEvent = getPeer().sweepIn();
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        mEvent.addCallback(eventCallback);
        assertEventIsAlive("sweepIn", mEvent);

        TestHelper.assertMatches("event shuld have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", mEvent.getResourceLocation());

        double lifetime = mEvent.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        assertLifetimeDecreases(mEvent, lifetime);

        assertEventIsExpired("sweepIn", mEvent);
        assertEquals("lifetime should be down to zero", 0.0, mEvent.getLifetime());
        assertTrue("should have got error callback", eventCallback.hadError);
    }

    public void testLinkingSweepInAndOutEvents() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);

        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);

        assertEventIsLinked(sweepOut);
        assertEventIsLinked(sweepIn);
    }

    public void testTransferingStringBetweenSweepInAndOutEvents() throws Exception {

        GenericStreamableContent content = new GenericStreamableContent();
        content.setContentType("text/plain");
        content.setFilename("thedemofilename.txt");
        content.openOutputStream().write("my hocced text".getBytes(), 0, "my hocced text".length());

        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);

        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);

        assertEventIsLinked(sweepOut);
        assertEventIsLinked(sweepIn);

        assertDataHasBeenUploaded(sweepOut);
        assertDataHasBeenDownloaded(sweepIn, "my hocced text");
        assertEquals("mime type should be as expected", "text/plain", sweepIn.getData()
                .getContentType());
        assertEquals("filename should be as expected", "thedemofilename.txt", sweepIn.getData()
                .getFilename());

    }

    public void testTransferingImageBetweenSweepInAndOutEvents() throws Exception {

        GenericStreamableContent content = new GenericStreamableContent();
        content.setContentType("image/png");
        content.setFilename("thedemofilename.png");

        byte[] imageData = HttpHelper
                .getAsByteArray("http://www.artcom.de/templates/artcom/css/images/artcom_rgb_screen_193x22.png");
        content.openOutputStream().write(imageData, 0, imageData.length);

        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);

        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);

        assertEventIsLinked(sweepOut);
        assertEventIsLinked(sweepIn);

        assertDataHasBeenUploaded(sweepOut);
        assertDataHasBeenDownloaded(sweepIn, imageData);

        assertEquals("mime type of sweep out should be as expected", "image/png", sweepOut
                .getData().getContentType());
        assertEquals("mime type should be as expected", "image/png", sweepIn.getData()
                .getContentType());
        assertEquals("received filename should be as expected", "thedemofilename.png", sweepIn
                .getData().getFilename());
    }

    public void testOnSuccessNotificationWhenDownloadIsDoneBeforLinkEstablished() throws Exception {

        StreamableContent content = new StreamableString("hocci");
        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        sweepOut.addCallback(eventCallback);

        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);

        assertDataHasBeenUploaded(sweepOut);
        assertEventIsNotLinked(sweepOut);
        assertFalse("event should not be successful right now", eventCallback.wasSuccessful);

        assertEventIsLinked(sweepOut);
        assertTrue("event should now be successful", eventCallback.wasSuccessful);
    }

    public void testOnSuccessNotificationWhenDownloadIsDoneAfterLinkEstablished() throws Exception {

        RetainStreamableString content = new RetainStreamableString("hocci");
        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        sweepOut.addCallback(eventCallback);

        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);

        assertEventIsLinked(sweepOut);
        assertDataHasNotBeenUploaded(sweepOut);
        assertFalse("event should not be successful right now", eventCallback.wasSuccessful);

        content.releaseInputStream();
        assertDataHasBeenUploaded(sweepOut);
        assertTrue("event should now be successful", eventCallback.wasSuccessful);
    }

    private void assertDataHasNotBeenUploaded(final SweepOutEvent sweepOut) throws Exception {
        assertFalse("data should not have been uploaded", sweepOut.hasDataBeenUploaded());
    }

    private void assertDataHasBeenUploaded(final SweepOutEvent sweepOut) throws Exception {
        TestHelper.blockUntilTrue("uploader request should have been created", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return sweepOut.mDataUploader != null;
                    }
                });

        TestHelper.blockUntilTrue("upload should have finished", 10000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() throws Exception {
                return sweepOut.hasDataBeenUploaded();
            }
        });

        Method m = AsyncHttpRequestWithBody.class.getDeclaredMethod("getRequest", null);
        m.setAccessible(true);
        HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) m.invoke(
                sweepOut.mDataUploader, null);
        String body = request.getEntity().toString();

        Logger.v(LOG_TAG, "body", body);
        assertEquals("uploaded data should have correct content-type", sweepOut.getData()
                .getContentType(), body.substring(body.indexOf("Content-Type: ") + 14, body
                .indexOf("\r\nContent-Transfer-Encoding")));
        assertEquals("uploaded data should have correct should have correct filename", sweepOut
                .getData().getFilename(), body.substring(body.indexOf("filename=\"") + 10, body
                .indexOf("\"\r\nContent-Type")));
    }

    private void assertDataHasBeenDownloaded(final SweepInEvent sweepIn, String pExpectedData)
            throws Exception {
        assertDownloadIsDone(sweepIn);

        assertEquals("incomming data should be as expected", pExpectedData, sweepIn.getData()
                .toString());
    }

    private void assertDataHasBeenDownloaded(final SweepInEvent sweepIn, byte[] pExpectedData)
            throws Exception {
        assertDownloadIsDone(sweepIn);

        TestHelper.assertInputStreamEquals("incomming data should be as expected",
                new ByteArrayInputStream(pExpectedData), sweepIn.getData().openInputStream());
    }

    private void assertDownloadIsDone(final SweepInEvent sweepIn) throws Exception {
        TestHelper.blockUntilTrue("downloader request should have been created", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return sweepIn.mDataDownloader != null;
                    }
                });

        TestHelper.blockUntilTrue("download should have finished", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return sweepIn.hasDataBeenDownloaded();
                    }
                });
    }

    private void assertLifetimeDecreases(final HocEvent hocEvent, final double lifetime)
            throws Exception {
        TestHelper.blockUntilTrue("lifetime should be decreasing", 5000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return hocEvent.getLifetime() < lifetime && hocEvent.getLifetime() > 0;
                    }
                });
    }

}
