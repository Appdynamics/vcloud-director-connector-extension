package com.singularity.ee.connectors.vCloud;

import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;

import com.singularity.ee.connectors.utils.Constants;
import com.singularity.ee.connectors.utils.RestClient;
import com.singularity.ee.connectors.vCloud.VdcParams.HrefElement;

import com.singularity.ee.connectors.api.ConnectorException;
import com.singularity.ee.connectors.api.IControllerServices;
import com.singularity.ee.connectors.api.InvalidObjectException;
import com.singularity.ee.connectors.entity.api.IComputeCenter;
import com.singularity.ee.connectors.entity.api.IImage;
import com.singularity.ee.connectors.entity.api.IMachine;
import com.singularity.ee.connectors.entity.api.IMachineDescriptor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class VappClient {
	
	private IControllerServices controllerServices;
	
	private static String authToken;
	private static final Object counterLock = new Object();
	private static volatile long counter;
	private static String vAppLink;
	private static String apiLink;
	private static String vAppName;
	
	public VappClient(IControllerServices controllerServices) {
		this.controllerServices = controllerServices;
	}
	
	public VappClient() {
	}

	/**
	 * Methods visible to VMwareCloudConnector
	 */
	
	public static String getvAppLink() {
		return vAppLink;
	}

	public void setvAppLink(String vAppLink) {
		VappClient.vAppLink = vAppLink;
	}
	
	public String getApiLink() {
		return apiLink;
	}

	public void setApiLink(String apiLink) {
		VappClient.apiLink = apiLink;
	}

	public static String getvAppName() {
		return vAppName;
	}

	public void setvAppName(String vAppName) {
		VappClient.vAppName = vAppName;
	}
	
	public IMachine createVm(IImage image, IMachineDescriptor machineDescriptor, IComputeCenter computeCenter) throws ConnectorException {
		
		String vmName = createName(Utils.VM_PREFIX);
	
		try {
		//
		// Getting appropriate information for creating recompose body
		//
		String response = doGet(vAppLink);
		
		Document doc = RestClient.stringToXmlDocument(response);
		Element childrenElement = (Element) doc.getElementsByTagName(Constants.CHILDREN).item(0);
		Element vmElement = (Element) childrenElement.getElementsByTagName(Constants.VM).item(0);
		
		String vmType = vmElement.getAttribute(Constants.TYPE);
		String vmRef = vmElement.getAttribute(Constants.HREF);
		
		//
		// Create RecomposeVAppParams body
		//
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(Constants.RECOMPOSE_VAPP);
		doc.appendChild(rootElement);
		
		rootElement.setAttribute("name", getvAppName());
		rootElement.setAttribute("xmlns", Constants.XMLNS);
		rootElement.setAttribute("xmlns:xsi", Constants.XMLNS_XSI);
		rootElement.setAttribute("xmlns:ovf", Constants.XMLNS_OVF);
		
		Element sourcedItem = doc.createElement(Constants.SOURCED_ITEM);
		sourcedItem.setAttribute(Constants.SOURCE_DELETE, "false");
		
		Element source = doc.createElement(Constants.SOURCE);
		source.setAttribute(Constants.TYPE, vmType);
		source.setAttribute(Constants.NAME, vmName);
		source.setAttribute(Constants.HREF, vmRef);
		
		sourcedItem.appendChild(source);
		rootElement.appendChild(sourcedItem);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		DOMSource dSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result= new StreamResult(writer);
		
		transformer.transform(dSource, result);
		String xmlBody = writer.toString();
		
		//
		// sending a POST request
		//
		
		StringEntity entity = new StringEntity(xmlBody);
		entity.setContentType(Constants.RECOMPOSE_VAPP_LINK);
		String createResponse = doPost(vAppLink + Constants.RECOMPOSE_ACTION, entity);
		
		IMachine machine = controllerServices.createMachineInstance(vmName, vmName, computeCenter, machineDescriptor, image, controllerServices.getDefaultAgentPort());
		return machine;
		}
		catch(Exception e){
			throw new ConnectorException(e.getMessage());
		}
	}
	
	// installs vmTools - problematic
	public String updateVm(IMachine machine) throws Exception {
		String vmName = machine.getName();
		String vmRef = getVmRef(vmName);
		
		return doPost(vmRef + Constants.INSTALL_VMTOOLS_ACTION, null);
	}
	
	public String shutDown(IMachine machine) throws ConnectorException{
		String vmName = machine.getName();
		String vmRef = getVmRef(vmName);
		
		String powerOffResp = doPost(vmRef + Constants.SHUTDOWN_ACTION, null);
		return powerOffResp;
	}
	
	public String delete(IMachine machine) throws Exception {
		String vmName = machine.getName();
		String vmRef = getVmRef(vmName);
		
		String deleteResp = doDelete(vmRef + "vmRef");
		return deleteResp;
	}
	
	public String restartVm(IMachine machine) throws ConnectorException{
		String vmName = machine.getName();
		String vmRef = getVmRef(vmName);
		
		return doPost(vmRef + Constants.RESTART_ACTION, null);
	}
	
	public String powerOn (String vmName) throws Exception{
		String vmRef = getVmRef(vmName);
		
		return doPost(vmRef + Constants.POWERON_ACTION, null);
	}
	
	public boolean isPoweredOn(String vmName) throws Exception {
		String vmSearch = doGet(apiLink + Utils.VMS_QUERY_SUFFIX);
		Document doc = RestClient.stringToXmlDocument(vmSearch);
		NodeList list = doc.getElementsByTagName(Constants.VM_RECORD);
		
		String vmRef = new String();
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getAttributes().getNamedItem(Constants.NAME).getTextContent().trim().equalsIgnoreCase(vmName)) 
			{
				if(list.item(i).getAttributes().getNamedItem(Constants.STATUS).getTextContent().trim().equalsIgnoreCase("POWERED_ON"))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Methods visible to VappClientLocator 
	 */
	
	public String login(String vCloudUrl, String username, String orgName, String password) throws Exception 
	{
		String loginString = Utils.CLOUD_URL_PREFIX + vCloudUrl + Utils.CLOUD_URL_SUFFIX;
		String creds;
		if(username.contains("@")) {
			creds = username +":" + password;
		} else {
			creds = username + "@" + orgName + ":" + password;
		}
		creds = new String(Base64.encodeBase64(creds.getBytes()));
		
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(Utils.AUTHORIZATION_HEADER, Utils.CRED_PREFIX + creds);
		headers.put(Utils.ACCEPT_HEADER, Constants.XML_CONTENT_TYPE);
		
		HttpResponse response = RestClient.doPost(loginString, null, headers);
		StatusLine status = response.getStatusLine();
		if( status.getStatusCode() == 401) {
			throw new Exception("Unauthorized credentials. Please check username and password");
		}
		
		HttpClient httpClient = RestClient.getHttpClient();
		authToken = RestClient.getAuthToken(response);
		return RestClient.getResponseString(httpClient, response);
	}
	
	public String retrieveAdminOrg(String orgsLink, String orgName) throws Exception{
		String response = doGet(orgsLink);
		String element = findElement(response, Constants.LINK, Constants.ADMIN_ORG_LINK, orgName);
		
		if( element == null){
			throw new Exception(" " + orgName);
		}
		return element;
	}
	
	public String retrieveNetworkPool(String adminLink) throws Exception{
		String response = doGet(adminLink + "extension/");
		String networkPools = findElement(response, Constants.VCLOUD_LINK, Constants.NETWORK_POOLS, null);
		
		response = doGet(networkPools);
		String element = findElement(response, Constants.NET_POOL, Constants.NETWORK_POOLS_REF, null);
		if( element == null){
			throw new Exception(" ");
		}
		return element;
	}
	/*
	public String CreateVdc(String adminLink, String orgLink, String vdcName, String networkPoolRef) throws Exception
	{
		String response = doGet(orgLink);
		String createVdcParamsHref = findElement(response, Constants.LINK, Constants.CREATE_VDC_LINK, null );
		HrefElement providerVdc = retrieveProviderVdcReference(adminLink); 
		
		response = doGet(providerVdc.getHref());
		String providerVdcStorageRef = findElement(response, Constants.P_STORAGE_PROFILE, Constants.P_VDC_STORAGE, null);
		
		CreateVdcParams vdcParams = new CreateVdcParams();
		vdcParams.setName(vdcName);
		vdcParams.setXmlns("http://www.vmware.com/vcloud/v1.5");
		vdcParams.setDescription("testing");
		vdcParams.setAllocationModel("AllocationVApp");
		
		Cpu Cpu = new Cpu();
		Cpu.setUnits("MHz");
		Cpu.setAllocated("2048");
		Cpu.setLimit("2048");
		
		Cpu mem = new Cpu();
		mem.setUnits("MB");
		mem.setAllocated("2048");
		mem.setLimit("2048");
		
		Resources res = new Resources();
		res.setCpu(Cpu);
		res.setMemory(mem);
		
		vdcParams.setResources(res);
		vdcParams.setNicQuota("0");
		vdcParams.setNetworkQuota("100");
		
		VdcStorageProfile vdcProfile = new VdcStorageProfile();
		vdcProfile.setEnabled("true");
		vdcProfile.setUnits("MB");
		vdcProfile.setLimit("20480");
		vdcProfile.setDefault("true");
		
		HrefElement pvdcStorageProfile = new HrefElement();
		pvdcStorageProfile.setHref(providerVdcStorageRef);
		vdcProfile.setProviderVdc(pvdcStorageProfile);
		
		vdcParams.setVdcProfile(vdcProfile);
		vdcParams.setResourceCpu("1");
		vdcParams.setResourceMemory("1");
		vdcParams.setvCpuIn("2048");
		vdcParams.setIsThin("false");
		HrefElement netPoolRef = new HrefElement();
		netPoolRef.setHref(networkPoolRef);
		
		HrefElement providerRef = new HrefElement();
		providerRef.setHref(providerVdc.getHref());
		providerRef.setName(providerVdc.getName());
		
		vdcParams.setNetRef(netPoolRef);
		vdcParams.setVdcRef(providerRef);
		vdcParams.setFastProvisioning("true");
		
		try {
			JAXBContext context = JAXBContext.newInstance(CreateVdcParams.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
			XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(baos, (String) m.getProperty(Marshaller.JAXB_ENCODING));

			sw.writeStartDocument((String) m.getProperty(Marshaller.JAXB_ENCODING), "1.0");
//			m.marshal(vdcParams, new File("./vdcParams1.xml"));
			m.marshal(vdcParams, sw);
			sw.writeEndDocument();
			sw.close();
            
			String postBody = new String(baos.toByteArray());
			
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put(Constants.VCLOUD_AUTH_HEADER, authToken);
			headers.put(Utils.ACCEPT_HEADER, Constants.XML_CONTENT_TYPE);
			
			StringEntity entity = new StringEntity(postBody);
			entity.setContentType(Constants.CREATE_VDC_XML_TYPE);
			HttpResponse resp= RestClient.doPost(createVdcParamsHref, entity, headers);
			HttpClient httpClient = RestClient.getHttpClient();
			response = RestClient.getResponseString(httpClient, resp);
		}
		catch (Exception e) {
			throw e;
		}
		return null;
	}
	*/
	
	public String retrieveOrg(String adminCloud , String orgName) throws Exception{
		String response = doGet(adminCloud);
		String element = findElement(response, Constants.ORG, Constants.ORG_LINK, orgName);
		if( element == null){
			throw new Exception("Could not find Organization with this name: " + orgName);
		}
		return element;
	}
	
	// remove if create vdc is removed
	public HrefElement retrieveProviderVdcReference(String adminLink) throws Exception {
		String response = doGet(adminLink);
		HrefElement providerVdc = new HrefElement();
		providerVdc.setHref(findElement(response, Constants.PROVIDER_VDC, Constants.PROVIDER_VDC_LINK, null));
		providerVdc.setName(findName(response, Constants.PROVIDER_VDC, Constants.PROVIDER_VDC_LINK));
		return providerVdc;
	}
	
	public String findVdc(String orgLink, String vdcName) throws Exception
	{
		String response = doGet(orgLink);
		String element = findElement(response, Constants.LINK, Constants.VDC_LINK, vdcName);
		
		if( element == null){
			throw new Exception("No VDC found that matches the given name: " + vdcName);
		}
		
		return element;
	}
	
	
	public String findCatalog(String orgLink, String catalogName) throws Exception
	{
		String response = doGet(orgLink);
		String element = findElement(response, Constants.LINK, Constants.CATALOG_LINK, catalogName);
		if( element == null){
			throw new Exception("No Catalog found that matches the given name: " + catalogName);
		}
		
		return element;
	}

	public String retrieveTemplate(String catalogLink, String templateName) throws Exception{
		String response = doGet(catalogLink);
		String catalogItemLink = findElement(response, Constants.CATALOGITEM, Constants.CATALOGITEM_LINK, templateName);
		
		response = doGet(catalogItemLink);
		String element = findElement(response, Constants.ENTITY, Constants.TEMPLATE_LINK, templateName);
		if( element == null){
			throw new Exception("No vApp template found that matches the given name: " + templateName);
		}
		return element;
	}
	
	public String newvAppFromTemplate( String vAppName, String templateLink, String catalogLink, String vdcLink, String network ) throws Exception 
	{
		// Step 1
		String response = doGet(templateLink);
		
		// Step 2 - look for NetowrkConnection element in the Vms contained
		Document doc = RestClient.stringToXmlDocument(response);
		Element childrenElement = (Element) doc.getElementsByTagName(Constants.CHILDREN).item(0);
		Element vmElement = (Element) childrenElement.getElementsByTagName(Constants.VM).item(0);
		Element netElement = (Element) vmElement.getElementsByTagName("NetworkConnectionSection").item(0);
		
		
		
		String ovfInfo = doc.getElementsByTagName("ovf:Info").item(0).getTextContent();
		String networkName = netElement.getElementsByTagName("NetworkConnection").item(0).getAttributes().getNamedItem("network").getTextContent().trim(); 
		
		String vdcResponse = doGet(vdcLink);
		String parentNetwork;
		if(network.isEmpty()) 
		{
			parentNetwork = findElement(vdcResponse, Constants.NETWORK, Constants.NETWORK_LINK, null);
		} 
		else {
			parentNetwork = findElement(vdcResponse, Constants.NETWORK, Constants.NETWORK_LINK, network);
		}
		
		// Step 3 - create an InstantiateVAppTemplate Params element
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		// root elements
		doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("InstantiateVAppTemplateParams");
		doc.appendChild(rootElement);
		
		rootElement.setAttribute("name", vAppName);
		rootElement.setAttribute("xmlns", Constants.XMLNS);
		rootElement.setAttribute("deploy", "true");
		rootElement.setAttribute("powerOn", "false");
		rootElement.setAttribute("xmlns:xsi", Constants.XMLNS_XSI);
		rootElement.setAttribute("xmlns:ovf", Constants.XMLNS_OVF);
		
		Element descElement = doc.createElement("Description");
		descElement.appendChild(doc.createTextNode("Created through Appdynamics controller"));
		rootElement.appendChild(descElement);
		
		Element paramsElement = doc.createElement("InstantiationParams");
		
		Element configElement = doc.createElement("Configuration");
		
		Element parentNetElement = doc.createElement("ParentNetwork");
		parentNetElement.setAttribute("href", parentNetwork);
		configElement.appendChild(parentNetElement);
		
		Element fenceElement = doc.createElement("FenceMode");
		fenceElement.appendChild(doc.createTextNode("bridged"));
		configElement.appendChild(fenceElement);
		
		Element netconfigSectionElement = doc.createElement("NetworkConfigSection");
		
		Element ovfInfoElement = doc.createElement("ovf:Info");
		ovfInfoElement.appendChild(doc.createTextNode(ovfInfo));
		netconfigSectionElement.appendChild(ovfInfoElement);
		
		Element netConfElement = doc.createElement("NetworkConfig");
		netConfElement.setAttribute("networkName", networkName);
		netConfElement.appendChild(configElement);
		netconfigSectionElement.appendChild(netConfElement);
		
		paramsElement.appendChild(netconfigSectionElement);
		rootElement.appendChild(paramsElement);
		
		Element sourceElement = doc.createElement("Source");
		sourceElement.setAttribute("href", templateLink);
		rootElement.appendChild(sourceElement);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		DOMSource source = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result= new StreamResult(writer);
		
		transformer.transform(source, result);
		String xmlBody = writer.toString();
		
		
		// Step 4 - make a POST 
		
		String instantiateVAppLink = findElement(vdcResponse, Constants.LINK, Constants.INSTANTIATE_VAPP, null);
		
		StringEntity entity = new StringEntity(xmlBody);
		entity.setContentType(Constants.INSTANTIATE_VAPP);
		
		return doPost(instantiateVAppLink, entity);
	}
	
	public String retrieveVappLink(String response) throws Exception{
		Document doc = RestClient.stringToXmlDocument(response);
		NodeList list = doc.getElementsByTagName(Constants.VAPP);
		
		return list.item(0).getAttributes().getNamedItem(Constants.HREF).getTextContent().trim();
	}
	 public String findVappLink(String apiUrl, String vAppName) throws Exception {
		String response = doGet(apiUrl + Utils.VAPPS_QUERY_SUFFIX);
		Document doc = RestClient.stringToXmlDocument(response);
		NodeList list = doc.getElementsByTagName(Constants.VAPP_RECORD); 
		
		String element = new String();
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getAttributes().getNamedItem(Constants.NAME).getTextContent().trim().equalsIgnoreCase(vAppName)){
				 element = list.item(i).getAttributes().getNamedItem(Constants.HREF).getTextContent().trim();
			}
		}
		
		if( element.isEmpty())
		{
			throw new Exception("No Vapp found that matches the given name: " + vAppName);
		}
			
		return element;
	 }
	// Helper methods
	
	private String doGet(String link) throws ConnectorException {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.VCLOUD_AUTH_HEADER, authToken);
		headers.put(Utils.ACCEPT_HEADER, Constants.XML_CONTENT_TYPE);
		
		return RestClient.doGet(link, headers);
	}
	
	private String doPost(String link, StringEntity entity) throws ConnectorException
	{
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.VCLOUD_AUTH_HEADER, authToken);
		headers.put(Utils.ACCEPT_HEADER, Constants.XML_CONTENT_TYPE);
		
		HttpResponse resp = RestClient.doPost(link, entity, headers);
		StatusLine status = resp.getStatusLine();
		HttpClient httpClient = RestClient.getHttpClient();
		String responseString = RestClient.getResponseString(httpClient, resp);
		String errorMessage = RestClient.getErrorMessage(responseString);
		
		if( status.getStatusCode() == 400) 
		{
			throw new ConnectorException("Bad Request. " + errorMessage);
		} 
		else if (status.getStatusCode() == 401) 
		{
			throw new ConnectorException("Unauthorized. " + errorMessage);
		}
		else if (status.getStatusCode() == 403)
		{
			throw new ConnectorException("Forbidden. " + errorMessage);
		}
		else if (status.getStatusCode() == 404)
		{
			throw new ConnectorException("Not Found. " + errorMessage);
		}
		
		return responseString;
	}
	
	private String doDelete(String link) throws Exception {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.VCLOUD_AUTH_HEADER, authToken);
		headers.put(Utils.ACCEPT_HEADER, Constants.XML_CONTENT_TYPE);
		
		return RestClient.doDelete(link, headers);
	}
	
	private String findElement(String response, String elementTag, String elementTypeLink, String elementName) throws InvalidObjectException, ConnectorException {
		Document doc = RestClient.stringToXmlDocument(response);
		NodeList list = doc.getElementsByTagName(elementTag); 
		
		for(int i=0; i < list.getLength();i++){
			if(list.item(i).getAttributes().getNamedItem(Constants.TYPE)!=null){
				if(list.item(i).getAttributes().getNamedItem(Constants.TYPE).getTextContent().trim().equalsIgnoreCase(elementTypeLink)) {
					if (elementName != null)
					{
						if(list.item(i).getAttributes().getNamedItem(Constants.NAME).getTextContent().trim().equalsIgnoreCase(elementName))
							return list.item(i).getAttributes().getNamedItem(Constants.HREF).getTextContent().trim();
					}
					else {
						return list.item(i).getAttributes().getNamedItem(Constants.HREF).getTextContent().trim();
					}
				}
			}	
		}
		
		//
		// if it reaches the end without finding the element, throw exception
		//
		if(elementName != null)
		{
			throw new InvalidObjectException("Element not found: " + elementName);
		}
		else 
		{
			throw new InvalidObjectException("Element not found: " + elementTypeLink);
		}
		
	}
	
	// remove this if removing create-VDC
	private String findName(String response, String elementTag, String elementType) throws Exception{
		Document doc = RestClient.stringToXmlDocument(response);
		NodeList list = doc.getElementsByTagName(elementTag); 
		
		for(int i=0; i < list.getLength();i++){
			if(list.item(i).getAttributes().getNamedItem(Constants.TYPE).getTextContent().trim().equalsIgnoreCase(elementType)) {	
				return list.item(i).getAttributes().getNamedItem(Constants.NAME).getTextContent().trim();
			}
		}
		
		//
		// if it reaches the end without finding the element, throw exception
		//
		throw new InvalidObjectException("Item not found: " + elementType);
	}
	
	private String getVmRef( String vmName) throws ConnectorException{
		String vmSearch = doGet(apiLink + Utils.VMS_QUERY_SUFFIX);
		
		Document doc = RestClient.stringToXmlDocument(vmSearch);
		NodeList list = doc.getElementsByTagName(Constants.VM_RECORD);
		
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getAttributes().getNamedItem(Constants.NAME).getTextContent().trim().equalsIgnoreCase(vmName)) 
			{
				return list.item(i).getAttributes().getNamedItem(Constants.HREF).getTextContent().trim();
			}
		}
		
		throw new ConnectorException("The following Vm is not being found or is corrupted: " + vmName);
	}
	
	private String createName(String prefix) {
		long count;
		synchronized (counterLock)
		{
			count = counter++;
		}
		String cloneName = prefix + System.currentTimeMillis() + count;
		return cloneName;
	}
	
	
}
