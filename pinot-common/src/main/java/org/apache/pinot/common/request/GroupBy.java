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
/**
 * AUTO GENERATED: DO NOT EDIT
 * GroupBy
 * 
 */
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2019-5-6")
public class GroupBy implements org.apache.thrift.TBase<GroupBy, GroupBy._Fields>, java.io.Serializable, Cloneable, Comparable<GroupBy> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("GroupBy");

  private static final org.apache.thrift.protocol.TField COLUMNS_FIELD_DESC = new org.apache.thrift.protocol.TField("columns", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField TOP_N_FIELD_DESC = new org.apache.thrift.protocol.TField("topN", org.apache.thrift.protocol.TType.I64, (short)2);
  private static final org.apache.thrift.protocol.TField EXPRESSIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("expressions", org.apache.thrift.protocol.TType.LIST, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new GroupByStandardSchemeFactory());
    schemes.put(TupleScheme.class, new GroupByTupleSchemeFactory());
  }

  private List<String> columns; // optional
  private long topN; // optional
  private List<String> expressions; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    COLUMNS((short)1, "columns"),
    TOP_N((short)2, "topN"),
    EXPRESSIONS((short)3, "expressions");

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
        case 1: // COLUMNS
          return COLUMNS;
        case 2: // TOP_N
          return TOP_N;
        case 3: // EXPRESSIONS
          return EXPRESSIONS;
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
  private static final int __TOPN_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.COLUMNS,_Fields.TOP_N,_Fields.EXPRESSIONS};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.COLUMNS, new org.apache.thrift.meta_data.FieldMetaData("columns", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.TOP_N, new org.apache.thrift.meta_data.FieldMetaData("topN", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.EXPRESSIONS, new org.apache.thrift.meta_data.FieldMetaData("expressions", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(GroupBy.class, metaDataMap);
  }

  public GroupBy() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public GroupBy(GroupBy other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetColumns()) {
      List<String> __this__columns = new ArrayList<String>(other.columns);
      this.columns = __this__columns;
    }
    this.topN = other.topN;
    if (other.isSetExpressions()) {
      List<String> __this__expressions = new ArrayList<String>(other.expressions);
      this.expressions = __this__expressions;
    }
  }

  public GroupBy deepCopy() {
    return new GroupBy(this);
  }

  @Override
  public void clear() {
    this.columns = null;
    setTopNIsSet(false);
    this.topN = 0;
    this.expressions = null;
  }

  public int getColumnsSize() {
    return (this.columns == null) ? 0 : this.columns.size();
  }

  public java.util.Iterator<String> getColumnsIterator() {
    return (this.columns == null) ? null : this.columns.iterator();
  }

  public void addToColumns(String elem) {
    if (this.columns == null) {
      this.columns = new ArrayList<String>();
    }
    this.columns.add(elem);
  }

  public List<String> getColumns() {
    return this.columns;
  }

  public void setColumns(List<String> columns) {
    this.columns = columns;
  }

  public void unsetColumns() {
    this.columns = null;
  }

  /** Returns true if field columns is set (has been assigned a value) and false otherwise */
  public boolean isSetColumns() {
    return this.columns != null;
  }

  public void setColumnsIsSet(boolean value) {
    if (!value) {
      this.columns = null;
    }
  }

  public long getTopN() {
    return this.topN;
  }

  public void setTopN(long topN) {
    this.topN = topN;
    setTopNIsSet(true);
  }

  public void unsetTopN() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TOPN_ISSET_ID);
  }

  /** Returns true if field topN is set (has been assigned a value) and false otherwise */
  public boolean isSetTopN() {
    return EncodingUtils.testBit(__isset_bitfield, __TOPN_ISSET_ID);
  }

  public void setTopNIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TOPN_ISSET_ID, value);
  }

  public int getExpressionsSize() {
    return (this.expressions == null) ? 0 : this.expressions.size();
  }

  public java.util.Iterator<String> getExpressionsIterator() {
    return (this.expressions == null) ? null : this.expressions.iterator();
  }

  public void addToExpressions(String elem) {
    if (this.expressions == null) {
      this.expressions = new ArrayList<String>();
    }
    this.expressions.add(elem);
  }

  public List<String> getExpressions() {
    return this.expressions;
  }

  public void setExpressions(List<String> expressions) {
    this.expressions = expressions;
  }

  public void unsetExpressions() {
    this.expressions = null;
  }

  /** Returns true if field expressions is set (has been assigned a value) and false otherwise */
  public boolean isSetExpressions() {
    return this.expressions != null;
  }

  public void setExpressionsIsSet(boolean value) {
    if (!value) {
      this.expressions = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case COLUMNS:
      if (value == null) {
        unsetColumns();
      } else {
        setColumns((List<String>)value);
      }
      break;

    case TOP_N:
      if (value == null) {
        unsetTopN();
      } else {
        setTopN((Long)value);
      }
      break;

    case EXPRESSIONS:
      if (value == null) {
        unsetExpressions();
      } else {
        setExpressions((List<String>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case COLUMNS:
      return getColumns();

    case TOP_N:
      return Long.valueOf(getTopN());

    case EXPRESSIONS:
      return getExpressions();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case COLUMNS:
      return isSetColumns();
    case TOP_N:
      return isSetTopN();
    case EXPRESSIONS:
      return isSetExpressions();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof GroupBy)
      return this.equals((GroupBy)that);
    return false;
  }

  public boolean equals(GroupBy that) {
    if (that == null)
      return false;

    boolean this_present_columns = true && this.isSetColumns();
    boolean that_present_columns = true && that.isSetColumns();
    if (this_present_columns || that_present_columns) {
      if (!(this_present_columns && that_present_columns))
        return false;
      if (!this.columns.equals(that.columns))
        return false;
    }

    boolean this_present_topN = true && this.isSetTopN();
    boolean that_present_topN = true && that.isSetTopN();
    if (this_present_topN || that_present_topN) {
      if (!(this_present_topN && that_present_topN))
        return false;
      if (this.topN != that.topN)
        return false;
    }

    boolean this_present_expressions = true && this.isSetExpressions();
    boolean that_present_expressions = true && that.isSetExpressions();
    if (this_present_expressions || that_present_expressions) {
      if (!(this_present_expressions && that_present_expressions))
        return false;
      if (!this.expressions.equals(that.expressions))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_columns = true && (isSetColumns());
    list.add(present_columns);
    if (present_columns)
      list.add(columns);

    boolean present_topN = true && (isSetTopN());
    list.add(present_topN);
    if (present_topN)
      list.add(topN);

    boolean present_expressions = true && (isSetExpressions());
    list.add(present_expressions);
    if (present_expressions)
      list.add(expressions);

    return list.hashCode();
  }

  @Override
  public int compareTo(GroupBy other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetColumns()).compareTo(other.isSetColumns());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumns()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.columns, other.columns);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTopN()).compareTo(other.isSetTopN());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTopN()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.topN, other.topN);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetExpressions()).compareTo(other.isSetExpressions());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExpressions()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.expressions, other.expressions);
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
    StringBuilder sb = new StringBuilder("GroupBy(");
    boolean first = true;

    if (isSetColumns()) {
      sb.append("columns:");
      if (this.columns == null) {
        sb.append("null");
      } else {
        sb.append(this.columns);
      }
      first = false;
    }
    if (isSetTopN()) {
      if (!first) sb.append(", ");
      sb.append("topN:");
      sb.append(this.topN);
      first = false;
    }
    if (isSetExpressions()) {
      if (!first) sb.append(", ");
      sb.append("expressions:");
      if (this.expressions == null) {
        sb.append("null");
      } else {
        sb.append(this.expressions);
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class GroupByStandardSchemeFactory implements SchemeFactory {
    public GroupByStandardScheme getScheme() {
      return new GroupByStandardScheme();
    }
  }

  private static class GroupByStandardScheme extends StandardScheme<GroupBy> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, GroupBy struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // COLUMNS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list62 = iprot.readListBegin();
                struct.columns = new ArrayList<String>(_list62.size);
                String _elem63;
                for (int _i64 = 0; _i64 < _list62.size; ++_i64)
                {
                  _elem63 = iprot.readString();
                  struct.columns.add(_elem63);
                }
                iprot.readListEnd();
              }
              struct.setColumnsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // TOP_N
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.topN = iprot.readI64();
              struct.setTopNIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // EXPRESSIONS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list65 = iprot.readListBegin();
                struct.expressions = new ArrayList<String>(_list65.size);
                String _elem66;
                for (int _i67 = 0; _i67 < _list65.size; ++_i67)
                {
                  _elem66 = iprot.readString();
                  struct.expressions.add(_elem66);
                }
                iprot.readListEnd();
              }
              struct.setExpressionsIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, GroupBy struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.columns != null) {
        if (struct.isSetColumns()) {
          oprot.writeFieldBegin(COLUMNS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.columns.size()));
            for (String _iter68 : struct.columns)
            {
              oprot.writeString(_iter68);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetTopN()) {
        oprot.writeFieldBegin(TOP_N_FIELD_DESC);
        oprot.writeI64(struct.topN);
        oprot.writeFieldEnd();
      }
      if (struct.expressions != null) {
        if (struct.isSetExpressions()) {
          oprot.writeFieldBegin(EXPRESSIONS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.expressions.size()));
            for (String _iter69 : struct.expressions)
            {
              oprot.writeString(_iter69);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class GroupByTupleSchemeFactory implements SchemeFactory {
    public GroupByTupleScheme getScheme() {
      return new GroupByTupleScheme();
    }
  }

  private static class GroupByTupleScheme extends TupleScheme<GroupBy> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, GroupBy struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetColumns()) {
        optionals.set(0);
      }
      if (struct.isSetTopN()) {
        optionals.set(1);
      }
      if (struct.isSetExpressions()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetColumns()) {
        {
          oprot.writeI32(struct.columns.size());
          for (String _iter70 : struct.columns)
          {
            oprot.writeString(_iter70);
          }
        }
      }
      if (struct.isSetTopN()) {
        oprot.writeI64(struct.topN);
      }
      if (struct.isSetExpressions()) {
        {
          oprot.writeI32(struct.expressions.size());
          for (String _iter71 : struct.expressions)
          {
            oprot.writeString(_iter71);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, GroupBy struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list72 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.columns = new ArrayList<String>(_list72.size);
          String _elem73;
          for (int _i74 = 0; _i74 < _list72.size; ++_i74)
          {
            _elem73 = iprot.readString();
            struct.columns.add(_elem73);
          }
        }
        struct.setColumnsIsSet(true);
      }
      if (incoming.get(1)) {
        struct.topN = iprot.readI64();
        struct.setTopNIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list75 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.expressions = new ArrayList<String>(_list75.size);
          String _elem76;
          for (int _i77 = 0; _i77 < _list75.size; ++_i77)
          {
            _elem76 = iprot.readString();
            struct.expressions.add(_elem76);
          }
        }
        struct.setExpressionsIsSet(true);
      }
    }
  }

}

