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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.comm.xmpp.interfaces.ICommManager;
import org.societies.api.context.CtxException;
import org.societies.api.context.event.CtxChangeEventListener;
import org.societies.api.context.model.CommunityCtxEntity;
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
import org.societies.api.context.model.IndividualCtxEntity;
import org.societies.api.context.model.util.SerialisationHelper;
import org.societies.api.identity.IIdentity;
import org.societies.api.identity.IIdentityManager;
import org.societies.api.identity.INetworkNode;
import org.societies.api.identity.IdentityType;
import org.societies.api.identity.InvalidFormatException;
import org.societies.api.internal.context.broker.ICtxBroker;
import org.societies.api.internal.context.model.CtxAttributeTypes;
import org.societies.api.internal.context.model.CtxEntityTypes;
import org.societies.api.internal.privacytrust.privacyprotection.model.privacyassessment.IPrivacyLogAppender;
import org.societies.context.api.community.db.ICommunityCtxDBMgr;
import org.societies.context.api.event.CtxChangeEventTopic;
import org.societies.context.api.event.ICtxEventMgr;
import org.societies.context.api.user.db.IUserCtxDBMgr;
import org.societies.context.api.user.history.IUserCtxHistoryMgr;
import org.societies.context.api.user.inference.IUserCtxInferenceMgr;
import org.societies.context.broker.api.CtxBrokerException;
import org.societies.context.broker.impl.util.CtxBrokerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/**
 * Internal Context Broker Implementation
 * This class implements the internal context broker interfaces and orchestrates the db 
 * management 
 */
@Service
public class InternalCtxBroker implements ICtxBroker {

	/** The logging facility. */
	private static final Logger LOG = LoggerFactory.getLogger(InternalCtxBroker.class);

	/** The privacy logging facility. */
	@Autowired(required=false)
	private IPrivacyLogAppender privacyLogAppender;

	private boolean hasPrivacyLogAppender = false;

	/**
	 * The IIdentity Mgmt service reference.
	 *
	 * @see {@link #setIdentityMgr(IIdentityManager)}
	 */
	private IIdentityManager idMgr;

	/** The Context Event Mgmt service reference. */
	@Autowired(required=true)
	private ICtxEventMgr ctxEventMgr;


	/**
	 * The User Context History Mgmt service reference. 
	 * 
	 * @see {@link #setUserCtxHistoryMgr(IUserCtxHistoryMgr)}
	 */
	@Autowired(required=true)
	private IUserCtxHistoryMgr userCtxHistoryMgr;

	/**
	 * The User Context DB Mgmt service reference.
	 * 
	 * @see {@link #setUserCtxDBMgr(IUserCtxDBMgr)}
	 */
	private IUserCtxDBMgr userCtxDBMgr;

	/**
	 * The Community Context DB Mgmt service reference.
	 * 
	 * @see {@link #setCommunityCtxDBMgr(ICommunityCtxDBMgr)}
	 */
	private ICommunityCtxDBMgr communityCtxDBMgr;

	/**
	 * The User Inference Mgmt service reference.
	 * 
	 * @see {@link #setUserCtxInferenceMgr(IUserCtxInferenceMgr)}
	 */
	@Autowired(required=true)
	private IUserCtxInferenceMgr userCtxInferenceMgr;

	/**
	 * Instantiates the platform Context Broker in Spring.
	 * 
	 * @param userCtxDBMgr
	 * @param commMgr
	 * @param communityCtxDBMgr
	 * @throws CtxException 
	 */
	@Autowired(required=true)
	InternalCtxBroker(IUserCtxDBMgr userCtxDBMgr, ICommManager commMgr,ICommunityCtxDBMgr communityCtxDBMgr) throws Exception {

		LOG.info(this.getClass() + " instantiated");
		this.userCtxDBMgr = userCtxDBMgr;
		this.communityCtxDBMgr = communityCtxDBMgr;
		LOG.info("Found ICommunityCtxDBMgr " + communityCtxDBMgr);

		this.idMgr = commMgr.getIdManager();
		final INetworkNode localCssNodeId = this.idMgr.getThisNetworkNode();
		LOG.info("Found local CSS node ID " + localCssNodeId);
		final IIdentity localCssId = this.idMgr.fromJid(localCssNodeId.getBareJid());
		LOG.info("Found local CSS ID " + localCssId);
		this.createIndividualEntity(localCssId, CtxEntityTypes.PERSON); // TODO remove? 
		// don't hardcode the cssOwner type
		this.createCssNode(localCssNodeId); // TODO remove?
	}

