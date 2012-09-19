package org.societies.context.broker.impl.comm.callbacks;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxEntity;
import org.societies.api.context.model.CtxIdentifier;
import org.societies.api.context.model.CtxModelObject;
import org.societies.context.broker.impl.comm.ICtxCallback;

public class CreateEntityCallback implements ICtxCallback {

	/** The logging facility. */
	private static final Logger LOG = LoggerFactory.getLogger(CreateEntityCallback.class);
	
	private CtxEntity result;

	@Override
	public void receiveCtxResult(Object retObject, String type) {

	}

	@Override
	public void onCreatedEntity(CtxEntity retObject) {

		LOG.info("onCreatedEntity retObject " +retObject);
		this.result = retObject;
		synchronized (this) {	            
			notifyAll();	        
		}
		LOG.info("onCreatedEntity, notify all done");
	}

	

	@Override
	public void onCreatedAttribute(CtxAttribute retObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLookupCallback(List<CtxIdentifier> ctxIdsList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRetrieveCtx(CtxModelObject ctxObj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateCtx(CtxModelObject ctxObj) {
		// TODO Auto-generated method stub
		
	}
	
	public CtxEntity getResult() {
		return this.result;
	}
}