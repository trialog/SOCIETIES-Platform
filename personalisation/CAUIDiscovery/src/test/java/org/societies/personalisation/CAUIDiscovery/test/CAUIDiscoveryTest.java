package org.societies.personalisation.CAUIDiscovery.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxAttributeIdentifier;
import org.societies.api.context.model.CtxAttributeTypes;
import org.societies.api.context.model.CtxEntity;
import org.societies.api.context.model.CtxEntityIdentifier;
import org.societies.api.context.model.CtxHistoryAttribute;
import org.societies.api.context.model.util.SerialisationHelper;
import org.societies.api.personalisation.model.Action;
import org.societies.api.personalisation.model.IAction;
import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
import org.societies.personalisation.CAUIDiscovery.impl.ActionDictObject;
import org.societies.personalisation.CAUIDiscovery.impl.CAUIDiscovery;
import org.societies.personalisation.CAUIDiscovery.impl.MockHistoryData;

public class CAUIDiscoveryTest {

	
	private static Long nextValue = 0L;
	private CtxEntity operator = null;
	
	static Map<CtxHistoryAttribute, List<CtxHistoryAttribute>> historyDataSet = new LinkedHashMap<CtxHistoryAttribute, List<CtxHistoryAttribute>>();
	
	CAUIDiscovery discovery;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
			
		this.discovery = new CAUIDiscovery();
		operator = createOperator();
		createContextHistoryAttributesSet();
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testGenerateHistory() {
	
		//printHistory(this.mapHocData);
		System.out.println("history size :"+ historyDataSet.size());
		assertEquals(80, historyDataSet.size());
	}

	@Test
	public void testConvertHistoryData() {
	
		List<MockHistoryData> mockData = this.discovery.convertHistoryData(historyDataSet);
		MockHistoryData mockHoC = mockData.get(2);
		
		assertNotNull(mockHoC.getActionValue());
		assertNotNull(mockHoC.getParameterName());
		assertNotNull(mockHoC.getContext());
		assertNotNull(mockHoC.getTimestamp());
		System.out.println("converted data size: "+mockData.size());
		assertEquals(160, mockData.size());
		//System.out.println("converted data: "+mockData);
		
	}

	
	@Test
	public void testGenerateDictionaries() {
		
		HashMap<Integer,LinkedHashMap<List<String>,ActionDictObject>> dictionaries = this.discovery.generateDictionaries(historyDataSet);
		//System.out.println("dictionary :"+ dictionaries);
		assertNotNull(dictionaries);
		assertEquals(3, dictionaries.size());
	
		LinkedHashMap<List<String>,ActionDictObject> dictionaryStep0 = dictionaries.get(1);
		//System.out.println("0 "+ dictionaryStep0.size());
		assertEquals(7, dictionaryStep0.size());
		
		LinkedHashMap<List<String>,ActionDictObject> dictionaryStep1 = dictionaries.get(2);
		//System.out.println("1 "+ dictionaryStep1.size());
		assertEquals(14, dictionaryStep1.size());
		
		LinkedHashMap<List<String>,ActionDictObject> dictionaryStep2 = dictionaries.get(3);
		//System.out.println("2 "+ dictionaryStep2.size());
		assertEquals(17, dictionaryStep2.size());
	}


	
	
