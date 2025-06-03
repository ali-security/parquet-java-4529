/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.parquet.avro;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.apache.parquet.column.Dictionary;
import org.apache.parquet.io.ParquetDecodingException;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.io.api.PrimitiveConverter;
import org.apache.parquet.schema.PrimitiveStringifier;
import org.apache.parquet.schema.PrimitiveType;

public class AvroConverters {

  public static final String[] SERIALIZABLE_PACKAGES;

  static {
    String prop = System.getProperty("org.apache.parquet.avro.SERIALIZABLE_PACKAGES");
    SERIALIZABLE_PACKAGES = prop == null ? new String[0] : prop.split(",");
  }

  public static final String[] DENY_PACKAGES;

  static {
    DENY_PACKAGES = System.getProperty(
            "bsh.XThis",
            "bsh.Interpreter",
            "com.mchange.v2.c3p0.PoolBackedDataSource",
            "com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase",
            "clojure.lang.PersistentArrayMap",
            "clojure.inspector.proxy$javax.swing.table.AbstractTableModel$ff19274a",
            "org.apache.commons.beanutils.BeanComparator",
            "org.apache.commons.collections.Transformer",
            "org.apache.commons.collections.functors.ChainedTransformer",
            "org.apache.commons.collections.functors.ConstantTransformer",
            "org.apache.commons.collections.functors.InstantiateTransformer",
            "org.apache.commons.collections.map.LazyMap",
            "org.apache.commons.collections.functors.InvokerTransformer",
            "org.apache.commons.collections.keyvalue.TiedMapEntry",
            "org.apache.commons.collections4.comparators.TransformingComparator",
            "org.apache.commons.collections4.functors.InvokerTransformer",
            "org.apache.commons.collections4.functors.ChainedTransformer",
            "org.apache.commons.collections4.functors.ConstantTransformer",
            "org.apache.commons.collections4.functors.InstantiateTransformer",
            "org.apache.commons.fileupload.disk.DiskFileItem",
            "org.apache.commons.io.output.DeferredFileOutputStream",
            "org.apache.commons.io.output.ThresholdingOutputStream",
            "org.apache.wicket.util.upload.DiskFileItem",
            "org.apache.wicket.util.io.DeferredFileOutputStream",
            "org.apache.wicket.util.io.ThresholdingOutputStream",
            "org.codehaus.groovy.runtime.ConvertedClosure",
            "org.codehaus.groovy.runtime.MethodClosure",
            "org.hibernate.engine.spi.TypedValue",
            "org.hibernate.tuple.component.AbstractComponentTuplizer",
            "org.hibernate.tuple.component.PojoComponentTuplizer",
            "org.hibernate.type.AbstractType",
            "org.hibernate.type.ComponentType",
            "org.hibernate.type.Type",
            "org.hibernate.EntityMode",
            "com.sun.rowset.JdbcRowSetImpl",
            "org.jboss.interceptor.builder.InterceptionModelBuilder",
            "org.jboss.interceptor.builder.MethodReference",
            "org.jboss.interceptor.proxy.DefaultInvocationContextFactory",
            "org.jboss.interceptor.proxy.InterceptorMethodHandler",
            "org.jboss.interceptor.reader.ClassMetadataInterceptorReference",
            "org.jboss.interceptor.reader.DefaultMethodMetadata",
            "org.jboss.interceptor.reader.ReflectiveClassMetadata",
            "org.jboss.interceptor.reader.SimpleInterceptorMetadata",
            "org.jboss.interceptor.spi.instance.InterceptorInstantiator",
            "org.jboss.interceptor.spi.metadata.InterceptorReference",
            "org.jboss.interceptor.spi.metadata.MethodMetadata",
            "org.jboss.interceptor.spi.model.InterceptionType",
            "org.jboss.interceptor.spi.model.InterceptionModel",
            "sun.rmi.server.UnicastRef",
            "sun.rmi.transport.LiveRef",
            "sun.rmi.transport.tcp.TCPEndpoint",
            "java.rmi.server.RemoteObject",
            "java.rmi.server.RemoteRef",
            "java.rmi.server.UnicastRemoteObject",
            "sun.rmi.server.ActivationGroupImpl",
            "sun.rmi.server.UnicastServerRef",
            "org.springframework.aop.framework.AdvisedSupport",
            "net.sf.json.JSONObject",
            "org.jboss.weld.interceptor.builder.InterceptionModelBuilder",
            "org.jboss.weld.interceptor.builder.MethodReference",
            "org.jboss.weld.interceptor.proxy.DefaultInvocationContextFactory",
            "org.jboss.weld.interceptor.proxy.InterceptorMethodHandler",
            "org.jboss.weld.interceptor.reader.ClassMetadataInterceptorReference",
            "org.jboss.weld.interceptor.reader.DefaultMethodMetadata",
            "org.jboss.weld.interceptor.reader.ReflectiveClassMetadata",
            "org.jboss.weld.interceptor.reader.SimpleInterceptorMetadata",
            "org.jboss.weld.interceptor.spi.instance.InterceptorInstantiator",
            "org.jboss.weld.interceptor.spi.metadata.InterceptorReference",
            "org.jboss.weld.interceptor.spi.metadata.MethodMetadata",
            "org.jboss.weld.interceptor.spi.model.InterceptionModel",
            "org.jboss.weld.interceptor.spi.model.InterceptionType",
            "org.python.core.PyObject",
            "org.python.core.PyBytecode",
            "org.python.core.PyFunction",
            "org.mozilla.javascript.**",
            "org.apache.myfaces.context.servlet.FacesContextImpl",
            "org.apache.myfaces.context.servlet.FacesContextImplBase",
            "org.apache.myfaces.el.CompositeELResolver",
            "org.apache.myfaces.el.unified.FacesELContext",
            "org.apache.myfaces.view.facelets.el.ValueExpressionMethodExpression",
            "com.sun.syndication.feed.impl.ObjectBean",
            "org.springframework.beans.factory.ObjectFactory",
            "org.springframework.aop.framework.AdvisedSupport",
            "org.springframework.aop.target.SingletonTargetSource",
            "com.vaadin.data.util.NestedMethodProperty",
            "com.vaadin.data.util.PropertysetIte",
            "org.apache.commons.collections.functors.InvokerTransformer",
            "org.apache.commons.collections.functors.InstantiateTransformer",
            "org.apache.commons.collections4.functors.InvokerTransformer",
            "org.apache.commons.collections4.functors.InstantiateTransformer",
            "org.codehaus.groovy.runtime.ConvertedClosure",
            "org.codehaus.groovy.runtime.MethodClosure",
            "org.springframework.beans.factory.ObjectFactory",
            "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
            "org.apache.xalan.xsltc.trax.TemplatesImpl",
            "com.sun.rowset.JdbcRowSetImpl",
            "java.util.logging.FileHandler",
            "java.rmi.server.UnicastRemoteObject",
            "org.springframework.beans.factory.config.PropertyPathFactoryBean",
            "org.springframework.aop.config.MethodLocatingFactoryBean",
            "org.springframework.beans.factory.config.BeanReferenceFactoryBean",
            "org.apache.tomcat.dbcp.dbcp2.BasicDataSource",
            "com.sun.org.apache.bcel.internal.util.ClassLoader",
            "org.hibernate.jmx.StatisticsService",
            "org.apache.ibatis.datasource.jndi.JndiDataSourceFactory",
            "org.apache.ibatis.parsing.XPathParser",
            "jodd.db.connection.DataSourceConnectionProvider",
            "oracle.jdbc.connector.OracleManagedConnectionFactory",
            "oracle.jdbc.rowset.OracleJDBCRowSet",
            "org.slf4j.ext.EventData",
            "flex.messaging.util.concurrent.AsynchBeansWorkManagerExecutor",
            "com.sun.deploy.security.ruleset.DRSHelper",
            "org.apache.axis2.jaxws.spi.handler.HandlerResolverImpl",
            "org.jboss.util.propertyeditor.DocumentEditor",
            "org.apache.openjpa.ee.RegistryManagedRuntime",
            "org.apache.openjpa.ee.JNDIManagedRuntime",
            "org.apache.axis2.transport.jms.JMSOutTransportInfo",
            "com.mysql.cj.jdbc.admin.MiniAdmin",
            "ch.qos.logback.core.db.DriverManagerConnectionSource",
            "org.jdom.transform.XSLTransformer",
            "org.jdom2.transform.XSLTransformer",
            "net.sf.ehcache.transaction.manager.DefaultTransactionManagerLookup",
            "net.sf.ehcache.hibernate.EhcacheJtaTransactionManagerLookup",
            "ch.qos.logback.core.db.JNDIConnectionSource",
            "com.zaxxer.hikari.HikariConfig",
            "com.zaxxer.hikari.HikariDataSource",
            "org.apache.cxf.jaxrs.provider.XSLTJaxbProvider",
            "org.apache.commons.configuration.JNDIConfiguration",
            "org.apache.commons.configuration2.JNDIConfiguration",
            "org.apache.xalan.lib.sql.JNDIConnectionPool",
            "com.sun.org.apache.xalan.internal.lib.sql.JNDIConnectionPool",
            "org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS",
            "org.apache.commons.dbcp.datasources.PerUserPoolDataSource",
            "org.apache.commons.dbcp.datasources.SharedPoolDataSource",
            "com.p6spy.engine.spy.P6DataSource",
            "org.apache.log4j.receivers.db.DriverManagerConnectionSource",
            "org.apache.log4j.receivers.db.JNDIConnectionSource",
            "net.sf.ehcache.transaction.manager.selector.GenericJndiSelector",
            "net.sf.ehcache.transaction.manager.selector.GlassfishSelector",
            "org.apache.xbean.propertyeditor.JndiConverter",
            "org.apache.hadoop.shaded.com.zaxxer.hikari.HikariConfig",
            "com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig",
            "br.com.anteros.dbcp.AnterosDBCPConfig",
            "br.com.anteros.dbcp.AnterosDBCPDataSource",
            "javax.swing.JEditorPane",
            "javax.swing.JTextPane",
            "org.apache.shiro.realm.jndi.JndiRealmFactory",
            "org.apache.shiro.jndi.JndiObjectFactory",
            "org.apache.ignite.cache.jta.jndi.CacheJndiTmLookup",
            "org.apache.ignite.cache.jta.jndi.CacheJndiTmFactory",
            "org.quartz.utils.JNDIConnectionProvider",
            "org.apache.aries.transaction.jms.internal.XaPooledConnectionFactory",
            "org.apache.aries.transaction.jms.RecoverablePooledConnectionFactory",
            "com.caucho.config.types.ResourceRef",
            "org.aoju.bus.proxy.provider.RmiProvider",
            "org.aoju.bus.proxy.provider.remoting.RmiProvider",
            "org.apache.activemq.ActiveMQXAConnectionFactory",
            "org.apache.activemq.spring.ActiveMQConnectionFactory",
            "org.apache.activemq.spring.ActiveMQXAConnectionFactory",
            "org.apache.activemq.pool.PooledConnectionFactory",
            "org.apache.activemq.pool.XaPooledConnectionFactory",
            "org.apache.activemq.jms.pool.JcaPooledConnectionFactory",
            "org.apache.commons.proxy.provider.remoting.RmiProvider",
            "org.apache.commons.jelly.impl.Embedded",
            "oadd.org.apache.xalan.lib.sql.JNDIConnectionPool",
            "oadd.org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS",
            "oadd.org.apache.commons.dbcp.datasources.PerUserPoolDataSource",
            "oadd.org.apache.commons.dbcp.datasources.SharedPoolDataSource",
            "oracle.jms.AQjmsQueueConnectionFactory",
            "oracle.jms.AQjmsXATopicConnectionFactory",
            "oracle.jms.AQjmsTopicConnectionFactory",
            "oracle.jms.AQjmsXAQueueConnectionFactory",
            "oracle.jms.AQjmsXAConnectionFactory",
            "org.jsecurity.realm.jndi.JndiRealmFactory",
            "com.pastdev.httpcomponents.configuration.JndiConfiguration",
            "com.nqadmin.rowset.JdbcRowSetImpl",
            "org.arrah.framework.rdbms.UpdatableJdbcRowsetImpl",
            "org.apache.commons.dbcp2.datasources.PerUserPoolDataSource",
            "org.apache.commons.dbcp2.datasources.SharedPoolDataSource",
            "org.apache.commons.dbcp2.cpdsadapter.DriverAdapterCPDS",
            "com.newrelic.agent.deps.ch.qos.logback.core.db.JNDIConnectionSource",
            "com.newrelic.agent.deps.ch.qos.logback.core.db.DriverManagerConnectionSource",
            "org.apache.tomcat.dbcp.dbcp.cpdsadapter.DriverAdapterCPDS",
            "org.apache.tomcat.dbcp.dbcp.datasources.PerUserPoolDataSource",
            "org.apache.tomcat.dbcp.dbcp.datasources.SharedPoolDataSource",
            "org.apache.tomcat.dbcp.dbcp2.cpdsadapter.DriverAdapterCPDS",
            "org.apache.tomcat.dbcp.dbcp2.datasources.PerUserPoolDataSource",
            "org.apache.tomcat.dbcp.dbcp2.datasources.SharedPoolDataSource",
            "com.oracle.wls.shaded.org.apache.xalan.lib.sql.JNDIConnectionPool",
            "org.docx4j.org.apache.xalan.lib.sql.JNDIConnectionPool")
        .split(",");
  }

