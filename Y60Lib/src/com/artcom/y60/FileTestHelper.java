package com.artcom.y60;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Browser;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;

public class FileTestHelper {
    public class TestFile {
        public String mFilename    = null;
        public String mContentType = null;
        public String mFilepath    = null;
        public byte[] mContent     = null;

        public TestFile(byte[] content, String contentType, String extension) throws IOException {
            mFilename = String.valueOf(System.currentTimeMillis() + "." + extension);
            mContent = content;
            mContentType = contentType;
            mFilepath = Environment.getExternalStorageDirectory() + "/" + mFilename;
            IoHelper.writeByteArrayToFile(content, mFilepath);
            Assert.assertTrue("file should exist", new File(mFilepath).exists());
        }

        public void delete() {
            new File(mFilepath).delete();
            Assert.assertFalse("file shouldnt exist", new File(mFilepath).exists());
        }

        public String toString() {
            return getClass().getSimpleName() + " " + mFilename + " " + mFilepath + " "
                    + mContentType;
        }

        public Uri getFileSchemeUri() {
            return Uri.parse("file://" + mFilepath);
        }
    }

    public abstract class ContentProviderTestFile extends TestFile {

        public ContentResolver mContentResolver = null;
        public Uri             mContentUri      = null;

        public ContentProviderTestFile(byte[] content, String contentType, String extension,
                ContentResolver contentResolver) throws IOException {
            super(content, contentType, extension);
            mContentResolver = contentResolver;
        }

        public void delete() {
            mContentResolver.delete(mContentUri, null, null);
            Cursor cursor = mContentResolver.query(mContentUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst() == true) {
                Assert.fail();
            }

            super.delete();
        }
    }

    public class TextFile extends TestFile {

        public TextFile(byte[] content, String contentType, String extension) throws IOException {
            super(content, contentType, extension);
        }

        @Override
        public String toString() {
            return super.toString() + " " + new String(mContent);
        }
    }

    public class ContentProviderImageFile extends ContentProviderTestFile {

        public ContentProviderImageFile(byte[] content, String contentType, String extension,
                ContentResolver contentResolver) throws IOException {

            super(content, contentType, extension, contentResolver);
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.DATA, mFilepath);
            mContentUri = mContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values);
        }

    }

    public class ContentProviderUrlFile extends ContentProviderTestFile {

        public ContentProviderUrlFile(byte[] content, String contentType, String extension,
                ContentResolver contentResolver) throws IOException {

            super(content, contentType, extension, contentResolver);
            ContentValues values = new ContentValues(1);
            values.put(Browser.BookmarkColumns.URL, new String(mContent));
            mContentUri = mContentResolver.insert(Browser.BOOKMARKS_URI, values);
        }

        public String getUrl() {
            Cursor cursor = mContentResolver.query(mContentUri, null, null, null, null);
            if (!cursor.moveToFirst()) {
                Assert.fail();
            }

            String attribute = cursor.getString(cursor.getColumnIndex(Browser.BookmarkColumns.URL));
            cursor.close();
            return attribute;
        }

    }

    public class ContentProviderAudioFile extends ContentProviderTestFile {

        public ContentProviderAudioFile(byte[] content, String contentType, String extension,
                ContentResolver contentResolver) throws IOException {

            super(content, contentType, extension, contentResolver);
            ContentValues values = new ContentValues(3);
            values.put(MediaStore.Audio.Media.DATA, mFilepath);
            mContentUri = mContentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    values);
        }

        public String getSongName() {
            Cursor cursor = mContentResolver.query(mContentUri, null, null, null, null);
            if (!cursor.moveToFirst()) {
                Assert.fail();
            }

            String name = cursor.getString(cursor.getColumnIndex(Audio.Media.TITLE));
            cursor.close();
            return name;
        }

        public String getAlbumName() {
            Cursor cursor = mContentResolver.query(mContentUri, null, null, null, null);
            if (!cursor.moveToFirst()) {
                Assert.fail();
            }

            String name = cursor.getString(cursor.getColumnIndex(Audio.Media.ALBUM));
            cursor.close();
            return name;
        }

        public String getArtistName() {
            Cursor cursor = mContentResolver.query(mContentUri, null, null, null, null);
            if (!cursor.moveToFirst()) {
                Assert.fail();
            }

            String name = cursor.getString(cursor.getColumnIndex(Audio.Media.ARTIST));
            cursor.close();
            return name;
        }

    }
}
