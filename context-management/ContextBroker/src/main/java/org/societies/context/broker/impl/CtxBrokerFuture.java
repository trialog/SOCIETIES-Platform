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
package org.societies.context.broker.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.societies.api.context.CtxException;
import org.societies.api.context.broker.ICtxBroker;
import org.societies.api.context.model.CtxAssociation;
import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxAttributeIdentifier;
import org.societies.api.context.model.CtxAttributeValueType;
import org.societies.api.context.model.CtxBond;
import org.societies.api.context.model.CtxEntity;
import org.societies.api.context.model.CtxEntityIdentifier;
import org.societies.api.context.model.CtxHistoryAttribute;
import org.societies.api.context.model.CtxIdentifier;
import org.societies.api.context.model.CtxModelObject;
import org.societies.api.context.model.CtxModelType;
import org.societies.api.mock.EntityIdentifier;

import org.societies.context.api.user.db.IUserCtxDBMgr;

/**
 * 3p Context Broker Implementation
 */
public class CtxBrokerFuture implements ICtxBroker {

	private IUserCtxDBMgr userDB;
	
	public CtxBrokerFuture() throws CtxException { 
	}
	
	public CtxBrokerFuture(IUserCtxDBMgr userDB) throws CtxException {
		this.userDB = userDB;
	}

	@Override
	public Future<CtxAssociation> createAssociation(EntityIdentifier requester,
			String type) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<CtxAttribute> createAttribute(EntityIdentifier requester,
			CtxEntityIdentifier scope, String type) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<CtxEntity> createEntity(EntityIdentifier requester,
			String type) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<Object>> evaluateSimilarity(
			Serializable objectUnderComparison,
			List<Serializable> referenceObjects) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Future<CtxModelObject> remove(EntityIdentifier requester,
			CtxIdentifier identifier) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<CtxModelObject> retrieve(EntityIdentifier requester,
			CtxIdentifier identifier) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxAttribute>> retrieveFuture(
			EntityIdentifier requester, CtxAttributeIdentifier attrId, Date date) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxAttribute>> retrieveFuture(
			EntityIdentifier requester, CtxAttributeIdentifier attrId,
			int modificationIndex) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxHistoryAttribute>> retrievePast(
			EntityIdentifier requester, CtxAttributeIdentifier attrId,
			int modificationIndex) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxHistoryAttribute>> retrievePast(
			EntityIdentifier requester, CtxAttributeIdentifier attrId,
			Date startDate, Date endDate) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Future<CtxModelObject> update(EntityIdentifier requester,
			CtxModelObject object) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<CtxEntity> retrieveAdministratingCSS(
			EntityIdentifier requester, CtxEntityIdentifier communityEntId) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Future<List<CtxEntityIdentifier>> retrieveCommunityMembers(
			EntityIdentifier requester, CtxEntityIdentifier community) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxEntityIdentifier>> retrieveParentCommunities(
			EntityIdentifier requester, CtxEntityIdentifier community) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxIdentifier>> lookup(EntityIdentifier requester,
			CtxModelType modelType, String type) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxEntityIdentifier>> lookupEntities(
			EntityIdentifier requester, String entityType, String attribType,
			Serializable minAttribValue, Serializable maxAttribValue)
			throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerForUpdates(EntityIdentifier requester,
			CtxEntityIdentifier scope, String attrType) throws CtxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerForUpdates(EntityIdentifier requester,
			CtxAttributeIdentifier attrId) throws CtxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterForUpdates(EntityIdentifier requester,
			CtxAttributeIdentifier attrId) throws CtxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterForUpdates(EntityIdentifier requester,
			CtxEntityIdentifier scope, String attributeType)
			throws CtxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Future<Set<CtxBond>> retrieveBonds(EntityIdentifier requester,
			CtxEntityIdentifier community) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxEntityIdentifier>> retrieveSubCommunities(
			EntityIdentifier requester, CtxEntityIdentifier community)
			throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}
		
	
}