  public abstract static class AvroGroupConverter extends GroupConverter {
    protected final ParentValueContainer parent;

    public AvroGroupConverter(ParentValueContainer parent) {
      this.parent = parent;
    }
  }

  static class AvroPrimitiveConverter extends PrimitiveConverter {
    protected final ParentValueContainer parent;

    public AvroPrimitiveConverter(ParentValueContainer parent) {
      this.parent = parent;
    }
  }

  abstract static class BinaryConverter<T> extends AvroPrimitiveConverter {
    private T[] dict = null;

    public BinaryConverter(ParentValueContainer parent) {
      super(parent);
    }

    public abstract T convert(Binary binary);

    @Override
    public void addBinary(Binary value) {
      parent.add(convert(value));
    }

    @Override
    public boolean hasDictionarySupport() {
      return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setDictionary(Dictionary dictionary) {
      dict = (T[]) new Object[dictionary.getMaxId() + 1];
      for (int i = 0; i <= dictionary.getMaxId(); i++) {
        dict[i] = convert(dictionary.decodeToBinary(i));
      }
    }

    public T prepareDictionaryValue(T value) {
      return value;
    }

    @Override
    public void addValueFromDictionary(int dictionaryId) {
      parent.add(prepareDictionaryValue(dict[dictionaryId]));
    }
  }

