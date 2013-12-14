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

public class Utils {
	
	//Compute Center constants
	public static final String IP_ADDRESS = "IP Address";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String ORG_NAME = "Organization Name";
	public static final String VDC_NAME = "Virtual Datacenter Name";
	public static final String VAPP_NAME = "vApp Name";
	public static final String CREATE_VAPP = "Create Vapp";
	public static final String CATALOG = "Catalog Name";
	public static final String TEMPLATE_NAME = "Template Name";
	public static final String NETWORK_NAME = "Network Name";
	public static final String VM_PREFIX = "AD_VAPP-VM";
	
	
	public static final String CPU_CAPACITY_ALLOCATED = "200";
	public static final String CPU_CAPACITY_LIMIT = "200";
	public static final String CPU_CAPACITY_UNIT = "MHz";
	
	public static final String MEMORY_CAPACITY_ALLOCATED = "200";
	public static final String MEMORY_CAPACITY_LIMIT = "200";
	public static final String MEMORY_CAPACITY_UNIT = "200";

	// REST Calls
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String ACCEPT_HEADER = "Accept";
	
	public static final String CRED_PREFIX = "Basic ";
	public static final String CLOUD_URL_PREFIX = "https://";
	public static final String CLOUD_URL_SUFFIX = "/api/sessions";
	public static final String VMS_QUERY_SUFFIX = "/vms/query";
	public static final String VAPPS_QUERY_SUFFIX = "/vApps/query";
	
	public static final String API = "api";
	
	
}
