package org.societies.integration.test.bit.userCtxInheritance;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.cis.attributes.MembershipCriteria;
import org.societies.api.cis.management.ICisManager;
import org.societies.api.cis.management.ICisOwned;
import org.societies.api.comm.xmpp.interfaces.ICommManager;
import org.societies.api.context.CtxException;
import org.societies.api.context.model.CommunityCtxEntity;
import org.societies.api.context.model.CtxAssociation;
import org.societies.api.context.model.CtxAssociationIdentifier;
import org.societies.api.context.model.CtxAssociationTypes;
import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxAttributeIdentifier;
import org.societies.api.context.model.CtxAttributeTypes;
import org.societies.api.context.model.CtxEntityIdentifier;
import org.societies.api.context.model.CtxIdentifier;
import org.societies.api.context.model.CtxModelType;
import org.societies.api.identity.IIdentity;
import org.societies.api.identity.InvalidFormatException;
import org.societies.api.internal.context.broker.ICtxBroker;




public class UserCtxInheritanceTest {

	private static Logger LOG = LoggerFactory.getLogger(UserCtxInheritanceTest.class);

	private IIdentity cssIDUniversity;

	// run test in university's container
	private String targetUniversity = "university.ict-societies.eu";

	public ICtxBroker ctxBroker;
	public ICommManager commManager;
	public ICisManager cisManager;

	public void setUp(){

		LOG.info("UserInheritanceCTX started");

	}

