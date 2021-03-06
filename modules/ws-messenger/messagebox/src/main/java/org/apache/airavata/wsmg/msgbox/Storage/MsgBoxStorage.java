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

package org.apache.airavata.wsmg.msgbox.Storage;

import java.util.List;

import org.apache.axiom.om.OMElement;

/**
 * Message Box storage backend. This has implemented in two ways in-memory and database.
 */
public interface MsgBoxStorage {
    public String createMsgBox() throws Exception;

    public void destroyMsgBox(String key) throws Exception;

    /**
     * IMPORTANT::: List retrieved from this method is sorted by time in ascending order i.e the newest message will
     * appear as the last item in the list.
     * 
     * @param key
     * @return
     * @throws Exception
     */
    public List<String> takeMessagesFromMsgBox(String key) throws Exception;

    public void putMessageIntoMsgBox(String msgBoxID, String messageID, String soapAction, OMElement message)
            throws Exception;

    /**
     * The ancientness is defined in the db.config file.
     */
    public void removeAncientMessages() throws Exception;

    /**
     * Clean up method
     */
    public void dispose();

}
