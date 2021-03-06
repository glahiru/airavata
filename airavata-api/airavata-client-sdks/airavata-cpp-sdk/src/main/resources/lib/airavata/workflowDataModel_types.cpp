/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#include "workflowDataModel_types.h"

#include <algorithm>



const char* Workflow::ascii_fingerprint = "0F4DE03295CE4C20055DE0E68CFA7A65";
const uint8_t Workflow::binary_fingerprint[16] = {0x0F,0x4D,0xE0,0x32,0x95,0xCE,0x4C,0x20,0x05,0x5D,0xE0,0xE6,0x8C,0xFA,0x7A,0x65};

uint32_t Workflow::read(::apache::thrift::protocol::TProtocol* iprot) {

  uint32_t xfer = 0;
  std::string fname;
  ::apache::thrift::protocol::TType ftype;
  int16_t fid;

  xfer += iprot->readStructBegin(fname);

  using ::apache::thrift::protocol::TProtocolException;

  bool isset_templateId = false;
  bool isset_name = false;

  while (true)
  {
    xfer += iprot->readFieldBegin(fname, ftype, fid);
    if (ftype == ::apache::thrift::protocol::T_STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->templateId);
          isset_templateId = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 2:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->name);
          isset_name = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 3:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readString(this->graph);
          this->__isset.graph = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 4:
        if (ftype == ::apache::thrift::protocol::T_STRING) {
          xfer += iprot->readBinary(this->image);
          this->__isset.image = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 5:
        if (ftype == ::apache::thrift::protocol::T_LIST) {
          {
            this->workflowInputs.clear();
            uint32_t _size0;
            ::apache::thrift::protocol::TType _etype3;
            xfer += iprot->readListBegin(_etype3, _size0);
            this->workflowInputs.resize(_size0);
            uint32_t _i4;
            for (_i4 = 0; _i4 < _size0; ++_i4)
            {
              xfer += this->workflowInputs[_i4].read(iprot);
            }
            xfer += iprot->readListEnd();
          }
          this->__isset.workflowInputs = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      case 6:
        if (ftype == ::apache::thrift::protocol::T_LIST) {
          {
            this->workflowOutputs.clear();
            uint32_t _size5;
            ::apache::thrift::protocol::TType _etype8;
            xfer += iprot->readListBegin(_etype8, _size5);
            this->workflowOutputs.resize(_size5);
            uint32_t _i9;
            for (_i9 = 0; _i9 < _size5; ++_i9)
            {
              xfer += this->workflowOutputs[_i9].read(iprot);
            }
            xfer += iprot->readListEnd();
          }
          this->__isset.workflowOutputs = true;
        } else {
          xfer += iprot->skip(ftype);
        }
        break;
      default:
        xfer += iprot->skip(ftype);
        break;
    }
    xfer += iprot->readFieldEnd();
  }

  xfer += iprot->readStructEnd();

  if (!isset_templateId)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  if (!isset_name)
    throw TProtocolException(TProtocolException::INVALID_DATA);
  return xfer;
}

uint32_t Workflow::write(::apache::thrift::protocol::TProtocol* oprot) const {
  uint32_t xfer = 0;
  xfer += oprot->writeStructBegin("Workflow");

  xfer += oprot->writeFieldBegin("templateId", ::apache::thrift::protocol::T_STRING, 1);
  xfer += oprot->writeString(this->templateId);
  xfer += oprot->writeFieldEnd();

  xfer += oprot->writeFieldBegin("name", ::apache::thrift::protocol::T_STRING, 2);
  xfer += oprot->writeString(this->name);
  xfer += oprot->writeFieldEnd();

  if (this->__isset.graph) {
    xfer += oprot->writeFieldBegin("graph", ::apache::thrift::protocol::T_STRING, 3);
    xfer += oprot->writeString(this->graph);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.image) {
    xfer += oprot->writeFieldBegin("image", ::apache::thrift::protocol::T_STRING, 4);
    xfer += oprot->writeBinary(this->image);
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.workflowInputs) {
    xfer += oprot->writeFieldBegin("workflowInputs", ::apache::thrift::protocol::T_LIST, 5);
    {
      xfer += oprot->writeListBegin(::apache::thrift::protocol::T_STRUCT, static_cast<uint32_t>(this->workflowInputs.size()));
      std::vector< ::apache::airavata::model::appcatalog::appinterface::InputDataObjectType> ::const_iterator _iter10;
      for (_iter10 = this->workflowInputs.begin(); _iter10 != this->workflowInputs.end(); ++_iter10)
      {
        xfer += (*_iter10).write(oprot);
      }
      xfer += oprot->writeListEnd();
    }
    xfer += oprot->writeFieldEnd();
  }
  if (this->__isset.workflowOutputs) {
    xfer += oprot->writeFieldBegin("workflowOutputs", ::apache::thrift::protocol::T_LIST, 6);
    {
      xfer += oprot->writeListBegin(::apache::thrift::protocol::T_STRUCT, static_cast<uint32_t>(this->workflowOutputs.size()));
      std::vector< ::apache::airavata::model::appcatalog::appinterface::OutputDataObjectType> ::const_iterator _iter11;
      for (_iter11 = this->workflowOutputs.begin(); _iter11 != this->workflowOutputs.end(); ++_iter11)
      {
        xfer += (*_iter11).write(oprot);
      }
      xfer += oprot->writeListEnd();
    }
    xfer += oprot->writeFieldEnd();
  }
  xfer += oprot->writeFieldStop();
  xfer += oprot->writeStructEnd();
  return xfer;
}

void swap(Workflow &a, Workflow &b) {
  using ::std::swap;
  swap(a.templateId, b.templateId);
  swap(a.name, b.name);
  swap(a.graph, b.graph);
  swap(a.image, b.image);
  swap(a.workflowInputs, b.workflowInputs);
  swap(a.workflowOutputs, b.workflowOutputs);
  swap(a.__isset, b.__isset);
}


