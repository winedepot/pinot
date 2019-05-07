/**
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
 */
/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.pinot.common.request;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2019-5-6")
public class HavingFilterQueryMap implements org.apache.thrift.TBase<HavingFilterQueryMap, HavingFilterQueryMap._Fields>, java.io.Serializable, Cloneable, Comparable<HavingFilterQueryMap> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("HavingFilterQueryMap");

  private static final org.apache.thrift.protocol.TField FILTER_QUERY_MAP_FIELD_DESC = new org.apache.thrift.protocol.TField("filterQueryMap", org.apache.thrift.protocol.TType.MAP, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new HavingFilterQueryMapStandardSchemeFactory());
    schemes.put(TupleScheme.class, new HavingFilterQueryMapTupleSchemeFactory());
  }

  private Map<Integer,HavingFilterQuery> filterQueryMap; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    FILTER_QUERY_MAP((short)1, "filterQueryMap");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // FILTER_QUERY_MAP
          return FILTER_QUERY_MAP;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields.FILTER_QUERY_MAP};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.FILTER_QUERY_MAP, new org.apache.thrift.meta_data.FieldMetaData("filterQueryMap", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32), 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, HavingFilterQuery.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(HavingFilterQueryMap.class, metaDataMap);
  }

  public HavingFilterQueryMap() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public HavingFilterQueryMap(HavingFilterQueryMap other) {
    if (other.isSetFilterQueryMap()) {
      Map<Integer,HavingFilterQuery> __this__filterQueryMap = new HashMap<Integer,HavingFilterQuery>(other.filterQueryMap.size());
      for (Map.Entry<Integer, HavingFilterQuery> other_element : other.filterQueryMap.entrySet()) {

        Integer other_element_key = other_element.getKey();
        HavingFilterQuery other_element_value = other_element.getValue();

        Integer __this__filterQueryMap_copy_key = other_element_key;

        HavingFilterQuery __this__filterQueryMap_copy_value = new HavingFilterQuery(other_element_value);

        __this__filterQueryMap.put(__this__filterQueryMap_copy_key, __this__filterQueryMap_copy_value);
      }
      this.filterQueryMap = __this__filterQueryMap;
    }
  }

  public HavingFilterQueryMap deepCopy() {
    return new HavingFilterQueryMap(this);
  }

  @Override
  public void clear() {
    this.filterQueryMap = null;
  }

  public int getFilterQueryMapSize() {
    return (this.filterQueryMap == null) ? 0 : this.filterQueryMap.size();
  }

  public void putToFilterQueryMap(int key, HavingFilterQuery val) {
    if (this.filterQueryMap == null) {
      this.filterQueryMap = new HashMap<Integer,HavingFilterQuery>();
    }
    this.filterQueryMap.put(key, val);
  }

  public Map<Integer,HavingFilterQuery> getFilterQueryMap() {
    return this.filterQueryMap;
  }

  public void setFilterQueryMap(Map<Integer,HavingFilterQuery> filterQueryMap) {
    this.filterQueryMap = filterQueryMap;
  }

  public void unsetFilterQueryMap() {
    this.filterQueryMap = null;
  }

  /** Returns true if field filterQueryMap is set (has been assigned a value) and false otherwise */
  public boolean isSetFilterQueryMap() {
    return this.filterQueryMap != null;
  }

  public void setFilterQueryMapIsSet(boolean value) {
    if (!value) {
      this.filterQueryMap = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case FILTER_QUERY_MAP:
      if (value == null) {
        unsetFilterQueryMap();
      } else {
        setFilterQueryMap((Map<Integer,HavingFilterQuery>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case FILTER_QUERY_MAP:
      return getFilterQueryMap();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case FILTER_QUERY_MAP:
      return isSetFilterQueryMap();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof HavingFilterQueryMap)
      return this.equals((HavingFilterQueryMap)that);
    return false;
  }

  public boolean equals(HavingFilterQueryMap that) {
    if (that == null)
      return false;

    boolean this_present_filterQueryMap = true && this.isSetFilterQueryMap();
    boolean that_present_filterQueryMap = true && that.isSetFilterQueryMap();
    if (this_present_filterQueryMap || that_present_filterQueryMap) {
      if (!(this_present_filterQueryMap && that_present_filterQueryMap))
        return false;
      if (!this.filterQueryMap.equals(that.filterQueryMap))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_filterQueryMap = true && (isSetFilterQueryMap());
    list.add(present_filterQueryMap);
    if (present_filterQueryMap)
      list.add(filterQueryMap);

    return list.hashCode();
  }

  @Override
  public int compareTo(HavingFilterQueryMap other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetFilterQueryMap()).compareTo(other.isSetFilterQueryMap());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFilterQueryMap()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.filterQueryMap, other.filterQueryMap);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("HavingFilterQueryMap(");
    boolean first = true;

    if (isSetFilterQueryMap()) {
      sb.append("filterQueryMap:");
      if (this.filterQueryMap == null) {
        sb.append("null");
      } else {
        sb.append(this.filterQueryMap);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class HavingFilterQueryMapStandardSchemeFactory implements SchemeFactory {
    public HavingFilterQueryMapStandardScheme getScheme() {
      return new HavingFilterQueryMapStandardScheme();
    }
  }

  private static class HavingFilterQueryMapStandardScheme extends StandardScheme<HavingFilterQueryMap> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, HavingFilterQueryMap struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // FILTER_QUERY_MAP
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map42 = iprot.readMapBegin();
                struct.filterQueryMap = new HashMap<Integer,HavingFilterQuery>(2*_map42.size);
                int _key43;
                HavingFilterQuery _val44;
                for (int _i45 = 0; _i45 < _map42.size; ++_i45)
                {
                  _key43 = iprot.readI32();
                  _val44 = new HavingFilterQuery();
                  _val44.read(iprot);
                  struct.filterQueryMap.put(_key43, _val44);
                }
                iprot.readMapEnd();
              }
              struct.setFilterQueryMapIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, HavingFilterQueryMap struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.filterQueryMap != null) {
        if (struct.isSetFilterQueryMap()) {
          oprot.writeFieldBegin(FILTER_QUERY_MAP_FIELD_DESC);
          {
            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.I32, org.apache.thrift.protocol.TType.STRUCT, struct.filterQueryMap.size()));
            for (Map.Entry<Integer, HavingFilterQuery> _iter46 : struct.filterQueryMap.entrySet())
            {
              oprot.writeI32(_iter46.getKey());
              _iter46.getValue().write(oprot);
            }
            oprot.writeMapEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class HavingFilterQueryMapTupleSchemeFactory implements SchemeFactory {
    public HavingFilterQueryMapTupleScheme getScheme() {
      return new HavingFilterQueryMapTupleScheme();
    }
  }

  private static class HavingFilterQueryMapTupleScheme extends TupleScheme<HavingFilterQueryMap> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, HavingFilterQueryMap struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetFilterQueryMap()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetFilterQueryMap()) {
        {
          oprot.writeI32(struct.filterQueryMap.size());
          for (Map.Entry<Integer, HavingFilterQuery> _iter47 : struct.filterQueryMap.entrySet())
          {
            oprot.writeI32(_iter47.getKey());
            _iter47.getValue().write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, HavingFilterQueryMap struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TMap _map48 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.I32, org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.filterQueryMap = new HashMap<Integer,HavingFilterQuery>(2*_map48.size);
          int _key49;
          HavingFilterQuery _val50;
          for (int _i51 = 0; _i51 < _map48.size; ++_i51)
          {
            _key49 = iprot.readI32();
            _val50 = new HavingFilterQuery();
            _val50.read(iprot);
            struct.filterQueryMap.put(_key49, _val50);
          }
        }
        struct.setFilterQueryMapIsSet(true);
      }
    }
  }

}

