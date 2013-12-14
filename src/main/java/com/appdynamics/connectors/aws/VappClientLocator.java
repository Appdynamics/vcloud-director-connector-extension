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
package com.appdynamics.connectors.aws;


import org.w3c.dom.Document;

import com.singularity.ee.connectors.api.ConnectorException;
import com.singularity.ee.connectors.api.IControllerServices;
import com.singularity.ee.connectors.api.InvalidObjectException;
import com.singularity.ee.connectors.entity.api.IComputeCenter;
import com.singularity.ee.connectors.entity.api.IImage;
import com.singularity.ee.connectors.entity.api.IImageStore;
import com.singularity.ee.connectors.entity.api.IMachine;
import com.singularity.ee.connectors.entity.api.IProperty;
import com.singularity.ee.connectors.utils.Constants;
import com.singularity.ee.connectors.utils.RestClient;

class VappClientLocator {
	
	private static final VappClientLocator INSTANCE = new VappClientLocator();

	
	/**
	 * Private constructor on singleton
	 */
	private VappClientLocator() {}
	
	public static VappClientLocator getInstance() {
		return INSTANCE;
	}
	
	private VappClient createVApp(IControllerServices controllerServices, String vCloudUrl, String username, String password, String orgName, String vdcName, String vAppName, String catalogName, String templateName, String network) throws ConnectorException
	{
		VappClient vCloudApp = new VappClient(controllerServices);
		
		try {
			// logging in
			String loginResponse = vCloudApp.login(vCloudUrl, username, orgName, password);
			
			// getting org links
			Document doc = RestClient.stringToXmlDocument(loginResponse);	
			String allOrgsLink = doc.getElementsByTagName(Constants.LINK).item(0).getAttributes().getNamedItem(Constants.HREF).getTextContent().trim();
			
			//
			//needs to be removed in case createVdc needs to be removed
			String adminLink = doc.getElementsByTagName(Constants.LINK).item(1).getAttributes().getNamedItem(Constants.HREF).getTextContent().trim();
			
			String orgLink = vCloudApp.retrieveOrg(allOrgsLink, orgName);
			
			// To instantiate a vApp template you need the vDc on which the vApp is going to be deployed and the object 
			// references for the catalog in which the vApp template will be entered.
			
			String vdcLink = vCloudApp.findVdc(orgLink, vdcName);
			String catalogLink = vCloudApp.findCatalog(orgLink, catalogName);
			String templateLink = vCloudApp.retrieveTemplate(catalogLink, templateName);
			
			String response = vCloudApp.newvAppFromTemplate(vAppName, templateLink, catalogLink, vdcLink, network);
			String vAppLink = vCloudApp.retrieveVappLink(response);
			vCloudApp.setvAppLink(vAppLink);
		}
		catch (Exception e) {
			throw new ConnectorException(e);
		}
		return vCloudApp;
	}
	
	private VappClient getVApp(IControllerServices controllerServices, String vCloudUrl, String username, String password, String orgName, String vdcName, String vAppName) throws ConnectorException 
	{
		VappClient vCloudApp = new VappClient(controllerServices);
		
		try {
			// logging in
			String loginResponse = vCloudApp.login(vCloudUrl, username, orgName, password);
			
			// getting org links
			Document doc = RestClient.stringToXmlDocument(loginResponse);	
			String allOrgsLink = doc.getElementsByTagName(Constants.LINK).item(0).getAttributes().getNamedItem(Constants.HREF).getTextContent().trim();
			String orgLink= vCloudApp.retrieveOrg(allOrgsLink, orgName);
			String delimiter = Utils.API;
			String apiUrl = orgLink.split(delimiter)[0] + delimiter;
			vCloudApp.setApiLink(apiUrl);
			
			String vAppLink = vCloudApp.findVappLink(apiUrl, vAppName);
			vCloudApp.setvAppLink(vAppLink);
		}
		catch (Exception e) {
			throw new ConnectorException(e);
		}
		
		return vCloudApp;
	}
	
	
	private VappClient getVApp(IProperty[] properties, IControllerServices controllerServices, boolean isCreateVapp) throws InvalidObjectException, ConnectorException
	{
		String ipAddress = controllerServices.getStringPropertyValueByName(properties, Utils.IP_ADDRESS);
		String username = controllerServices.getStringPropertyValueByName(properties, Utils.USERNAME);
		String password = controllerServices.getStringPropertyValueByName(properties, Utils.PASSWORD);
		String orgName = controllerServices.getStringPropertyValueByName(properties, Utils.ORG_NAME);
		String vdcName = controllerServices.getStringPropertyValueByName(properties, Utils.VDC_NAME);
		String vAppName = controllerServices.getStringPropertyValueByName(properties, Utils.VAPP_NAME);
		String catalog = controllerServices.getStringPropertyValueByName(properties, Utils.CATALOG);
		String template = controllerServices.getStringPropertyValueByName(properties, Utils.TEMPLATE_NAME);
		String network = controllerServices.getStringPropertyValueByName(properties, Utils.NETWORK_NAME);
		
		VappClient vAppRef;
		try {
			if(isCreateVapp){
				if(catalog.isEmpty() || template.isEmpty()) {
					throw new ConnectorException("Template name and the Catalog it resides in are required if creating vApp.");
				}
				vAppRef = createVApp(controllerServices, ipAddress, username, password, orgName, vdcName, vAppName, catalog, template, network);
			}
			vAppRef = getVApp(controllerServices, ipAddress, username, password, orgName, vdcName, vAppName);
			vAppRef.setvAppName(vAppName);
		} 
		catch (Exception e){
			throw new InvalidObjectException(e);
		}
		return vAppRef;
	}
	
	public VappClient getVApp(IComputeCenter computeCenter, IControllerServices controllerServices) throws InvalidObjectException, ConnectorException
	{
		boolean isCreateVapp = isCreateVapp(computeCenter.getProperties(), controllerServices);
		if(isCreateVapp) {
			return getVApp(computeCenter.getProperties(), controllerServices, true);
		}
		return getVApp(computeCenter.getProperties(), controllerServices, false);
	}
	
	public VappClient getVApp(IImageStore imageStore, IControllerServices controllerServices) throws InvalidObjectException, ConnectorException
	{
		return getVApp(imageStore.getProperties(), controllerServices, false);
	}
	
	public VappClient getVApp(IImage image, IControllerServices controllerServices) throws InvalidObjectException, ConnectorException
	{
		return getVApp(image.getImageStore().getProperties(), controllerServices, false);
	}
	
	public VappClient getVApp(IMachine machine, IControllerServices controllerServices) throws InvalidObjectException, ConnectorException
	{
		return getVApp(machine.getImage().getImageStore().getProperties(), controllerServices, false);
	}
	
	private boolean isCreateVapp(IProperty[] properties, IControllerServices controllerServices){
		String createVapp = controllerServices.getStringPropertyValueByName(properties, Utils.CREATE_VAPP);
		return createVapp.equals("Yes") ? true : false;
	}
	
}
