/**
 * Autogenerated by Thrift Compiler (0.9.3)
 * <p>
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *
 * @generated
 */
package com.ym.materials.thrift.codegen.gened;

import org.apache.thrift.TBase;
import org.apache.thrift.TBaseHelper;
import org.apache.thrift.TFieldRequirementType;
import org.apache.thrift.meta_data.FieldMetaData;
import org.apache.thrift.meta_data.FieldValueMetaData;
import org.apache.thrift.protocol.*;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.transport.TIOStreamTransport;

import javax.annotation.Generated;
import java.util.*;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2019-01-01")
public class User implements TBase<User, User._Fields>, java.io.Serializable, Cloneable, Comparable<User> {
    private static final TStruct STRUCT_DESC = new TStruct("User");

    private static final TField NAME_FIELD_DESC = new TField("name", TType.STRING, (short) 1);
    private static final TField EMAIL_FIELD_DESC = new TField("email", TType.STRING, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new UserStandardSchemeFactory());
        schemes.put(TupleScheme.class, new UserTupleSchemeFactory());
    }

    public String name; // required
    public String email; // required

    /**
     * The set of fields this struct contains, along with convenience methods for finding and manipulating them.
     */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
        NAME((short) 1, "name"),
        EMAIL((short) 2, "email");

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
            switch (fieldId) {
                case 1: // NAME
                    return NAME;
                case 2: // EMAIL
                    return EMAIL;
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
    public static final Map<_Fields, FieldMetaData> metaDataMap;

    static {
        Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.NAME, new FieldMetaData("name", TFieldRequirementType.DEFAULT,
                new FieldValueMetaData(TType.STRING)));
        tmpMap.put(_Fields.EMAIL, new FieldMetaData("email", TFieldRequirementType.DEFAULT,
                new FieldValueMetaData(TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        FieldMetaData.addStructMetaDataMap(User.class, metaDataMap);
    }

    public User() {
    }

    public User(
            String name,
            String email) {
        this();
        this.name = name;
        this.email = email;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public User(User other) {
        if (other.isSetName()) {
            this.name = other.name;
        }
        if (other.isSetEmail()) {
            this.email = other.email;
        }
    }

    public User deepCopy() {
        return new User(this);
    }

    @Override
    public void clear() {
        this.name = null;
        this.email = null;
    }

    public String getName() {
        return this.name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public void unsetName() {
        this.name = null;
    }

    /**
     * Returns true if field name is set (has been assigned a value) and false otherwise
     */
    public boolean isSetName() {
        return this.name != null;
    }

    public void setNameIsSet(boolean value) {
        if (!value) {
            this.name = null;
        }
    }

    public String getEmail() {
        return this.email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public void unsetEmail() {
        this.email = null;
    }

    /**
     * Returns true if field email is set (has been assigned a value) and false otherwise
     */
    public boolean isSetEmail() {
        return this.email != null;
    }

    public void setEmailIsSet(boolean value) {
        if (!value) {
            this.email = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch (field) {
            case NAME:
                if (value == null) {
                    unsetName();
                } else {
                    setName((String) value);
                }
                break;

            case EMAIL:
                if (value == null) {
                    unsetEmail();
                } else {
                    setEmail((String) value);
                }
                break;

        }
    }

    public Object getFieldValue(_Fields field) {
        switch (field) {
            case NAME:
                return getName();

            case EMAIL:
                return getEmail();

        }
        throw new IllegalStateException();
    }

    /**
     * Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise
     */
    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }

        switch (field) {
            case NAME:
                return isSetName();
            case EMAIL:
                return isSetEmail();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof User)
            return this.equals((User) that);
        return false;
    }

    public boolean equals(User that) {
        if (that == null)
            return false;

        boolean this_present_name = true && this.isSetName();
        boolean that_present_name = true && that.isSetName();
        if (this_present_name || that_present_name) {
            if (!(this_present_name && that_present_name))
                return false;
            if (!this.name.equals(that.name))
                return false;
        }

        boolean this_present_email = true && this.isSetEmail();
        boolean that_present_email = true && that.isSetEmail();
        if (this_present_email || that_present_email) {
            if (!(this_present_email && that_present_email))
                return false;
            if (!this.email.equals(that.email))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<Object>();

        boolean present_name = true && (isSetName());
        list.add(present_name);
        if (present_name)
            list.add(name);

        boolean present_email = true && (isSetEmail());
        list.add(present_email);
        if (present_email)
            list.add(email);

        return list.hashCode();
    }

    @Override
    public int compareTo(User other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }

        int lastComparison = 0;

        lastComparison = Boolean.valueOf(isSetName()).compareTo(other.isSetName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetName()) {
            lastComparison = TBaseHelper.compareTo(this.name, other.name);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetEmail()).compareTo(other.isSetEmail());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetEmail()) {
            lastComparison = TBaseHelper.compareTo(this.email, other.email);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        return 0;
    }

    public _Fields fieldForId(int fieldId) {
        return _Fields.findByThriftId(fieldId);
    }

    public void read(TProtocol iprot) throws org.apache.thrift.TException {
        schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(TProtocol oprot) throws org.apache.thrift.TException {
        schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("User(");
        boolean first = true;

        sb.append("name:");
        if (this.name == null) {
            sb.append("null");
        } else {
            sb.append(this.name);
        }
        first = false;
        if (!first) sb.append(", ");
        sb.append("email:");
        if (this.email == null) {
            sb.append("null");
        } else {
            sb.append(this.email);
        }
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        // check for required fields
        // check for sub-struct validity
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            write(new TCompactProtocol(new TIOStreamTransport(out)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            read(new TCompactProtocol(new TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class UserStandardSchemeFactory implements SchemeFactory {
        public UserStandardScheme getScheme() {
            return new UserStandardScheme();
        }
    }

    private static class UserStandardScheme extends StandardScheme<User> {

        public void read(TProtocol iprot, User struct) throws org.apache.thrift.TException {
            TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == TType.STOP) {
                    break;
                }
                switch (schemeField.id) {
                    case 1: // NAME
                        if (schemeField.type == TType.STRING) {
                            struct.name = iprot.readString();
                            struct.setNameIsSet(true);
                        } else {
                            TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2: // EMAIL
                        if (schemeField.type == TType.STRING) {
                            struct.email = iprot.readString();
                            struct.setEmailIsSet(true);
                        } else {
                            TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    default:
                        TProtocolUtil.skip(iprot, schemeField.type);
                }
                iprot.readFieldEnd();
            }
            iprot.readStructEnd();

            // check for required fields of primitive type, which can't be checked in the validate method
            struct.validate();
        }

        public void write(TProtocol oprot, User struct) throws org.apache.thrift.TException {
            struct.validate();

            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.name != null) {
                oprot.writeFieldBegin(NAME_FIELD_DESC);
                oprot.writeString(struct.name);
                oprot.writeFieldEnd();
            }
            if (struct.email != null) {
                oprot.writeFieldBegin(EMAIL_FIELD_DESC);
                oprot.writeString(struct.email);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }

    }

    private static class UserTupleSchemeFactory implements SchemeFactory {
        public UserTupleScheme getScheme() {
            return new UserTupleScheme();
        }
    }

    private static class UserTupleScheme extends TupleScheme<User> {

        @Override
        public void write(TProtocol prot, User struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetName()) {
                optionals.set(0);
            }
            if (struct.isSetEmail()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetName()) {
                oprot.writeString(struct.name);
            }
            if (struct.isSetEmail()) {
                oprot.writeString(struct.email);
            }
        }

        @Override
        public void read(TProtocol prot, User struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.name = iprot.readString();
                struct.setNameIsSet(true);
            }
            if (incoming.get(1)) {
                struct.email = iprot.readString();
                struct.setEmailIsSet(true);
            }
        }
    }

}

