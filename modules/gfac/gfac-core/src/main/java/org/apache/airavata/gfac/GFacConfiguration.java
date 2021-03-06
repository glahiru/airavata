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

package org.apache.airavata.gfac;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.airavata.gfac.core.handler.GFacHandlerConfig;
import org.apache.airavata.gfac.core.provider.GFacProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class GFacConfiguration {
    public static final Logger log = LoggerFactory.getLogger(GFacConfiguration.class);


//    private AiravataAPI airavataAPI;

    private static Document handlerDoc;
    // Keep list of full qualified class names of GFac handlers which should invoked before
    // the provider
    private List<GFacHandlerConfig> inHandlers = new ArrayList<GFacHandlerConfig>();

    // Keep list of full qualified class names of GFac handlers which should invoked after
    // the provider
    private List<GFacHandlerConfig> outHandlers = new ArrayList<GFacHandlerConfig>();

    public ExecutionMode executionMode = ExecutionMode.SYNCHRONOUS; // default execution mode is SYNCHRONOUS

    public GFacConfiguration() {
    }

//    public AiravataAPI getAiravataAPI() {
//        return airavataAPI;
//    }


    public List<GFacHandlerConfig> getInHandlers() {
        //This will avoid the misconfiguration done by user in gfac-config.xml
        return removeDuplicateWithOrder(inHandlers);
    }

    public List<GFacHandlerConfig> getOutHandlers() {
        //This will avoid the misconfiguration done by user in gfac-config.xml
        return removeDuplicateWithOrder(outHandlers);
    }
    public void setInHandlers(List<GFacHandlerConfig> inHandlers) {
        this.inHandlers = inHandlers;
    }

    public void setOutHandlers(List<GFacHandlerConfig> outHandlers) {
        this.outHandlers = outHandlers;
    }

    public void setInHandlers(String providerName, String applicationName) {
        try {
            this.inHandlers = getHandlerConfig(handlerDoc, Constants.XPATH_EXPR_GLOBAL_INFLOW_HANDLERS, Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
            if (applicationName != null) {
                String xPath = Constants.XPATH_EXPR_APPLICATION_HANDLERS_START + applicationName + Constants.XPATH_EXPR_APPLICATION_INFLOW_HANDLERS_END;
                List<GFacHandlerConfig> handlers = getHandlerConfig(handlerDoc, xPath, Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
                this.inHandlers.addAll(handlers);
            }
            if (providerName != null) {
                String xPath = Constants.XPATH_EXPR_PROVIDER_HANDLERS_START + providerName + Constants.XPATH_EXPR_PROVIDER_INFLOW_HANDLERS_END;
                List<GFacHandlerConfig> handlers = getHandlerConfig(handlerDoc, xPath, Constants.GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE);
                this.inHandlers.addAll(handlers);
            }
        } catch (XPathExpressionException e) {
            new GFacException("Error parsing Handler Configuration", e);
        }
    }

    public void setOutHandlers(String providerName, String applicationName) {
        try {
            this.outHandlers = getHandlerConfig(handlerDoc, Constants.XPATH_EXPR_GLOBAL_OUTFLOW_HANDLERS, Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
            if (applicationName != null) {
                String xPath = Constants.XPATH_EXPR_APPLICATION_HANDLERS_START + applicationName + Constants.XPATH_EXPR_APPLICATION_OUTFLOW_HANDLERS_END;
                List<GFacHandlerConfig> handlers = getHandlerConfig(handlerDoc, xPath, Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
                this.outHandlers.addAll(handlers);
            }
            if(providerName != null) {
                String xPath = Constants.XPATH_EXPR_PROVIDER_HANDLERS_START + providerName + Constants.XPATH_EXPR_PROVIDER_OUTFLOW_HANDLERS_END;
                List<GFacHandlerConfig> handlers = getHandlerConfig(handlerDoc, xPath, Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
                this.outHandlers.addAll(handlers);
            }
        } catch (XPathExpressionException e) {
            new GFacException("Error parsing Handler Configuration", e);
        }
    }

    /**
     * Parse GFac configuration file and populate GFacConfiguration object. XML configuration
     * file for GFac will look like below.
     * <p/>
     * <GFac>
     * <GlobalHandlers>
     * <InHandlers>
     * <Handler class="org.apache.airavata.gfac.GlobalHandler1">
     * </InHandler>
     * <OutHandlers>
     * <Handler class="org.apache.airavata.gfac.GlabalHandler2">
     * </OutHandlers>
     * </GlobalHandlers>
     * <Provider class="org.apache.airavata.gfac.providers.LocalProvider" host="LocalHost">
     * <InHandlers>
     * <Handler class="org.apache.airavata.gfac.handlers.LocalEvenSetupHandler">
     * </InHandlers>
     * <OutHandlers>
     * <Handler>org.apache.airavata.LocalOutHandler1</Handler>
     * </OutHandlers>
     * </Provider>
     * <Application name="UltraScan">
     * <InHandlers>
     * <Handler class="org.apache.airavata.gfac.handlers.LocalEvenSetupHandler">
     * </InHandlers>
     * <OutHandlers>
     * <Handler class="org.apache.airavata.gfac.LocalOutHandler1">
     * </OutHandlers>
     * </Application>
     * </GFac>
     *
     * @param configFile configuration file
     * @return GFacConfiguration object.
     */
    //FIXME
    public static GFacConfiguration create(File configFile,  Properties configurationProperties) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        handlerDoc = docBuilder.parse(configFile);
        return new GFacConfiguration();
    }

    private static String xpathGetText(Document doc, String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);

        return (String) expr.evaluate(doc, XPathConstants.STRING);
    }

    /**
     * Select matching node set and extract specified attribute value.
     *
     * @param doc        XML document
     * @param expression expression to match node set
     * @param attribute  name of the attribute to extract
     * @return list of attribute values.
     * @throws XPathExpressionException
     */
    public static List<GFacHandlerConfig> getHandlerConfig(Document doc, String expression, String attribute) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);

        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        List<GFacHandlerConfig> gFacHandlerConfigs = new ArrayList<GFacHandlerConfig>();
        Properties properties = new Properties();
        String className = "";
        for (int i = 0; i < nl.getLength(); i++) {
            className = ((Element) nl.item(i)).getAttribute(attribute);
            NodeList childNodes = (nl.item(i)).getChildNodes();
            for(int j = 0;j < childNodes.getLength();j++){
               if(Constants.PROPERTY.equals(childNodes.item(j).getNodeName())) {
                   String name = ((Element) childNodes.item(j)).getAttribute(Constants.NAME);
                   String value = ((Element) childNodes.item(j)).getAttribute(Constants.VALUE);
                   properties.put(name, value);
               }
            }
            GFacHandlerConfig gFacHandlerConfig = new GFacHandlerConfig(properties,className);
            gFacHandlerConfigs.add(gFacHandlerConfig);
        }
        return gFacHandlerConfigs;
    }

    public static List<GFacProviderConfig> getProviderConfig(Document doc, String expression, String attribute) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);

        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        List<GFacProviderConfig> gFacProviderConfigs = new ArrayList<GFacProviderConfig>();
        Map<String, String> properties = new HashMap<String, String>();
        String className = "";
        for (int i = 0; i < nl.getLength(); i++) {
            className = ((Element) nl.item(i)).getAttribute(attribute);
            NodeList childNodes = (nl.item(i)).getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (Constants.PROPERTY.equals(childNodes.item(j).getNodeName())) {
                    String name = ((Element) childNodes.item(j)).getAttribute(Constants.NAME);
                    String value = ((Element) childNodes.item(j)).getAttribute(Constants.VALUE);
                    properties.put(name, value);
                }
            }
            GFacProviderConfig gFacProviderConfig = new GFacProviderConfig(properties,className);
            gFacProviderConfigs.add(gFacProviderConfig);
        }
        return gFacProviderConfigs;
    }

     public static String getAttributeValue(Document doc, String expression, String attribute) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);

        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        String className = null;
        for (int i = 0; i < nl.getLength(); i++) {
            className = ((Element) nl.item(i)).getAttribute(attribute);
            break;
        }
        return className;
    }

    public static GFacConfiguration create(Properties configProps) {
        return null;
    }

    private static List removeDuplicateWithOrder(List arlList) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = arlList.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        arlList.clear();
        arlList.addAll(newList);
        return arlList;
    }
    public static List<GFacHandlerConfig> getDaemonHandlers(File configFile)throws ParserConfigurationException, IOException, SAXException, XPathExpressionException{
       DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        handlerDoc = docBuilder.parse(configFile);
        return getHandlerConfig(handlerDoc, Constants.XPATH_EXPR_DAEMON_HANDLERS, Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
    }
    public static Document getHandlerDoc() {
        return handlerDoc;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }
}
