/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.connectors.vcd.VdcParams;

import javax.xml.bind.annotation.XmlAttribute;

public class HrefElement {
	private String href;
	private String name;

	public String getHref() {
		return href;
	}
	
	@XmlAttribute
	public void setHref(String href) {
		this.href = href;
	}
	
	public String getName() {
		return name;
	}
	
	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}
}
