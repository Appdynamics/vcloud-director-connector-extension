/**
 * Copyright 2013 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.connectors.vcd.VdcParams;

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
