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
 * Societies Android app SocietiesXMPPRegistration function(s) namespace
 * 
 * @namespace SocietiesXMPPRegistration
 */


var SocietiesXMPPRegistration = {
	/**
	 * @methodOf SocietiesXMPPRegistration#
	 * @description Validate that viable registration credentials have been entered
	 * @param {Object} username
	 * @param {Object} password
	 * @param {Object} repeatPassword 
	 * @returns boolean true if credentials viable
	 */

	validateRegistrationCredentials: function(name, password, repeatPassword, termsAck) {
		var retValue = true;
		alert ("checkbox value: " + termsAck);
		
		if (name.length > 0 && password.length > 0) {
			if (repeatPassword.length === 0) {
				retValue  = false;
				alert("validateRegistrationCredentials: " + "repeat entry of password must be completed");
				
			} else if (password !== repeatPassword) {
				retValue  = false;
				alert("validateRegistrationCredentials: " + "passwords are not the same - re-enter");
			} else if (!termsAck) {
				retValue  = false;
				alert("validateRegistrationCredentials: " + "Terms & Conditions must be acknowledged");
			}
		} else {
			alert("validateRegistrationCredentials: " + "User credentials must be entered");
			retValue  = false;
		}
		return retValue;
	},
	/**
	 * @methodOf SocietiesXMPPRegistration#
	 * @description Actions carried in the event that a successful Identity Domain registration occurs
	 * @returns null
	 */

	xmppRegistration: function() {
		console.log("Regsister identity with chosen Identity domain");

		function success(data) {
			SocietiesXMPPRegistration.updateRegisteredCredentialPreferences();
			
			
			console.log("Current page: " + $.mobile.activePage[0].id);
			
			$.mobile.changePage( ($("#main")), { transition: "slideup"} );
		}
		
		function failure(data) {
			alert("xmppRegistration - failure: " + data);
		}
	    window.plugins.SocietiesLocalCSSManagerService.registerXMPPServer(success, failure);

	},
	/**
	 * @methodOf SocietiesXMPPRegistration#
	 * @description updates the registered user credentials and domain server for future login purposes
	 * @returns null
	 */
	updateRegisteredCredentialPreferences: function () {
		function success(data) {
			console.log("updateRegisteredCredentialPreferences - successful: " + data.value);
		}
		
		function failure(data) {
			alert("updateRegisteredCredentialPreferences - failure: " + data);
		}
		window.plugins.SocietiesAppPreferences.putStringPrefValue(success, failure, "cssIdentity", jQuery("#regUsername").val());
		window.plugins.SocietiesAppPreferences.putStringPrefValue(success, failure, "cssPassword", jQuery("#regUserpass").val());
		window.plugins.SocietiesAppPreferences.putStringPrefValue(success, failure, "daURI", jQuery("#domainServers").val());
		
		//Update the login page with XMPP registered values
		jQuery("#username").val(jQuery("#regUsername").val());
		jQuery("#userpass").val(jQuery("#regUserpass").val());
		jQuery("#identitydomain").val(jQuery("#domainServers").val());

	}

}

/**
 * JQuery boilerplate to attach JS functions to relevant HTML elements
 * 
 * @description Add Javascript functions and/or event handlers to various HTML tags using JQuery on pageinit
 * N.B. this event is fired once per page load
 * @returns null
 */
$(document).bind('pageinit',function(){

	console.log("jQuery pageinit action(s)");

	$('#registerXMPP').click(function() {
		if (SocietiesXMPPRegistration.validateRegistrationCredentials(jQuery("#regUsername").val(), jQuery("#regUserpass").val(), jQuery("#repeatRegUserpass").val(), jQuery("#regSocietiesTerms").val())) {
			SocietiesLocalCSSManagerService.connectToLocalCSSManager(SocietiesXMPPRegistration.xmppRegistration);
	}
	});

});
