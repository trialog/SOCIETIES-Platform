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
package org.societies.android.api.privacytrust.trust;

import java.io.Serializable;

import org.societies.android.api.common.ADate;
import org.societies.android.api.css.manager.IServiceManager;
import org.societies.api.schema.identity.RequestorBean;
import org.societies.api.schema.privacytrust.trust.model.TrustEvidenceTypeBean;
import org.societies.api.schema.privacytrust.trust.model.TrustValueTypeBean;
import org.societies.api.schema.privacytrust.trust.model.TrustedEntityIdBean;

/**
 * Describe your class here...
 *
 * @author <a href="mailto:nicolas.liampotis@cn.ntua.gr">Nicolas Liampotis</a> (ICCS)
 * @since 0.5
 */
public interface ITrustClient extends IServiceManager {
	
	public static final String INTENT_RETURN_VALUE_KEY = 
			"org.societies.android.privacytrust.trust.ReturnValue";
    public static final String INTENT_RETURN_STATUS_KEY = 
    		"org.societies.android.privacytrust.trust.ReturnStatus";
    
    public static final String RETRIEVE_TRUST_VALUE = 
    		"org.societies.android.api.privacytrust.trust.RETRIEVE_TRUST_VALUE";
    public static final String ADD_DIRECT_TRUST_EVIDENCE = 
    		"org.societies.android.api.privacytrust.trust.ADD_DIRECT_TRUST_EVIDENCE";

    String methodsArray [] = {
    		"retrieveTrustValue(String client, org.societies.api.schema.identity.RequestorBean requestor, org.societies.api.schema.privacytrust.trust.model.TrustedEntityIdBean trustorId, org.societies.api.schema.privacytrust.trust.model.TrustedEntityIdBean trusteeId, org.societies.api.schema.privacytrust.trust.model.TrustValueTypeBean trustValueType)",
            "addDirectTrustEvidence(String client, org.societies.api.schema.identity.RequestorBean requestor, org.societies.api.schema.privacytrust.trust.model.TrustedEntityIdBean subjectId, org.societies.api.schema.privacytrust.trust.model.TrustedEntityIdBean objectId, org.societies.api.schema.privacytrust.trust.model.TrustEvidenceTypeBean type, org.societies.android.api.common.ADate timestamp, Serializable info",
			"startService()",
			"stopService()" };

	/**
	 * Retrieves the trust value of the given type which the specified trustor
	 * has assigned to the supplied trustee. The method returns <code>null</code>
	 * if no trust value has been assigned to the specified trustee by the given
	 * trustor.
	 * 
	 * @param client
	 *            TODO
	 * @param requestor
	 *            the requestor on whose behalf to retrieve the trust value.
	 * @param trustorId
	 *            the identifier of the entity which has assigned the trust
	 *            value to retrieve.
	 * @param trusteeId
	 *            the identifier of the entity whose trust value to retrieve.
	 * @param trustValueType
	 *            the type of the trust value to retrieve, i.e. one of
	 *            {@link TrustValueTypeBean#DIRECT}, 
	 *            {@link TrustValueTypeBean#INDIRECT}, or
	 *            {@link TrustValueTypeBean#USER_PERCEIVED}.
	 * @throws NullPointerException if any of the specified parameters is
	 *         <code>null</code>.
	 * @since 1.0
	 */
	public void retrieveTrustValue(final String client, final RequestorBean requestor, 
			final TrustedEntityIdBean trustorId, 
			final TrustedEntityIdBean trusteeId, 
			final TrustValueTypeBean trustValueType);
	
	/**
	 * Adds the specified piece of direct trust evidence. The
	 * {@link ATrustedEntityId ATrustedEntityIds} of the subject and the object
	 * this piece of evidence refers to, its type, as well as, the time the
	 * evidence was recorded are also supplied. Finally, depending on the
	 * evidence type, the method allows specifying supplementary information.
	 * 
	 * @param client
	 *            TODO
	 * @param requestor
	 *            the requestor on whose behalf to add the direct trust evidence.
	 * @param subjectId
	 *            the {@link ATrustedEntityId} of the subject the piece of
	 *            evidence refers to.
	 * @param objectId
	 *            the {@link ATrustedEntityId} of the object the piece of
	 *            evidence refers to.
	 * @param type
	 *            the type of the evidence to be added.
	 * @param timestamp
	 *            the time the evidence was recorded.
	 * @param info
	 *            supplementary information if applicable; <code>null</code>
	 *            otherwise.
	 * @throws NullPointerException
	 *            if any of the specified requestor, subjectId, objectId, type or 
	 *            timestamp parameter is <code>null</code>.
	 * @since 1.0
	 */
	public void addDirectTrustEvidence(final String client, 
			final RequestorBean requestor, final TrustedEntityIdBean subjectId,
			final TrustedEntityIdBean objectId,	
			final TrustEvidenceTypeBean type, final ADate timestamp, 
			final Serializable info);
}