package com.artcom.y60.hoccer;

import com.artcom.y60.TestHelper;
import com.artcom.y60.data.GenericStreamableContent;
import com.artcom.y60.data.StreamableString;
import com.artcom.y60.http.HttpHelper;

public class TestThrowCatchData extends HocEventTestCase {

    public void testLonelyThrow() throws Exception {

        ThrowEvent hocEvent = getPeer().throwIt(new StreamableString("my hocced data"));
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        hocEvent.addCallback(eventCallback);
        blockUntilEventIsAlive("throw", hocEvent);

        TestHelper.assertMatches("event should have a valid resource location", getPeer()
                .getRemoteServer()
                + "/events/\\w*", hocEvent.getResourceLocation());

        double lifetime = hocEvent.getRemainingLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        blockUntilLifetimeDecreases(hocEvent, lifetime);

        blockUntilEventIsExpired("throw", hocEvent);
        assertEquals("lifetime should be down to zero", 0.0, hocEvent.getRemainingLifetime());
        blockUntilDataHasBeenUploaded(hocEvent);
        assertTrue("should have got error callback", eventCallback.hadError);
        assertPollingHasStopped(hocEvent);
    }

    public void testCollisionOfTwoThrowersOneCatcher() throws Exception {

        StreamableString content = new StreamableString("provoke collision");

        final ThrowEvent throwEventA = getPeer().throwIt(content);
        blockUntilEventIsAlive("throwEventA", throwEventA);
        CatchEvent catchEvent = getPeer().catchIt();
        blockUntilEventIsAlive("catchEvent", catchEvent);

        blockUntilEventHasNumberOfPeers(throwEventA, 1);
        blockUntilEventHasNumberOfPeers(catchEvent, 1);

        assertEventIsNotLinked(throwEventA);
        assertEventIsNotLinked(catchEvent);

        final ThrowEvent throwEventB = getPeer().throwIt(content);
        blockUntilEventHasCollision("throwEventB", throwEventB);
        blockUntilEventHasCollision("throwEventA", throwEventA);
        blockUntilEventHasCollision("catchEvent", catchEvent);

        assertPollingHasStopped(catchEvent);
        assertPollingHasStopped(throwEventA);
        assertPollingHasStopped(throwEventB);
    }

    public void testThrowingImageDataToTwoCatchers() throws Exception {

        GenericStreamableContent content = new GenericStreamableContent();
        content.setContentType("image/png");
        content.setFilename("thedemofilename.png");

        byte[] imageData = HttpHelper
                .getAsByteArray("http://www.artcom.de/templates/artcom/css/images/artcom_rgb_screen_193x22.png");
        content.openOutputStream().write(imageData, 0, imageData.length);

        CatchEvent catchEventA = getPeer().catchIt();
        blockUntilEventIsAlive("catchEventA", catchEventA);
        final ThrowEvent throwEvent = getPeer().throwIt(content);
        blockUntilEventIsAlive("throwEvent", throwEvent);
        CatchEvent catchEventB = getPeer().catchIt();
        blockUntilEventIsAlive("catchEventB", catchEventB);

        blockUntilEventHasNumberOfPeers(throwEvent, 2);
        blockUntilEventHasNumberOfPeers(catchEventA, 2);
        blockUntilEventHasNumberOfPeers(catchEventB, 2);

        blockUntilEventIsLinked(throwEvent);
        blockUntilEventIsLinked(catchEventA);
        blockUntilEventIsLinked(catchEventB);

        blockUntilDataHasBeenUploaded(throwEvent);
        blockUntilDataHasBeenDownloaded(catchEventA, imageData);
        blockUntilDataHasBeenDownloaded(catchEventB, imageData);

        assertEquals("mime type of throw should be as expected", "image/png", throwEvent.getData()
                .getContentType());
        assertEquals("mime type should be as expected", "image/png", catchEventA.getData()
                .getContentType());
        assertEquals("received filename should be as expected", "thedemofilename.png", catchEventA
                .getData().getFilename());
        assertEquals("mime type should be as expected", "image/png", catchEventB.getData()
                .getContentType());
    }
}
