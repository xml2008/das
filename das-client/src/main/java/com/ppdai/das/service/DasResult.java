/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.ppdai.das.service;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2019-09-29")
public class DasResult implements org.apache.thrift.TBase<DasResult, DasResult._Fields>, java.io.Serializable, Cloneable, Comparable<DasResult> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DasResult");

  private static final org.apache.thrift.protocol.TField ROW_COUNT_FIELD_DESC = new org.apache.thrift.protocol.TField("rowCount", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField ROWS_FIELD_DESC = new org.apache.thrift.protocol.TField("rows", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField BATCH_ROWS_INDEX_FIELD_DESC = new org.apache.thrift.protocol.TField("batchRowsIndex", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField PARAMETERS_FIELD_DESC = new org.apache.thrift.protocol.TField("parameters", org.apache.thrift.protocol.TType.STRUCT, (short)4);
  private static final org.apache.thrift.protocol.TField DIAG_INFO_FIELD_DESC = new org.apache.thrift.protocol.TField("diagInfo", org.apache.thrift.protocol.TType.STRUCT, (short)5);
  private static final org.apache.thrift.protocol.TField ENTITY_META_FIELD_DESC = new org.apache.thrift.protocol.TField("entityMeta", org.apache.thrift.protocol.TType.STRUCT, (short)6);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new DasResultStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new DasResultTupleSchemeFactory();

  public int rowCount; // optional
  public java.util.List<Entity> rows; // optional
  public java.util.List<Integer> batchRowsIndex; // optional
  public DasParameters parameters; // optional
  public DasDiagInfo diagInfo; // optional
  public EntityMeta entityMeta; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ROW_COUNT((short)1, "rowCount"),
    ROWS((short)2, "rows"),
    BATCH_ROWS_INDEX((short)3, "batchRowsIndex"),
    PARAMETERS((short)4, "parameters"),
    DIAG_INFO((short)5, "diagInfo"),
    ENTITY_META((short)6, "entityMeta");

    private static final java.util.Map<String, _Fields> byName = new java.util.HashMap<String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ROW_COUNT
          return ROW_COUNT;
        case 2: // ROWS
          return ROWS;
        case 3: // BATCH_ROWS_INDEX
          return BATCH_ROWS_INDEX;
        case 4: // PARAMETERS
          return PARAMETERS;
        case 5: // DIAG_INFO
          return DIAG_INFO;
        case 6: // ENTITY_META
          return ENTITY_META;
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
  private static final int __ROWCOUNT_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.ROW_COUNT,_Fields.ROWS,_Fields.BATCH_ROWS_INDEX,_Fields.PARAMETERS,_Fields.DIAG_INFO,_Fields.ENTITY_META};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ROW_COUNT, new org.apache.thrift.meta_data.FieldMetaData("rowCount", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.ROWS, new org.apache.thrift.meta_data.FieldMetaData("rows", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Entity.class))));
    tmpMap.put(_Fields.BATCH_ROWS_INDEX, new org.apache.thrift.meta_data.FieldMetaData("batchRowsIndex", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32))));
    tmpMap.put(_Fields.PARAMETERS, new org.apache.thrift.meta_data.FieldMetaData("parameters", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, DasParameters.class)));
    tmpMap.put(_Fields.DIAG_INFO, new org.apache.thrift.meta_data.FieldMetaData("diagInfo", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, DasDiagInfo.class)));
    tmpMap.put(_Fields.ENTITY_META, new org.apache.thrift.meta_data.FieldMetaData("entityMeta", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, EntityMeta.class)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DasResult.class, metaDataMap);
  }

  public DasResult() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public DasResult(DasResult other) {
    __isset_bitfield = other.__isset_bitfield;
    this.rowCount = other.rowCount;
    if (other.isSetRows()) {
      java.util.List<Entity> __this__rows = new java.util.ArrayList<Entity>(other.rows.size());
      for (Entity other_element : other.rows) {
        __this__rows.add(new Entity(other_element));
      }
      this.rows = __this__rows;
    }
    if (other.isSetBatchRowsIndex()) {
      java.util.List<Integer> __this__batchRowsIndex = new java.util.ArrayList<Integer>(other.batchRowsIndex);
      this.batchRowsIndex = __this__batchRowsIndex;
    }
    if (other.isSetParameters()) {
      this.parameters = new DasParameters(other.parameters);
    }
    if (other.isSetDiagInfo()) {
      this.diagInfo = new DasDiagInfo(other.diagInfo);
    }
    if (other.isSetEntityMeta()) {
      this.entityMeta = new EntityMeta(other.entityMeta);
    }
  }

  public DasResult deepCopy() {
    return new DasResult(this);
  }

  @Override
  public void clear() {
    setRowCountIsSet(false);
    this.rowCount = 0;
    this.rows = null;
    this.batchRowsIndex = null;
    this.parameters = null;
    this.diagInfo = null;
    this.entityMeta = null;
  }

  public int getRowCount() {
    return this.rowCount;
  }

  public DasResult setRowCount(int rowCount) {
    this.rowCount = rowCount;
    setRowCountIsSet(true);
    return this;
  }

  public void unsetRowCount() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __ROWCOUNT_ISSET_ID);
  }

  /** Returns true if field rowCount is set (has been assigned a value) and false otherwise */
  public boolean isSetRowCount() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __ROWCOUNT_ISSET_ID);
  }

  public void setRowCountIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __ROWCOUNT_ISSET_ID, value);
  }

  public int getRowsSize() {
    return (this.rows == null) ? 0 : this.rows.size();
  }

  public java.util.Iterator<Entity> getRowsIterator() {
    return (this.rows == null) ? null : this.rows.iterator();
  }

  public void addToRows(Entity elem) {
    if (this.rows == null) {
      this.rows = new java.util.ArrayList<Entity>();
    }
    this.rows.add(elem);
  }

  public java.util.List<Entity> getRows() {
    return this.rows;
  }

  public DasResult setRows(java.util.List<Entity> rows) {
    this.rows = rows;
    return this;
  }

  public void unsetRows() {
    this.rows = null;
  }

  /** Returns true if field rows is set (has been assigned a value) and false otherwise */
  public boolean isSetRows() {
    return this.rows != null;
  }

  public void setRowsIsSet(boolean value) {
    if (!value) {
      this.rows = null;
    }
  }

  public int getBatchRowsIndexSize() {
    return (this.batchRowsIndex == null) ? 0 : this.batchRowsIndex.size();
  }

  public java.util.Iterator<Integer> getBatchRowsIndexIterator() {
    return (this.batchRowsIndex == null) ? null : this.batchRowsIndex.iterator();
  }

  public void addToBatchRowsIndex(int elem) {
    if (this.batchRowsIndex == null) {
      this.batchRowsIndex = new java.util.ArrayList<Integer>();
    }
    this.batchRowsIndex.add(elem);
  }

  public java.util.List<Integer> getBatchRowsIndex() {
    return this.batchRowsIndex;
  }

  public DasResult setBatchRowsIndex(java.util.List<Integer> batchRowsIndex) {
    this.batchRowsIndex = batchRowsIndex;
    return this;
  }

  public void unsetBatchRowsIndex() {
    this.batchRowsIndex = null;
  }

  /** Returns true if field batchRowsIndex is set (has been assigned a value) and false otherwise */
  public boolean isSetBatchRowsIndex() {
    return this.batchRowsIndex != null;
  }

  public void setBatchRowsIndexIsSet(boolean value) {
    if (!value) {
      this.batchRowsIndex = null;
    }
  }

  public DasParameters getParameters() {
    return this.parameters;
  }

  public DasResult setParameters(DasParameters parameters) {
    this.parameters = parameters;
    return this;
  }

  public void unsetParameters() {
    this.parameters = null;
  }

  /** Returns true if field parameters is set (has been assigned a value) and false otherwise */
  public boolean isSetParameters() {
    return this.parameters != null;
  }

  public void setParametersIsSet(boolean value) {
    if (!value) {
      this.parameters = null;
    }
  }

  public DasDiagInfo getDiagInfo() {
    return this.diagInfo;
  }

  public DasResult setDiagInfo(DasDiagInfo diagInfo) {
    this.diagInfo = diagInfo;
    return this;
  }

  public void unsetDiagInfo() {
    this.diagInfo = null;
  }

  /** Returns true if field diagInfo is set (has been assigned a value) and false otherwise */
  public boolean isSetDiagInfo() {
    return this.diagInfo != null;
  }

  public void setDiagInfoIsSet(boolean value) {
    if (!value) {
      this.diagInfo = null;
    }
  }

  public EntityMeta getEntityMeta() {
    return this.entityMeta;
  }

  public DasResult setEntityMeta(EntityMeta entityMeta) {
    this.entityMeta = entityMeta;
    return this;
  }

  public void unsetEntityMeta() {
    this.entityMeta = null;
  }

  /** Returns true if field entityMeta is set (has been assigned a value) and false otherwise */
  public boolean isSetEntityMeta() {
    return this.entityMeta != null;
  }

  public void setEntityMetaIsSet(boolean value) {
    if (!value) {
      this.entityMeta = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ROW_COUNT:
      if (value == null) {
        unsetRowCount();
      } else {
        setRowCount((Integer)value);
      }
      break;

    case ROWS:
      if (value == null) {
        unsetRows();
      } else {
        setRows((java.util.List<Entity>)value);
      }
      break;

    case BATCH_ROWS_INDEX:
      if (value == null) {
        unsetBatchRowsIndex();
      } else {
        setBatchRowsIndex((java.util.List<Integer>)value);
      }
      break;

    case PARAMETERS:
      if (value == null) {
        unsetParameters();
      } else {
        setParameters((DasParameters)value);
      }
      break;

    case DIAG_INFO:
      if (value == null) {
        unsetDiagInfo();
      } else {
        setDiagInfo((DasDiagInfo)value);
      }
      break;

    case ENTITY_META:
      if (value == null) {
        unsetEntityMeta();
      } else {
        setEntityMeta((EntityMeta)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ROW_COUNT:
      return getRowCount();

    case ROWS:
      return getRows();

    case BATCH_ROWS_INDEX:
      return getBatchRowsIndex();

    case PARAMETERS:
      return getParameters();

    case DIAG_INFO:
      return getDiagInfo();

    case ENTITY_META:
      return getEntityMeta();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ROW_COUNT:
      return isSetRowCount();
    case ROWS:
      return isSetRows();
    case BATCH_ROWS_INDEX:
      return isSetBatchRowsIndex();
    case PARAMETERS:
      return isSetParameters();
    case DIAG_INFO:
      return isSetDiagInfo();
    case ENTITY_META:
      return isSetEntityMeta();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof DasResult)
      return this.equals((DasResult)that);
    return false;
  }

  public boolean equals(DasResult that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_rowCount = true && this.isSetRowCount();
    boolean that_present_rowCount = true && that.isSetRowCount();
    if (this_present_rowCount || that_present_rowCount) {
      if (!(this_present_rowCount && that_present_rowCount))
        return false;
      if (this.rowCount != that.rowCount)
        return false;
    }

    boolean this_present_rows = true && this.isSetRows();
    boolean that_present_rows = true && that.isSetRows();
    if (this_present_rows || that_present_rows) {
      if (!(this_present_rows && that_present_rows))
        return false;
      if (!this.rows.equals(that.rows))
        return false;
    }

    boolean this_present_batchRowsIndex = true && this.isSetBatchRowsIndex();
    boolean that_present_batchRowsIndex = true && that.isSetBatchRowsIndex();
    if (this_present_batchRowsIndex || that_present_batchRowsIndex) {
      if (!(this_present_batchRowsIndex && that_present_batchRowsIndex))
        return false;
      if (!this.batchRowsIndex.equals(that.batchRowsIndex))
        return false;
    }

    boolean this_present_parameters = true && this.isSetParameters();
    boolean that_present_parameters = true && that.isSetParameters();
    if (this_present_parameters || that_present_parameters) {
      if (!(this_present_parameters && that_present_parameters))
        return false;
      if (!this.parameters.equals(that.parameters))
        return false;
    }

    boolean this_present_diagInfo = true && this.isSetDiagInfo();
    boolean that_present_diagInfo = true && that.isSetDiagInfo();
    if (this_present_diagInfo || that_present_diagInfo) {
      if (!(this_present_diagInfo && that_present_diagInfo))
        return false;
      if (!this.diagInfo.equals(that.diagInfo))
        return false;
    }

    boolean this_present_entityMeta = true && this.isSetEntityMeta();
    boolean that_present_entityMeta = true && that.isSetEntityMeta();
    if (this_present_entityMeta || that_present_entityMeta) {
      if (!(this_present_entityMeta && that_present_entityMeta))
        return false;
      if (!this.entityMeta.equals(that.entityMeta))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetRowCount()) ? 131071 : 524287);
    if (isSetRowCount())
      hashCode = hashCode * 8191 + rowCount;

    hashCode = hashCode * 8191 + ((isSetRows()) ? 131071 : 524287);
    if (isSetRows())
      hashCode = hashCode * 8191 + rows.hashCode();

    hashCode = hashCode * 8191 + ((isSetBatchRowsIndex()) ? 131071 : 524287);
    if (isSetBatchRowsIndex())
      hashCode = hashCode * 8191 + batchRowsIndex.hashCode();

    hashCode = hashCode * 8191 + ((isSetParameters()) ? 131071 : 524287);
    if (isSetParameters())
      hashCode = hashCode * 8191 + parameters.hashCode();

    hashCode = hashCode * 8191 + ((isSetDiagInfo()) ? 131071 : 524287);
    if (isSetDiagInfo())
      hashCode = hashCode * 8191 + diagInfo.hashCode();

    hashCode = hashCode * 8191 + ((isSetEntityMeta()) ? 131071 : 524287);
    if (isSetEntityMeta())
      hashCode = hashCode * 8191 + entityMeta.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(DasResult other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetRowCount()).compareTo(other.isSetRowCount());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRowCount()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.rowCount, other.rowCount);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetRows()).compareTo(other.isSetRows());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRows()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.rows, other.rows);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetBatchRowsIndex()).compareTo(other.isSetBatchRowsIndex());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBatchRowsIndex()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.batchRowsIndex, other.batchRowsIndex);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetParameters()).compareTo(other.isSetParameters());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetParameters()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.parameters, other.parameters);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDiagInfo()).compareTo(other.isSetDiagInfo());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDiagInfo()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.diagInfo, other.diagInfo);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetEntityMeta()).compareTo(other.isSetEntityMeta());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEntityMeta()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.entityMeta, other.entityMeta);
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
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DasResult(");
    boolean first = true;

    if (isSetRowCount()) {
      sb.append("rowCount:");
      sb.append(this.rowCount);
      first = false;
    }
    if (isSetRows()) {
      if (!first) sb.append(", ");
      sb.append("rows:");
      if (this.rows == null) {
        sb.append("null");
      } else {
        sb.append(this.rows);
      }
      first = false;
    }
    if (isSetBatchRowsIndex()) {
      if (!first) sb.append(", ");
      sb.append("batchRowsIndex:");
      if (this.batchRowsIndex == null) {
        sb.append("null");
      } else {
        sb.append(this.batchRowsIndex);
      }
      first = false;
    }
    if (isSetParameters()) {
      if (!first) sb.append(", ");
      sb.append("parameters:");
      if (this.parameters == null) {
        sb.append("null");
      } else {
        sb.append(this.parameters);
      }
      first = false;
    }
    if (isSetDiagInfo()) {
      if (!first) sb.append(", ");
      sb.append("diagInfo:");
      if (this.diagInfo == null) {
        sb.append("null");
      } else {
        sb.append(this.diagInfo);
      }
      first = false;
    }
    if (isSetEntityMeta()) {
      if (!first) sb.append(", ");
      sb.append("entityMeta:");
      if (this.entityMeta == null) {
        sb.append("null");
      } else {
        sb.append(this.entityMeta);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
    if (parameters != null) {
      parameters.validate();
    }
    if (diagInfo != null) {
      diagInfo.validate();
    }
    if (entityMeta != null) {
      entityMeta.validate();
    }
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

  private static class DasResultStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public DasResultStandardScheme getScheme() {
      return new DasResultStandardScheme();
    }
  }

  private static class DasResultStandardScheme extends org.apache.thrift.scheme.StandardScheme<DasResult> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, DasResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ROW_COUNT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.rowCount = iprot.readI32();
              struct.setRowCountIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ROWS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list212 = iprot.readListBegin();
                struct.rows = new java.util.ArrayList<Entity>(_list212.size);
                Entity _elem213;
                for (int _i214 = 0; _i214 < _list212.size; ++_i214)
                {
                  _elem213 = new Entity();
                  _elem213.read(iprot);
                  struct.rows.add(_elem213);
                }
                iprot.readListEnd();
              }
              struct.setRowsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // BATCH_ROWS_INDEX
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list215 = iprot.readListBegin();
                struct.batchRowsIndex = new java.util.ArrayList<Integer>(_list215.size);
                int _elem216;
                for (int _i217 = 0; _i217 < _list215.size; ++_i217)
                {
                  _elem216 = iprot.readI32();
                  struct.batchRowsIndex.add(_elem216);
                }
                iprot.readListEnd();
              }
              struct.setBatchRowsIndexIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // PARAMETERS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.parameters = new DasParameters();
              struct.parameters.read(iprot);
              struct.setParametersIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // DIAG_INFO
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.diagInfo = new DasDiagInfo();
              struct.diagInfo.read(iprot);
              struct.setDiagInfoIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // ENTITY_META
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.entityMeta = new EntityMeta();
              struct.entityMeta.read(iprot);
              struct.setEntityMetaIsSet(true);
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

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, DasResult struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.isSetRowCount()) {
        oprot.writeFieldBegin(ROW_COUNT_FIELD_DESC);
        oprot.writeI32(struct.rowCount);
        oprot.writeFieldEnd();
      }
      if (struct.rows != null) {
        if (struct.isSetRows()) {
          oprot.writeFieldBegin(ROWS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.rows.size()));
            for (Entity _iter218 : struct.rows)
            {
              _iter218.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.batchRowsIndex != null) {
        if (struct.isSetBatchRowsIndex()) {
          oprot.writeFieldBegin(BATCH_ROWS_INDEX_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, struct.batchRowsIndex.size()));
            for (int _iter219 : struct.batchRowsIndex)
            {
              oprot.writeI32(_iter219);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.parameters != null) {
        if (struct.isSetParameters()) {
          oprot.writeFieldBegin(PARAMETERS_FIELD_DESC);
          struct.parameters.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.diagInfo != null) {
        if (struct.isSetDiagInfo()) {
          oprot.writeFieldBegin(DIAG_INFO_FIELD_DESC);
          struct.diagInfo.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.entityMeta != null) {
        if (struct.isSetEntityMeta()) {
          oprot.writeFieldBegin(ENTITY_META_FIELD_DESC);
          struct.entityMeta.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class DasResultTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public DasResultTupleScheme getScheme() {
      return new DasResultTupleScheme();
    }
  }

  private static class DasResultTupleScheme extends org.apache.thrift.scheme.TupleScheme<DasResult> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, DasResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetRowCount()) {
        optionals.set(0);
      }
      if (struct.isSetRows()) {
        optionals.set(1);
      }
      if (struct.isSetBatchRowsIndex()) {
        optionals.set(2);
      }
      if (struct.isSetParameters()) {
        optionals.set(3);
      }
      if (struct.isSetDiagInfo()) {
        optionals.set(4);
      }
      if (struct.isSetEntityMeta()) {
        optionals.set(5);
      }
      oprot.writeBitSet(optionals, 6);
      if (struct.isSetRowCount()) {
        oprot.writeI32(struct.rowCount);
      }
      if (struct.isSetRows()) {
        {
          oprot.writeI32(struct.rows.size());
          for (Entity _iter220 : struct.rows)
          {
            _iter220.write(oprot);
          }
        }
      }
      if (struct.isSetBatchRowsIndex()) {
        {
          oprot.writeI32(struct.batchRowsIndex.size());
          for (int _iter221 : struct.batchRowsIndex)
          {
            oprot.writeI32(_iter221);
          }
        }
      }
      if (struct.isSetParameters()) {
        struct.parameters.write(oprot);
      }
      if (struct.isSetDiagInfo()) {
        struct.diagInfo.write(oprot);
      }
      if (struct.isSetEntityMeta()) {
        struct.entityMeta.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, DasResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(6);
      if (incoming.get(0)) {
        struct.rowCount = iprot.readI32();
        struct.setRowCountIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list222 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.rows = new java.util.ArrayList<Entity>(_list222.size);
          Entity _elem223;
          for (int _i224 = 0; _i224 < _list222.size; ++_i224)
          {
            _elem223 = new Entity();
            _elem223.read(iprot);
            struct.rows.add(_elem223);
          }
        }
        struct.setRowsIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list225 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, iprot.readI32());
          struct.batchRowsIndex = new java.util.ArrayList<Integer>(_list225.size);
          int _elem226;
          for (int _i227 = 0; _i227 < _list225.size; ++_i227)
          {
            _elem226 = iprot.readI32();
            struct.batchRowsIndex.add(_elem226);
          }
        }
        struct.setBatchRowsIndexIsSet(true);
      }
      if (incoming.get(3)) {
        struct.parameters = new DasParameters();
        struct.parameters.read(iprot);
        struct.setParametersIsSet(true);
      }
      if (incoming.get(4)) {
        struct.diagInfo = new DasDiagInfo();
        struct.diagInfo.read(iprot);
        struct.setDiagInfoIsSet(true);
      }
      if (incoming.get(5)) {
        struct.entityMeta = new EntityMeta();
        struct.entityMeta.read(iprot);
        struct.setEntityMetaIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}
