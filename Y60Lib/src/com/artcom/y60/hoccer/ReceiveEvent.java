package com.artcom.y60.hoccer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.data.BadContentResolverUriException;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.data.UnknownContentTypeException;
import com.artcom.y60.http.AsyncHttpGet;
import com.artcom.y60.http.HttpResponseHandler;

public abstract class ReceiveEvent extends HocEvent {

    private static String LOG_TAG         = "ReceiveEvent";
    AsyncHttpGet          mDataDownloader = null;

    ReceiveEvent(Peer peer) {
        super(peer);
    }

    @Override
    protected void updateStatusFromJson(JSONObject status) throws JSONException, IOException {
        if (status.has("uploads")) {
            JSONArray possible_pieces = status.getJSONArray("uploads");
            if (possible_pieces.length() > 0) {
                onPossibleDownloadsAvailable(possible_pieces);
            }
        }

        super.updateStatusFromJson(status);
    }

    protected void onPossibleDownloadsAvailable(JSONArray pieces) throws JSONException, IOException {
        if (mDataDownloader != null) {
            // skip if we are already downloading someting
            return;
        }

        String uri = pieces.getJSONObject(0).getString("uri");
        downloadDataFrom(uri);
    }

    protected void downloadDataFrom(String uri) {
        mDataDownloader = new AsyncHttpGet(uri);
        mDataDownloader.setUncaughtExceptionHandler(getPeer().getErrorReporter());
        mDataDownloader.registerResponseHandler(new HttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, StreamableContent body) {
                ReceiveEvent.this.tryForSuccess();
            }

            @Override
            public void onReceiving(double progress) {
                ReceiveEvent.this.onTransferProgress(progress);
            }

            @Override
            public void onError(int statusCode, StreamableContent body) {
                Logger.e(LOG_TAG, "upload failed with: ", body);
                ReceiveEvent.this.onError(new HocEventException("download failed with status code "
                        + statusCode, "failed", "<unknown uri>"));
            }

            @Override
            public void onHeaderAvailable(HashMap<String, String> headers) {
                try {
                    String filename = parseFilename(headers.get("Content-Disposition"));
                    StreamableContent streamable = getPeer().getContentFactory()
                            .createStreamableContent(headers.get("Content-Type"), filename);
                    mDataDownloader.setStreamableContent(streamable);

                } catch (FileNotFoundException e) {
                    Logger.e(LOG_TAG, e);
                } catch (IOException e) {
                    Logger.e(LOG_TAG, e);
                } catch (UnknownContentTypeException e) {
                    Logger.e(LOG_TAG, e);
                } catch (IllegalStateException e) {
                    Logger.e(LOG_TAG, e);
                } catch (BadContentResolverUriException e) {
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
                ReceiveEvent.this.onError(new HocEventException(e));
            }

        });
        mDataDownloader.start();
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

    @Override
    public void abort() throws HocEventException {
        super.abort();
        if (mDataDownloader != null) {
            Logger.v(LOG_TAG, "interrupting: ", mDataDownloader);
            mDataDownloader.interrupt();
        }
    }

}
