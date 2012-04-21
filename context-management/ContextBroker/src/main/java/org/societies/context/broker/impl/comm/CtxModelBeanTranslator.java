package org.societies.context.broker.impl.comm;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.societies.api.context.model.CtxAssociation;
import org.societies.api.context.model.CtxAssociationIdentifier;
import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxAttributeIdentifier;
import org.societies.api.context.model.CtxAttributeValueType;
import org.societies.api.context.model.CtxEntity;
import org.societies.api.context.model.CtxEntityIdentifier;
import org.societies.api.context.model.CtxIdentifier;
import org.societies.api.context.model.CtxModelObject;
import org.societies.api.context.model.CtxModelType;
import org.societies.api.context.model.CtxOriginType;
import org.societies.api.context.model.CtxQuality;
import org.societies.api.context.model.MalformedCtxIdentifierException;
import org.societies.api.internal.context.broker.ICtxBroker;
import org.societies.api.schema.context.model.CtxAssociationBean;
import org.societies.api.schema.context.model.CtxAssociationIdentifierBean;
import org.societies.api.schema.context.model.CtxAttributeBean;
import org.societies.api.schema.context.model.CtxAttributeIdentifierBean;
import org.societies.api.schema.context.model.CtxAttributeValueTypeBean;
import org.societies.api.schema.context.model.CtxEntityBean;
import org.societies.api.schema.context.model.CtxEntityIdentifierBean;
import org.societies.api.schema.context.model.CtxIdentifierBean;
import org.societies.api.schema.context.model.CtxModelObjectBean;
import org.societies.api.schema.context.model.CtxModelTypeBean;
import org.societies.api.schema.context.model.CtxOriginTypeBean;
import org.societies.api.schema.context.model.CtxQualityBean;
import org.springframework.beans.factory.annotation.Autowired;

public final class CtxModelBeanTranslator {
	
	private static CtxModelBeanTranslator instance = new CtxModelBeanTranslator();
	
	private CtxModelBeanTranslator() {}
	
	public static synchronized CtxModelBeanTranslator getInstance() {
		
		return instance;
	}
	
	public CtxEntityBean fromCtxEntity(CtxEntity entity) throws DatatypeConfigurationException{
		
		CtxEntityBean bean=new CtxEntityBean();
		bean.setId(fromCtxIdentifier(entity.getId()));
		List<CtxAssociationIdentifierBean> assocIdBeans = new ArrayList<CtxAssociationIdentifierBean>();
		for (CtxAssociationIdentifier assoc : entity.getAssociations()) {
			assocIdBeans.add((CtxAssociationIdentifierBean) fromCtxIdentifier(assoc));
		}
		bean.setAssociations(assocIdBeans);
		
		List<CtxAttributeBean> attrIdBeans = new ArrayList<CtxAttributeBean>();
		for (CtxAttribute attr : entity.getAttributes()) {
			attrIdBeans.add(fromCtxAttribute(attr));
		}
		bean.setAssociations(assocIdBeans);
		
		
		return bean;
		
	}
	
	
	public CtxIdentifierBean fromCtxIdentifier(CtxIdentifier identifier) {
		
		if (identifier.getModelType().equals(CtxModelType.ENTITY)) {
			return new CtxEntityIdentifierBean();
		}
		else if (identifier.getModelType().equals(CtxModelType.ATTRIBUTE)) {
			return new CtxAttributeIdentifierBean();
		}
		else if (identifier.getModelType().equals(CtxModelType.ASSOCIATION)) {
			return new CtxAssociationIdentifierBean();
		}
		else
			return null;
		
	}
	
	public CtxAttributeBean fromCtxAttribute(CtxAttribute attr) throws DatatypeConfigurationException {
		
		CtxAttributeBean bean=new CtxAttributeBean();
		bean.setBinaryValue(attr.getBinaryValue());
		bean.setDoubleValue(attr.getDoubleValue());
		bean.setHistoryRecorded(attr.isHistoryRecorded());
		bean.setId(fromCtxIdentifier(attr.getId()));
		bean.setIntegerValue(attr.getIntegerValue());
		bean.setLastModified(DateToXMLGregorianCalendar(attr.getLastModified()));
		bean.setSourceId(attr.getSourceId());
		bean.setStringValue(attr.getStringValue());
		bean.setValueMetric(attr.getValueMetric());
		bean.setValueType(fromCtxAttributeValueType(attr.getValueType()));
		bean.setSourceId(attr.getSourceId());
		bean.setQuality(CtxQualityBeanFromCtxQuality(attr.getQuality()));
		
		return bean;
		
	}
	
	public CtxAssociationBean fromCtxAssociation(CtxAssociation assoc) throws DatatypeConfigurationException {
		
		CtxAssociationBean bean=new CtxAssociationBean();
		//bean.setChildEntities(assoc.getChildEntities());
		bean.setLastModified(DateToXMLGregorianCalendar(assoc.getLastModified()));
		bean.setId(fromCtxIdentifier(assoc.getId()));
		
		List<CtxEntityIdentifierBean> childEntities = new ArrayList<CtxEntityIdentifierBean>();
		for (CtxEntityIdentifier child : assoc.getChildEntities()) {
			childEntities.add(childEntitiesBeanFromChildEntities(child));
		}
		bean.setChildEntities(childEntities);
		bean.setParentEntity(parentEntityBeanFromParentEntity(assoc.getParentEntity()));
		
		return bean;
		
	}
	
	public CtxIdentifierBean fromCtxModelObject(CtxModelObject object) {
		
		CtxIdentifierBean bean=new CtxEntityIdentifierBean();
		bean.setString(object.toString());
		return bean;
		
	}
	
	private CtxEntityIdentifierBean parentEntityBeanFromParentEntity(
			CtxEntityIdentifier parentEntity) {
		
		CtxEntityIdentifierBean bean = new CtxEntityIdentifierBean();
		bean.setString(parentEntity.toString());
		return bean;
	}

	public CtxEntityIdentifierBean childEntitiesBeanFromChildEntities(
			CtxEntityIdentifier child) {
		
		CtxEntityIdentifierBean bean = new CtxEntityIdentifierBean();
		bean.setString(child.toString());
		return bean;
	}

	public CtxQualityBean CtxQualityBeanFromCtxQuality(CtxQuality quality) throws DatatypeConfigurationException {
		
		CtxQualityBean bean=new CtxQualityBean();
		bean.setPrecision(quality.getPrecision());
		bean.setUpdateFrequency(quality.getUpdateFrequency());
		bean.setOriginType(CtxOriginTypeBeanFromCtxOriginType(quality.getOriginType()));
		bean.setLastUpdated(DateToXMLGregorianCalendar(quality.getLastUpdated()));
		
		return bean;
	}

	public CtxOriginTypeBean CtxOriginTypeBeanFromCtxOriginType(
			CtxOriginType originType) {
		
		return CtxOriginTypeBean.valueOf(originType.toString());	
	}
	
	public CtxModelTypeBean CtxModelTypeBeanFromCtxModelType(
			CtxModelType modelType) {
		
		return CtxModelTypeBean.valueOf(modelType.toString());	
	}
	
	public CtxAttributeValueTypeBean fromCtxAttributeValueType(CtxAttributeValueType valueType) {
		
		return CtxAttributeValueTypeBean.valueOf(valueType.toString());	
	}
	
	public XMLGregorianCalendar DateToXMLGregorianCalendar(Date myDate) throws DatatypeConfigurationException {
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(myDate);
		XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		return xmlDate;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Clone is not allowed.");
	}

}
