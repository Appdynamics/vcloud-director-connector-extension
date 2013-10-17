package com.singularity.ee.connectors.vCloud.VdcParams;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
