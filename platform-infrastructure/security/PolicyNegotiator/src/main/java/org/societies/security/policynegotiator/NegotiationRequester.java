package org.societies.security.policynegotiator;

import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.ResponsePolicy;
import org.societies.api.internal.security.policynegotiator.INegotiationRequester;
import org.societies.api.internal.security.policynegotiator.INegotiationRequesterCallback;

public class NegotiationRequester implements INegotiationRequester {

	@Override
	public void getPolicyOptions(INegotiationRequesterCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptPolicy(String signedPolicyOption,
			INegotiationRequesterCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void negotiatePolicy(int policyOptionId,
			ResponsePolicy modifiedPolicy,
			INegotiationRequesterCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reject() {
		// TODO Auto-generated method stub

	}

}
