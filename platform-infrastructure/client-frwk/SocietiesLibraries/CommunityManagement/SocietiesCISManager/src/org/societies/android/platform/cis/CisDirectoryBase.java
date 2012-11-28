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
package org.societies.android.platform.cis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.societies.android.api.cis.directory.ACisAdvertisementRecord;
import org.societies.android.api.cis.directory.ICisDirectory;
import org.societies.api.comm.xmpp.datatypes.Stanza;
import org.societies.api.comm.xmpp.datatypes.XMPPInfo;
import org.societies.api.comm.xmpp.exceptions.XMPPError;
import org.societies.api.comm.xmpp.interfaces.ICommCallback;
import org.societies.api.identity.IIdentity;
import org.societies.api.schema.cis.directory.CisAdvertisementRecord;
import org.societies.api.schema.cis.directory.CisDirectoryBean;
import org.societies.api.schema.cis.directory.CisDirectoryBeanResult;
import org.societies.api.schema.cis.directory.MethodType;
import org.societies.comm.xmpp.client.impl.ClientCommunicationMgr;
import org.societies.utilities.DBC.Dbc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

/**
 * Describe your class here...
 *
 * @author aleckey
 *
 */
public class CisDirectoryBase implements ICisDirectory {
	//LOGGING TAG
	private static final String LOG_TAG = CisDirectoryBase.class.getName();
	
	//COMMS REQUIRED VARIABLES
	private static final List<String> ELEMENT_NAMES = Arrays.asList("cisDirectoryBean", "cisDirectoryBeanResult");
	private static final List<String> NAME_SPACES = Collections.unmodifiableList(Arrays.asList("http://societies.org/api/schema/cis/directory"));
	private static final List<String> PACKAGES = Collections.unmodifiableList(Arrays.asList("org.societies.api.schema.cis.directory"));
    
	private ClientCommunicationMgr commMgr;
    private Context androidContext;
    
    /**
     * Default constructor
     */
    public CisDirectoryBase(Context androidContext) {
    	Log.d(LOG_TAG, "Object created");
    	
    	this.androidContext = androidContext;
    	
		try {
			//INSTANTIATE COMMS MANAGER
			this.commMgr = new ClientCommunicationMgr(androidContext);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
        }
    }
    
	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ICisDirectory METHODS >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	/* @see org.societies.android.api.cis.directory.ICisDirectory#findAllCisAdvertisementRecords(java.lang.String) */
	public ACisAdvertisementRecord[] findAllCisAdvertisementRecords(String client) {
        Log.d(LOG_TAG, "findAllCisAdvertisementRecords called by client: " + client);
		
        AsyncDirFunctions methodAsync = new AsyncDirFunctions();
		String params[] = {client, ICisDirectory.FIND_ALL_CIS, ""};
		methodAsync.execute(params);
		
		return null;
	}

	/* @see org.societies.android.api.cis.directory.ICisDirectory#findForAllCis(java.lang.String, java.lang.String) */
	public ACisAdvertisementRecord[] findForAllCis(String client, String filter) {       
        Log.d(LOG_TAG, "findForAllCis called by client: " + client);
		
        AsyncDirFunctions methodAsync = new AsyncDirFunctions();
		String params[] = {client, ICisDirectory.FILTER_CIS, filter};
		methodAsync.execute(params);
		
		return null;
	}

	/* @see org.societies.android.api.cis.directory.ICisDirectory#searchByID(java.lang.String, java.lang.String) */
	public ACisAdvertisementRecord searchByID(String client, String cis_id) {
		Log.d(LOG_TAG, "searchByID called by client: " + client);
		
        AsyncDirFunctions methodAsync = new AsyncDirFunctions();
		String params[] = {client, ICisDirectory.FIND_CIS_ID, cis_id};
		methodAsync.execute(params);
		
		return null;
	}

	/**
	 * AsyncTask classes required to carry out threaded tasks. These classes should be used where it is estimated that 
	 * the task length is unknown or potentially long. While direct usage of the Communications components for remote 
	 * method invocation is an explicitly asynchronous operation, other usage is not and the use of these types of classes
	 * is encouraged. Remember, Android Not Responding (ANR) exceptions will be invoked if the main app thread is abused
	 * and the app will be closed down by Android very soon after.
	 * 
	 * Although the result of an AsyncTask can be obtained by using <AsyncTask Object>.get() it's not a good idea as 
	 * it will effectively block the parent method until the result is delivered back and so render the use if the AsyncTask
	 * class ineffective. Use Intents as an asynchronous callback mechanism.
	 */

	/**
	 * This class carries out the GetFriendRequests method call asynchronously
	 */
	private class AsyncDirFunctions extends AsyncTask<String, Void, String[]> {
		
