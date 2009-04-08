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

package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

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
import android.widget.Toast;

import com.artcom.y60.BindingListener;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.PreferencesActivity;
import com.artcom.y60.gom.GomAttribute;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.GomProxyHelper;

public class DeviceControllerService extends Service {
	private NotificationManager mNM;

	private Server server;
	private boolean _useNIO;
	private int _port;

	private static Resources __resources;

	private SharedPreferences preferences;
	public static final String DEFAULT_NIONAME = "com.artcom.y60.dc.nio";
	public static final String DEFAULT_PORTNAME = "com.artcom.y60.dc.port";

	private static final String LOG_TAG = "DeviceControllerService";
	private static final int GOM_NOT_ACCESSIBLE_NOTIFICATION_ID = 42;

	private IBinder binder = new DeviceControllerBinder();
	private NotificationManager mNotificationManager;
	GomProxyHelper mGom = null;

	public void onCreate() {
		Logger.i(LOG_TAG, "onCreate called");
		__resources = getResources();

		// Get the notification manager serivce.
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Get the gom proxy helper and run watcher thread if gom is available.
		mGom = new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {

			public void bound(GomProxyHelper helper) {

				mGom = helper;
				Thread thread = new Thread(null, mDeviceHistoryWatcherLoop,
						"watch net connection");
				mIsDeviceHistoryWatcherRunning = true;
				thread.start();
			}

			public void unbound(GomProxyHelper helper) {

				mIsDeviceHistoryWatcherRunning = false;
				mGom = null;
			}
		});

	}

	private boolean mIsDeviceHistoryWatcherRunning = true;
	private Runnable mDeviceHistoryWatcherLoop = new Runnable() {

		// TODO Refactor this very long method into an "Watchdog" class
		@Override
		public void run() {
			DeviceConfiguration dc = DeviceConfiguration.load();

			Intent configureDC = new Intent(
					"y60.intent.CONFIGURE_DEVICE_CONTROLLER");
			configureDC.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			Notification notification = new Notification(
					R.drawable.network_down_status_icon,
					"Y60's GOM not accessible.", System.currentTimeMillis());

			String historyLog = "";
			while (mIsDeviceHistoryWatcherRunning) {

				String timestamp = (new SimpleDateFormat(
						"MM/dd/yyyy HH:mm:ss")).format(new Date());

				// this will take some time so we do not need a "Thread.sleep"
				String pingStatistic = getPingStatistics(dc);

				mNotificationManager
				.cancel(GOM_NOT_ACCESSIBLE_NOTIFICATION_ID);

				try {
					// Log.v(LOG_TAG, "checking gom: " + pingStatistic);
					GomNode device = mGom.getNode(dc.getDevicePath());
					device.getAttribute("last_alive_update")
							.putValue(timestamp);
					
					GomAttribute historyAttribute = device
							.getAttribute("history_log");
					historyAttribute.refresh();
					historyAttribute.putValue(historyAttribute.getValue()
							+ historyLog + "\n" + timestamp + ": "
							+ pingStatistic);
					historyLog = "";
				
				} catch (NoSuchElementException e) {
					ErrorHandling.signalMissingGomEntryError(LOG_TAG, e, DeviceControllerService.this);
//					throw new RuntimeException("Missing GOM entry");
					return;
				} catch (Exception e) {
					Logger.w(LOG_TAG, "no network available", e);
					PendingIntent pint = PendingIntent.getActivity(
							DeviceControllerService.this, 0, configureDC,
							PendingIntent.FLAG_ONE_SHOT);

					notification.setLatestEventInfo(
							DeviceControllerService.this, "GOM not accessible",
							"network might be down", pint);
					mNotificationManager.notify(
							GOM_NOT_ACCESSIBLE_NOTIFICATION_ID, notification);
					// startActivity(configureDC);

					historyLog += "\n" + timestamp + ": network failure";
					historyLog += "\n" + timestamp + ": " + pingStatistic;
				}
			}

			ErrorHandling.signalServiceError(LOG_TAG, new Exception(
                    "Watchdog has stopped unexpetedly"), DeviceControllerService.this);
            Logger.w(LOG_TAG, "watcher thread stopped...");
		}

		private String getPingStatistics(DeviceConfiguration dc) {
			Runtime runtime = Runtime.getRuntime();
			Process process;
			try {
				process = runtime.exec("ping -q -c 20 -i 0.1 "
						+ Uri.parse(dc.getGomUrl()).getHost());
			} catch (IOException e) {
				throw new RuntimeException("Could not execute ping command.", e);
			}
			InputStreamReader reader = new InputStreamReader(process
					.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(reader);

			String line = "";
			List<String> pingStatistic = new ArrayList<String>();
			try {
				while ((line = bufferedReader.readLine()) != null) {
					pingStatistic.add(line.toString());
				}
			} catch (IOException e) {
				throw new RuntimeException(
						"Could not read result of ping command.", e);
			}

			if (pingStatistic.size() < 3) {
				return pingStatistic.toString();
			}
			
			return pingStatistic.get(pingStatistic.size() - 2);
		}

	};

	public void onStart(Intent intent, int startId) {
		Logger.i(LOG_TAG, "onStart called");
		if (server != null) {
			Toast.makeText(DeviceControllerService.this,
					R.string.jetty_already_started, Toast.LENGTH_SHORT).show();
			Logger.i("Jetty", "already running");
			return;
		}

		try {
			preferences = PreferenceManager.getDefaultSharedPreferences(this);

			String portDefault = getText(R.string.pref_port_value).toString();
			// Log.v(LOG_TAG, "Default port is " + portDefault);
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

			Logger.i("Jetty", "pref port = ",
					preferences.getString(portKey, portDefault));
			Logger.i("Jetty", "pref nio = ",
					preferences.getBoolean(nioKey, Boolean
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
			Logger.i(LOG_TAG, "DeviceControllerService started");
			super.onStart(intent, startId);
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Error starting DeviceControllerService", e);
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
				Logger.i(LOG_TAG, "DeviceControllerService stopped");
				__resources = null;
			} else {
				Logger.i(LOG_TAG, "DeviceControllerService not running");
				Toast.makeText(DeviceControllerService.this,
						R.string.jetty_not_running, Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Error stopping DeviceControllerService", e);
			Toast.makeText(this, getText(R.string.jetty_not_stopped),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onLowMemory() {
		Logger.w(LOG_TAG, "Low on memory");
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
		Logger.d(LOG_TAG, "onBind called");

		return binder;
	}

	private void startJetty() throws Exception {
		server = new Server();
		Connector connector;
		if (_useNIO) {
			SelectChannelConnector nioConnector = new SelectChannelConnector();
			nioConnector.setUseDirectBuffers(false);
			nioConnector.setPort(_port);
			nioConnector.setHost("0.0.0.0"); // listen on any ip address
			connector = nioConnector;
		} else {
			SocketConnector bioConnector = new SocketConnector();
			bioConnector.setPort(_port);
			bioConnector.setHost("0.0.0.0"); // listen on any ip address
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
		Logger.i(LOG_TAG, "DeviceControllerService stopping");
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