  static final class FieldByteConverter extends AvroPrimitiveConverter {
    public FieldByteConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public void addInt(int value) {
      parent.addByte((byte) value);
    }
  }

  static final class FieldShortConverter extends AvroPrimitiveConverter {
    public FieldShortConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public void addInt(int value) {
      parent.addShort((short) value);
    }
  }

  static final class FieldCharConverter extends AvroPrimitiveConverter {
    public FieldCharConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public void addInt(int value) {
      parent.addChar((char) value);
    }
  }

  static final class FieldBooleanConverter extends AvroPrimitiveConverter {
    public FieldBooleanConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public final void addBoolean(boolean value) {
      parent.addBoolean(value);
    }
  }

  static final class FieldIntegerConverter extends AvroPrimitiveConverter {
    public FieldIntegerConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public final void addInt(int value) {
      parent.addInt(value);
    }
  }

  static final class FieldLongConverter extends AvroPrimitiveConverter {
    public FieldLongConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public final void addInt(int value) {
      parent.addLong((long) value);
    }

    @Override
    public final void addLong(long value) {
      parent.addLong(value);
    }
  }

  static final class FieldFloatConverter extends AvroPrimitiveConverter {
    public FieldFloatConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public final void addInt(int value) {
      parent.addFloat((float) value);
    }