	/* 
	 * helper classes
	 */		
	public void createContextHistoryAttributesSet(){
		
		//create actions
		//IIdentity identity = new MockIdentity(IdentityType.CSS, "user", "societies.org");
		ServiceResourceIdentifier serviceId1 = new ServiceResourceIdentifier();
		ServiceResourceIdentifier serviceId2 = new ServiceResourceIdentifier();
		try {
			serviceId1.setIdentifier(new URI("http://testService1"));
			serviceId2.setIdentifier(new URI("http://testService2"));
			serviceId1.setServiceInstanceIdentifier("http://testService1");
			serviceId2.setServiceInstanceIdentifier("http://testService2");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		//create actions
		IAction action1 = new Action(serviceId1, "testService", "volume", "high");
		IAction action2 = new Action(serviceId2, "testService", "volume", "low");
		IAction action3 = new Action(serviceId1, "testService", "volume", "mute");
		IAction actionX = new Action(serviceId1, "testService", "XXXX", "XXXX");
		IAction action4 = new Action(serviceId2, "testService", "colour", "blue");
		IAction action5 = new Action(serviceId2, "testService", "colour", "green");
		IAction actionY = new Action(serviceId1, "testService", "YYYY", "YYYY");
		//System.out.println ("action service ID "+actionY.getServiceID().getServiceInstanceIdentifier());
		for (int i=0; i<4; i++){

			//monitorAction(action1,"home","free",10);
			monitorAction(action1,"country","free",10);
			monitorAction(action2,"office","busy",15);
			monitorAction(action3,"park","away",25);
			monitorAction(actionX,"park","away",25);
			monitorAction(actionY,"park","away",25);
			monitorAction(action4,"park","away",25);
			monitorAction(action5,"park","away",25);
		
			monitorAction(actionY,"park","away",25);
			monitorAction(actionX,"park","away",25);
			monitorAction(action1,"home","free",10);
			monitorAction(action2,"office","busy",15);
			monitorAction(action3,"zoo","away",25);
			monitorAction(actionY,"park","away",25);
			monitorAction(actionY,"park","away",25);
			monitorAction(action1,"home","free",10);
			monitorAction(action2,"office","busy",15);
			monitorAction(action3,"park","away",25);
			monitorAction(actionX,"park","away",25);
			monitorAction(action4,"park","away",25);
			monitorAction(action5,"park","away",25);
	
		}
		
	}

	private void monitorAction(IAction action, String location, String status, Integer temperature){

		CtxHistoryAttribute mockPrimaryHocActionAttrX = createMockHocActionAttr(action);
		List<CtxHistoryAttribute> escortingCtxDataX = new ArrayList<CtxHistoryAttribute>();
		CtxHistoryAttribute attrLocationX = createMockHocAttr(CtxAttributeTypes.LOCATION_SYMBOLIC,location);
		CtxHistoryAttribute attrStatusX = createMockHocAttr(CtxAttributeTypes.STATUS,status);
		CtxHistoryAttribute attrTemperatureX = createMockHocAttr(CtxAttributeTypes.TEMPERATURE,temperature);
		escortingCtxDataX.add(attrLocationX);
		escortingCtxDataX.add(attrStatusX);
		escortingCtxDataX.add(attrTemperatureX);
		historyDataSet.put(mockPrimaryHocActionAttrX, escortingCtxDataX);

	}

	private CtxHistoryAttribute createMockHocAttr(String ctxAttrType, Serializable ctxAttrValue){

		CtxAttributeIdentifier ctxAttrID = new CtxAttributeIdentifier(operator.getId(),ctxAttrType.toString(),getNextValue());
		CtxAttribute ctxAttr = new CtxAttribute(ctxAttrID);

		if(ctxAttrValue instanceof String){
			ctxAttr.setStringValue(ctxAttrValue.toString());
		} else if (ctxAttrValue instanceof Double){
			ctxAttr.setDoubleValue((Double)ctxAttrValue);
		}else if (ctxAttrValue instanceof Integer){
			ctxAttr.setIntegerValue((Integer)ctxAttrValue);
		}
		CtxHistoryAttribute ctxHocAttr = new CtxHistoryAttribute(ctxAttr,getNextValue());
		return ctxHocAttr;
	}


	private CtxHistoryAttribute createMockHocActionAttr(IAction action){
		CtxHistoryAttribute ctxHocAttr = null;
		try {
			CtxAttributeIdentifier ctxAttrID = new CtxAttributeIdentifier(operator.getId(),CtxAttributeTypes.LAST_ACTION,getNextValue());
			CtxAttribute ctxAttr = new CtxAttribute(ctxAttrID);
			ctxAttr.setBinaryValue(SerialisationHelper.serialise(action));
			ctxHocAttr = new CtxHistoryAttribute(ctxAttr,getNextValue());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ctxHocAttr;
	}
	
	public static Long getNextValue() {
		return nextValue++;
	}
	
	private CtxEntity createOperator(){
		CtxEntityIdentifier ctxEntId = new CtxEntityIdentifier("operatorID","person",getNextValue());
		CtxEntity ctxEntity = new CtxEntity(ctxEntId);
		setOperatorEntity(ctxEntity);
		return ctxEntity;
	}

	private void setOperatorEntity(CtxEntity entity){
		operator = entity;
	}
	
	private void printHistory(Map<CtxHistoryAttribute, List<CtxHistoryAttribute>> mapHocData){
		int i = 0;
		for(CtxHistoryAttribute ctxHocAttr :mapHocData.keySet()){

			try {
				IAction action = (IAction)SerialisationHelper.deserialise(ctxHocAttr.getBinaryValue(), this.getClass().getClassLoader());
				List<CtxHistoryAttribute> escortingAttrList = mapHocData.get(ctxHocAttr);
				System.out.println(i+" primary Attr: {"+action.getparameterName() +" "+action.getvalue()+"} escorting: {" +escortingAttrList.get(0).getStringValue()+" "+escortingAttrList.get(1).getStringValue()+" "+escortingAttrList.get(2).getStringValue()+"}");
				i++;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
