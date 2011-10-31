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

package org.apache.airavata.xbaya.component.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentRegistryLoader implements Cancelable, Observer {

    private static final Logger logger = LoggerFactory.getLogger(ComponentRegistryLoader.class);

    private XBayaEngine engine;

    private Thread loadThread;

    private boolean canceled;

    private WaitDialog loadingDialog;
    
    private ComponentTreeNode componentTree;

    private Observable observableRegistry;
    
    private Map<String,ComponentTreeNode> componentTreeNodesMap;
    
    /**
     * Constructs a WorkflowLoader.
     * 
     * @param engine
     */
    public ComponentRegistryLoader(XBayaEngine engine) {
        this.setEngine(engine);

        this.loadingDialog = new WaitDialog(this, "Loading a Component List.", "Loading a Component List. "
                + "Please wait for a moment.", this.getEngine());
        getEngine().getConfiguration().addObserver(this);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.loadThread.interrupt();
    }

    /**
     * Loads the workflow.
     * 
     * @param registry
     * 
     */
    public void load(final ComponentRegistry registry) {
        this.canceled = false;
        
        this.loadThread = new Thread() {
            @Override
            public void run() {
                runInThread(registry);
            }
        };
        this.loadThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.loadingDialog.show();
    }

    /**
     * @param registry
     */
    private void runInThread(ComponentRegistry registry) {
        try {
        	if (getComponentTreeNodesMap().containsKey(registry.getName())){
        		this.getEngine().getGUI().getComponentSelector().removeComponentTree(getComponentTreeNodesMap().get(registry.getName()));
        		getComponentTreeNodesMap().remove(registry.getName());
        	}
            componentTree = registry.getComponentTree();
            if (this.canceled) {
                return;
            }
            this.getEngine().getGUI().getComponentSelector().addComponentTree(componentTree);
            getComponentTreeNodesMap().put(registry.getName(),componentTree);
            this.loadingDialog.hide();
        } catch (ComponentRegistryException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.getEngine().getErrorWindow().error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.getEngine().getErrorWindow().error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (Error e) {
            this.getEngine().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.loadingDialog.hide();
        }
    }

	@Override
	public void update(Observable observable, Object o) {
		if ((observable instanceof XBayaConfiguration) && (o instanceof ComponentRegistry)){
			ComponentRegistry componentRegistry=(ComponentRegistry)o;
			if (observableRegistry!=null){
				observableRegistry.deleteObserver(this);
			}
			if (componentRegistry instanceof JCRComponentRegistry){
				Registry registry = ((JCRComponentRegistry)componentRegistry).getRegistry();
				if (registry!=null && registry instanceof Observable){
					(observableRegistry=(Observable)registry).addObserver(this);
				}
			}
			load(componentRegistry);
		}else if (observable instanceof Registry){
			load(getEngine().getConfiguration().getJcrComponentRegistry());
		}
	}
	
	public XBayaEngine getEngine() {
		return engine;
	}

	public void setEngine(XBayaEngine engine) {
		this.engine = engine;
	}

	public Map<String,ComponentTreeNode> getComponentTreeNodesMap() {
		if (componentTreeNodesMap==null){
			componentTreeNodesMap=new HashMap<String, ComponentTreeNode>();
		}
		return componentTreeNodesMap;
	}

}