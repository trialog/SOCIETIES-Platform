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
package org.societies.api.internal.privacytrust.privacyprotection;

import org.societies.privacytrust.privacyprotection.api.IDataObfuscationManager;
import org.societies.api.internal.privacytrust.privacyprotection.model.privacyPolicy.RequestPolicy;
import org.societies.api.internal.privacytrust.privacyprotection.model.privacyPolicy.ResponseItem;
import org.societies.privacytrust.privacyprotection.mock.DataIdentifier;
import org.societies.privacytrust.privacyprotection.mock.EntityIdentifier;
import org.societies.privacytrust.privacyprotection.mock.ServiceResourceIdentifier;

/**
 * External interface to do actions when using a data.
 * @author olivierm
 * @version 1.0
 * @created 09-nov.-2011 16:45:26
 */
public interface IPrivacyDataManager extends IDataObfuscationManager{
	/**
	 * Check permission to access/use/disclose a data for a service usage
	 * Example of use:
	 * - Context Broker, to get permissions to disclose context data.
	 * - Preference Manager, to get permission to disclose user preference data.
	 * - "Content Accessor", to get permissions to disclose a content data.
	 * 
	 * @param dataId ID of the requested data.
	 * @param ownerId the ID of the owner of the data. Here this is the CSS_Id of the CSS receiving the request.
	 * @param requestorId The ID of the requestor of the data. Here it is the CSS_Id  of the CSS which creates the Service.
	 * @param serviceId The service_Id the service
	 * @return A ResponseItem with permission information in it
	 */
	public ResponseItem checkPermission(DataIdentifier dataId, EntityIdentifier ownerId, EntityIdentifier requestorId, ServiceResourceIdentifier serviceId);

	/**
	 * Check permission to access/use/disclose a data for a CIS usage
	 * Example of use:
	 * - Context Broker, to get permissions to disclose context data.
	 * - Preference Manager, to get permission to disclose user preference data.
	 * - "Content Accessor", to get permissions to disclose a content data
	 * 
	 * @param dataId ID of the requested data.
	 * @param ownerId The ID of the owner of the data. Here it is the CSS_Id of the CSS receiving the request.
	 * @param requestorId The ID of the requestor of the data. Here it is the CSS_Id  of the CSS which creates the CIS.
	 * @param cisId the ID of the CIS which wants to access the data
	 * @return A ResponseItem with permission information in it
	 */
	public ResponseItem checkPermission(DataIdentifier dataId, EntityIdentifier ownerId, EntityIdentifier requestorId, EntityIdentifier cisId);

	/**
	 * Check permission to access/use/disclose a data in a case that no negotiation have been done.
	 * Example of use:
	 * - Context Broker, to get permissions to disclose context data.
	 * - Preference Manager, to get permission to disclose user preference data.
	 * - "Content Accessor", to get permissions to disclose a content data.
	 * 
	 * @param dataId ID of the requested data.
	 * @param ownerId ID of the owner of the data. Here it is the CSS_Id of the CSS receiving the request.
	 * @param requestorId ID of the requestor of the data. Here it is the CSS_Id of the CSS which request the data.
	 * @param usage Information about the use of the data: purpose, retention-time, people who will access this data... Need to be formalised!
	 * @return A ResponseItem with permission information in it
	 */
	public ResponseItem checkPermission(DataIdentifier dataId, EntityIdentifier ownerId, EntityIdentifier requestorId, RequestPolicy usage);
}