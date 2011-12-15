/**
 * Copyright (c) 2011, SOCIETIES Consortium
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
package org.societies.personalisation.preference.api.UserPreferenceConditionMonitor;

import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxModelObject;
import org.societies.api.internal.personalisation.model.IFeedbackEvent;
import org.societies.api.mock.EntityIdentifier;
import org.societies.api.mock.ServiceResourceIdentifier;
import org.societies.personalisation.preference.api.model.IPreferenceOutcome;


/**
 * @author Eliza
 * @version 1.0
 * @created 11-Nov-2011 14:52:53
 */
public interface IUserPreferenceConditionMonitor {

	public void disableAllPCM();

	/**
	 * 
	 * @param dpi
	 */
	public void disablePCM(EntityIdentifier dpi);

	public void enableAllPCM();

	/**
	 * 
	 * @param dpi
	 */
	public void enablePCM(EntityIdentifier dpi);

	/**
	 * 
	 * @param contextAttribute
	 * @param user_id
	 */
	public IPreferenceOutcome getOutcome(CtxAttribute contextAttribute, EntityIdentifier user_id);

	/**
	 * 
	 * @param user_id
	 * @param serviceType
	 * @param serviceID
	 * @param preferenceName
	 */
	public IPreferenceOutcome requestOutcomeWithCurrentContext(EntityIdentifier user_id, String serviceType, ServiceResourceIdentifier serviceID, String preferenceName);

	/**
	 * 
	 * @param user_id
	 * @param serviceType
	 * @param serviceID
	 * @param preferenceName
	 */
	public IPreferenceOutcome requestOutcomeWithFutureContext(EntityIdentifier user_id, String serviceType, ServiceResourceIdentifier serviceID, String preferenceName);

	/**
	 * 
	 * @param FeedbackEvent
	 */
	public void sendFeedback(IFeedbackEvent FeedbackEvent);

	/**
	 * 
	 * @param ctxModelObj
	 */
	public void updateReceived(CtxModelObject ctxModelObj);

}