		@Override
		protected String[] doInBackground(String... params) {
			Dbc.require("At least one parameter must be supplied", params.length >= 1);
			Log.d(LOG_TAG, "AsyncFriendRequests - doInBackground");
			
			//PARAMETERS
			String client = params[0];
			String method = params[1];
			String filterCis = params[2];
			//RETURN OBJECT
			String results[] = new String[1];
			results[0] = client;
			//MESSAGE BEAN
			CisDirectoryBean messageBean = new CisDirectoryBean();
			if (method.equals(ICisDirectory.FIND_CIS_ID)) {
				messageBean.setMethod(MethodType.SEARCH_BY_ID);
				messageBean.setFilter(filterCis);
			} 
			else if (method.equals(ICisDirectory.FILTER_CIS)) {
				messageBean.setMethod(MethodType.FIND_FOR_ALL_CIS);
				CisAdvertisementRecord advert = new CisAdvertisementRecord();
				advert.setName(filterCis);
				messageBean.setCisA(advert);
			} 
			else {
				messageBean.setMethod(MethodType.FIND_ALL_CIS_ADVERTISEMENT_RECORDS);
			}
			//COMMS CONFIG
			IIdentity toID = commMgr.getIdManager().getDomainAuthorityNode();
			ICommCallback cisCallback = new CisDirectoryCallback(client, method);
			Stanza stanza = new Stanza(toID);
	        try {
	        	commMgr.register(ELEMENT_NAMES, cisCallback);
	        	commMgr.sendIQ(stanza, IQ.Type.GET, messageBean, cisCallback);
			} catch (Exception e) {
				Log.e(LOG_TAG, "ERROR sending message: " + e.getMessage());
	        }
			return results;
		}

		@Override
		protected void onPostExecute(String results []) {
			Log.d(LOG_TAG, "DomainRegistration - onPostExecute");
	    }
	}

	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> COMMS CALLBACK >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	/**
	 * Callback required for Android Comms Manager
	 */
	private class CisDirectoryCallback implements ICommCallback {
		private String returnIntent;
		private String client;
		
		/**Constructor sets the calling client and Intent to be returned
		 * @param client
		 * @param returnIntent
		 */
		public CisDirectoryCallback(String client, String returnIntent) {
			this.client = client;
			this.returnIntent = returnIntent;
		}

		public List<String> getXMLNamespaces() {
			return NAME_SPACES;
		}

		public List<String> getJavaPackages() {
			return PACKAGES;
		}

		public void receiveError(Stanza arg0, XMPPError err) {
			Log.d(LOG_TAG, "Callback receiveError:" + err.getMessage());			
		}

		public void receiveInfo(Stanza arg0, String arg1, XMPPInfo arg2) {
			Log.d(LOG_TAG, "Callback receiveInfo");
		}

		public void receiveItems(Stanza arg0, String arg1, List<String> arg2) {
			Log.d(LOG_TAG, "Callback receiveItems");
		}

		public void receiveMessage(Stanza arg0, Object arg1) {
			Log.d(LOG_TAG, "Callback receiveMessage");	
		}

		public void receiveResult(Stanza returnStanza, Object msgBean) {
			Log.d(LOG_TAG, "Callback receiveResult");
			
			if (client != null) {
				Intent intent = new Intent(returnIntent);
				
				Log.d(LOG_TAG, ">>>>>Return Stanza: " + returnStanza.toString());
				if (msgBean==null) Log.d(LOG_TAG, ">>>>msgBean is null");
				// --------- cisDirectoryBeanResult Bean ---------
				if (msgBean instanceof CisDirectoryBeanResult) {
					Log.d(LOG_TAG, "CisDirectoryBeanResult Result!");
					CisDirectoryBeanResult dirResult = (CisDirectoryBeanResult) msgBean;
					List<CisAdvertisementRecord> listReturned = dirResult.getResultCis();
					//CONVERT TO PARCEL BEANS
					Parcelable returnArray[] = new Parcelable[listReturned.size()];
					for (int i=0; i<listReturned.size(); i++) {
						ACisAdvertisementRecord record = ACisAdvertisementRecord.convertCisAdvertRecord(listReturned.get(i)); 
						returnArray[i] = record;
						Log.d(LOG_TAG, "Added record: " + record.getId());
					}
					//NOTIFY CALLING CLIENT
					intent.putExtra(ICisDirectory.INTENT_RETURN_VALUE, returnArray);
						
					intent.setPackage(client);
					CisDirectoryBase.this.androidContext.sendBroadcast(intent);
					CisDirectoryBase.this.commMgr.unregister(ELEMENT_NAMES, this);
				}
			}
		}
	}//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> END COMMS CALLBACK >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
}