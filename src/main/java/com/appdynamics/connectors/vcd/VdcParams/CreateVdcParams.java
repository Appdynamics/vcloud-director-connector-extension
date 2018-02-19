/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.connectors.vcd.VdcParams;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "CreateVdcParams")
public class CreateVdcParams {
	private String mName;
	private String mXmlns;
	private String mDescription;
	private String mAllocationModel;
	private Resources resources;
	private String nicQuota;
	private String networkQuota;
	private VdcStorageProfile vdcProfile;
	private String resourceCpu;
	private String resourceMemory;
	private String vCpuIn;
	private String isThin;
	private HrefElement NetRef;
	private HrefElement vdcRef;
	private String fastProvisioning;
	
	public String getName() {
		return mName;
	}
	
	@XmlAttribute
	public void setName(String Name) {
		this.mName = Name;
	}

	public String getXmlns() {
		return mXmlns;
	}
	
	@XmlAttribute
	public void setXmlns(String Xmlns) {
		this.mXmlns = Xmlns;
	}

	public String getDescription() {
		return mDescription;
	}
	
	@XmlElement(name = "Description")
	public void setDescription(String Description) {
		this.mDescription = Description;
	}

	public String getAllocationModel() {
		return mAllocationModel;
	}
	@XmlElement(name = "AllocationModel")
	public void setAllocationModel(String AllocationModel) {
		mAllocationModel = AllocationModel;
	}

	public Resources getResources() {
		return resources;
	}
	
	@XmlElement(name = "ComputeCapacity")
	public void setResources(Resources resources) {
		this.resources = resources;
	}

	public String getNicQuota() {
		return nicQuota;
	}
	
	@XmlElement(name = "NicQuota")
	public void setNicQuota(String nicQuota) {
		this.nicQuota = nicQuota;
	}

	public String getNetworkQuota() {
		return networkQuota;
	}
	
	
	@XmlElement(name="NetworkQuota")
	public void setNetworkQuota(String networkQuota) {
		this.networkQuota = networkQuota;
	}

	public VdcStorageProfile getVdcProfile() {
		return vdcProfile;
	}
	
	@XmlElement(name="VdcStorageProfile")
	public void setVdcProfile(VdcStorageProfile vdcProfile) {
		this.vdcProfile = vdcProfile;
	}

	public String getResourceCpu() {
		return resourceCpu;
	}
	@XmlElement(name="ResourceGuaranteedCpu")
	public void setResourceCpu(String resourceCpu) {
		this.resourceCpu = resourceCpu;
	}

	public String getResourceMemory() {
		return resourceMemory;
	}
	@XmlElement(name="ResourceGuaranteedMemory")
	public void setResourceMemory(String resourceMemory) {
		this.resourceMemory = resourceMemory;
	}

	public String getvCpuIn() {
		return vCpuIn;
	}
	@XmlElement(name="VCpuInMhz")
	public void setvCpuIn(String vCpuIn) {
		this.vCpuIn = vCpuIn;
	}

	public String getIsThin() {
		return isThin;
	}
	@XmlElement(name="IsThinProvision")
	public void setIsThin(String isThin) {
		this.isThin = isThin;
	}
	
	public HrefElement getNetRef() {
		return NetRef;
	}
	@XmlElement(name="NetworkPoolReference")
	public void setNetRef(HrefElement netRef) {
		NetRef = netRef;
	}

	public HrefElement getVdcRef() {
		return vdcRef;
	}
	@XmlElement(name="ProviderVdcReference")
	public void setVdcRef(HrefElement vdcRef) {
		this.vdcRef = vdcRef;
	}

	public String getFastProvisioning() {
		return fastProvisioning;
	}
	@XmlElement(name="UsesFastProvisioning")
	public void setFastProvisioning(String fastProvisioning) {
		this.fastProvisioning = fastProvisioning;
	}

	
}
