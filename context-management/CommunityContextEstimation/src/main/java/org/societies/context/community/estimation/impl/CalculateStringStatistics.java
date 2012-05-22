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

package org.societies.context.community.estimation.impl;

import java.util.ArrayList;
import java.util.Hashtable;

public class CalculateStringStatistics {
	public Integer proffesionCounter1 = 0;

	public void CalculateAttributeStatistics() {
		
		ArrayList <String> proffesions = new ArrayList<String>();
		proffesions.add("Engineer");
		proffesions.add("Engineer");
		proffesions.add("Engineer");
		proffesions.add("Engineer");
		proffesions.add("Engineer");
		proffesions.add("Chef");
		proffesions.add("Architect");
		proffesions.add("Plumber");
		proffesions.add("Plumber");
		proffesions.add("Cook");

// Ta parapano einai mono gia testing. Kanonika prepei na pernaei h lista san orisma kai na
// epistrefetai h lista me th syxnothta emfanishs kathe timhs. Isos akoma na baloume kai ena
// counter synoliko kai na to "kotsaroume" sto telos tou pinaka gia na mporoume na bgazoume
// eykola ta pososta meta ...

		Hashtable <String, Integer> frequencyMap = new Hashtable();
		ArrayList<String> finalList = new ArrayList<String>();
		


		for (int i=0; i<proffesions.size(); i++){			
				if (finalList.contains(proffesions.get(i)))
				{
					int elementCount = 
							Integer.parseInt(frequencyMap.get(proffesions.get(i)).toString());
					elementCount++;
					frequencyMap.put(proffesions.get(i), elementCount);
				}
				else
				{
					finalList.add(proffesions.get(i));
					frequencyMap.put(proffesions.get(i), 1);
				}
			}
			System.out.println(frequencyMap);
		}
}
	 
			 
	
	 
//}

//proffesions.remove(0);
//for (int j=0; j<proffesions.size(); j++){
//	if (proffesions.get(i).compareTo(proffesions.get(j))>0){
//		proffesions.remove(j);
//		finalListOfProffesions.
//		
//		proffesionCounter1++;
//		proffesions.
//	}
//	
//	foreach(String a in animals) {
//	  if(frequencymap.containsKey(a)) {
//	    frequencymap.put(a, frequencymap.get(a)+1);
//	  }
//	  else{ frequencymap.put(a, 1); }
//	}
