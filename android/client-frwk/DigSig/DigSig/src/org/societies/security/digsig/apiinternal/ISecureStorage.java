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
package org.societies.security.digsig.apiinternal;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import android.app.Activity;
import android.content.Intent;


/**
 * Interface for retrieving X.509 certificates and private keys from android secure storage.
 *
 * @author Mitja Vardjan
 *
 */
public interface ISecureStorage {

	/**
	 * Check if both certificate and private key exist for given index.
	 * 
	 * @param index
	 * @return True if both certificate and private key already exist
	 */
	public boolean containsCertificateAndKey(int index);
	
	/**
	 * Get the identity's public certificate.
	 * 
	 * @param index Index of identity
	 * @return The certificate, or null if there is no certificate with given index
	 */
	public X509Certificate getCertificate(int index);	

	/**
	 * Get the identity's private key.
	 * 
	 * @param index Index of identity
	 * @return The private key, or null if there is no key with given index
	 */
	public PrivateKey getPrivateKey(int index);
	
	/**
	 * Put an identity's public certificate and private key.
	 *
	 * @param certificate The certificate to set
	 * @param key The private key to set
	 * @return Generated index of new identity.
	 * If the identity already exists, then its existing index is returned.
	 */
	public int put(X509Certificate certificate, PrivateKey key);
	
	/**
	 * Test the secure storage if it is unlocked and ready to use.
	 * If secure storage is not ready and locked, it could be unlocked with
	 * {@link Activity#startActivityForResult(android.content.Intent, int)}
	 * where {@link Intent} action is {@link UNLOCK_ACTION_HONEYCOMB}.
	 * 
	 * @return True if ready to use, false otherwise.
	 */
	public boolean isReady();
}