	@Test
	public void TestUserCtxInheritance() {

		LOG.info("TestUserInheritabceCtx");
		this.ctxBroker=Test1999.getCtxBroker();
		this.commManager= Test1999.getCommManager();
		this.cisManager = Test1999.getCisManager();

		LOG.info("Context broker service: "+ this.ctxBroker);
		LOG.info("comm manager service"+ this.commManager);

		try {
			this.cssIDUniversity =  this.commManager.getIdManager().fromJid(targetUniversity);

			// setting university attributes 
			
			/*CtxAttribute locationUniversity = updateStringAttr(this.cssIDUniversity, CtxAttributeTypes.LOCATION_SYMBOLIC,"zoneA" );
			assertEquals(locationUniversity.getType(), CtxAttributeTypes.LOCATION_SYMBOLIC);
			LOG.info("university's location created : " + locationUniversity.getId());*/

			// create CIS
			IIdentity cisID = this.createCIS();

			// at this point a community Entity should be created in university's container
			// at this point an association should be created in university's container
			
			Thread.sleep(40000);

			CtxEntityIdentifier ctxCommunityEntityIdentifier = this.ctxBroker.retrieveCommunityEntityId(cisID).get();
			CommunityCtxEntity communityEntity = (CommunityCtxEntity) this.ctxBroker.retrieve(ctxCommunityEntityIdentifier).get();
			CtxAttribute locationSym1stCis = updateCISStringAttr(cisID, CtxAttributeTypes.LOCATION_SYMBOLIC, "Zone 1");

			//create second CIS
			IIdentity cisId2 = this.createCIS2();
			
			Thread.sleep(40000);
			
			CtxEntityIdentifier ctxCommunityEntityIdentifier2 = this.ctxBroker.retrieveCommunityEntityId(cisId2).get();
			CommunityCtxEntity communityEntity2 = (CommunityCtxEntity) this.ctxBroker.retrieve(ctxCommunityEntityIdentifier2).get();	
			CtxAttribute locationSym2ndCis = updateStringAttr(cisId2, CtxAttributeTypes.LOCATION_SYMBOLIC, "Zone 2");
			// the following lines will be removed with code adding a css member to the cis
			// adding university to both CISs 
			Set<CtxAssociationIdentifier> comAssocIdSet = communityEntity.getAssociations(CtxAssociationTypes.HAS_MEMBERS);		
			Set<CtxAssociationIdentifier> comAssocIdSet2 = communityEntity2.getAssociations(CtxAssociationTypes.HAS_MEMBERS);


			CtxAssociation hasMembersAssoc1 = null;	
			if(comAssocIdSet != null ){
				for(CtxAssociationIdentifier assocID : comAssocIdSet){
					hasMembersAssoc1 = (CtxAssociation) this.ctxBroker.retrieve(assocID).get();	
					CtxEntityIdentifier uniEntityID = this.ctxBroker.retrieveIndividualEntityId(null,this.cssIDUniversity).get();
					hasMembersAssoc1.addChildEntity(uniEntityID);
					hasMembersAssoc1 = (CtxAssociation) this.ctxBroker.update(hasMembersAssoc1).get();
				}
			}
										
			CtxAssociation hasMembersAssoc2 = null;
			if(comAssocIdSet2 != null ){
				for(CtxAssociationIdentifier assocID : comAssocIdSet2){
					hasMembersAssoc2 = (CtxAssociation) this.ctxBroker.retrieve(assocID).get();	
					CtxEntityIdentifier uniEntityID = this.ctxBroker.retrieveIndividualEntityId(null,this.cssIDUniversity).get();
					hasMembersAssoc2.addChildEntity(uniEntityID);
					hasMembersAssoc2 = (CtxAssociation) this.ctxBroker.update(hasMembersAssoc2).get();
				}
			}
		
			
			CommunityCtxEntity communityEntityUpdated = (CommunityCtxEntity) this.ctxBroker.retrieve(ctxCommunityEntityIdentifier).get();
			CommunityCtxEntity communityEntityUpdated2 = (CommunityCtxEntity) this.ctxBroker.retrieve(ctxCommunityEntityIdentifier2).get();		
			
				
			LOG.info("Updated ctxCommunityEntity : " + communityEntityUpdated.getMembers());
			LOG.info("Updated ctxCommunityEntity members : " + communityEntityUpdated.getMembers());
			
			LOG.info("The location of the CIS-AA is: "+locationSym1stCis.getStringValue());
			LOG.info("The location of the CIS-BB is: "+locationSym2ndCis.getStringValue());
			// the upper lines will be removed with code adding a css member to the cis
			// two communities now exist with university being in both communities

			//test inheritance 
			LOG.info("testing inheritance - start");
			ctxBroker.communityInheritance(locationSym1stCis.getId());
			LOG.info("testing inheritance - end");
			
			
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	}

	private CtxAttribute updateStringAttr(IIdentity identity, String attributeType, String value){
		
		CtxAttribute attributeUpdated = null;
		List<CtxIdentifier> attributeList;
		try {
			attributeList = this.ctxBroker.lookup(identity, CtxModelType.ATTRIBUTE,attributeType).get();

			CtxAttribute attribute = null;

			if( attributeList.size() == 0){
				
				CtxEntityIdentifier entityID = this.ctxBroker.retrieveIndividualEntityId(null, identity).get();
				attribute = this.ctxBroker.createAttribute(entityID, attributeType).get();
			} else {
				attribute = (CtxAttribute) this.ctxBroker.retrieve(attributeList.get(0)).get();
			}
			attribute.setStringValue(value);
			attributeUpdated = (CtxAttribute) this.ctxBroker.update(attribute).get();
			
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

		return attributeUpdated;
	}
	
	private CtxAttribute updateCISStringAttr(IIdentity identity, String attributeType, String value){
		
		CtxAttribute attributeUpdated = null;
		List<CtxIdentifier> attributeList;
		try {
			attributeList = this.ctxBroker.lookup(identity, CtxModelType.ATTRIBUTE,attributeType).get();

			CtxAttribute attribute = null;

			if( attributeList.size() == 0){
				//CtxEntityIdentifier entityID = this.ctxBroker.retrieveIndividualEntityId(null, identity).get();
				CtxEntityIdentifier entityId = this.ctxBroker.retrieveCommunityEntityId(null, identity).get();
				attribute = this.ctxBroker.createAttribute(entityId, attributeType).get();
			} else {
				attribute = (CtxAttribute) this.ctxBroker.retrieve(attributeList.get(0)).get();
			}

			attribute.setStringValue(value);
			attributeUpdated = (CtxAttribute) this.ctxBroker.update(attribute).get();
			
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

		return attributeUpdated;
	}

/*	private CtxAttribute updateIntegerAttr(IIdentity identity, String attributeType, Integer value){
		CtxAttribute attributeUpdated = null;
		List<CtxIdentifier> attributeList;
		//List<CtxIdentifier> emmaInterestList = this.ctxBroker.lookup(null, this.cssIDEmma,CtxModelType.ATTRIBUTE,CtxAttributeTypes.INTERESTS).get();
		try {
			attributeList = this.ctxBroker.lookup(identity, CtxModelType.ATTRIBUTE,attributeType).get();
			CtxAttribute attribute = null;
			LOG.info("the attributeList size is aaaaa:"+attributeList.size());
			if( attributeList.size() == 0){

				CtxEntityIdentifier entityID = this.ctxBroker.retrieveIndividualEntityId(null, identity).get();
				attribute = this.ctxBroker.createAttribute(entityID, attributeType).get();

			} else {
				LOG.info("The attribute is :" + attributeList.get(0));
				attribute = (CtxAttribute) this.ctxBroker.retrieve(attributeList.get(0)).get();
				//attribute = (CtxAttribute) this.ctxBroker.retrieveAttribute((CtxAttributeIdentifier)attributeList.get(0), false).get();
				LOG.info("The attribute 2 is :" + attribute);
			}
			attribute.setIntegerValue(value);
			attributeUpdated = (CtxAttribute) this.ctxBroker.update(attribute).get();

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

		return attributeUpdated;
	}*/




	public void fetchCommunityValue(CtxEntityIdentifier communityEntityId, String ctxAttributeType){

		//String estimatedValue = null;

		LOG.info("**** lookup "+ ctxAttributeType); 
		List<CtxIdentifier> communityAttrList;
		try {
			communityAttrList = this.ctxBroker.lookup(communityEntityId, CtxModelType.ATTRIBUTE, ctxAttributeType).get();

			LOG.info("**** lookup community attribute results : " +communityAttrList);

			if(communityAttrList.size() > 0 ) {
				CtxAttributeIdentifier communityAttrId = (CtxAttributeIdentifier) communityAttrList.get(0);	

				LOG.info("**** INITIATE ESTIMATION ");
				//CtxAttribute communityAttribute = (CtxAttribute) this.ctxBroker.retrieve(communityAttrId).get();
				CtxAttribute communityAttribute = (CtxAttribute) this.ctxBroker.retrieveAttribute(communityAttrId, true).get();

				LOG.info("**** communityInterests id : " + communityAttribute.getId());
				LOG.info("  ******************** string outcome pairs...........  " + communityAttribute.getComplexValue().getPairs());
				LOG.info("  ******************** numeric outcome average...........  " + communityAttribute.getComplexValue().getAverage());
				LOG.info("  ******************** numeric outcome median...........  " + communityAttribute.getComplexValue().getMedian());
				LOG.info("  ******************** numeric outcome mode...........  " + communityAttribute.getComplexValue().getMode());
				LOG.info("  ******************** numeric outcome max...........  " + communityAttribute.getComplexValue().getRangeMax());
				LOG.info("  ******************** numeric outcome min...........  " + communityAttribute.getComplexValue().getRangeMin());

				//LOG.info("**** communityInterests value : " + communityInterestsAttribute.getStringValue());
				//estimatedValue = communityInterestsAttribute.getStringValue();
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

	}



	protected IIdentity createCIS() {
		IIdentity cisID = null;
		try {
			Hashtable<String, MembershipCriteria> cisCriteria = new Hashtable<String, MembershipCriteria> ();
			
			LOG.info("*** trying to create cis:");
			ICisOwned cisOwned = this.cisManager.createCis("testCIS-AA", "cisType", cisCriteria, "nice CIS-AA").get();		
			
			String cisIDString = cisOwned.getCisId();
			cisID = this.commManager.getIdManager().fromJid(cisIDString);
			LOG.info("*** cis created: "+cisOwned.getCisId());
			LOG.info("*** cisOwned " +cisOwned);
			LOG.info("*** cisOwned.getCisId() " +cisOwned.getCisId());

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cisID;
	}
	
	protected IIdentity createCIS2() {
		LOG.info("Iam trying to create the 2nd CIS");
		IIdentity cisID = null;
		try {
			Hashtable<String, MembershipCriteria> cisCriteria2 = new Hashtable<String, MembershipCriteria> ();
			
			LOG.info("*** trying to create cis-B:");
			ICisOwned cisOwned = this.cisManager.createCis("testCIS-BB", "cisType", cisCriteria2, "nice CIS-BB").get();		
			
			String cisIDString = cisOwned.getCisId();
			cisID = this.commManager.getIdManager().fromJid(cisIDString);
			LOG.info("*** cis created: "+cisOwned.getCisId());
			LOG.info("*** cisOwned " +cisOwned);
			LOG.info("*** cisOwned.getCisId() " +cisOwned.getCisId());

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cisID;
	}

}