/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.client.impl;

import org.apache.airavata.client.AiravataAPIUtils;
import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.ExecutionManager;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.stub.interpretor.NameValue;
import org.apache.airavata.client.stub.interpretor.WorkflowInterpretorStub;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.*;
import org.apache.airavata.ws.monitor.MonitorUtil.EventType;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;

import javax.xml.stream.XMLStreamException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;

public class ExecutionManagerImpl implements ExecutionManager {
	private AiravataClient client;

    public ExecutionManagerImpl() {
    }

    public ExecutionManagerImpl(AiravataClient client) {
		setClient(client);
	}

	@Override
	public String runExperiment(String workflowTemplateId,
			List<WorkflowInput> inputs) throws AiravataAPIInvocationException {
		ExperimentAdvanceOptions options = createExperimentAdvanceOptions(workflowTemplateId+"_"+Calendar.getInstance().getTime().toString(), getClient().getCurrentUser(), null);
		return runExperiment(workflowTemplateId, inputs ,options);
	}
	
	@Override
	public String runExperiment(String workflow,
			List<WorkflowInput> inputs, ExperimentAdvanceOptions options)
			throws AiravataAPIInvocationException {
		return runExperimentGeneral(extractWorkflow(workflow), inputs, options, null);
	}
	

	@Override
	public String runExperiment(Workflow workflow, List<WorkflowInput> inputs, ExperimentAdvanceOptions options)
			throws AiravataAPIInvocationException {
		return runExperimentGeneral(workflow,inputs, options, null).toString();
	}
	

	@Override
	public ExperimentAdvanceOptions createExperimentAdvanceOptions()
			throws AiravataAPIInvocationException {
		return new ExperimentAdvanceOptions();
	}

	@Override
	public ExperimentAdvanceOptions createExperimentAdvanceOptions(
			String experimentName, String experimentUser,
			String experimentMetadata) throws AiravataAPIInvocationException {
		ExperimentAdvanceOptions options = createExperimentAdvanceOptions();
		options.setExperimentName(experimentName);
		options.setExperimentCustomMetadata(experimentMetadata);
		options.setExperimentExecutionUser(experimentUser);
		return options;
	}

	@Override
	public void waitForExperimentTermination(String experimentId)
			throws AiravataAPIInvocationException {
//		Monitor experimentMonitor = getExperimentMonitor(experimentId, new EventDataListenerAdapter() {
//			@Override
//			public void notify(EventDataRepository eventDataRepo,
//					EventData eventData) {
//				if (eventData.getType()==EventType.WORKFLOW_TERMINATED){
//					getMonitor().stopMonitoring();
//				}
//			}
//		});
//		experimentMonitor.startMonitoring();
//		try {
//			WorkflowExecutionStatus workflowInstanceStatus = getClient().getProvenanceManager().getWorkflowInstanceStatus(experimentId, experimentId);
//			if (workflowInstanceStatus.getExecutionStatus()==State.FINISHED || workflowInstanceStatus.getExecutionStatus()==State.FAILED){
//				experimentMonitor.stopMonitoring();
//				return;
//			}
//		} catch (AiravataAPIInvocationException e) {
//			//Workflow may not have started yet. Best to use the monitor to follow the progress
//		}
//		experimentMonitor.waitForCompletion();
	}
	
	@Override
	public Monitor getExperimentMonitor(String experimentId)
			throws AiravataAPIInvocationException {
		return getExperimentMonitor(experimentId,null);
	}

