/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.connectors.vcd.VdcParams;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ComputeCapacity")
public class Resources {
	private Cpu cpu;
	private Cpu Memory;
	
	public Cpu getCpu() {
		return cpu;
	}
	@XmlElement(name="Cpu")
	
	public void setCpu(Cpu cpu) {
		this.cpu = cpu;
	}
	
	public Cpu getMemory() {
		return Memory;
	}
	
	@XmlElement(name="Memory")
	public void setMemory(Cpu memory) {
		Memory = memory;
	}
	
}
