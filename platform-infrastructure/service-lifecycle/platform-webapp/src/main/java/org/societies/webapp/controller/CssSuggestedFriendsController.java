package org.societies.webapp.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.validation.Valid;

import org.societies.api.cis.directory.ICisDirectoryRemote;
import org.societies.api.comm.xmpp.interfaces.ICommManager;
import org.societies.api.internal.css.management.ICSSLocalManager;
import org.societies.api.internal.servicelifecycle.IServiceDiscovery;
import org.societies.api.schema.cis.directory.CisAdvertisementRecord;
import org.societies.api.schema.css.directory.CssAdvertisementRecord;
import org.societies.api.schema.cssmanagement.CssAdvertisementRecordDetailed;
import org.societies.api.schema.cssmanagement.CssNode;
import org.societies.api.schema.cssmanagement.CssRequest;
import org.societies.api.schema.cssmanagement.CssRequestOrigin;
import org.societies.api.schema.cssmanagement.CssRequestStatusType;
import org.societies.cis.directory.client.CisDirectoryRemoteClient;
//import org.societies.webapp.models.CISDirectoryForm;
import org.societies.webapp.models.SuggestedFriendsForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CssSuggestedFriendsController {

	/**
	 * OSGI service get auto injected
	 */
	@Autowired
	private ICSSLocalManager cssLocalManager;
	@Autowired
	private ICommManager commManager;
	@Autowired
	private IServiceDiscovery sdService;

	public IServiceDiscovery getSDService() {
		return sdService;
	}

	public void getSDService(IServiceDiscovery sdService) {
		this.sdService = sdService;
	}

	public ICSSLocalManager getCssLocalManager() {
		return cssLocalManager;
	}

	public void setCssLocalManager(ICSSLocalManager cssLocalManager) {
		this.cssLocalManager = cssLocalManager;
	}

	private ICisDirectoryRemote cisDirectoryRemote;

	public ICisDirectoryRemote getCisDirectoryRemote() {
		return cisDirectoryRemote;
	}

	public void setCisDirectoryRemote(ICisDirectoryRemote cisDirectoryRemote) {
		this.cisDirectoryRemote = cisDirectoryRemote;
	}

	@RequestMapping(value = "/suggestedfriends.html", method = RequestMethod.GET)
	public ModelAndView SuggestedFriends() {

		// CREATE A HASHMAP OF ALL OBJECTS REQUIRED TO PROCESS THIS PAGE
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("message", "Please input values and submit");

		// ADD THE BEAN THAT CONTAINS ALL THE FORM DATA FOR THIS PAGE
		SuggestedFriendsForm sfForm = new SuggestedFriendsForm();
		model.put("sfForm", sfForm);

		// ADD ALL THE SELECT BOX VALUES USED ON THE FORM
		Map<String, String> methods = new LinkedHashMap<String, String>();

		methods.put("findFriends", "Find Friends");
		methods.put("pendingfriendreq", "Find Pending Friend Request");
		model.put("methods", methods);

		model.put("suggestedfriendsresult", "CSS Suggested Friends Result :");
		return new ModelAndView("suggestedfriends", model);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/suggestedfriends.html", method = RequestMethod.POST)
	public ModelAndView SuggestedFriends(@Valid SuggestedFriendsForm sfForm,
			BindingResult result, Map model) {

		if (result.hasErrors()) {
			model.put("result", "CSS Suggested Friends form error");
			return new ModelAndView("suggestedfriends", model);
		}

		if (getCssLocalManager() == null) {
			model.put("errormsg",
					"CSS Suggested Friends reference not avaiable");
			return new ModelAndView("error", model);
		}

		String method = sfForm.getMethod();

		String res = null;

		try {

			if (method.equalsIgnoreCase("findFriends")) {
				res = "CSS Suggested Friends Result ";

				Future<List<CssAdvertisementRecord>> asynchcssfriends = getCssLocalManager()
						.suggestedFriends();

				model.put("result", res);
				model.put("cssfriends", asynchcssfriends.get());

			} else if (method.equalsIgnoreCase("pendingfriendreq")) {

				res = "All Pending Friend Requests";

				Future<List<CssAdvertisementRecord>> asynchfriendrequests = cssLocalManager
						.getFriendRequests();

				model.put("result", res);
				model.put("cssfriends", asynchfriendrequests.get());

			} else {
				res = "error unknown metod";
			}

			model.put("result", res);

		} catch (Exception e) {
			res = "Oops!!!!<br/>";
		}
		;

		return new ModelAndView("suggestedfriendsresult", model);
	}

	@RequestMapping(value = "/suggestedfriendspilot.html", method = RequestMethod.GET)
	public ModelAndView SuggestedFriendsPilot() {

		// CREATE A HASHMAP OF ALL OBJECTS REQUIRED TO PROCESS THIS PAGE
		Map<String, Object> model = new HashMap<String, Object>();

		String res = null;

		try {

			Future<List<CssAdvertisementRecord>> asynchSnsSuggestedFriends = getCssLocalManager().suggestedFriends();
			List<CssAdvertisementRecord> snsSuggestedFriends = asynchSnsSuggestedFriends.get();
			

			// Another Hack for the pilot!!!! DO Not copy!!!
			// CssManager should return complete and intelligent list, but since
			// ico not available this will have to do for the pilot

			Future<List<CssAdvertisementRecordDetailed>> asynchallcss = getCssLocalManager().getCssAdvertisementRecordsFull();
			List<CssAdvertisementRecordDetailed> allcssDetails = asynchallcss.get();
			List<CssAdvertisementRecordDetailed> otherFriends = new ArrayList<CssAdvertisementRecordDetailed>();
			List<CssAdvertisementRecordDetailed> snsFriends = new ArrayList<CssAdvertisementRecordDetailed>();
			
			Future<List<CssRequest>> asynchFR = getCssLocalManager().findAllCssRequests();
			List<CssRequest> friendReq = asynchFR.get();
			
			for (int index = 0; index < allcssDetails.size(); index++) {
				// ignore myself!

				if (!allcssDetails.get(index).getResultCssAdvertisementRecord().getId().contains(commManager.getIdManager().getThisNetworkNode().getBareJid())) 
				{
					// skip people we are already friends with	
					if (allcssDetails.get(index).getStatus() != CssRequestStatusType.ACCEPTED) 
					{
						
						for ( int indexFR = 0; indexFR < friendReq.size(); indexFR++)
						{
							if (allcssDetails.get(index).getResultCssAdvertisementRecord().getId().contains(friendReq.get(indexFR).getCssIdentity()))
							{
								// We have a pending FR from this people, change status. This should be done in the CssManager 
								// but not for the pilot
								allcssDetails.get(index).setStatus(CssRequestStatusType.NEEDSRESP);
								indexFR = friendReq.size();
							}
						}
						
						// Now we want to check , if we have a pending FR from this people
						// not friends yet, check that it's nt already in the
						// sns suggested friends
						boolean bAlreadySuggested = false;
						for (int snsIndex = 0; snsIndex < snsSuggestedFriends.size(); snsIndex++) {
							if (snsSuggestedFriends.get(snsIndex).getId().contains(allcssDetails.get(index).getResultCssAdvertisementRecord().getId()))
							{
								bAlreadySuggested = true;
								snsFriends.add(allcssDetails.get(index));
							}
						}

						if (bAlreadySuggested == false) {
							otherFriends.add(allcssDetails.get(index));
						}
					}
				}
			}
			model.put("otherFriends", otherFriends);
			model.put("snsFriends", snsFriends);

		} catch (Exception e) {
			res = "Oops!!!!<br/>";
		}
		;

		return new ModelAndView("suggestedfriendspilot", model);

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/suggestedfriendspilot.html", method = RequestMethod.POST)
	public ModelAndView SuggestedFriendsPilot(
			@Valid SuggestedFriendsForm sfForm, BindingResult result, Map model) {

		if (result.hasErrors()) {
			model.put("result", "CSS Suggested Friends form error");
			return new ModelAndView("suggestedfriends", model);
		}

		if (getCssLocalManager() == null) {
			model.put("errormsg",
					"CSS Suggested Friends reference not avaiable");
			return new ModelAndView("error", model);
		}

		if (sfForm.getMethod() != null)
		{
			if (sfForm.getMethod().contains("accept")) {
				// acept the pending friend request
				CssRequest pendingFR = new CssRequest();
				pendingFR.setCssIdentity(sfForm.getFriendId());
				pendingFR.setRequestStatus(CssRequestStatusType.ACCEPTED);
				pendingFR.setOrigin(CssRequestOrigin.LOCAL);
				getCssLocalManager().acceptCssFriendRequest(pendingFR);
				
			} else if (sfForm.getMethod().contains("cancel")) {
				// acept the pending friend request
				CssRequest pendingFR = new CssRequest();
				pendingFR.setCssIdentity(sfForm.getFriendId());
				pendingFR.setRequestStatus(CssRequestStatusType.CANCELLED);
				pendingFR.setOrigin(CssRequestOrigin.LOCAL);
				getCssLocalManager().updateCssRequest(pendingFR);
				// cancel pendinf fr
			} else {
				// send fr
				getCssLocalManager().sendCssFriendRequest(sfForm.getFriendId());
			}
		}
		
		String res = null;

		try {

			Future<List<CssAdvertisementRecord>> asynchSnsSuggestedFriends = getCssLocalManager().suggestedFriends();
			List<CssAdvertisementRecord> snsSuggestedFriends = asynchSnsSuggestedFriends.get();
			

			// Another Hack for the pilot!!!! DO Not copy!!!
			// CssManager should return complete and intelligent list, but since
			// ico not available this will have to do for the pilot

			Future<List<CssAdvertisementRecordDetailed>> asynchallcss = getCssLocalManager().getCssAdvertisementRecordsFull();
			List<CssAdvertisementRecordDetailed> allcssDetails = asynchallcss.get();
			List<CssAdvertisementRecordDetailed> otherFriends = new ArrayList<CssAdvertisementRecordDetailed>();
			List<CssAdvertisementRecordDetailed> snsFriends = new ArrayList<CssAdvertisementRecordDetailed>();
			
			Future<List<CssRequest>> asynchFR = getCssLocalManager().findAllCssRequests();
			List<CssRequest> friendReq = asynchFR.get();
			
			for (int index = 0; index < allcssDetails.size(); index++) {
				// ignore myself!

				if (!allcssDetails.get(index).getResultCssAdvertisementRecord().getId().contains(commManager.getIdManager().getThisNetworkNode().getBareJid())) 
				{
					// skip people we are already friends with	
					if (allcssDetails.get(index).getStatus() != CssRequestStatusType.ACCEPTED) 
					{
						
						for ( int indexFR = 0; indexFR < friendReq.size(); indexFR++)
						{
							if (allcssDetails.get(index).getResultCssAdvertisementRecord().getId().contains(friendReq.get(indexFR).getCssIdentity()))
							{
								// We have a pending FR from this people, change status. This should be done in the CssManager 
								// but not for the pilot
								allcssDetails.get(index).setStatus(CssRequestStatusType.NEEDSRESP);
								indexFR = friendReq.size();
							}
						}
						
						// Now we want to check , if we have a pending FR from this people
						// not friends yet, check that it's nt already in the
						// sns suggested friends
						boolean bAlreadySuggested = false;
						for (int snsIndex = 0; snsIndex < snsSuggestedFriends.size(); snsIndex++) {
							if (snsSuggestedFriends.get(snsIndex).getId().contains(allcssDetails.get(index).getResultCssAdvertisementRecord().getId()))
							{
								bAlreadySuggested = true;
								snsFriends.add(allcssDetails.get(index));
							}
						}

						if (bAlreadySuggested == false) {
							otherFriends.add(allcssDetails.get(index));
						}
					}
				}
			}
			model.put("otherFriends", otherFriends);
			model.put("snsFriends", snsFriends);

		} catch (Exception e) {
			res = "Oops!!!!<br/>";
		}
		;

		return new ModelAndView("suggestedfriendspilot", model);
	}

	@RequestMapping(value = "/friendspilot.html", method = RequestMethod.GET)
	public ModelAndView FriendsPilot() {

		// CREATE A HASHMAP OF ALL OBJECTS REQUIRED TO PROCESS THIS PAGE
		Map<String, Object> model = new HashMap<String, Object>();

		String res = null;

		try {

			res = "CSS Friends Result ";

			Future<List<CssAdvertisementRecord>> asynchcssfriends = getCssLocalManager()
					.getCssFriends();

			model.put("cssfriends", asynchcssfriends.get());
			model.put("result", res);

		} catch (Exception e) {
			res = "Oops!!!!<br/>";
		}
		;

		return new ModelAndView("friendsresult", model);

	}

}