	/*
	 * Used for JUnit testing only.
	 */
	public InternalCtxBroker() {

		LOG.info(this.getClass() + " instantiated");
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#createAssociation(java.lang.String)
	 */
	@Override
	@Async
	public Future<CtxAssociation> createAssociation(String type) throws CtxException {

		CtxAssociation association = userCtxDBMgr.createAssociation(type);

		if (association!=null)
			return new AsyncResult<CtxAssociation>(association);
		else 
			return new AsyncResult<CtxAssociation>(null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#createAttribute(org.societies.api.context.model.CtxEntityIdentifier, java.lang.String)
	 */
	@Override
	@Async
	public Future<CtxAttribute> createAttribute(CtxEntityIdentifier scope,
			String type) throws CtxException {

		CtxAttribute attribute = null;

		attribute =	this.userCtxDBMgr.createAttribute(scope, type);	
		//TODO uncomment following lines when id manager is complete
		/*
		try {
			IIdentity scopeID = this.idMgr.fromJid(scope.getOwnerId());

			if (IdentityType.CSS.equals(scopeID.getType())){

				attribute =	this.userCtxDBMgr.createAttribute(scope, type);	

			} else if (IdentityType.CIS.equals(scopeID.getType())){

				attribute =	this.communityCtxDBMgr.createCommunityAttribute(scope, type);

			} 
		} catch (InvalidFormatException ife) {

			throw new CtxBrokerException(scope.getOwnerId()
					+ ": Invalid owner IIdentity String: " 
					+ ife.getLocalizedMessage(), ife);
		}
		 */
		return new AsyncResult<CtxAttribute>(attribute);
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#createEntity(java.lang.String)
	 */
	@Override
	@Async
	public Future<CtxEntity> createEntity(String type) throws CtxException {

		final CtxEntity entity = 
				this.userCtxDBMgr.createEntity(type);

		return new AsyncResult<CtxEntity>(entity);
	}

	/*
	 * @see org.societies.api.internal.context.broker.ICtxBroker#createIndividualEntity(org.societies.api.identity.IIdentity, java.lang.String)
	 */
	@Override
	@Async
	public Future<IndividualCtxEntity> createIndividualEntity(
			final IIdentity cssId, final String ownerType) throws CtxException {

		if (cssId == null)
			throw new NullPointerException("cssId can't be null");
		if (ownerType == null)
			throw new NullPointerException("ownerType can't be null");

		IndividualCtxEntity cssOwnerEnt = null;

		try {
			LOG.info("Checking if CSS owner context entity " + cssId + " exists...");
			cssOwnerEnt = this.retrieveIndividualEntity(cssId).get();
			if (cssOwnerEnt != null) {

				LOG.info("Found CSS owner context entity " + cssOwnerEnt.getId());
			} else {

				cssOwnerEnt = this.userCtxDBMgr.createIndividualCtxEntity(ownerType); 
				final CtxAttribute cssIdAttr = this.userCtxDBMgr.createAttribute(
						cssOwnerEnt.getId(), CtxAttributeTypes.ID); 

				this.updateAttribute(cssIdAttr.getId(), cssId.toString());
				LOG.info("Created CSS owner context entity " + cssOwnerEnt.getId());
			}

			return new AsyncResult<IndividualCtxEntity>(cssOwnerEnt);

		} catch (Exception e) {

			throw new CtxBrokerException("Could not create CSS owner context entity " + cssId
					+ ": " + e.getLocalizedMessage(), e);
		}
	}

	@Override
	@Async
	@Deprecated
	public Future<IndividualCtxEntity> createIndividualEntity(String type)
			throws CtxException {

		IndividualCtxEntity individualCtxEnt = this.userCtxDBMgr.createIndividualCtxEntity(type);
		return new AsyncResult<IndividualCtxEntity>(individualCtxEnt);
	}

	@Override
	@Async
	public Future<CommunityCtxEntity> createCommunityEntity(IIdentity cisId)
			throws CtxException {

		CommunityCtxEntity communityCtxEnt = communityCtxDBMgr.createCommunityEntity(cisId);

		return new AsyncResult<CommunityCtxEntity>(communityCtxEnt);
	}

	@Override
	public void disableCtxMonitoring(CtxAttributeValueType type) throws CtxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableCtxRecording() throws CtxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableCtxMonitoring(CtxAttributeValueType type) throws CtxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableCtxRecording() throws CtxException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#lookupEntities(java.lang.String, java.lang.String, java.io.Serializable, java.io.Serializable)
	 */
	@Override
	@Async
	public Future<List<CtxEntityIdentifier>> lookupEntities(String entityType,
			String attribType, Serializable minAttribValue,
			Serializable maxAttribValue) throws CtxException {

		final List<CtxEntityIdentifier> results = new ArrayList<CtxEntityIdentifier>(); 

		results.addAll(
				this.userCtxDBMgr.lookupEntities(entityType, attribType, minAttribValue, maxAttribValue));

		return new AsyncResult<List<CtxEntityIdentifier>>(results);
	}



	/*
	 * returns a list of entities with a specified value for a specified attribute type
	 */
	@Override
	@Async
	public Future<List<CtxEntityIdentifier>> lookupEntities(List<CtxEntityIdentifier> ctxEntityIDList, String ctxAttributeType, Serializable value){

		List<CtxEntityIdentifier> entityList = new ArrayList<CtxEntityIdentifier>(); 
		try {
			for(CtxEntityIdentifier entityId :ctxEntityIDList){
				CtxEntity entity = (CtxEntity) this.retrieve(entityId).get();

				Set<CtxAttribute> ctxAttrSet = entity.getAttributes(ctxAttributeType);
				for(CtxAttribute ctxAttr : ctxAttrSet){
					//LOG.info("---- lookupEntities,  attr id " +ctxAttr.getId());

					if(CtxBrokerUtils.compareAttributeValues(ctxAttr,value)) {
						//LOG.info("---- lookupEntities,  " +ctxAttr.getId());
						entityList.add(entityId);
					}
				}
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CtxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return new AsyncResult<List<CtxEntityIdentifier>>(entityList);


	}


	@Override
	public Future<CtxModelObject> remove(CtxIdentifier identifier) throws CtxException {

		final CtxModelObject modelObj = this.userCtxDBMgr.remove(identifier);

		return new AsyncResult<CtxModelObject>(modelObj) ;
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#retrieve(org.societies.api.context.model.CtxIdentifier)
	 */
	@Override
	@Async
	public Future<CtxModelObject> retrieve(CtxIdentifier identifier) throws CtxException {

		Boolean inferValue = false;

		IIdentity targetCss;
		try {
			targetCss = this.idMgr.fromJid(identifier.getOwnerId());
			if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
				this.privacyLogAppender.logContext(null, targetCss);
		} catch (InvalidFormatException ife) {
			throw new CtxBrokerException("Could not create IIdentity from JID '"
					+ identifier.getOwnerId() + "': " + ife.getLocalizedMessage(), ife);
		}
		CtxModelObject modelObj = this.userCtxDBMgr.retrieve(identifier);
		
		LOG.info("obj to be inferred "+ modelObj.getId());
		
		// inference code
		if (modelObj instanceof CtxAttribute){
			CtxAttribute ctxAttr = (CtxAttribute) modelObj;
			LOG.info("obj casted - to be inferred "+ ctxAttr.getId());
			
			Boolean isInferable = false;
			LOG.info("inference manager instance : "+this.userCtxInferenceMgr );
			LOG.info("inference manager types : "+this.userCtxInferenceMgr.getInferrableTypes());

			if(this.userCtxInferenceMgr.getInferrableTypes().contains(ctxAttr.getType()))  isInferable = true;

			if( !CtxBrokerUtils.hasValue(ctxAttr) && isInferable) {
				LOG.info("has value "+ CtxBrokerUtils.hasValue(ctxAttr));
				inferValue = true;
			}
/*
			if (CtxBrokerUtils.hasValue(ctxAttr) && isInferable) {
				if(CtxBrokerUtils.isPoorQuality(ctxAttr.getQuality())) inferValue = true;
			}
*/
			LOG.info("inferValue: "+ inferValue);

			if(inferValue){
				LOG.info("before inference infered CtxAttr: "+ ctxAttr.getStringValue());
				CtxAttribute inferedCtxAttr = userCtxInferenceMgr.predictContext(ctxAttr.getId(), new Date());	
				LOG.info("after inference inferedCtxAttr: "+ inferedCtxAttr.getId());
				LOG.info("after inference inferedCtxAttr: "+ inferedCtxAttr.getStringValue());
				modelObj = (CtxModelObject) inferedCtxAttr;
				LOG.info("return modelObj: "+ modelObj.getId()) ;
			}
		}

		return new AsyncResult<CtxModelObject>(modelObj);
	}	

	@Override
	public Future<CtxAttribute> retrieveAttribute(
			CtxAttributeIdentifier identifier, boolean enableInference)
					throws CtxException {

		IIdentity targetCss;
		try {
			targetCss = this.idMgr.fromJid(identifier.getOwnerId());
			if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
				this.privacyLogAppender.logContext(null, targetCss);
		} catch (InvalidFormatException ife) {
			throw new CtxBrokerException("Could not create IIdentity from JID '"
					+ identifier.getOwnerId() + "': " + ife.getLocalizedMessage(), ife);
		}

		CtxModelObject modelObj = this.userCtxDBMgr.retrieve(identifier);
		CtxAttribute ctxAttr = (CtxAttribute) modelObj;

		// inference code
		if(enableInference == true){


			Boolean inferValue = false;
			Boolean isInferable = false;

			if(userCtxInferenceMgr.getInferrableTypes().contains(ctxAttr.getType()))  isInferable = true;

			if( !CtxBrokerUtils.hasValue(ctxAttr) && isInferable) {
				inferValue = true;
			}

			if (CtxBrokerUtils.hasValue(ctxAttr) && isInferable) {
				if(CtxBrokerUtils.isPoorQuality(ctxAttr.getQuality())) inferValue = true;
			}

			LOG.info("inferValue: "+ inferValue);

			if(inferValue){

				CtxAttribute inferedCtxAttr = userCtxInferenceMgr.predictContext(ctxAttr.getId(), new Date());	
				LOG.info("inferedCtxAttr: "+ inferedCtxAttr.getId());
			}		

		}

		return new AsyncResult<CtxAttribute>(ctxAttr);
	}



	/*
	 * @see org.societies.api.internal.context.broker.ICtxBroker#retrieveIndividualEntity(org.societies.api.identity.IIdentity)
	 */
	@Override
	@Async
	public Future<IndividualCtxEntity> retrieveIndividualEntity(
			final IIdentity cssId) throws CtxException {

		if (cssId == null)
			throw new NullPointerException("cssId can't be null");

		if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
			this.privacyLogAppender.logContext(null, cssId);

		IndividualCtxEntity cssOwner = null;
		final List<CtxIdentifier> attrIds = this.userCtxDBMgr.lookup(
				CtxModelType.ATTRIBUTE, CtxAttributeTypes.ID);
		for (final CtxIdentifier attrId : attrIds) {

			final CtxAttribute cssIdAttr = (CtxAttribute) this.userCtxDBMgr.retrieve(attrId);
			if (!CtxEntityTypes.CSS_NODE.equals(cssIdAttr.getScope().getType())
					&& cssId.toString().equals(cssIdAttr.getStringValue())) {
				cssOwner = (IndividualCtxEntity) this.userCtxDBMgr.retrieve(cssIdAttr.getScope());
				break;
			}
		}

		return new AsyncResult<IndividualCtxEntity>(cssOwner);
	}

	/*
	 * @see org.societies.api.internal.context.broker.ICtxBroker#retrieveCssOperator()
	 */
	@Override
	@Async
	@Deprecated
	public Future<IndividualCtxEntity> retrieveCssOperator()
			throws CtxException {

		IIdentity localCssId;
		try {
			localCssId = this.idMgr.fromJid(this.idMgr.getThisNetworkNode().getBareJid());
		} catch (InvalidFormatException ife) {

			throw new CtxBrokerException("Could not retrieve local CSS IIdentity: "
					+ ife.getLocalizedMessage(), ife);
		}

		return this.retrieveIndividualEntity(localCssId);
	}

	/*
	 * @see org.societies.api.internal.context.broker.ICtxBroker#retrieveCssNode(org.societies.api.identity.INetworkNode)
	 */
	@Override
	@Async
	public Future<CtxEntity> retrieveCssNode(final INetworkNode cssNodeId) 
			throws CtxException {

		if (cssNodeId == null)
			throw new NullPointerException("cssNodeId can't be null");

		CtxEntity cssNode = null;
		final List<CtxEntityIdentifier> entIds = this.userCtxDBMgr.lookupEntities(
				CtxEntityTypes.CSS_NODE, CtxAttributeTypes.ID, cssNodeId.toString(), cssNodeId.toString());
		if (!entIds.isEmpty()) {

			try {
				cssNode = (CtxEntity) this.retrieve(entIds.get(0)).get();
			} catch (Exception e) {

				throw new CtxBrokerException("Failed to retrieve CSS node context entity " + cssNodeId
						+ ": " + e.getLocalizedMessage(), e);
			}
		}
		return new AsyncResult<CtxEntity>(cssNode);
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#update(org.societies.api.context.model.CtxModelObject)
	 */
	@Override
	@Async
	public Future<CtxModelObject> update(CtxModelObject identifier) throws CtxException {

		final CtxModelObject modelObject = this.userCtxDBMgr.update(identifier);

		// this part allows the storage of attribute updates to context history
		if (CtxModelType.ATTRIBUTE.equals(modelObject.getModelType())) {
			final CtxAttribute ctxAttr = (CtxAttribute) modelObject;
			if (ctxAttr.isHistoryRecorded() && this.userCtxHistoryMgr != null)
				this.userCtxHistoryMgr.storeHoCAttribute(ctxAttr);

			// check if ctxAttr is also registered to be stored in tuples			
			try {
				// the list of escorting atts is empty
				List<CtxAttributeIdentifier> escList = new ArrayList<CtxAttributeIdentifier>();
				List<CtxAttributeIdentifier> hocTuplesList = this.getHistoryTuples(ctxAttr.getId(),escList).get();
				if( hocTuplesList.size()>0 ) this.storeHoCAttributeTuples(ctxAttr);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return new AsyncResult<CtxModelObject>(modelObject);
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#updateAttribute(org.societies.api.context.model.CtxAttributeIdentifier, java.io.Serializable)
	 */
	@Override
	@Async
	public Future<CtxAttribute> updateAttribute(
			CtxAttributeIdentifier attributeId, Serializable value) throws CtxException {

		// Implies <code>null</code> valueMetric param
		return this.updateAttribute(attributeId, value, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#updateAttribute(org.societies.api.context.model.CtxAttributeIdentifier, java.io.Serializable, java.lang.String)
	 */
	@Override
	@Async
	public Future<CtxAttribute> updateAttribute(
			CtxAttributeIdentifier attributeId, Serializable value,
			String valueMetric) throws CtxException {

		if (attributeId == null)
			throw new NullPointerException("attributeId can't be null");
		// Will throw IllegalArgumentException if value type is not supported
		final CtxAttributeValueType valueType = CtxBrokerUtils.findAttributeValueType(value);


		CtxAttribute attribute = (CtxAttribute) this.userCtxDBMgr.retrieve(attributeId);

		if (attribute == null) {
			// Requested attribute not found
			return new AsyncResult<CtxAttribute>(null);
		} else {
			if (CtxAttributeValueType.EMPTY.equals(valueType))
				attribute.setStringValue(null);
			else if (CtxAttributeValueType.STRING.equals(valueType))
				attribute.setStringValue((String) value);
			else if (CtxAttributeValueType.INTEGER.equals(valueType))
				attribute.setIntegerValue((Integer) value);
			else if (CtxAttributeValueType.DOUBLE.equals(valueType))
				attribute.setDoubleValue((Double) value);
			else if (CtxAttributeValueType.BINARY.equals(valueType))
				attribute.setBinaryValue((byte[]) value);


			attribute.setValueType(valueType);
			try {
				attribute = (CtxAttribute) this.update(attribute).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return new AsyncResult<CtxAttribute>(attribute);
		}

	}



	@Override
	public Future<List<CtxIdentifier>> lookup(CtxModelType modelType,
			String type) throws CtxException {

		final List<CtxIdentifier> results = this.userCtxDBMgr.lookup(modelType, type);

		return new AsyncResult<List<CtxIdentifier>>(results) ;

	}

	//***********************************************
	//     Context Update Events Methods  
	//***********************************************

	@Override
	public void registerForUpdates(CtxAttributeIdentifier attrId) throws CtxException {
		// TODO remove DEPRECATED

	}

	/* (non-Javadoc)
	 * @see org.societies.api.context.broker.ICtxBroker#registerForChanges(org.societies.api.context.event.CtxChangeEventListener, org.societies.api.context.model.CtxIdentifier)
	 */
	@Override
	public void registerForChanges(final CtxChangeEventListener listener,
			final CtxIdentifier ctxId) throws CtxException {

		if (listener == null)
			throw new NullPointerException("listener can't be null");
		if (ctxId == null)
			throw new NullPointerException("ctxId can't be null");

		final String[] topics = new String[] {
				CtxChangeEventTopic.UPDATED,
				CtxChangeEventTopic.MODIFIED,
				CtxChangeEventTopic.REMOVED,
		};
		if (this.ctxEventMgr != null) {
			if (LOG.isInfoEnabled())
				LOG.info("Registering context change event listener for object '"
						+ ctxId + "' to topics '" + Arrays.toString(topics) + "'");
			this.ctxEventMgr.registerChangeListener(listener, topics, ctxId);
		} else {
			throw new CtxBrokerException("Could not register context change event listener for object '"
					+ ctxId + "' to topics '" + Arrays.toString(topics)
					+ "': ICtxEventMgr service is not available");
		}
	}

	/* (non-Javadoc)
	 * @see org.societies.api.context.broker.ICtxBroker#unregisterFromChanges(org.societies.api.context.event.CtxChangeEventListener, org.societies.api.context.model.CtxIdentifier)
	 */
	@Override
	public void unregisterFromChanges(final CtxChangeEventListener listener,
			final CtxIdentifier ctxId) throws CtxException {

		if (listener == null)
			throw new NullPointerException("listener can't be null");
		if (ctxId == null)
			throw new NullPointerException("ctxId can't be null");

		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.societies.api.context.broker.ICtxBroker#registerForChanges(org.societies.api.context.event.CtxChangeEventListener, org.societies.api.context.model.CtxEntityIdentifier, java.lang.String)
	 */
	@Override
	public void registerForChanges(final CtxChangeEventListener listener,
			final CtxEntityIdentifier scope, final String attrType) throws CtxException {

		if (listener == null)
			throw new NullPointerException("listener can't be null");
		if (scope == null)
			throw new NullPointerException("scope can't be null");
		if (attrType == null)
			throw new NullPointerException("attrType can't be null");

		final String[] topics = new String[] {
				CtxChangeEventTopic.UPDATED,
				CtxChangeEventTopic.MODIFIED,
				CtxChangeEventTopic.REMOVED,
		};
		if (this.ctxEventMgr != null) {
			if (LOG.isInfoEnabled())
				LOG.info("Registering context change event listener for attributes with scope '"
						+ scope + "' and type '" + attrType + "' to topics '" 
						+ Arrays.toString(topics) + "'");
			this.ctxEventMgr.registerChangeListener(listener, topics, scope, attrType );
		} else {
			throw new CtxBrokerException("Could not register context change event listener for attributes with scope '"
					+ scope + "' and type '" + attrType + "' to topics '" + Arrays.toString(topics)
					+ "': ICtxEventMgr service is not available");
		}
	}

	/* (non-Javadoc)
	 * @see org.societies.api.context.broker.ICtxBroker#unregisterFromChanges(org.societies.api.context.event.CtxChangeEventListener, org.societies.api.context.model.CtxEntityIdentifier, java.lang.String)
	 */
	@Override
	public void unregisterFromChanges(final CtxChangeEventListener listener,
			final CtxEntityIdentifier scope, final String attrType) throws CtxException {

		if (listener == null)
			throw new NullPointerException("listener can't be null");
		if (scope == null)
			throw new NullPointerException("scope can't be null");
		if (attrType == null)
			throw new NullPointerException("attrType can't be null");

		// TODO Auto-generated method stub
	}


	//***********************************************
	//     Context Inference Methods  
	//***********************************************

	@Override
	public Future<List<Object>> evaluateSimilarity(
			Serializable objectUnderComparison,
			List<Serializable> referenceObjects) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Future<List<CtxAttribute>> retrieveFuture(
			CtxAttributeIdentifier attrId, Date date) throws CtxException {
		// TODO Auto-generated method stub
		IIdentity targetCss;
		try {
			targetCss = this.idMgr.fromJid(attrId.getOwnerId());
			if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
				this.privacyLogAppender.logContext(null, targetCss);
		} catch (InvalidFormatException ife) {
			throw new CtxBrokerException("Could not create IIdentity from JID '"
					+ attrId.getOwnerId() + "': " + ife.getLocalizedMessage(), ife);
		}
		return null;
	}

	@Override
	public Future<List<CtxAttribute>> retrieveFuture(
			CtxAttributeIdentifier attrId, int modificationIndex) throws CtxException {
		// TODO Auto-generated method stub
		IIdentity targetCss;
		try {
			targetCss = this.idMgr.fromJid(attrId.getOwnerId());
			if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
				this.privacyLogAppender.logContext(null, targetCss);
		} catch (InvalidFormatException ife) {
			throw new CtxBrokerException("Could not create IIdentity from JID '"
					+ attrId.getOwnerId() + "': " + ife.getLocalizedMessage(), ife);
		}
		return null;
	}

	//***********************************************
	//     Community Context Management Methods  
	//***********************************************
	@Override
	public Future<List<CtxEntityIdentifier>> retrieveCommunityMembers(
			CtxEntityIdentifier community) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxEntityIdentifier>> retrieveParentCommunities(
			CtxEntityIdentifier community) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxEntityIdentifier>> retrieveSubCommunities(
			CtxEntityIdentifier community) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<IndividualCtxEntity> retrieveAdministratingCSS(
			CtxEntityIdentifier community) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Set<CtxBond>> retrieveBonds(CtxEntityIdentifier community) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}


	//***********************************************
	//     Context History Management Methods  
	//***********************************************


	@Override
	@Async
	public Future<List<CtxHistoryAttribute>> retrieveHistory(
			CtxAttributeIdentifier attrId, Date startDate, Date endDate) throws CtxException {

		final List<CtxHistoryAttribute> result = new ArrayList<CtxHistoryAttribute>();
		IIdentity targetCss;
		try {
			targetCss = this.idMgr.fromJid(attrId.getOwnerId());
			if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
				this.privacyLogAppender.logContext(null, targetCss);
		} catch (InvalidFormatException ife) {
			throw new CtxBrokerException("Could not create IIdentity from JID '"
					+ attrId.getOwnerId() + "': " + ife.getLocalizedMessage(), ife);
		}
		result.addAll(this.userCtxHistoryMgr.retrieveHistory(attrId, startDate, endDate));

		return new AsyncResult<List<CtxHistoryAttribute>>(result);
	}

	@Override
	public Future<Integer> removeHistory(String type, Date startDate, Date endDate) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<CtxHistoryAttribute>> retrieveHistory(
			CtxAttributeIdentifier attrId, int arg1) throws CtxException {
		// TODO Auto-generated method stub
		IIdentity targetCss;
		try {
			targetCss = this.idMgr.fromJid(attrId.getOwnerId());
			if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
				this.privacyLogAppender.logContext(null, targetCss);
		} catch (InvalidFormatException ife) {
			throw new CtxBrokerException("Could not create IIdentity from JID '"
					+ attrId.getOwnerId() + "': " + ife.getLocalizedMessage(), ife);
		}
		printHocDB(); // TODO remove
		return null;
	}


	@Override
	public Future<List<CtxAttributeIdentifier>> getHistoryTuples(
			CtxAttributeIdentifier primaryAttrIdentifier, List<CtxAttributeIdentifier> arg1)
					throws CtxException {

		List<CtxAttributeIdentifier> tupleAttrIDs = new ArrayList<CtxAttributeIdentifier>(); 

		//	final String tupleAttrType = "tupleIds_" + primaryAttrIdentifier.getType();
		final String tupleAttrType = "tupleIds_" + primaryAttrIdentifier.toString();
		List<CtxIdentifier> ls;
		try {
			ls = this.lookup(CtxModelType.ATTRIBUTE, tupleAttrType).get();
			if (ls.size() > 0) {
				CtxIdentifier id = ls.get(0);
				final CtxAttribute tupleIdsAttribute = (CtxAttribute) this.userCtxDBMgr.retrieve(id);

				//deserialise object
				tupleAttrIDs = (List<CtxAttributeIdentifier>) SerialisationHelper.deserialise(tupleIdsAttribute.getBinaryValue(), this.getClass().getClassLoader());
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new AsyncResult<List<CtxAttributeIdentifier>>(tupleAttrIDs);
	}

	@Override
	public Future<Boolean> removeHistoryTuples(CtxAttributeIdentifier arg0,
			List<CtxAttributeIdentifier> arg1) throws CtxException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.societies.api.internal.context.broker.ICtxBroker#setHistoryTuples(org.societies.api.context.model.CtxAttributeIdentifier, java.util.List)
	 */
	@Override
	@Async
	public Future<Boolean> setHistoryTuples(CtxAttributeIdentifier primaryAttrIdentifier,
			List<CtxAttributeIdentifier> listOfEscortingAttributeIds) throws CtxException {

		boolean result = false;

		LOG.info("setting history tuples primaryAttrIdentifier: "+primaryAttrIdentifier );

		try {
			// set hoc recording flag for the attributes contained in tuple list
			final List<CtxAttributeIdentifier> allAttrIds = new ArrayList<CtxAttributeIdentifier>();
			// add the primary attr id
			allAttrIds.add(0,primaryAttrIdentifier);
			// add the escorting attr ids
			allAttrIds.addAll(listOfEscortingAttributeIds);

			for (CtxAttributeIdentifier escortingAttrID : allAttrIds) {

				// set history flag for all escorting attributes
				CtxAttribute attr = (CtxAttribute) this.userCtxDBMgr.retrieve(escortingAttrID);
				attr.setHistoryRecorded(true);
				this.update(attr);
			}
			// set hoc recording flag for the attributes contained in tuple list --end

			//this attr will maintain the attr ids of all the (not only the escorting) hoc_attibutes in a blob
			//final String tupleAttrType = "tupleIds_" + primaryAttrIdentifier.getType();
			final String tupleAttrType = "tupleIds_" + primaryAttrIdentifier.toString();

			final CtxAttribute tupleAttr = (CtxAttribute) this.createAttribute(primaryAttrIdentifier.getScope(), tupleAttrType).get();

			byte[] attrIdsBlob = SerialisationHelper.serialise((Serializable) allAttrIds);
			tupleAttr.setBinaryValue(attrIdsBlob);
			CtxModelObject updatedAttr = (CtxAttribute) this.update(tupleAttr).get();

			if(updatedAttr != null && updatedAttr.getType().contains("tupleIds_")) result = true;

			LOG.info("tuple Attr ids "+allAttrIds);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return new AsyncResult<Boolean>(result);
	}

	@Override
	public Future<List<CtxAttributeIdentifier>> updateHistoryTuples(
			CtxAttributeIdentifier primaryAttrIdentifier, List<CtxAttributeIdentifier> newAttrList)
					throws CtxException {

		List<CtxAttributeIdentifier> results = new ArrayList<CtxAttributeIdentifier>();
		try {
			for (CtxAttributeIdentifier escortingAttrID : newAttrList) {
				// set history flag for all escorting attributes

				CtxAttribute attr = (CtxAttribute) this.userCtxDBMgr.retrieve(escortingAttrID);

				attr.setHistoryRecorded(true);
				this.update(attr);
			}
			//			List<CtxAttributeIdentifier> oldTupleAttrIDs = new ArrayList<CtxAttributeIdentifier>();
			//final String tupleAttrType = "tupleIds_" + primaryAttrIdentifier.getType();
			final String tupleAttrType = "tupleIds_" + primaryAttrIdentifier.toString();
			List<CtxAttributeIdentifier> newTupleAttrIDs = new ArrayList<CtxAttributeIdentifier>(); 
			newTupleAttrIDs.add(0,primaryAttrIdentifier);
			newTupleAttrIDs.addAll(newAttrList);

			List<CtxIdentifier> ls;			
			ls = this.lookup(CtxModelType.ATTRIBUTE, tupleAttrType).get();
			if (ls.size() > 0) {
				CtxIdentifier id = ls.get(0);
				final CtxAttribute tupleIdsAttribute = (CtxAttribute) this.userCtxDBMgr.retrieve(id);
				//deserialise object
				byte[] attrIdsBlob = SerialisationHelper.serialise((Serializable) newTupleAttrIDs);
				tupleIdsAttribute.setBinaryValue(attrIdsBlob);
				CtxAttribute updatedAttr = (CtxAttribute) this.update(tupleIdsAttribute).get();
				// set hoc recording flag for the attributes contained in tuple list --end
				if(updatedAttr != null && updatedAttr.getType().contains("tupleIds_")) {
					results = (List<CtxAttributeIdentifier>) SerialisationHelper.deserialise(updatedAttr.getBinaryValue(), this.getClass().getClassLoader());
				}
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new AsyncResult<List<CtxAttributeIdentifier>>(results);
	}

	@Override
	public Future<Map<CtxHistoryAttribute, List<CtxHistoryAttribute>>> retrieveHistoryTuples(
			String attributeType, List<CtxAttributeIdentifier> escortingAttrIds,
			Date startDate, Date endDate) {

		Map<CtxHistoryAttribute, List<CtxHistoryAttribute>> tupleResults = new LinkedHashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>>();

		try {
			List<CtxIdentifier> ctxAttrListIds = this.lookup(CtxModelType.ATTRIBUTE, attributeType).get();
			//LOG.info("ctxAttribute list "+ctxAttrListIds);
			CtxAttributeIdentifier primaryAttrId = null;

			for(int i=0; i< ctxAttrListIds.size(); i++){
				primaryAttrId = (CtxAttributeIdentifier) ctxAttrListIds.get(i);

				IIdentity targetCss;
				try {
					targetCss = this.idMgr.fromJid(primaryAttrId.getOwnerId());
					if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
						this.privacyLogAppender.logContext(null, targetCss);
				} catch (InvalidFormatException ife) {
					throw new CtxBrokerException("Could not create IIdentity from JID", ife);
				}

				List<CtxAttributeIdentifier> listOfEscortingAttributeIds = new ArrayList<CtxAttributeIdentifier>();
				Map<CtxHistoryAttribute, List<CtxHistoryAttribute>> tempTupleResults = new HashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>>(); 

				tempTupleResults =	retrieveHistoryTuples(primaryAttrId, listOfEscortingAttributeIds, startDate, endDate).get();
				tupleResults.putAll(tempTupleResults);
			}			
			// short tupleResults data based on timestamps
			tupleResults = shortByTime((HashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>>) tupleResults);


		} catch (CtxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new AsyncResult<Map<CtxHistoryAttribute, List<CtxHistoryAttribute>>>(tupleResults);
	}


	private LinkedHashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>> shortByTime(HashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>> data){
		LinkedHashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>> result = new LinkedHashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>>();

		//		System.out.println("********************");
		//		System.out.println(data);
		TreeMap<Date,CtxHistoryAttribute> tempHocDataTreeMap = new TreeMap<Date,CtxHistoryAttribute>();

		for(CtxHistoryAttribute hocAttr: data.keySet()){
			//	System.out.println(hocAttr.getId());
			//	System.out.println("NOT shortByTime"+ hocAttr.getLastModified().getTime());
			tempHocDataTreeMap.put(hocAttr.getLastModified(),hocAttr);
		}

		for(Date date :tempHocDataTreeMap.keySet()){
			//		System.out.println(date.getTime());
			//		System.out.println(tempHocDataTreeMap.get(date));
			CtxHistoryAttribute keyHocAttr = tempHocDataTreeMap.get(date);
			result.put(keyHocAttr, data.get(keyHocAttr));
		}
		/*	
		for(CtxHistoryAttribute hocAttr: result.keySet()){
			System.out.println("result Short"+ hocAttr.getLastModified().getTime()+" id:"+hocAttr.getId());
			System.out.println("escorting hoc "+ result.get(hocAttr));
		}
		System.out.println("********************");
		 */
		return result;
	}

	@Override
	public Future<Map<CtxHistoryAttribute, List<CtxHistoryAttribute>>> retrieveHistoryTuples(
			CtxAttributeIdentifier primaryAttrId, List<CtxAttributeIdentifier> escortingAttrIds,
			Date arg2, Date arg3) throws CtxException {

		Map<CtxHistoryAttribute, List<CtxHistoryAttribute>> results = new LinkedHashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>>();

		//LOG.info("retrieveHistoryTuples updating hocAttrs primaryAttr: "+primaryAttrId);

		if(primaryAttrId!= null){ // TODO throw NPE otherwise

			IIdentity targetCss;
			try {
				targetCss = this.idMgr.fromJid(primaryAttrId.getOwnerId());
				if (this.hasPrivacyLogAppender && this.privacyLogAppender != null)
					this.privacyLogAppender.logContext(null, targetCss);
			} catch (InvalidFormatException ife) {
				throw new CtxBrokerException("Could not create IIdentity from JID", ife);
			}

			String tupleAttrType = "tuple_"+primaryAttrId.toString();
			List<CtxIdentifier> listIds;

			try {
				listIds = this.lookup(CtxModelType.ATTRIBUTE,tupleAttrType).get();
				if( listIds.size()>0){


					CtxAttributeIdentifier tupleAttrTypeID = (CtxAttributeIdentifier) listIds.get(0);

					// retrieve historic attrs of type "tuple_action"
					// each hoc attr contains a value (blob) list of historic attrs store together
					List<CtxHistoryAttribute> hocResults = retrieveHistory(tupleAttrTypeID,null,null).get();            

					// for each "tuple_status" hoc attr 
					for (CtxHistoryAttribute hocAttr : hocResults) {

						// get the list of hoc attrs stored as BlobValue
						List<CtxHistoryAttribute> tupleValueList = (List<CtxHistoryAttribute>) SerialisationHelper.deserialise(hocAttr.getBinaryValue(), this.getClass().getClassLoader());

						// list of historic attributes contained in "tuple_status" retrieved
						//LOG.info("retrieveHistoryTuples tupleValueList: "+tupleValueList);
						//int ia = 0;
						//for each historic attr 
						for (CtxHistoryAttribute tupledHoCAttrTemp : tupleValueList){
							//the key , primary historic attribute
							CtxHistoryAttribute keyAttr = null;
							//the escorting historic attributes
							List<CtxHistoryAttribute> listEscHocAttrs = new ArrayList<CtxHistoryAttribute>();
							//for each historic attr in blob value check if the identifier equals the primary identifier
							if (tupledHoCAttrTemp.getId().toString().equals(primaryAttrId.toString())){
								//	ia++;
								keyAttr = tupledHoCAttrTemp;
								for (CtxHistoryAttribute tupledHoCAttrEscorting : tupleValueList){
									if (!(tupledHoCAttrEscorting.getId().toString().equals(primaryAttrId.toString()))){
										listEscHocAttrs.add(tupledHoCAttrEscorting);
									}  
								}
								results.put(keyAttr, listEscHocAttrs);    
							}
						}// end of for loop
					}	
				}//if size
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(results == null){
			results = new LinkedHashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>>();
		}
		//LOG.info("retrieveHistoryTuples results: "+results);

		return new AsyncResult<Map<CtxHistoryAttribute, List<CtxHistoryAttribute>>>(results);
	}


	public void storeHoCAttributeTuples(CtxAttribute primaryAttr){

		//String tupleAttrType = "tuple_"+primaryAttr.getType().toString();

		String tupleAttrType = "tuple_"+primaryAttr.getId().toString();
		// the attr that will maintain the tuples; 
		CtxAttribute tupleAttr = null;
		List<CtxHistoryAttribute> tupleValueList = new ArrayList<CtxHistoryAttribute>();
		try {
			List<CtxAttributeIdentifier> tempEscListIds = new ArrayList<CtxAttributeIdentifier>();
			List<CtxAttributeIdentifier> tupleListIds = this.getHistoryTuples(primaryAttr.getId(),tempEscListIds).get();

			List<CtxIdentifier> tupleAttrIDsList = this.lookup(CtxModelType.ATTRIBUTE, tupleAttrType).get();
			if(tupleAttrIDsList.size() != 0){
				//tuple_status retrieved
				tupleAttr = (CtxAttribute) this.retrieveAttribute( (CtxAttributeIdentifier) tupleAttrIDsList.get(0), false).get();
			}
			if(tupleAttrIDsList.size() == 0){
				//tuple_status created
				tupleAttr = this.createAttribute(primaryAttr.getScope(), tupleAttrType).get();
			} 

			//prepare value of ctxAttribute
			for (CtxAttributeIdentifier tupleAttrID : tupleListIds) {
				//for one of the escorting attrIds retrieve all history and find the latest value
				List<CtxHistoryAttribute> allValues = this.retrieveHistory(tupleAttrID, null, null).get();
				if (allValues != null){
					//finding latest hoc value
					int size = allValues.size();
					int last = 0;
					if (size >= 1){
						last = size-1;    
						CtxHistoryAttribute latestHoCAttr2 = allValues.get(last);
						if (latestHoCAttr2 != null )tupleValueList.add(latestHoCAttr2);
					}
				}           
			}
			byte[] tupleValueListBlob = SerialisationHelper.serialise((Serializable) tupleValueList);
			if(tupleAttr != null) tupleAttr.setBinaryValue(tupleValueListBlob);

			CtxHistoryAttribute hocAttr = this.userCtxHistoryMgr.createHistoryAttribute(tupleAttr);

			//LOG.info("storeHoCAttributeTuples updating hocAttrs: "+hocAttr);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CtxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/*
	 * 
	public void createHistoryAttributeTuples(CtxAttribute primaryAttr, Map<CtxHistoryAttribute, List<CtxHistoryAttribute>> hocTuples){

		String tupleAttrType = "tuple_"+primaryAttr.getType();
		CtxAttribute tupleAttr = null;

		List<CtxIdentifier> tupleAttrIDsList = this.lookup(CtxModelType.ATTRIBUTE, tupleAttrType).get();
		if(tupleAttrIDsList.size() != 0){
			//tuple_status retrieved
			tupleAttr = (CtxAttribute) this.retrieve(tupleAttrIDsList.get(0)).get();
		}
		if(tupleAttrIDsList.size() == 0){
			//tuple_status created
			tupleAttr = this.createAttribute(primaryAttr.getScope(), tupleAttrType).get();
		}
	}
	 */

	@Override
	public Future<CtxHistoryAttribute> createHistoryAttribute(CtxAttributeIdentifier attID, Date date, Serializable value, CtxAttributeValueType valueType){

		CtxHistoryAttribute hocAttr = this.userCtxHistoryMgr.createHistoryAttribute(attID,date,value,valueType);
		return new AsyncResult<CtxHistoryAttribute>(hocAttr);
	}

	void printHocDB(){
		this.userCtxHistoryMgr.printHocDB();
	}

	/*
	 * HoC tuples will be stored in an attribute of type "tuple_attibuteType" (tuple_status)
	 * the value will contain a list of ICtxHistoricAttribute 
	 * 
	 * tupleAttrIDs  the list of escorting attributes (also contains primary attribute id)
	 * ctxHocAttr  primary attribute to be stored 
	 */

	/*
	private void storeHoCTuples(ICtxHistoricAttribute ctxHocAttr ,List<ICtxAttributeIdentifier> tupleAttrIDs, IDigitalPersonalIdentifier dpi){

        String tupleAttrType = "tuple_"+ctxHocAttr.getType().toString();
        ICtxAttribute tupleAttr = null;
        ICtxIdentifier id;
        List<ICtxHistoricAttribute> tupleValueList = new ArrayList<ICtxHistoricAttribute>();
        // the attribute ids to be stored in same tuple.
        ICtxIdentifier tuple_type_attr_id;
        try {
            ICtxEntity operator = ctxDBManager.retrieveOperator();
            tuple_type_attr_id = this.identManager.getMappedCtxIdentifier(dpi, tupleAttrType);
            log.info(" storing tuples tuple_type_attr_id  :"+tuple_type_attr_id);
            if (tuple_type_attr_id != null) {
                // tuple_type_attr_id.setOperatorId(dpi.toUriString());
                tupleAttr = (ICtxAttribute) ctxDBManager.retrieve(tuple_type_attr_id); 
                tupleAttr.getCtxIdentifier().setOperatorId(dpi.toUriString());
            } 
            //create a new tuple_type attribute 
            if (tuple_type_attr_id == null) {
                tupleAttr = ctxDBManager.createAttribute(operator.getCtxIdentifier(), tupleAttrType);
                tuple_type_attr_id = this.identManager.addMappedCtxIdentifier(dpi, tupleAttr.getCtxIdentifier());
            }
            //            log.info("ATTRIBUTE exists and RETRIEVED ***** "+tupleAttr);

            for (ICtxAttributeIdentifier tupleAttrID : tupleAttrIDs) {

                //for one of the escorting attrIds retrieve all history and find the latest value
                List<ICtxHistoricAttribute> allValues = ctxHistoryDBManager.retrieveHistory(tupleAttrID);

                if (allValues != null){
                    //finding latest hoc value
                    int size = allValues.size();
                    int last = 0;
                    if (size >= 1){
                        last = size-1;    
                        ICtxHistoricAttribute latestHoCAttr2 = allValues.get(last);
                        if (latestHoCAttr2 != null )tupleValueList.add(latestHoCAttr2);
                    }
                }           
            }
            tupleAttr.setBlobValue((Serializable) tupleValueList);
            tupleAttr.getCtxIdentifier().setOperatorId(dpi.toUriString());
            ICtxHistoricAttribute hocAttr = ctxHistoryDBManager.createHistoricAttribute(tupleAttr);

            ctxHistoryDBManager.storeHistoricAttribute(hocAttr);
        }  catch (ContextDBException e) {
            this.log.error("Exception "+ e.getLocalizedMessage());
            //e.printStackTrace();
        } catch (ContextModelException e) {
            this.log.error("Exception "+ e);
            // e.printStackTrace();
        }
    }
	 */


	//********************************************************************
	//**************** end of hoc code  **********************************


	/**
	 * Sets the User Context DB Mgmt service reference.
	 * 
	 * @param userDB
	 *            the User Context DB Mgmt service reference to set.
	 */
	public void setUserCtxDBMgr(IUserCtxDBMgr userDB) {

		this.userCtxDBMgr = userDB;
	}

	/**
	 * Sets the Community Context DB Mgmt service reference.
	 * 
	 * @param userDB
	 *            the User Context DB Mgmt service reference to set.
	 */
	public void setCommunityCtxDBMgr(ICommunityCtxDBMgr communityCtxDBMgr) {

		this.communityCtxDBMgr = communityCtxDBMgr;
	}

	/**
	 * Sets the User Context History Mgmt service reference.
	 * 
	 * @param userCtxHistoryMgr
	 *            the User Context History Mgmt service reference to set
	 */
	public void setUserCtxHistoryMgr(IUserCtxHistoryMgr userCtxHistoryMgr) {

		this.userCtxHistoryMgr = userCtxHistoryMgr;
	}

	/**
	 * Sets the IIdentity Mgmt service reference.
	 * 
	 * @param idMgr
	 *            the IIdentity Mgmt service reference to set.
	 */
	public void setIdentityMgr(IIdentityManager identityMgr) {

		this.idMgr = identityMgr;
	}


	/**
	 * Sets the UserCtxInferenceMgr service reference.
	 * 
	 * @param idMgr
	 *            the UserCtxInferenceMgr service reference to set.
	 */
	public void setUserCtxInferenceMgr(IUserCtxInferenceMgr userCtxInferenceMgr) {

		System.out.println("inf manager set");
		this.userCtxInferenceMgr = userCtxInferenceMgr;
	}


	/**
	 * This method is called when the {@link IPrivacyLogAppender} service is
	 * bound.
	 * 
	 * @param privacyLogAppender
	 *            the service that was bound
	 * @param props
	 *            the set of properties that the service was registered with
	 */
	public void bindPrivacyLogAppender(IPrivacyLogAppender privacyLogAppender, Dictionary<Object,Object> props) {

		LOG.info("Binding service reference " + privacyLogAppender);
		this.hasPrivacyLogAppender = true;
	}

	/**
	 * This method is called when the {@link IPrivacyLogAppender} service is
	 * unbound.
	 * 
	 * @param privacyLogAppender
	 *            the service that was unbound
	 * @param props
	 *            the set of properties that the service was registered with
	 */
	public void unbindPrivacyLogAppender(IPrivacyLogAppender privacyLogAppender, Dictionary<Object,Object> props) {

		LOG.info("Unbinding service reference " + privacyLogAppender);
		this.hasPrivacyLogAppender = false;
	}

	// TODO remove
	public void createCssNode(INetworkNode cssNodeId) throws CtxException {

		try {
			LOG.info("Checking if CSS node context entity " + cssNodeId + " exists...");
			CtxEntity cssNodeEnt = this.retrieveCssNode(cssNodeId).get();
			if (cssNodeEnt != null) {

				LOG.info("Found CSS node context entity " + cssNodeEnt.getId());
				return;
			}

			cssNodeEnt = this.createEntity(CtxEntityTypes.CSS_NODE).get();
			final CtxAttribute cssNodeIdAttr = this.createAttribute(cssNodeEnt.getId(), CtxAttributeTypes.ID).get();
			this.updateAttribute(cssNodeIdAttr.getId(), cssNodeId.toString());
			LOG.info("Created CSS node context entity " + cssNodeEnt.getId());

		} catch (Exception e) {

			throw new CtxBrokerException("Could not create CSS node context entity " + cssNodeId
					+ ": " + e.getLocalizedMessage(), e);
		}
	}




}