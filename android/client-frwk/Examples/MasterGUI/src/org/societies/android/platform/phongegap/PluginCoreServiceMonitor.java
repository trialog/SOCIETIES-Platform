/**
Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 

(SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp (IBM),
INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
ITALIA S.p.a.(TI), TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.societies.android.platform.phongegap;

import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.societies.android.api.utilities.ServiceMethodTranslator;
import org.societies.android.api.internal.servicelifecycle.AService;
import org.societies.android.api.internal.servicelifecycle.AServiceResourceIdentifier;
import org.societies.android.api.internal.servicelifecycle.IServiceControl;
import org.societies.android.api.internal.servicelifecycle.IServiceDiscovery;
import org.societies.android.api.internal.servicemonitor.AndroidActiveServices;
import org.societies.android.api.internal.servicemonitor.AndroidActiveTasks;
import org.societies.android.api.internal.servicemonitor.ICoreServiceMonitor;
import org.societies.android.platform.servicemonitor.CoreServiceMonitor;
import org.societies.android.platform.servicemonitor.ServiceManagement;
import org.societies.android.platform.servicemonitor.CoreServiceMonitor.LocalBinder;
import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
/**
 * PhoneGap plugin to allow the CoreMonitor service to be used by HTML web views.
 * 
 * Note: As a PhoneGap plugin is not a standard Android component a lot of assumed 
 * functionality such as creating intents and bind to services is not automatic. The 
 * Plugin class does however have an application context, this.ctx, which supplies the 
 * context to allow this functionality to operate.
 * 
 *
 */

public class PluginCoreServiceMonitor extends Plugin {
	//Logging tag
	private static final String LOG_TAG = PluginCoreServiceMonitor.class.getName();
	private static final String CLASSNAME_SEPARATOR = "\\.";

	/**
	 * Actions required to bind and unbind to any Android service(s) 
	 * required by this plugin. It is imperative that dependent 
	 * services are binded to before invoking invoking methods.
	 */
	private static final String CONNECT_SERVICE = "connectService";
	private static final String DISCONNECT_SERVICE = "disconnectService";
	
	//Required to match method calls with callbackIds
	private HashMap<String, String> methodCallbacks;;

    private ICoreServiceMonitor coreServiceMonitor;
    private boolean connectedtoCoreMonitor = false;
    
    private IServiceDiscovery serviceDisco;
    private boolean serviceDiscoConnected = false;
    private IServiceControl serviceControl;

    /**
     * Constructor
     */
    public PluginCoreServiceMonitor() {
    	super();
    	this.methodCallbacks = new HashMap<String, String>();
    }

