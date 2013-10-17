package com.singularity.ee.connectors.vCloud.VdcParams;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
