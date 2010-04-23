package com.artcom.y60.hoccer;

import java.io.ByteArrayInputStream;

import com.artcom.y60.TestHelper;
import com.artcom.y60.data.GenericStreamableContent;
import com.artcom.y60.data.StreamableString;
import com.artcom.y60.http.HttpHelper;

public class TestPassingData extends HocEventTestCase {

    private static final String LOG_TAG = "TestPassingData";
    private HocEvent            mEvent;

    public void testLonelySweepOutEvent() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
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

    public void testLonelySweepInEvent() throws Exception {
        mEvent = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", mEvent);

        TestHelper.assertMatches("event shuld have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", mEvent.getResourceLocation());

        double lifetime = mEvent.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        assertLifetimeDecreases(mEvent, lifetime);

        assertEventIsExpired("sweepIn", mEvent);
        assertEquals("lifetime should be down to zero", 0.0, mEvent.getLifetime());
    }

    public void testLinkingSweepInAndOutEvents() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);

        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);
    }

    public void testTransferingStringBetweenSweepInAndOutEvents() throws Exception {

        GenericStreamableContent textContent = new GenericStreamableContent();
        textContent.setContentType("text/plain");
        textContent.write("my hocced text".getBytes(), 0, "my hocced text".length());

        final SweepOutEvent sweepOut = getPeer().sweepOut(textContent);
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);

        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);

        assertDataHasBeenUploaded(sweepOut);
        assertDataHasBeenDownloaded(sweepIn, "my hocced text");
        assertEquals("mime type should be as expected", "text/plain", sweepIn.getData()
                .getContentType());
    }

    public void testTransferingImageBetweenSweepInAndOutEvents() throws Exception {

        GenericStreamableContent imageContent = new GenericStreamableContent();
        imageContent.setContentType("image/png");

        byte[] imageData = HttpHelper
                .getAsByteArray("http://www.artcom.de/templates/artcom/css/images/artcom_rgb_screen_193x22.png");
        imageContent.write(imageData, 0, imageData.length);

        final SweepOutEvent sweepOut = getPeer().sweepOut(imageContent);
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);

        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);

        assertDataHasBeenUploaded(sweepOut);
        assertDataHasBeenDownloaded(sweepIn, imageData);

        assertEquals("mime type of sweep out should be as expected", "image/png", sweepOut
                .getData().getContentType());
        assertEquals("mime type should be as expected", "image/png", sweepIn.getData()
                .getContentType());
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
                new ByteArrayInputStream(pExpectedData), sweepIn.getData().getStream());
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
}
