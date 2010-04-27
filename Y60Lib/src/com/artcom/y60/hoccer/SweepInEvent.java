package com.artcom.y60.hoccer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.data.UnknownContentTypeException;
import com.artcom.y60.http.AsyncHttpGet;
import com.artcom.y60.http.HttpHelper;
import com.artcom.y60.http.HttpResponseHandler;

public class SweepInEvent extends HocEvent {

    private static String LOG_TAG         = "SweepInEvent";
    AsyncHttpGet          mDataDownloader = null;
    private final Peer    mPeer;

    SweepInEvent(HocLocation pLocation, DefaultHttpClient pHttpClient, Peer pPeer) {
        super(pLocation, pHttpClient);
        mPeer = pPeer;
    }

    @Override
    protected void updateStatusFromJson(JSONObject status) throws JSONException, IOException {
        super.updateStatusFromJson(status);
        if (status.has("uploads") && mDataDownloader == null) {
            JSONArray uris = status.getJSONArray("uploads");
            if (uris.length() > 0) {
                String uri = uris.getJSONObject(0).getString("uri");
                if (HttpHelper.getStatusCode(uri) == 200) {
                    downloadDataFrom(uri);
                } else {
                    resetStatusPollingDelay();
                }
            }
        }
    }

    private void downloadDataFrom(String uri) throws JSONException, IOException {
        mDataDownloader = new AsyncHttpGet(uri);
        mDataDownloader.registerResponseHandler(new HttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, StreamableContent body) {
                SweepInEvent.this.tryForSuccess();
            }

            @Override
            public void onReceiving(double progress) {
            }

            @Override
            public void onError(int statusCode, StreamableContent body) {
                Logger.e(LOG_TAG, "upload failed with: ", body);
                SweepInEvent.this.onError(new HocEventException("download failed with status code "
                        + statusCode, "failed", "<unknown uri>"));
            }

            @Override
            public void onHeaderAvailable(HashMap<String, String> headers) {
                try {
                    String filename = parseFilename(headers.get("Content-Disposition"));
                    StreamableContent streamable = mPeer.getContentFactory()
                            .createStreamableContent(headers.get("Content-Type"), filename);
                    mDataDownloader.setStreamableContent(streamable);

                } catch (FileNotFoundException e) {
                    Logger.e(LOG_TAG, e);
                } catch (IOException e) {
                    Logger.e(LOG_TAG, e);
                } catch (UnknownContentTypeException e) {
                    Logger.e(LOG_TAG, e);
                }
            }

            private String parseFilename(String filename) {
                if (filename == null || !filename.matches(".*filename=\".*\"")) {
                    return "hocced_file.unknown";
                }

                filename = filename.substring(filename.indexOf("filename=\"") + 10, filename
                        .length() - 1);
                // Logger.v(LOG_TAG, "Filename: ", filename);
                return filename;
            }

            @Override
            public void onError(Exception e) {
                SweepInEvent.this.onError(new HocEventException(e));
            }

        });
        mDataDownloader.start();
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepIn");
        return eventParams;
    }

    public boolean hasDataBeenDownloaded() {
        if (mDataDownloader == null) {
            return false;
        }
        return mDataDownloader.isRequestCompleted();
    }

    @Override
    protected boolean wasSuccessful() {
        return super.wasSuccessful() && hasDataBeenDownloaded();
    }

    @Override
    public StreamableContent getData() {
        if (mDataDownloader == null) {
            return null;
        }
        return mDataDownloader.getBodyAsStreamableContent();
    }
}
