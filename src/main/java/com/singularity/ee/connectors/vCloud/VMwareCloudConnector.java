package com.singularity.ee.connectors.vCloud;


import com.singularity.ee.connectors.api.ConnectorException;
import com.singularity.ee.connectors.api.IConnector;
import com.singularity.ee.connectors.api.IControllerServices;
import com.singularity.ee.connectors.api.InvalidObjectException;
import com.singularity.ee.connectors.entity.api.IComputeCenter;
import com.singularity.ee.connectors.entity.api.IImage;
import com.singularity.ee.connectors.entity.api.IImageStore;
import com.singularity.ee.connectors.entity.api.IMachine;
import com.singularity.ee.connectors.entity.api.IMachineDescriptor;
import com.singularity.ee.connectors.entity.api.MachineState;

import java.util.logging.Logger;

public class VMwareCloudConnector implements IConnector{
	
	private static Logger logger = Logger.getLogger(VMwareCloudConnector.class.getName());
	private IControllerServices controllerServices;
	
	/**
	 * Public no-arg constructor is required by the connector framework.
	 */
	public VMwareCloudConnector() {}
	
	
	@Override
	public void setControllerServices(IControllerServices controllerServices) {
		this.controllerServices = controllerServices;
	}

	
	@Override
	public int getAgentPort() {
		return controllerServices.getDefaultAgentPort();
	}

	@Override
	public void validate(IComputeCenter computeCenter)
			throws InvalidObjectException, ConnectorException {
		logger.info("Validating ComputeCloud: " + computeCenter.getName());
		
		
	}
	
	@Override
	public void validate(IImageStore imageStore) throws InvalidObjectException,
			ConnectorException {
		logger.info("Validating ImageStore: " + imageStore.getName());
		
		
		@SuppressWarnings("unused")
		VappClient vApp = VappClientLocator.getInstance().getVApp(imageStore, controllerServices);
		
	}
	

	@Override
	public void validate(IImage image) throws InvalidObjectException,
			ConnectorException {
		logger.info("Validating Image: " + image.getName());
		
		@SuppressWarnings("unused")
		VappClient vApp = VappClientLocator.getInstance().getVApp(image, controllerServices);
			
	}
	
	@Override
	public IMachine createMachine(IComputeCenter computeCenter, IImage image,
			IMachineDescriptor machineDescriptor)
			throws InvalidObjectException, ConnectorException {
		logger.info("Spinning up clone: " + image.getName());
	
		VappClient vApp = VappClientLocator.getInstance().getVApp(image, controllerServices);
		IMachine machine = vApp.createVm(image, machineDescriptor, computeCenter);
		return machine;
		
	}

	@Override
	public void refreshMachineState(IMachine machine) throws InvalidObjectException, ConnectorException {
		try {
			String vmName = machine.getName();
			MachineState currentState = machine.getState();
			VappClient vApp = VappClientLocator.getInstance().getVApp(machine, controllerServices);
			
			if(currentState == MachineState.STARTING) 
			{
				if(vApp.isPoweredOn(vmName))
				{
					machine.setState(MachineState.STARTED);
					// installs vm tools
//					vApp.updateVm(machine);
				} 
				else 
				{
					vApp.powerOn(vmName);
				}
			}
			
			else if(currentState == MachineState.STOPPING)
			{
				if(vApp.isPoweredOn(vmName))
				{
					vApp.shutDown(machine);
					machine.setState(MachineState.STOPPED);
				}
				else 
				{
					machine.setState(MachineState.STOPPED);
				}
			}
		}
		catch(Exception e){
			throw new ConnectorException("Failed to retrieve vm status: ", e);
		}
	}

	@Override
	public void terminateMachine(IMachine machine)
			throws InvalidObjectException, ConnectorException {
		
		logger.info("Powering off machine: " + machine.getName());
		
		VappClient vApp = VappClientLocator.getInstance().getVApp(machine, controllerServices);
		vApp.shutDown(machine);
	}

	@Override
	public void restartMachine(IMachine machine) throws InvalidObjectException,
			ConnectorException {
		
		logger.info("Restarting machine: " + machine.getName());
		
		VappClient vApp = VappClientLocator.getInstance().getVApp(machine, controllerServices);
		vApp.restartVm(machine);
		
	}

	@Override
	public void deleteImage(IImage image) throws InvalidObjectException,
			ConnectorException {
		
		
	}

	@Override
	public void refreshImageState(IImage image) throws InvalidObjectException,
			ConnectorException {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void configure(IComputeCenter computeCenter)
			throws InvalidObjectException, ConnectorException {
		logger.info("Configuring compute center: " + computeCenter.getName());
		
		@SuppressWarnings("unused")
		VappClient vApp = VappClientLocator.getInstance().getVApp(computeCenter, controllerServices);
		
	}

	@Override
	public void unconfigure(IComputeCenter computeCenter)
			throws InvalidObjectException, ConnectorException {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void configure(IImageStore imageStore)
			throws InvalidObjectException, ConnectorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unconfigure(IImageStore imageStore)
			throws InvalidObjectException, ConnectorException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void configure(IImage image) throws InvalidObjectException,
			ConnectorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unconfigure(IImage image) throws InvalidObjectException,
			ConnectorException {
		// TODO Auto-generated method stub
		
	}

}
