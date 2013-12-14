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
package com.appdynamics.connectors.aws.VdcParams;

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
