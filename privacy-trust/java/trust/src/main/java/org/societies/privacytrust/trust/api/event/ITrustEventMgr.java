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
package org.societies.privacytrust.trust.api.event;

import org.societies.api.privacytrust.trust.event.ITrustEventListener;
import org.societies.api.privacytrust.trust.event.TrustEvent;
import org.societies.api.privacytrust.trust.model.TrustedEntityId;

/**
 * The Trust Event Manager is responsible for the subscription and publishing
 * of trust-related events.
 *
 * @author <a href="mailto:nicolas.liampotis@cn.ntua.gr">Nicolas Liampotis</a> (ICCS)
 * @since 0.0.5
 */
public interface ITrustEventMgr {

	/**
     * Publishes the specified {@link TrustEvent}.
     * 
     * @param event
     *            the event to be published.
     * @param topics
     *            the topics to which the event will be published.
     * @param source
     *            the publisher of the event.
     * @throws TrustEventMgrExceptionException 
     *             if publishing of the specified event fails.
     * @throws NullPointerException
     *             if any of the specified parameters is <code>null</code>.
     * @since 0.0.7
     */
    public void postEvent(final TrustEvent event, final String[] topics, 
    		final String source) throws TrustEventMgrException;
    
    /**
     * Registers the specified {@link ITrustEventListener} for events of the
     * supplied topics. Once registered, the <code>ITrustEventListener</code>
     * will handle {@link TrustEvent TrustEvents} associated with the
     * identified trusted entity.
     * <p>
     * To unregister the specified <code>ITrustEventListener</code>, use the
     * {@link #unregisterListener(TODO)}
     * method.
     * 
     * @param listener
     *            the <code>ITrustEventListener</code> to register.
     * @param topics
     *            the event topics to register for.
     * @param teid
     *            the identifier of the trusted entity whose events to
     *            register for.
     * @throws TrustEventMgrException
     *             if the registration process of the specified
     *             <code>ITrustEventListener</code> fails.
     * @throws NullPointerException
     *             if any of the specified parameters is <code>null</code>.
     */
    public void registerListener(final ITrustEventListener listener, 
			final String[] topics, final TrustedEntityId teid) throws TrustEventMgrException;
}