package com.singularity.ee.connectors.vCloud.VdcParams;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="VdcStorageProfile")
public class VdcStorageProfile {
	private String enabled;
	private String units;
	private String limit;
	private String Default;
	private HrefElement providerVdc;
	
	public String getEnabled() {
		return enabled;
	}
	
	@XmlElement(name= "Enabled")
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getUnits() {
		return units;
	}
	
	@XmlElement(name= "Units")
	public void setUnits(String units) {
		this.units = units;
	}

	public String getLimit() {
		return limit;
	}

	@XmlElement(name= "Limit")
	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getDefault() {
		return Default;
	}

	@XmlElement(name= "Default")
	public void setDefault(String _default) {
		Default = _default;
	}

	public HrefElement getProviderVdc() {
		return providerVdc;
	}
	
	@XmlElement(name= "ProviderVdcStorageProfile")
	public void setProviderVdc(HrefElement providerVdc) {
		this.providerVdc = providerVdc;
	}
}
