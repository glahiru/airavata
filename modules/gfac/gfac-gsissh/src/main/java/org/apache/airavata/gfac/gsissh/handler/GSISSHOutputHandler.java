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
package org.apache.airavata.gfac.gsissh.handler;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.handler.AbstractHandler;
import org.apache.airavata.gfac.core.handler.AbstractRecoverableHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.core.utils.OutputUtils;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.gsissh.util.GFACGSISSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskOutputChangeEvent;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GSISSHOutputHandler extends AbstractRecoverableHandler {
    private static final Logger log = LoggerFactory.getLogger(GSISSHOutputHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        super.invoke(jobExecutionContext);
        int index = 0;
        int oldIndex = 0;
        List<String> oldFiles = new ArrayList<String>();
        StringBuffer data = new StringBuffer("|");
        if (jobExecutionContext.getApplicationContext().getHostDescription().getType() instanceof GsisshHostType) { // this is because we don't have the right jobexecution context
            // so attempting to get it from the registry
            if (Constants.PUSH.equals(((GsisshHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType()).getMonitorMode())) {
                log.warn("During the out handler chain jobExecution context came null, so trying to handler");
                ApplicationDescription applicationDeploymentDescription = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
                TaskDetails taskData = null;
                try {
                    taskData = (TaskDetails) jobExecutionContext.getRegistry().get(RegistryModelType.TASK_DETAIL, jobExecutionContext.getTaskData().getTaskID());
                } catch (RegistryException e) {
                    log.error("Error retrieving job details from Registry");
                    throw new GFacHandlerException("Error retrieving job details from Registry", e);
                }
                JobDetails jobDetails = taskData.getJobDetailsList().get(0);
                String jobDescription = jobDetails.getJobDescription();
                if (jobDescription != null) {
                    JobDescriptor jobDescriptor = null;
                    try {
                        jobDescriptor = JobDescriptor.fromXML(jobDescription);
                    } catch (XmlException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    applicationDeploymentDescription.getType().setScratchWorkingDirectory(
                            jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getWorkingDirectory());
                    applicationDeploymentDescription.getType().setInputDataDirectory(jobDescriptor.getInputDirectory());
                    applicationDeploymentDescription.getType().setOutputDataDirectory(jobDescriptor.getOutputDirectory());
                    applicationDeploymentDescription.getType().setStandardError(jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getStandardErrorFile());
                    applicationDeploymentDescription.getType().setStandardOutput(jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getStandardOutFile());
                }
            }
        }
        try {
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) == null) {

                GFACGSISSHUtils.addSecurityContext(jobExecutionContext);
            }
        }  catch (Exception e) {
        	 try {
  				GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
  			} catch (GFacException e1) {
  				 log.error(e1.getLocalizedMessage());
  			}  
            log.error(e.getMessage());
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        }
        DataTransferDetails detail = new DataTransferDetails();
        TransferStatus status = new TransferStatus();

        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext()
                .getApplicationDeploymentDescription().getType();
        Cluster cluster = null;
        
        try {
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) != null) {
                cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            } else {
                cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            }
            if (cluster == null) {
                GFacUtils.saveErrorDetails(jobExecutionContext, "Security context is not set properly", CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.FILE_SYSTEM_FAILURE);
                
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }

            // Get the Stdouts and StdErrs
            String pluginData = GFacUtils.getPluginData(jobExecutionContext, this.getClass().getName());
            if (pluginData != null) {
                try {
                    oldIndex = Integer.parseInt(pluginData.split("\\|")[0].trim());
                    oldFiles = Arrays.asList(pluginData.split("\\|")[1].split(","));
                    if (oldIndex == oldFiles.size()) {
                        log.info("Old data looks good !!!!");
                    } else {
                        oldIndex = 0;
                        oldFiles.clear();
                    }
                } catch (NumberFormatException e) {
                    log.error("Previously stored data " + pluginData + " is wrong so we continue the operations");
                }
            }

            String timeStampedExperimentID = GFacUtils.createUniqueNameWithDate(jobExecutionContext.getExperimentID());

            TaskDetails taskData = jobExecutionContext.getTaskData();
            String outputDataDir = null;
            File localStdOutFile;
            File localStdErrFile;
            //FIXME: AdvancedOutput is remote location and third party transfer should work to make this work 
//            if (taskData.getAdvancedOutputDataHandling() != null) {
//                outputDataDir = taskData.getAdvancedOutputDataHandling().getOutputDataDir();
//            }
            if (outputDataDir == null) {
                outputDataDir = File.separator + "tmp";
            }
            outputDataDir = outputDataDir + File.separator + jobExecutionContext.getExperimentID() + "-" + jobExecutionContext.getTaskData().getTaskID();
            (new File(outputDataDir)).mkdirs();
         	
            String stdOutStr = "";
            if (index < oldIndex) {
                localStdOutFile = new File(oldFiles.get(index));
                data.append(oldFiles.get(index++)).append(",");
            } else {
            	int i = 0;
                localStdOutFile = new File(outputDataDir + File.separator + timeStampedExperimentID + "stdout");
                while(stdOutStr.isEmpty()){
                try {
                	cluster.scpFrom(app.getStandardOutput(), localStdOutFile.getAbsolutePath());
                	stdOutStr = GFacUtils.readFileToString(localStdOutFile.getAbsolutePath());
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
					  Thread.sleep(2000);
		        }
                i++;
                if(i==3)break;
                }
                
                StringBuffer temp = new StringBuffer(data.append(localStdOutFile.getAbsolutePath()).append(",").toString());
                GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
            }
            if (index < oldIndex) {
                localStdErrFile = new File(oldFiles.get(index));
                data.append(oldFiles.get(index++)).append(",");
            } else {
                localStdErrFile = new File(outputDataDir + File.separator + timeStampedExperimentID + "stderr");
                cluster.scpFrom(app.getStandardError(), localStdErrFile.getAbsolutePath());
                StringBuffer temp = new StringBuffer(data.append(localStdErrFile.getAbsolutePath()).append(",").toString());
                GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
            }

            String stdErrStr = GFacUtils.readFileToString(localStdErrFile.getAbsolutePath());
            status.setTransferState(TransferState.STDOUT_DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDOUT:" + stdOutStr);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());

            status.setTransferState(TransferState.STDERROR_DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDERR:" + stdErrStr);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());

            //todo this is a mess we have to fix this
            List<DataObjectType> outputArray = new ArrayList<DataObjectType>();
            Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
            Set<String> keys = output.keySet();
            for (String paramName : keys) {
                ActualParameter actualParameter = (ActualParameter) output.get(paramName);
                if ("URI".equals(actualParameter.getType().getType().toString())) {

                    List<String> outputList = null;
                    int retry=3;
                    while(retry>0){
                    	 outputList = cluster.listDirectory(app.getOutputDataDirectory());
                        if (outputList.size() == 1 && outputList.get(0).isEmpty()) {
                            Thread.sleep(10000);
                        } else if (outputList.size() > 0) {
                            break;
                        }else{
                            Thread.sleep(10000);
                        }
                        retry--;
                        if(retry==0){
//                            log.info("Ohhhhhhh shitttttttOhhhhhhh shitttttttOhhhhhhh shitttttttOhhhhhhh shitttttttOhhhhhhh shitttttttOhhhhhhh shittttttt");
                        }
                    	 Thread.sleep(10000);
                    }
                    if (outputList.size() == 0 || outputList.get(0).isEmpty() || outputList.size() > 1) {
                        OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr, outputArray);
                        Set<String> strings = output.keySet();
                        outputArray.clear();
                        for (String key : strings) {
                            ActualParameter actualParameter1 = (ActualParameter) output.get(key);
                            if ("URI".equals(actualParameter1.getType().getType().toString())) {
                                String downloadFile = MappingFactory.toString(actualParameter1);
                                String localFile;
                                if (index < oldIndex) {
                                    localFile = oldFiles.get(index);
                                    data.append(oldFiles.get(index++)).append(",");
                                } else {
                                    cluster.scpFrom(downloadFile, outputDataDir);
                                    String fileName = downloadFile.substring(downloadFile.lastIndexOf(File.separatorChar) + 1, downloadFile.length());
                                    localFile = outputDataDir + File.separator + fileName;
                                    StringBuffer temp = new StringBuffer(data.append(localFile).append(",").toString());
                                    GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
                                }
                                jobExecutionContext.addOutputFile(localFile);
                                MappingFactory.fromString(actualParameter1, localFile);
                                DataObjectType dataObjectType = new DataObjectType();
                                dataObjectType.setValue(localFile);
                                dataObjectType.setKey(key);
                                dataObjectType.setType(DataType.URI);
                                outputArray.add(dataObjectType);
                            }
                        }
                        break;
                    } else if(outputList.size() == 1) { //FIXME: this is ultrascan specific
                        String valueList = outputList.get(0);
                        String outputFile;
                        if (index < oldIndex) {
                            outputFile = oldFiles.get(index);
                            data.append(oldFiles.get(index++)).append(",");
                        } else {
                            cluster.scpFrom(app.getOutputDataDirectory() + File.separator + valueList, outputDataDir);
                            outputFile = outputDataDir + File.separator + valueList;
                            jobExecutionContext.addOutputFile(outputFile);
                            StringBuffer temp = new StringBuffer(data.append(outputFile).append(",").toString());
                            GFacUtils.savePluginData(jobExecutionContext, temp.insert(0, ++index), this.getClass().getName());
                        }
                        jobExecutionContext.addOutputFile(outputFile);
                        MappingFactory.fromString(actualParameter, outputFile);
                        DataObjectType dataObjectType = new DataObjectType();
                        dataObjectType.setValue(valueList);
                        dataObjectType.setKey(paramName);
                        dataObjectType.setType(DataType.URI);
                        outputArray.add(dataObjectType);
                    }
                } else {
                    OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr, outputArray);
                    break;
                }
            }
            if (outputArray == null || outputArray.isEmpty()) {
                if(jobExecutionContext.getTaskData().getAdvancedOutputDataHandling() == null){
                throw new GFacHandlerException(
                        "Empty Output returned from the Application, Double check the application"
                                + "and ApplicationDescriptor output Parameter Names"
                );
                }
            }
            app.setStandardError(localStdErrFile.getAbsolutePath());
            app.setStandardOutput(localStdOutFile.getAbsolutePath());
            app.setOutputDataDirectory(outputDataDir);
            status.setTransferState(TransferState.DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription(outputDataDir);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
            registry.add(ChildDataType.EXPERIMENT_OUTPUT, outputArray, jobExecutionContext.getExperimentID());
            fireTaskOutputChangeEvent(jobExecutionContext, outputArray);
        } catch (Exception e) {
            try {
                status.setTransferState(TransferState.FAILED);
                detail.setTransferStatus(status);
                detail.setTransferDescription(e.getLocalizedMessage());
                registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
                GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.FILE_SYSTEM_FAILURE);
            } catch (Exception e1) {
                throw new GFacHandlerException("Error persisting status", e1, e1.getLocalizedMessage());
            }
            throw new GFacHandlerException("Error in retrieving results", e);
        }
     }

    public void initProperties(Properties properties) throws GFacHandlerException {

    }

    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        this.invoke(jobExecutionContext);
    }
}
