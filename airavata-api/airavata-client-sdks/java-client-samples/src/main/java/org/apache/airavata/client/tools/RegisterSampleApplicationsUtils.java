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

package org.apache.airavata.client.tools;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegisterSampleApplicationsUtils {


    public static ComputeResourcePreference
        createComputeResourcePreference(String computeResourceId, String allocationProjectNumber,
                                        boolean overridebyAiravata, String preferredBatchQueue,
                                        String preferredJobSubmissionProtocol,String preferredDataMovementProtocol,
                                        String scratchLocation) {
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setOverridebyAiravata(overridebyAiravata);
        computeResourcePreference.setAllocationProjectNumber(allocationProjectNumber);
        computeResourcePreference.setPreferredBatchQueue(preferredBatchQueue);
        computeResourcePreference.setPreferredDataMovementProtocol(preferredDataMovementProtocol);
        computeResourcePreference.setPreferredJobSubmissionProtocol(preferredJobSubmissionProtocol);
        computeResourcePreference.setScratchLocation(scratchLocation);
        return computeResourcePreference;
    }

    public static ApplicationDeploymentDescription createApplicationDeployment(
           String appModuleId, String computeResourceId, String executablePath,
           ApplicationParallelismType parallelism, String appDeploymentDescription) {
        ApplicationDeploymentDescription deployment = new ApplicationDeploymentDescription();
//		deployment.setIsEmpty(false);
        deployment.setAppDeploymentDescription(appDeploymentDescription);
        deployment.setAppModuleId(appModuleId);
        deployment.setComputeHostId(computeResourceId);
        deployment.setExecutablePath(executablePath);
        deployment.setParallelism(parallelism);
        return deployment;
    }

    public static ApplicationModule createApplicationModule(String appModuleName,
                                                            String appModuleVersion, String appModuleDescription) {
        ApplicationModule module = new ApplicationModule();
        module.setAppModuleDescription(appModuleDescription);
        module.setAppModuleName(appModuleName);
        module.setAppModuleVersion(appModuleVersion);
        return module;
    }

    public static DataMovementInterface createDataMovementInterface(
            String dataMovementInterfaceId,
            DataMovementProtocol dataMovementProtocolType, int priorityOrder) {
        DataMovementInterface dataMovementInterface = new DataMovementInterface();
        dataMovementInterface.setDataMovementInterfaceId(dataMovementInterfaceId);
        dataMovementInterface.setDataMovementProtocol(dataMovementProtocolType);
        dataMovementInterface.setPriorityOrder(priorityOrder);
        return dataMovementInterface;
    }

    public static JobSubmissionInterface createJobSubmissionInterface(
            String jobSubmissionInterfaceId,
            JobSubmissionProtocol jobSubmissionProtocolType, int priorityOrder) {
        JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
        jobSubmissionInterface.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
        jobSubmissionInterface.setJobSubmissionProtocol(jobSubmissionProtocolType);
        jobSubmissionInterface.setPriorityOrder(priorityOrder);
        return jobSubmissionInterface;
    }

    public static ComputeResourceDescription createComputeResourceDescription(
            String hostName, String hostDesc, Set<String> hostAliases, Set<String> ipAddresses) {
        ComputeResourceDescription host = new ComputeResourceDescription();
        host.setHostName(hostName);
        host.setResourceDescription(hostDesc);
        host.setIpAddresses(ipAddresses);
        host.setHostAliases(hostAliases);
        return host;
    }

    public static ResourceJobManager createResourceJobManager(
            ResourceJobManagerType resourceJobManagerType, String pushMonitoringEndpoint, String jobManagerBinPath,
            Map<JobManagerCommand, String> jobManagerCommands) {
        ResourceJobManager resourceJobManager = new ResourceJobManager();
        resourceJobManager.setResourceJobManagerType(resourceJobManagerType);
        resourceJobManager.setPushMonitoringEndpoint(pushMonitoringEndpoint);
        resourceJobManager.setJobManagerBinPath(jobManagerBinPath);
        resourceJobManager.setJobManagerCommands(jobManagerCommands);
        return resourceJobManager;
    }

    public static InputDataObjectType createAppInput
            (String inputName, String value, DataType type,
             String applicationArgument, boolean stdIn, String description, String metadata) {
        InputDataObjectType input = new InputDataObjectType();
//        input.setIsEmpty(false);
        if (inputName != null) input.setName(inputName);
        if (value != null) input.setValue(value);
        if (type != null) input.setType(type);
        if (applicationArgument != null) input.setApplicationArgument(applicationArgument);
        if (description != null) input.setUserFriendlyDescription(description);
        input.setStandardInput(stdIn);
        if (metadata != null) input.setMetaData(metadata);
        return input;
    }

    public static OutputDataObjectType createAppOutput(String inputName, String value, DataType type) {
        OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
//        outputDataObjectType.setIsEmpty(false);
        if (inputName != null) outputDataObjectType.setName(inputName);
        if (value != null) outputDataObjectType.setValue(value);
        if (type != null) outputDataObjectType.setType(type);
        return outputDataObjectType;
    }

    public static ApplicationInterfaceDescription createApplicationInterfaceDescription
            (String applicationName, String applicationDesription, List<String> applicationModules,
            List<InputDataObjectType> applicationInputs, List<OutputDataObjectType>applicationOutputs) {
        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();

        applicationInterfaceDescription.setApplicationName(applicationName);
        if (applicationDesription != null) applicationInterfaceDescription.setApplicationDesription(applicationDesription);
        if (applicationModules != null) applicationInterfaceDescription.setApplicationModules(applicationModules);
        if (applicationInputs != null) applicationInterfaceDescription.setApplicationInputs(applicationInputs);
        if (applicationOutputs != null) applicationInterfaceDescription.setApplicationOutputs(applicationOutputs);

        return applicationInterfaceDescription;
    }

}