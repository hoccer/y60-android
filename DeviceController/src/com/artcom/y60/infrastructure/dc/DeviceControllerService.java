//========================================================================
//$Id: DeviceControllerService.java 170 2008-10-21 05:37:59Z janb.webtide $
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package com.artcom.y60.infrastructure.dc;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.artcom.y60.conf.DeviceConfiguration;
import com.artcom.y60.infrastructure.GomNode;
import com.artcom.y60.infrastructure.GomRepository;
import com.artcom.y60.infrastructure.PreferencesActivity;

public class DeviceControllerService extends Service {
	private NotificationManager mNM;

	private Server server;
	private boolean _useNIO;
	private int _port;

	private static Resources __resources;

	private SharedPreferences preferences;
	public static final String DEFAULT_NIONAME = "com.artcom.y60.infrastructure.dc.nio";
	public static final String DEFAULT_PORTNAME = "com.artcom.y60.infrastructure.dc.port";

	private static final String LOG_TAG = "DeviceControllerService";
	private static final int GOM_NOT_ACCESSIBLE_NOTIFICATION_ID = 42;
	
	private IBinder binder = new DeviceControllerBinder();
	private NotificationManager mNotificationManager;
	
	public void onCreate() {
		Log.i(LOG_TAG, "onCreate called");
		__resources = getResources();

		// Get the notification manager serivce.
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Thread thread = new Thread(null, mDeviceHistoryWatcherLoop,
				"watch net connection");
		mIsDeviceHistoryWatcherRunning = true;
		thread.start();
	}

	private boolean mIsDeviceHistoryWatcherRunning = true;
	private Runnable mDeviceHistoryWatcherLoop = new Runnable() {

		@Override
		public void run() {
			DeviceConfiguration dc = DeviceConfiguration.load();
			GomRepository repo = new GomRepository(Uri.parse(dc.getGomUrl()));

			Intent configureDC = new Intent(
					"y60.intent.CONFIGURE_DEVICE_CONTROLLER");
			configureDC.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			Notification notification = new Notification(
					R.drawable.network_down_status_icon,
					"gom not accessible, network might be down", System
							.currentTimeMillis());
			while (mIsDeviceHistoryWatcherRunning) {

				try {
					Thread.sleep(2 * 1000);

					Log.v(LOG_TAG, "checking gom");
					GomNode device = repo.getNode(dc.getDevicePath());
					String timestamp = (new SimpleDateFormat(
							"MM/dd/yyyy HH:mm:ss.SS")).format(new Date());
					device.getAttribute("last_alive_update")
							.putValue(timestamp);
					mNotificationManager.cancel(GOM_NOT_ACCESSIBLE_NOTIFICATION_ID);
					
				} catch (Exception e) {
					Log.w(LOG_TAG, "no network avialable", e);
					PendingIntent pint = PendingIntent.getActivity(
							DeviceControllerService.this, 0, configureDC,
							PendingIntent.FLAG_ONE_SHOT);
					
					notification.setLatestEventInfo(DeviceControllerService.this,
							"GOM not accessible", "network might be down", pint);
					mNotificationManager.notify(GOM_NOT_ACCESSIBLE_NOTIFICATION_ID, notification);
					// startActivity(configureDC);
				}
			}
		}

	};