    @Override
    public final void addLong(long value) {
      parent.addFloat((float) value);
    }

    @Override
    public final void addFloat(float value) {
      parent.addFloat(value);
    }
  }

  static final class FieldDoubleConverter extends AvroPrimitiveConverter {
    public FieldDoubleConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public final void addInt(int value) {
      parent.addDouble((double) value);
    }

    @Override
    public final void addLong(long value) {
      parent.addDouble((double) value);
    }

    @Override
    public final void addFloat(float value) {
      parent.addDouble((double) value);
    }

    @Override
    public final void addDouble(double value) {
      parent.addDouble(value);
    }
  }

  static final class FieldByteArrayConverter extends BinaryConverter<byte[]> {
    public FieldByteArrayConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public byte[] convert(Binary binary) {
      return binary.getBytes();
    }
  }

  static final class FieldByteBufferConverter extends BinaryConverter<ByteBuffer> {
    public FieldByteBufferConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public ByteBuffer convert(Binary binary) {
      return ByteBuffer.wrap(binary.getBytes());
    }

    @Override
    public ByteBuffer prepareDictionaryValue(ByteBuffer value) {
      return value.duplicate();
    }
  }

  static final class FieldStringConverter extends BinaryConverter<String> {
    public FieldStringConverter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public String convert(Binary binary) {
      return binary.toStringUsingUTF8();
    }
  }