	@Override
	public Monitor getExperimentMonitor(String experimentId,final EventDataListener listener)
			throws AiravataAPIInvocationException {
		MonitorConfiguration monitorConfiguration;
		try {
			monitorConfiguration = new MonitorConfiguration(
					getClient().getClientConfiguration().getMessagebrokerURL().toURI(), experimentId,
					true, getClient().getClientConfiguration().getMessageboxURL().toURI());
			final Monitor monitor = new Monitor(monitorConfiguration);
			monitor.printRawMessage(false);
			if (listener!=null) {
				monitor.getEventDataRepository().registerEventListener(listener);
				listener.setExperimentMonitor(monitor);
			}
			if (!monitor.getExperimentId().equals(">")){
				monitor.getEventDataRepository().registerEventListener(new EventDataListenerAdapter() {
					@Override
					public void notify(EventDataRepository eventDataRepo, EventData eventData) {
//						if (eventData.getType()==EventType.WORKFLOW_TERMINATED || eventData.getType()==EventType.SENDING_FAULT){
//							monitor.stopMonitoring();
//						}
					}
				});
			}
			return monitor;
		} catch (URISyntaxException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String runExperiment(String workflow, List<WorkflowInput> inputs,
			ExperimentAdvanceOptions options, EventDataListener listener)
			throws AiravataAPIInvocationException {
		return runExperimentGeneral(extractWorkflow(workflow), inputs, options, listener);
	}
	
	public AiravataClient getClient() {
		return client;
	}
	public void setClient(AiravataClient client) {
		this.client = client;
	}

	private String runExperimentGeneral(Workflow workflowObj, List<WorkflowInput> inputs, ExperimentAdvanceOptions options, EventDataListener listener) throws AiravataAPIInvocationException {
//		try {
//			List<NameValue> inputValues = new ArrayList<NameValue>();
//			for (WorkflowInput input : inputs) {
//				NameValue nameValue = new NameValue();
//				nameValue.setName(input.getName());
//				nameValue.setValue(String.valueOf(input.getValue()==null?input.getDefaultValue():input.getValue()));
//				inputValues.add(nameValue);
//			}
//			String workflowString = XMLUtil.xmlElementToString(workflowObj.toXML());
//			List<WSComponentPort> ports = getWSComponentPortInputs(workflowObj);
//			for (WorkflowInput input : inputs) {
//				WSComponentPort port = getWSComponentPort(input.getName(),
//						ports);
//				if (port != null) {
//					port.setValue(input.getValue());
//				}
//			}
//			for (WSComponentPort port : ports) {
//				NameValue nameValue = new NameValue();
//				nameValue.setName(port.getName());
//				if (port.getValue() == null) {
//					nameValue.setValue(port.getDefaultValue());
//				} else {
//					nameValue.setValue(port.getValue().toString());
//				}
//				inputValues.add(nameValue);
//			}
//			String experimentID=options.getCustomExperimentId();
//			String workflowTemplateName = workflowObj.getName();
//			if (experimentID == null || experimentID.isEmpty()) {
//				experimentID = workflowTemplateName + "_" + UUID.randomUUID();
//			}
//	        getClient().getProvenanceManager().setWorkflowInstanceTemplateName(experimentID,workflowTemplateName);
//
//	        //TODO - fix user passing
//	        String submissionUser = getClient().getUserManager().getAiravataUser();
//			String executionUser=options.getExperimentExecutionUser();
//			if (executionUser==null){
//				executionUser=submissionUser;
//			}
//			WorkflowContextHeaderBuilder builder = AiravataAPIUtils.createWorkflowContextHeaderBuilder(options, executionUser, submissionUser);
//			runPreWorkflowExecutionTasks(experimentID, executionUser, options.getExperimentMetadata(), options.getExperimentName());
//			NameValue[] inputVals = inputValues.toArray(new NameValue[] {});
//			if (listener!=null){
//				getExperimentMonitor(experimentID, listener).startMonitoring();
//			}
//			launchWorkflow(experimentID, workflowString, inputVals, builder);
//			return experimentID;
////		}  catch (GraphException e) {
////			throw new AiravataAPIInvocationException(e);
////		} catch (ComponentException e) {
////			throw new AiravataAPIInvocationException(e);
//		} catch (Exception e) {
//	        throw new AiravataAPIInvocationException("Error working with Airavata Registry: " + e.getLocalizedMessage(), e);
//	    }
        return null;
	}

    private Workflow extractWorkflow(String workflowName) throws AiravataAPIInvocationException {
        Workflow workflowObj = null;
        //FIXME - There should be a better way to figure-out if the passed string is a name or an xml
        if(!workflowName.contains("http://airavata.apache.org/xbaya/xwf")){//(getClient().getWorkflowManager().isWorkflowExists(workflowName)) {
            workflowObj = getClient().getWorkflowManager().getWorkflow(workflowName);
        }else {
            try{
                workflowObj = getClient().getWorkflowManager().getWorkflowFromString(workflowName);
            }catch (AiravataAPIInvocationException e){
            	getClient().getWorkflowManager().getWorkflow(workflowName);
            }
        }
        return workflowObj;
    }
    
//	private List<WSComponentPort> getWSComponentPortInputs(Workflow workflow)
//			throws GraphException, ComponentException {
//		workflow.createScript();
//		List<WSComponentPort> inputs = workflow.getInputs();
//		return inputs;
//	}
//
//	private WSComponentPort getWSComponentPort(String name,
//			List<WSComponentPort> ports) {
//		for (WSComponentPort port : ports) {
//			if (port.getName().equals(name)) {
//				return port;
//			}
//		}
//		return null;
//	}
	
	private void launchWorkflow(String experimentId, String workflowGraph, NameValue[] inputs,
			WorkflowContextHeaderBuilder builder) throws AiravataAPIInvocationException {
		try {
			builder.getWorkflowMonitoringContext().setExperimentId(experimentId);
			WorkflowInterpretorStub stub = new WorkflowInterpretorStub(getClient().getAiravataManager().getWorkflowInterpreterServiceURL().toString());
			OMElement wchOMElement = AXIOMUtil.stringToOM(XMLUtil.xmlElementToString(builder
					.getXml()));
			wchOMElement.addAttribute("submissionUser", builder.getSubmissionUser(), wchOMElement.getNamespace());
			stub._getServiceClient().addHeader(
					wchOMElement);
			stub.launchWorkflow(workflowGraph, experimentId, inputs);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void runPreWorkflowExecutionTasks(String experimentId, String user,
			String metadata, String experimentName) throws AiravataAPIInvocationException {
//		if (user != null) {
//			getClient().getProvenanceManager().setExperimentUser(experimentId, user);
//		}
//		if (metadata != null) {
//			getClient().getProvenanceManager().setExperimentMetadata(experimentId, metadata);
//		}
//		if (experimentName == null) {
//			experimentName = experimentId;
//		}
//		getClient().getProvenanceManager().setExperimentName(experimentId, experimentName);

	}

	public static void main(String[] args) {
		ExecutionManagerImpl a = new ExecutionManagerImpl(null);
		try {
			ExperimentAdvanceOptions b = a.createExperimentAdvanceOptions();
			b.getCustomWorkflowOutputDataSettings().addNewOutputDataSettings("la", "di", "da", false);
			WorkflowContextHeaderBuilder c = AiravataAPIUtils.createWorkflowContextHeaderBuilder(b, "sheeeeeeeeeeee","meeee");
			System.out.println(XMLUtil.xmlElementToString(c.getXml()));
		} catch (AiravataAPIInvocationException e) {
			e.printStackTrace();
		}
	}

//	@Override
//	public List<ExperimentExecutionError> getExperimentExecutionErrors(
//			String experimentId) throws AiravataAPIInvocationException {
////		try {
////			return getClient().getRegistryClient().getExperimentExecutionErrors(experimentId);
////		} catch (Exception e) {
////			throw new AiravataAPIInvocationException(e);
////		}
//        return null;
//	}

//	@Override
//	public List<WorkflowExecutionError> getWorkflowExecutionErrors(
//			String experimentId, String workflowInstanceId)
//			throws AiravataAPIInvocationException {
////		try {
////			return getClient().getRegistryClient().getWorkflowExecutionErrors(experimentId,
////					workflowInstanceId);
////		} catch (Exception e) {
////			throw new AiravataAPIInvocationException(e);
////		}
//        return null;
//	}

//	@Override
//	public List<NodeExecutionError> getNodeExecutionErrors(String experimentId,
//			String workflowInstanceId, String nodeId)
//			throws AiravataAPIInvocationException {
////		try {
////			return getClient().getRegistryClient().getNodeExecutionErrors(experimentId,
////					workflowInstanceId, nodeId);
////		} catch (Exception e) {
////			throw new AiravataAPIInvocationException(e);
////		}
//        return null;
//	}

//	@Override
//	public List<ApplicationJobExecutionError> getApplicationJobErrors(String experimentId,
//			String workflowInstanceId, String nodeId, String gfacJobId)
//			throws AiravataAPIInvocationException {
//		try {
//			return getClient().getRegistryClient().getApplicationJobErrors(experimentId,
//					workflowInstanceId, nodeId, gfacJobId);
//		} catch (Exception e) {
//			throw new AiravataAPIInvocationException(e);
//		}
//	}

//	@Override
//	public List<ApplicationJobExecutionError> getApplicationJobErrors(String gfacJobId)
//			throws AiravataAPIInvocationException {
//		try {
//			return getClient().getRegistryClient().getApplicationJobErrors(gfacJobId);
//		} catch (Exception e) {
//			throw new AiravataAPIInvocationException(e);
//		}
//	}

//	@Override
//	public List<ExecutionError> getExecutionErrors(String experimentId,
//			String workflowInstanceId, String nodeId, String gfacJobId,
//			Source... filterBy) throws AiravataAPIInvocationException {
//		try {
//			return getClient().getRegistryClient().getExecutionErrors(experimentId,
//					workflowInstanceId, nodeId, gfacJobId, filterBy);
//		} catch (Exception e) {
//			throw new AiravataAPIInvocationException(e);
//		}
//	}

//	@Override
//	public int addExperimentError(ExperimentExecutionError error)
//			throws AiravataAPIInvocationException {
//		try {
//			return getClient().getRegistryClient().addExperimentError(error);
//		} catch (Exception e) {
//			throw new AiravataAPIInvocationException(e);
//		}
//	}

//	@Override
//	public int addWorkflowExecutionError(WorkflowExecutionError error)
//			throws AiravataAPIInvocationException {
//		try {
//			return getClient().getRegistryClient().addWorkflowExecutionError(error);
//		} catch (Exception e) {
//			throw new AiravataAPIInvocationException(e);
//		}
//	}

//	@Override
//	public int addNodeExecutionError(NodeExecutionError error)
//			throws AiravataAPIInvocationException {
//		try {
//			return getClient().getRegistryClient().addNodeExecutionError(error);
//		} catch (Exception e) {
//			throw new AiravataAPIInvocationException(e);
//		}
//	}

//	@Override
//	public int addApplicationJobExecutionError(ApplicationJobExecutionError error)
//			throws AiravataAPIInvocationException {
//		try {
//			return getClient().getRegistryClient().addApplicationJobExecutionError(error);
//		} catch (Exception e) {
//			throw new AiravataAPIInvocationException(e);
//		}
//	}
}
