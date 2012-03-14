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


package org.societies.api.internal.cis.management;

/**
 * Stores meta data relevant for a CIS.
 * 
 * @author Babak Farshchian
 * @version 0
 */

import java.util.List;
import java.util.Set;

import org.societies.api.internal.cis.collaboration.IServiceSharingRecord;


@Deprecated
public class CisRecord {
	public ICisActivityFeed feed;
	public String ownerCss;
	public String membershipCriteria;
	public String cisId;
	public String fullJid;


	/**
	 * permaLink is a permanent URL to this CIS. A type of CIS homepage.
	 */
	public String permaLink;
	public Set<CisParticipant> membersCss;
	private String password = "none";
	private String host = "none";
	public Set<IServiceSharingRecord> sharedServices;
	

	
	public CisRecord(ICisActivityFeed feed, String ownerCss,
			String membershipCriteria, String cisId, String permaLink,
			Set<CisParticipant> membersCss, String password, String host,
			Set<IServiceSharingRecord> sharedServices) {
		super();
		this.feed = feed;
		this.ownerCss = ownerCss;
		this.membershipCriteria = membershipCriteria;
		this.cisId = cisId;
		this.permaLink = permaLink;
		this.membersCss = membersCss;
		this.password = password;
		this.host = host;
		this.sharedServices = sharedServices;
		
		this.fullJid = cisId + "." + host;
	}
	
	/**
	 * @deprecated  Replaced membersCss from String to CisParticipants type
	 */
	
	@Deprecated
	public CisRecord(ICisActivityFeed feed, String ownerCss,
			String membershipCriteria, String cisId, String permaLink,
			String[] membersCss, String password, String host,
			Set<IServiceSharingRecord> sharedServices) {
		super();
		this.feed = feed;
		this.ownerCss = ownerCss;
		this.membershipCriteria = membershipCriteria;
		this.cisId = cisId;
		this.permaLink = permaLink;
		//this.membersCss = membersCss;
		this.password = password;
		this.host = host;
		this.sharedServices = sharedServices;
	}
	
	
	/**
	 * @deprecated  Replaced by constructor which has the new host field and replaced membersCss from String to CisParticipants type
	 */
	
	@Deprecated
	public CisRecord(ICisActivityFeed feed, String ownerCss,
			String membershipCriteria, String cisId, String permaLink,
			String[] membersCss, String password,
			Set<IServiceSharingRecord> sharedServices) {
		super();
		this.feed = feed;
		this.ownerCss = ownerCss;
		this.membershipCriteria = membershipCriteria;
		this.cisId = cisId;
		this.permaLink = permaLink;
		//this.membersCss = membersCss;
		this.password = password;
		this.sharedServices = sharedServices;
	}
	

	 // hash code and equals using cisId and host

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cisId == null) ? 0 : cisId.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CisRecord other = (CisRecord) obj;
		if (cisId == null) {
			if (other.cisId != null)
				return false;
		} else if (!cisId.equals(other.cisId))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		return true;
	}


	public String getOwnerCss() {
		return ownerCss;
	}


	public void setOwnerCss(String ownerCss) {
		this.ownerCss = ownerCss;
	}


	public String getCisId() {
		return cisId;
	}


	public void setCisId(String cisId) {
		// TODO: double check that this is consistent with the fulljid
		this.cisId = cisId;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}



	public String getFullJid() {
		return fullJid;
	}

	public void setFullJid(String fullJid) {
		this.fullJid = fullJid;
	}

	

	

}
