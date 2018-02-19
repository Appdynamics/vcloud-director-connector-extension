/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.connectors.vcd.VdcParams;

import javax.xml.bind.annotation.XmlElement;

 
public class Cpu {
	private String Units;
	private String Allocated;
	private String Limit;

	
	@XmlElement(name="Units")
	public void setUnits(String units) {
		Units = units;
	}
	public String getUnits() {
		return Units;
	}

	
	@XmlElement(name="Allocated")
	public void setAllocated(String allocated) {
		Allocated = allocated;
	}
	public String getAllocated() {
		return Allocated;
	}
	
	@XmlElement(name="Limit")
	public void setLimit(String limit) {
		Limit = limit;
	}
	public String getLimit() {
		return Limit;
	}

}