    /**
     * CoreServiceMonitor service connection
     */
    private ServiceConnection coreServiceMonitorConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
        	Log.d(LOG_TAG, "Disconnecting from CoreServiceMonitor service");
        	connectedtoCoreMonitor = false;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
        	Log.d(LOG_TAG, "Connecting to CoreServiceMonitor service");
        	//get a local binder
            LocalBinder binder = (LocalBinder) service;
            //obtain the service's API
            coreServiceMonitor = (ICoreServiceMonitor) binder.getService();
            connectedtoCoreMonitor = true;
        }
    };
    
    /**
     * IServiceDiscovery service connection
     */
    private ServiceConnection serviceDiscoConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
        	Log.d(LOG_TAG, "Connecting to IServiceDiscovery service");

        	//GET LOCAL BINDER
            LocalBinder binder = (LocalBinder) service;

            //OBTAIN SERVICE DISCOVERY API
            serviceDisco = (IServiceDiscovery) binder.getService();
            serviceDiscoConnected = true;
        }
        
        public void onServiceDisconnected(ComponentName name) {
        	Log.d(LOG_TAG, "Disconnecting from ServiceDiscovery service");
        	serviceDiscoConnected = false;
        }
    };
    
    /**
     * Bind to the target service
     */
    private void initialiseServiceBinding() {
    	//CREATE INTENT FOR EACH SERVICE
    	Intent intentServiceMon = new Intent(this.ctx.getContext(), CoreServiceMonitor.class);
    	Intent intentServiceDisco = new Intent(this.ctx.getContext(), ServiceManagement.class);
    	
    	//BIND TO SERVICES
    	this.ctx.getContext().bindService(intentServiceMon, coreServiceMonitorConnection, Context.BIND_AUTO_CREATE);
    	this.ctx.getContext().bindService(intentServiceDisco, serviceDiscoConnection, Context.BIND_AUTO_CREATE);
    	
    	//REGISTER BROADCAST
        IntentFilter intentFilter = new IntentFilter() ;
        
        intentFilter.addAction(CoreServiceMonitor.ACTIVE_SERVICES);
        intentFilter.addAction(CoreServiceMonitor.ACTIVE_TASKS);
        
        intentFilter.addAction(ServiceManagement.GET_SERVICES);
        intentFilter.addAction(ServiceManagement.GET_SERVICE);
        intentFilter.addAction(ServiceManagement.SEARCH_SERVICES);
        
        this.ctx.getContext().registerReceiver(new bReceiver(), intentFilter);
    }
    
    /**
     * Unbind from service
     */
    private void disconnectServiceBinding() {
    	if (connectedtoCoreMonitor) {
    		this.ctx.getContext().unbindService(coreServiceMonitorConnection);
    	}
    	if (serviceDiscoConnected) {
    		this.ctx.getContext().unbindService(serviceDiscoConnection);
    	}
    }
    

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		Log.d(LOG_TAG, "Phonegap Plugin executing: " + action);
		PluginResult result = null;
		
		if (action.equals(CONNECT_SERVICE)) {
			if (!connectedtoCoreMonitor) {
				this.initialiseServiceBinding();
			}
            result = new PluginResult(PluginResult.Status.OK, "connected");
            result.setKeepCallback(false);
            return result;
		} 

		if (action.equals(DISCONNECT_SERVICE)) {
			this.disconnectServiceBinding();
            result = new PluginResult(PluginResult.Status.OK, "disconnected");
            result.setKeepCallback(false);
            return result;
		} 
		
		if (this.validRemoteCall(action) && connectedtoCoreMonitor) {
			try {
				Log.d(LOG_TAG, "parameters: " + data.getString(0));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			Log.d(LOG_TAG, "adding to Map store: " + callbackId + " for action: " + action);
			this.methodCallbacks.put(action, callbackId);
			
			//>>>>>>>>>  CoreServiceMonitor METHODS >>>>>>>>>>>>>>>>>>>>>>>>>>
			if (action.equals(ServiceMethodTranslator.getMethodName(ICoreServiceMonitor.methodsArray, 2))) {
				try {
					this.coreServiceMonitor.activeServices(data.getString(0));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (action.equals(ServiceMethodTranslator.getMethodName(ICoreServiceMonitor.methodsArray, 0))) {
				try {
					this.coreServiceMonitor.activeTasks(data.getString(0));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			//>>>>>>>>>  IServiceDiscovery METHODS >>>>>>>>>>>>>>>>>>>>>>>>>>
			else if (action.equals(ServiceMethodTranslator.getMethodName(IServiceDiscovery.methodsArray, 0))) {
				try {
					this.serviceDisco.getServices(data.getString(0), data.getString(1));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (action.equals(ServiceMethodTranslator.getMethodName(IServiceDiscovery.methodsArray, 1))) {
//				try {
//					this.serviceDisco.getService(data.getString(0), (AServiceResourceIdentifier) data.get(1), data.getString(2));
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
			} else if (action.equals(ServiceMethodTranslator.getMethodName(IServiceDiscovery.methodsArray, 2))) {
//				try {
//					this.serviceDisco.searchService(data.getString(0), (AService) data.get(1), data.getString(2));
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
			}
			//>>>>>>>>>  IServiceDiscovery METHODS >>>>>>>>>>>>>>>>>>>>>>>>>>
			//TODO
			
            // Don't return any result now, since status results will be sent when events come in from broadcast receiver 
            result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
		} else {
            result = new PluginResult(PluginResult.Status.ERROR);
            result.setKeepCallback(false);
		}
		return result;	
	}

	
	/**
	 * Unbind from service to prevent service being kept alive
	 */
	@Override
	public void onDestroy() {
		disconnectServiceBinding();
    }

    /**
     * Broadcast receiver to receive intent return values from service method calls
     */
	private class bReceiver extends BroadcastReceiver  {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOG_TAG, intent.getAction());
			
			//>>>>>>>>>  CoreServiceMonitor METHODS >>>>>>>>>>>>>>>>>>>>>>>>>>
			if (intent.getAction().equals(CoreServiceMonitor.ACTIVE_SERVICES)) {
				String mapKey = ServiceMethodTranslator.getMethodName(ICoreServiceMonitor.methodsArray, 2);
				
				String methodCallbackId = PluginCoreServiceMonitor.this.methodCallbacks.get(mapKey);
				if (methodCallbackId != null) {
					
					//unmarshall intent extra
					Parcelable parcels [] =  intent.getParcelableArrayExtra(CoreServiceMonitor.INTENT_RETURN_KEY);
					ActivityManager.RunningServiceInfo services [] = new ActivityManager.RunningServiceInfo[parcels.length];
					for (int i = 0; i < parcels.length; i++) {
						services[i] = (ActivityManager.RunningServiceInfo) parcels[i];
					}

					PluginResult result = new PluginResult(PluginResult.Status.OK, convertAndroidActiveServices(getActiveServices(services)));
					result.setKeepCallback(false);
					PluginCoreServiceMonitor.this.success(result, methodCallbackId);
					
					//remove callback ID for given method invocation
					PluginCoreServiceMonitor.this.methodCallbacks.remove(mapKey);

					Log.d(LOG_TAG, "Plugin success method called, target: " + methodCallbackId);

				}
			} else if (intent.getAction().equals(CoreServiceMonitor.ACTIVE_TASKS)) {
				String mapKey = ServiceMethodTranslator.getMethodName(ICoreServiceMonitor.methodsArray, 0);
				
				String methodCallbackId = PluginCoreServiceMonitor.this.methodCallbacks.get(mapKey);
				if (methodCallbackId != null) {
					
					//unmarshall intent extra
					Parcelable parcels [] =  intent.getParcelableArrayExtra(CoreServiceMonitor.INTENT_RETURN_KEY);
					ActivityManager.RunningTaskInfo tasks [] = new ActivityManager.RunningTaskInfo[parcels.length];
					for (int i = 0; i < parcels.length; i++) {
						tasks[i] = (ActivityManager.RunningTaskInfo) parcels[i];
					}

					PluginResult result = new PluginResult(PluginResult.Status.OK, convertAndroidActiveTasks(getActiveTasks((tasks))));
					result.setKeepCallback(false);
					PluginCoreServiceMonitor.this.success(result, methodCallbackId);
					
					//remove callback ID for given method invocation
					PluginCoreServiceMonitor.this.methodCallbacks.remove(mapKey);

					Log.d(LOG_TAG, "Plugin success method called, target: " + methodCallbackId);
				}
			} 
			//>>>>>>>>>  IServiceDiscovery METHODS >>>>>>>>>>>>>>>>>>>>>>>>>>
			else if (intent.getAction().equals(ServiceManagement.GET_SERVICES)) {
				String mapKey = ServiceMethodTranslator.getMethodName(IServiceDiscovery.methodsArray, 0);
				
				String methodCallbackId = PluginCoreServiceMonitor.this.methodCallbacks.get(mapKey);
				if (methodCallbackId != null) {
					
					//UNMARSHALL THE SERVICES FROM Parcels BACK TO Services
					Parcelable parcels[] =  intent.getParcelableArrayExtra(ServiceManagement.INTENT_RETURN_VALUE);
					AService services[] = new AService[parcels.length];
					for (int i = 0; i < parcels.length; i++) {
						services[i] = (AService) parcels[i];
					}
					
					PluginResult result = new PluginResult(PluginResult.Status.OK, convertAServiceToJSONArray(services));
					result.setKeepCallback(false);
					PluginCoreServiceMonitor.this.success(result, methodCallbackId);
					
					//remove callback ID for given method invocation
					PluginCoreServiceMonitor.this.methodCallbacks.remove(mapKey);

					Log.d(LOG_TAG, "Plugin success method called, target: " + methodCallbackId);
				}
			}
		}
	};
	
	/**
	 * Create an Active services model for the GUI
	 * @param services
	 * @return AndroidActiveServices array
	 */
	private AndroidActiveServices [] getActiveServices(ActivityManager.RunningServiceInfo services []) {
		AndroidActiveServices activeServices [] = new AndroidActiveServices[services.length];
		
		for (int i = 0; i < services.length; i++) {
			AndroidActiveServices element = new AndroidActiveServices();
			
			element.setActiveSince(services[i].activeSince);
			element.setClassName(extractServiceName(services[i].service.getClassName()));
			element.setPackageName(services[i].service.getPackageName());
			element.setProcess(services[i].process);
			activeServices[i] = element;
		}
		return activeServices;
	}
	/**
	 * Create an Active tasks model for the GUI
	 * @param tasks
	 * @return AndroidActiveTasks array
	 */
	private AndroidActiveTasks [] getActiveTasks(ActivityManager.RunningTaskInfo tasks []) {
		AndroidActiveTasks activeTasks [] = new AndroidActiveTasks[tasks.length];
		for (int i = 0; i < tasks.length; i++) {
			AndroidActiveTasks element = new AndroidActiveTasks();
			
			element.setClassName(extractServiceName(tasks[i].baseActivity.getClassName()));
			element.setPackageName(tasks[i].baseActivity.getPackageName());
			element.setNumRunningActivities(tasks[i].numRunning);
			activeTasks[i] = element;
		}

		return activeTasks;
	}
 
    /**
     * Creates a JSONArray for a given AndroidActiveServices array
     * 
     * @param node
     * @return JSONArray 
     */
    private JSONArray convertAServiceToJSONArray(AService array[]) {
    	JSONArray jObj = new JSONArray();
		Gson gson = new Gson();
		try {
			jObj =  (JSONArray) new JSONTokener(gson.toJson(array)).nextValue();
			
			Log.d(LOG_TAG, jObj.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return jObj;
    }
    
    /**
     * Creates a JSONArray for a given AndroidActiveServices array
     * 
     * @param node
     * @return JSONArray 
     */
    private JSONArray convertAndroidActiveServices(AndroidActiveServices services []) {
    	JSONArray jObj = new JSONArray();
		Gson gson = new Gson();
		try {
			jObj =  (JSONArray) new JSONTokener(gson.toJson(services)).nextValue();
			
			Log.d(LOG_TAG, jObj.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return jObj;
    }
    
    /**
     * Creates a JSONArray for a given AndroidActiveTasks array
     * 
     * @param node
     * @return JSONArray 
     */
    private JSONArray convertAndroidActiveTasks(AndroidActiveTasks tasks []) {
    	JSONArray jObj = new JSONArray();
		Gson gson = new Gson();
		try {
			jObj =  (JSONArray) new JSONTokener(gson.toJson(tasks)).nextValue();
			
			Log.d(LOG_TAG, jObj.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return jObj;
    }

    /**
     * Determine if the Javascript action is a valid.
     * 
     * N.B. Assumes that the Javascript method name is the exact same as the 
     * Java implementation. 
     * 
     * @param action
     * @return boolean
     */
    private boolean validRemoteCall(String action) {
    	boolean retValue = false;
    	//CHECK IServiceDisovery METHODS
    	for (int i = 0; i < IServiceDiscovery.methodsArray.length; i++) {
        	if (action.equals(ServiceMethodTranslator.getMethodName(IServiceDiscovery.methodsArray, i))) {
        		return true;
        	}
    	}
    	//CHECK ICoreServiceMonitor METHODS
    	for (int i = 0; i < ICoreServiceMonitor.methodsArray.length; i++) {
        	if (action.equals(ServiceMethodTranslator.getMethodName(ICoreServiceMonitor.methodsArray, i))) {
        		return true;
        	}
    	}
    	
    	if (!retValue) {
    		Log.d(LOG_TAG, "Unable to find method name for given action: " + action);
    	}
    	return retValue;
    }
    /**
     * Extract a service or activity from its class name
     * @param className
     * @return String
     */
    private String extractServiceName(String className) {
    	Log.d(LOG_TAG, "extractService for class: " + className );
    	String serviceName = className;
    	
    	String tokens [] = className.split(CLASSNAME_SEPARATOR);
    	if (tokens.length > 0) {
        	serviceName = tokens[tokens.length - 1];
    	}
    	return serviceName;
    }
}