  static final class FieldUTF8Converter extends BinaryConverter<Utf8> {
    public FieldUTF8Converter(ParentValueContainer parent) {
      super(parent);
    }

    @Override
    public Utf8 convert(Binary binary) {
      return new Utf8(binary.getBytes());
    }
  }

  static final class FieldStringableConverter extends BinaryConverter<Object> {
    private final String stringableName;
    private final Constructor<?> ctor;

    public FieldStringableConverter(ParentValueContainer parent, Class<?> stringableClass) {
      super(parent);
      checkSecurity(stringableClass);
      stringableName = stringableClass.getName();
      try {
        this.ctor = stringableClass.getConstructor(String.class);
      } catch (NoSuchMethodException e) {
        throw new ParquetDecodingException("Unable to get String constructor for " + stringableName, e);
      }
    }

    @Override
    public Object convert(Binary binary) {
      try {
        return ctor.newInstance(binary.toStringUsingUTF8());
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new ParquetDecodingException("Cannot convert binary to " + stringableName, e);
      }
    }

    private void checkSecurity(Class<?> clazz) throws SecurityException {
      List<String> trustedPackages = Arrays.asList(SERIALIZABLE_PACKAGES);
      List<String> denyPackages = Arrays.asList(DENY_PACKAGES);
      
      if (clazz.isPrimitive()) {
        return; // primitives are always allowed
      }

      Package thePackage = clazz.getPackage();
      if (thePackage == null) {
        throw new SecurityException("Class " + clazz + " has no package defined. "
            + "This is not allowed for classes used in Avro schema using java-class.");
      }
      
      // if trusted packages is '*' or empty, check denied packages
      boolean trustAllPackages = trustedPackages.size() == 1 && "*".equals(trustedPackages.get(0));
      if (trustAllPackages || trustedPackages.isEmpty()) {
        // Check if the class is in a denied package
        for (String denyPackage : denyPackages) {
          if (thePackage.getName().equals(denyPackage)
              || thePackage.getName().startsWith(denyPackage + ".")) {
            throw new SecurityException("Forbidden " + clazz
              + "! This class is not trusted to be included in Avro schema using java-class."
              + " The class is marked as denied due to being potentially unsafe.");
          }
        }
        return; // trustAllPackages && not denied
      }

      // Check if the class is in a trusted package
      for (String trustedPackage : trustedPackages) {
        if (thePackage.getName().equals(trustedPackage)
            || thePackage.getName().startsWith(trustedPackage + ".")) {
          return; // trusted package
        }
        throw new SecurityException("Forbidden " + clazz
            + "! This class is not trusted to be included in Avro schema using java-class."
            + " Please set org.apache.parquet.avro.SERIALIZABLE_PACKAGES system property"
            + " with the packages you trust.");
      }
    }
  }

  static final class FieldEnumConverter extends BinaryConverter<Object> {
    private final Schema schema;
    private final GenericData model;

    public FieldEnumConverter(ParentValueContainer parent, Schema enumSchema, GenericData model) {
      super(parent);
      this.schema = enumSchema;
      this.model = model;
    }

    @Override
    public Object convert(Binary binary) {
      return model.createEnum(binary.toStringUsingUTF8(), schema);
    }
  }

  static final class FieldFixedConverter extends BinaryConverter<Object> {
    private final Schema schema;
    private final GenericData model;

    public FieldFixedConverter(ParentValueContainer parent, Schema avroSchema, GenericData model) {
      super(parent);
      this.schema = avroSchema;
      this.model = model;
    }

    @Override
    public Object convert(Binary binary) {
      return model.createFixed(null /* reuse */, binary.getBytes(), schema);
    }
  }

  static final class FieldUUIDConverter extends BinaryConverter<String> {
    private final PrimitiveStringifier stringifier;

    public FieldUUIDConverter(ParentValueContainer parent, PrimitiveType type) {
      super(parent);
      stringifier = type.stringifier();
    }

    @Override
    public String convert(Binary binary) {
      return stringifier.stringify(binary);
    }
  }
}
