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

/**
 * Societies Android app CIS list function(s) namespace
 * 
 * @namespace SocietiesCISListService
 */

var	SocietiesCISListService = {
		
	mCommunitities: {}, //USED TO STORE ALL COMMUNITIES TO SAVE ROUND TRIPS
	
	/**
	 * @methodOf SocietiesCISListService#
	 * @description update the CIS data on communities_list.html 
	 * @param {Object} successCallback The callback which will be called when result is successful
	 * @param {Object} failureCallback The callback which will be called when result is unsuccessful
	 * @returns none
	 */
	populateCISListpage: function(data) {
		
		mCommunitities = data;
		//EMPTY TABLE - NEED TO LEAVE THE HEADER
		while( $('ul#CommunitiesListDiv').children().length >1 )
			$('ul#CommunitiesListDiv li:last').remove();
		
		//DISPLAY COMMUNTIES
		for (i  = 0; i < data.length; i++) {
			var tableEntry = '<li><a href="#" onclick="SocietiesCISListService.showCISDetails(' + i + ')"><img src="images/community_profile_icon.png" class="profile_list" alt="logo" >' +
							 '<h2>' + data[i].communityName + '</h2>' + 
							 '<p>' + data[i].communityType + '</p>' + 
							 '</a></li>';
			$('ul#CommunitiesListDiv').append(tableEntry);
		}
		$('#CommunitiesListDiv').listview('refresh');
	},
	
	showCISDetails: function (cisPos) {
		// GET SERVICE FROM ARRAY AT POSITION
		var communityObj = mCommunitities[ cisPos ];
		if ( communityObj ) {
			//VALID SERVICE OBJECT
			var markup = "<h1>" + communityObj.communityName + "</h1>" + 
						 "<p>Type: " + communityObj.communityType + "</p>" + 
						 "<p>" + communityObj.description + "</p>" + 
						 "<p>Owner: " + communityObj.ownerJid + "</p>";
			//INJECT
			$("#community_profile_info").html( markup );
			
			try {//REFRESH FORMATTING
				//ERRORS THE FIRST TIME AS YOU CANNOT refresh() A LISTVIEW IF NOT INITIALISED
				$('ul#community_details').listview('refresh');
			}
			catch(err) {}
			$.mobile.changePage($("#community-details-page"), {transition: "fade"});
			
			SocietiesCISListService.showCISActivities(communityObj.communityJid);
			SocietiesCISListService.showCISMembers(communityObj.communityJid);
			ServiceManagementServiceHelper.connectToServiceManagement(function() {
								SocietiesCISListService.showCISServices(communityObj.communityJid); }
								)
		}
	},
	
	showCISActivities: function (cisId) {
		function success(data) {
			//EMPTY TABLE - NEED TO LEAVE THE HEADER
			while( $('ul#cis_activity_feed').children().length >0 )
				$('ul#cis_activity_feed li:last').remove();
			
			for (i  = 0; i < data.length; i++) {
				var tableEntry = "<li>"+ data[i].actor + " " + 
					 	 		 data[i].verb  + " " + 
					 	 		 data[i].object + "</li>";
				$('ul#cis_activity_feed').append(tableEntry);
			}
			$('ul#cis_activity_feed').listview('refresh');
			if (data.length <3)
				$('ul#cis_activity_feed').trigger( "expand" );
		}
		function failure(data) {
			alert("showCISActivities - failure: " + data); //console.log
		}
		
		window.plugins.SocietiesLocalCISManager.getActivityFeed(cisId, success, failure);
	},
	
	showCISMembers: function (cisId) {
		
		function success(data) {
			//EMPTY TABLE - NEED TO LEAVE THE HEADER
			while( $('ul#cis_members').children().length >0 )
				$('ul#cis_members li:last').remove();
			
			for (i  = 0; i < data.length; i++) {
				var tableEntry = "<li><h2>"+ data[i].jid + "</h2>" + 
									 "<p>" + data[i].role  + "</p></li>";
				$('ul#cis_members').append(tableEntry);
			}
			$('ul#cis_members').listview('refresh');
			
			//AUTO EXPAND IF ROW COUNT IS SMALL 
			if (data.length <3)
				$('ul#cis_members').trigger( "expand" );
		}
		
		function failure(data) {
			alert("showCISActivities - failure: " + data); //console.log
		}
		
		window.plugins.SocietiesLocalCISManager.getMembers(cisId, success, failure);
	},
	
	/**
	 * @methodOf SocietiesCISListService#
	 * @description retrieves list of services from a CIS 
	 * @param {Object} successCallback The callback which will be called when result is successful
	 * @param {Object} failureCallback The callback which will be called when result is unsuccessful
	 * @returns none
	 */
	showCISServices: function(cisId) {

		function success(data) {
			//EMPTY TABLE - NEED TO LEAVE THE HEADER
			while( $('ul#cis_shared_apps').children().length >0 )
				$('ul#cis_shared_apps li:last').remove();

			//DISPLAY SERVICES
			for (i  = 0; i < data.length; i++) {
				var tableEntry = '<li><a href="#" onclick="Societies3PServices.installService(' + i + ')"><img src="images/printer_icon.png" class="profile_list" alt="logo" >' +
					'<h2>' + data[i].serviceName + '</h2>' + 
					'<p>' + data[i].serviceDescription + '</p>' + 
					'</a></li>';
				$('ul#cis_shared_apps').append(tableEntry);
			}
			$('#cis_shared_apps').listview('refresh');
		}
		
		function failure(data) {
			alert("showCISServices - failure: " + data);
		}
		
		window.plugins.ServiceManagementService.getServices(cisId, success, failure);
	}
}
