/**
 * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
 * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
 * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
 * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
 * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
 * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.societies.android.platform.events.notifications;

import org.societies.android.api.events.IAndroidSocietiesEvents;
import org.societies.android.api.utilities.ServiceMethodTranslator;
import org.societies.android.platform.androidutils.AndroidNotifier;
import org.societies.api.schema.css.directory.CssAdvertisementRecord;
import org.societies.api.schema.css.directory.CssFriendEvent;

import android.app.IntentService;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

/**
 * Describe your class here...
 *
 * @author aleckey
 *
 */
public class FriendsService extends IntentService {

	private static final String LOG_TAG = FriendsService.class.getName();
	private static final String SERVICE_NAME = "SOCIETIES Friends Service";
	
	//TRACKING CONNECTION TO EVENTS MANAGER
	private boolean boundToEventMgrService = false;
	private Messenger eventMgrService = null;
	private static final String SERVICE_ACTION   = "org.societies.android.platform.events.ServicePlatformEventsRemote";
	private static final String CLIENT_NAME      = "org.societies.android.platform.events.notifications.FriendsService";
	private static final String EXTRA_CSS_ADVERT = "org.societies.api.schema.css.directory.CssAdvertisementRecord";
	private static final String ALL_CSS_FRIEND_INTENTS = "org.societies.android.css.friends";
	
	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>STARTING THIS SERVICE>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	/**Constructor 
	 * @param name service name
	 */
	public FriendsService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//DO NOTHING
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "Friends service starting");

		if (!boundToEventMgrService) {
			setupBroadcastReceiver();
			bindToEventsManagerService();
		}
		return super.onStartCommand(intent,flags,startId);
	}
	
	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "Friends service terminating");
	}
	
	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>BIND TO EXTERNAL "EVENT MANAGER">>>>>>>>>>>>>>>>>>>>>>>>>>>>
	/** Bind to the Events Manager Service */
	private void bindToEventsManagerService() {
    	Intent serviceIntent = new Intent(SERVICE_ACTION);
    	Log.d(LOG_TAG, "Binding to Events Manager Service: ");
   		bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			boundToEventMgrService = true;
			eventMgrService = new Messenger(service);
			Log.d(this.getClass().getName(), "Connected to the Societies Event Mgr Service");
			
			//BOUND TO SERVICE - SUBSCRIBE TO RELEVANT EVENTS
			InvokeRemoteMethod invoke  = new InvokeRemoteMethod(CLIENT_NAME);
    		invoke.execute();
		}
		
		public void onServiceDisconnected(ComponentName name) {
			boundToEventMgrService = false;
		}
	};

	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>REGISTER FOR EVENTS>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	/**Create a broadcast receiver
     * @return the created broadcast receiver
     */
    private BroadcastReceiver setupBroadcastReceiver() {
    	Log.d(LOG_TAG, "Set up broadcast receiver");
    	BroadcastReceiver receiver = new MainReceiver();
        this.registerReceiver(receiver, createIntentFilter());
        Log.d(LOG_TAG, "Registered broadcast receiver");

        return receiver;
    }
    
    /**Broadcast receiver to receive intent return values from EventManager service*/
    private class MainReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOG_TAG, "Received action: " + intent.getAction());
			
			//EVENT MANAGER INTENTS
			if (intent.getAction().equals(IAndroidSocietiesEvents.SUBSCRIBE_TO_EVENT)) {
				Log.d(LOG_TAG, "Subscribed to all event - listening to: " + intent.getIntExtra(IAndroidSocietiesEvents.INTENT_RETURN_VALUE_KEY, -999));
			} else if (intent.getAction().equals(IAndroidSocietiesEvents.SUBSCRIBE_TO_EVENTS)) {
				Log.d(LOG_TAG, "Subscribed to events - listening to: " + intent.getIntExtra(IAndroidSocietiesEvents.INTENT_RETURN_VALUE_KEY, -999));
			} else if (intent.getAction().equals(IAndroidSocietiesEvents.UNSUBSCRIBE_FROM_EVENTS)) {
				Log.d(LOG_TAG, "Un-subscribed from events - listening to: " + intent.getIntExtra(IAndroidSocietiesEvents.INTENT_RETURN_VALUE_KEY, -999));
			}
			//PUBSUB EVENTS - payload is CssFriendEvent 
			else if (intent.getAction().equals(IAndroidSocietiesEvents.CSS_FRIEND_REQUEST_RECEIVED_EVENT)) {
				Log.d(LOG_TAG, "Frient Request received: " + intent.getIntExtra(IAndroidSocietiesEvents.INTENT_RETURN_VALUE_KEY, -999));
				CssFriendEvent eventPayload = intent.getParcelableExtra(IAndroidSocietiesEvents.GENERIC_INTENT_PAYLOAD_KEY);
				String description = eventPayload.getCssAdvert().getName() + " sent a friend request";
				addNotification(description, "Friend Request", eventPayload.getCssAdvert());
			}
			else if (intent.getAction().equals(IAndroidSocietiesEvents.CSS_FRIEND_REQUEST_ACCEPTED_EVENT)) {
				Log.d(LOG_TAG, "Frient Request accepted: " + intent.getIntExtra(IAndroidSocietiesEvents.INTENT_RETURN_VALUE_KEY, -999));
				CssFriendEvent eventPayload = intent.getParcelableExtra(IAndroidSocietiesEvents.GENERIC_INTENT_PAYLOAD_KEY);
				String description = eventPayload.getCssAdvert().getName() + " accepted your friend request";
				addNotification(description, "Friend Request Accepted", eventPayload.getCssAdvert());
			}			
		}
    }
    
    /**Create a suitable intent filter
     * @return IntentFilter
     */
    private IntentFilter createIntentFilter() {
    	//register broadcast receiver to receive SocietiesEvents return values 
        IntentFilter intentFilter = new IntentFilter();
        //EVENT MANAGER INTENTS
        intentFilter.addAction(IAndroidSocietiesEvents.SUBSCRIBE_TO_EVENT);
        intentFilter.addAction(IAndroidSocietiesEvents.SUBSCRIBE_TO_EVENTS);
        intentFilter.addAction(IAndroidSocietiesEvents.UNSUBSCRIBE_FROM_EVENTS);
        //PUBSUB INTENTS
        intentFilter.addAction(IAndroidSocietiesEvents.CSS_FRIEND_REQUEST_RECEIVED_INTENT);
        intentFilter.addAction(IAndroidSocietiesEvents.CSS_FRIEND_REQUEST_ACCEPTED_INTENT);
        return intentFilter;
    }
    
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>SUBSCRIBE TO PUBSUB EVENTS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	/** Async task to invoke remote service method */
    private class InvokeRemoteMethod extends AsyncTask<Void, Void, Void> {

    	private final String LOCAL_LOG_TAG = InvokeRemoteMethod.class.getName();
    	private String client;

    	public InvokeRemoteMethod(String client) {
    		this.client = client;
    	}

    	protected Void doInBackground(Void... args) {
    		//METHOD: subscribeToEvents(String client, String intentFilter) - ARRAY POSITION: 1
    		String targetMethod = IAndroidSocietiesEvents.methodsArray[1];
    		Message outMessage = Message.obtain(null, ServiceMethodTranslator.getMethodIndex(IAndroidSocietiesEvents.methodsArray, targetMethod), 0, 0);
    		Bundle outBundle = new Bundle();

    		//PARAMETERS
    		outBundle.putString(ServiceMethodTranslator.getMethodParameterName(targetMethod, 0), this.client);
    		outBundle.putString(ServiceMethodTranslator.getMethodParameterName(targetMethod, 1), ALL_CSS_FRIEND_INTENTS);
    		Log.d(LOCAL_LOG_TAG, "Client Package Name: " + this.client);
    		outMessage.setData(outBundle);

    		Log.d(LOCAL_LOG_TAG, "Sending event registration");
    		try {
    			eventMgrService.send(outMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    		return null;
    	}
    }

    private void addNotification(String description, String eventType, CssAdvertisementRecord advert) {
    	//CREATE ANDROID NOTIFICATION
		int notifierflags [] = new int [1];
		notifierflags[0] = Notification.FLAG_AUTO_CANCEL;
		AndroidNotifier notifier = new AndroidNotifier(FriendsService.this.getApplicationContext(), Notification.DEFAULT_SOUND, notifierflags);
		
		//CREATE INTENT FOR LAUNCHING ACTIVITY
		Intent intent = new Intent(this.getApplicationContext(), FriendsActivity.class);
		intent.putExtra(EXTRA_CSS_ADVERT, (Parcelable)advert);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		notifier.notifyMessage(description, eventType, FriendsActivity.class, intent);
	}

}
