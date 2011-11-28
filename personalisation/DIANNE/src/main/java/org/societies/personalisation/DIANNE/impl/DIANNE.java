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
package org.societies.personalisation.DIANNE.impl;

import java.util.HashMap;

import org.societies.personalisation.DIANNE.api.DianneNetwork.IDIANNE;
import org.societies.personalisation.common.api.model.ContextAttribute;
import org.societies.personalisation.common.api.model.EntityIdentifier;
import org.societies.personalisation.common.api.model.IOutcome;
import org.societies.personalisation.common.api.model.ServiceResourceIdentifier;

public class DIANNE implements IDIANNE{

	private HashMap<EntityIdentifier, NetworkRunner> networks;

	public DIANNE(){
		networks = new HashMap<EntityIdentifier, NetworkRunner>();
	}

	@Override
	public IOutcome getOutcome(EntityIdentifier ownerId,
			ServiceResourceIdentifier serviceId, String preferenceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOutcome getOutcome(EntityIdentifier ownerId,
			ServiceResourceIdentifier serviceId, String preferenceName,
			ContextAttribute attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableDIANNELearning(EntityIdentifier ownerId) {
		System.out.println("Enabling incremental learning for identity: "+ ownerId);
		if(networks.containsKey(ownerId)){
			NetworkRunner network = networks.get(ownerId);
			network.play();
		}else{
			System.out.println("No networks exist for this identity");
		}
	}

	@Override
	public void disableDIANNELearning(EntityIdentifier ownerId) {
		System.out.println("Disabling incremental learning for identity: "+ ownerId);	
		if(networks.containsKey(ownerId)){
			NetworkRunner network = networks.get(ownerId);
			network.pause();
		}else{
			System.out.println("No networks exist for this identity");
		}
	}
}