	public void onStart(Intent intent, int startId) {
		Log.i(LOG_TAG, "onStart called");
		if (server != null) {
			Toast.makeText(DeviceControllerService.this,
					R.string.jetty_already_started, Toast.LENGTH_SHORT).show();
			Log.i("Jetty", "already running");
			return;
		}

		try {
			preferences = PreferenceManager.getDefaultSharedPreferences(this);

			String portDefault = getText(R.string.pref_port_value).toString();
			Log.v(LOG_TAG, "Default port is " + portDefault);
			String nioDefault = getText(R.string.pref_nio_value).toString();

			String portKey = getText(R.string.pref_port_key).toString();
			String nioKey = getText(R.string.pref_nio_key).toString();

			_useNIO = preferences.getBoolean(nioKey, Boolean
					.valueOf(nioDefault));

			Bundle bundle = intent.getExtras();
			if (bundle.containsKey(DEFAULT_PORTNAME)) {
				_port = Integer.parseInt(bundle.getString(DEFAULT_PORTNAME));
			} else {
				_port = Integer.parseInt(preferences.getString(portKey,
						portDefault));
			}

			Log.i("Jetty", "pref port = "
					+ preferences.getString(portKey, portDefault));
			Log.i("Jetty", "pref nio = "
					+ preferences.getBoolean(nioKey, Boolean
							.valueOf(nioDefault)));
			// Log.i("Jetty", "pref pwd = "+preferences.getString(pwdKey,
			// pwdDefault));

			startJetty();

			mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			Toast.makeText(DeviceControllerService.this,
					R.string.jetty_started, Toast.LENGTH_SHORT).show();

			// The PendingIntent to launch DeviceControllerActivity activity if
			// the user selects this notification
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, DeviceControllerActivity.class), 0);

			CharSequence text = getText(R.string.manage_jetty);

			Notification notification = new Notification(R.drawable.smooth,
					text, System.currentTimeMillis());

			notification.setLatestEventInfo(this, getText(R.string.app_name),
					text, contentIntent);

			mNM.notify(R.string.jetty_started, notification);
			Log.i(LOG_TAG, "DeviceControllerService started");
			super.onStart(intent, startId);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error starting DeviceControllerService", e);
			Toast.makeText(this, getText(R.string.jetty_not_started),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onDestroy() {
		mIsDeviceHistoryWatcherRunning = true;
		try {
			if (server != null) {
				stopJetty();
				// Cancel the persistent notification.
				mNM.cancel(R.string.jetty_started);
				// Tell the user we stopped.
				Toast.makeText(this, getText(R.string.jetty_stopped),
						Toast.LENGTH_SHORT).show();
				Log.i(LOG_TAG, "DeviceControllerService stopped");
				__resources = null;
			} else {
				Log.i(LOG_TAG, "DeviceControllerService not running");
				Toast.makeText(DeviceControllerService.this,
						R.string.jetty_not_running, Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error stopping DeviceControllerService", e);
			Toast.makeText(this, getText(R.string.jetty_not_stopped),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onLowMemory() {
		Log.w(LOG_TAG, "Low on memory");
		super.onLowMemory();
	}

	/**
	 * Hack to get around bug in ResourceBundles
	 * 
	 * @param id
	 * @return
	 */
	public static InputStream getStreamToRawResource(int id) {
		if (__resources != null)
			return __resources.openRawResource(id);
		else
			return null;
	}

	public String getGomLocation() {

		return preferences.getString(PreferencesActivity.KEY_GOM_LOCATION, "");
	}

	public String getSelfPath() {

		return preferences.getString(PreferencesActivity.KEY_DEVICES_PATH, "")
				+ "/"
				+ preferences.getString(PreferencesActivity.KEY_DEVICE_ID, "");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "onBind called");

		return binder;
	}

	private void startJetty() throws Exception {
		server = new Server();
		Connector connector;
		if (_useNIO) {
			SelectChannelConnector nioConnector = new SelectChannelConnector();
			nioConnector.setUseDirectBuffers(false);
			nioConnector.setPort(_port);
			connector = nioConnector;
		} else {
			SocketConnector bioConnector = new SocketConnector();
			bioConnector.setPort(_port);
			connector = bioConnector;
		}
		server.setConnectors(new Connector[] { connector });

		// Bridge Jetty logging to Android logging
		System.setProperty("org.mortbay.log.class",
				"org.mortbay.log.AndroidLog");
		// org.mortbay.log.Log.setLog(new AndroidLog());

		HandlerCollection handlers = new HandlerCollection();

		handlers
				.setHandlers(new Handler[] { new DeviceControllerHandler(this) });
		server.setHandler(handlers);

		server.start();
	}

	private void stopJetty() throws Exception {
		Log.i(LOG_TAG, "DeviceControllerService stopping");
		server.stop();
		server.join();
		server = null;
	}

	public class DeviceControllerBinder extends Binder {

		public DeviceControllerService getService() {

			return DeviceControllerService.this;
		}
	}
}
