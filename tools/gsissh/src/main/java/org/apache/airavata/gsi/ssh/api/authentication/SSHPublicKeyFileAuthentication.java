package org.apache.airavata.gsi.ssh.api.authentication;/*
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


/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/4/13
 * Time: 9:52 AM
 */

/**
 * Public key authentication for vanilla SSH.
 * The public key and private key stored files are returned. API user should implement this.
 */
public interface SSHPublicKeyFileAuthentication extends SSHKeyAuthentication {

    /**
     * The file which contains the public key.
     * @param userName The user who is trying to SSH
     * @param hostName The host which user wants to connect to.
     * @return The name of the file which contains the public key.
     */
    String getPublicKeyFile(String userName, String hostName);

    /**
     * The file which contains the public key.
     * @param userName The user who is trying to SSH
     * @param hostName The host which user wants to connect to.
     * @return The name of the file which contains the private key.
     */
    String getPrivateKeyFile(String userName, String hostName);


}
