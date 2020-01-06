/*
* Copyright 2008-2020 Async-IO.org
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/
/*
 * Copyright 2009 Richard Zschech.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.gwt20.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.IncrementalGenerator;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.RebindMode;
import com.google.gwt.core.ext.RebindResult;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.Serializer;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracle;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracleBuilder;
import com.google.gwt.user.rebind.rpc.TypeSerializerCreator;
import org.atmosphere.gwt20.client.GwtRpcSerialTypes;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SerializerGenerator extends IncrementalGenerator {
    private static final Constructor<SerializableTypeOracleBuilder> SERIALIZABLE_TYPE_ORACLE_BUILDER_GWT25_CTOR =
            probeSerializableTypeOracleBuilderConstructor(TreeLogger.class, PropertyOracle.class, GeneratorContext.class);
    private static final Constructor<SerializableTypeOracleBuilder> SERIALIZABLE_TYPE_ORACLE_BUILDER_GWT28_CTOR =
            probeSerializableTypeOracleBuilderConstructor(TreeLogger.class, GeneratorContext.class);

    private static Constructor<SerializableTypeOracleBuilder> probeSerializableTypeOracleBuilderConstructor(Class<?>... args) {
        try {
            return SerializableTypeOracleBuilder.class.getConstructor(args);
        } catch (Throwable t) {
            return null;
        }
    }
    private static SerializableTypeOracleBuilder createSerializableTypeOracleBuilder(TreeLogger logger, PropertyOracle propertyOracle, GeneratorContext context) throws UnableToCompleteException {
        SerializableTypeOracleBuilder obj = null;
        try {
            if (SERIALIZABLE_TYPE_ORACLE_BUILDER_GWT25_CTOR != null) {
                obj = SERIALIZABLE_TYPE_ORACLE_BUILDER_GWT25_CTOR.newInstance(logger, propertyOracle, context);
            } else {
                obj = SERIALIZABLE_TYPE_ORACLE_BUILDER_GWT28_CTOR.newInstance(logger, context);
            }
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof UnableToCompleteException) {
                throw (UnableToCompleteException)e.getTargetException();
            } else if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            // ignore
        }
        return obj;
    }

    @Override
    public long getVersionId() {
        return 1L;
    }

    @Override
    public RebindResult generateIncrementally(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {

        TypeOracle typeOracle = context.getTypeOracle();

        // Create the CometSerializer impl
        String packageName = "comet";
        String className = typeName.replace('.', '_') + "Impl";
        PrintWriter printWriter = context.tryCreate(logger, packageName, className);

        if (printWriter != null) {

            try {
                JClassType type = typeOracle.getType(typeName);
                GwtRpcSerialTypes annotation = type.getAnnotation(GwtRpcSerialTypes.class);
                if (annotation == null) {
                    logger.log(TreeLogger.ERROR, "No SerialTypes annotation on CometSerializer type: " + typeName);
                    throw new UnableToCompleteException();
                }

                SerializableTypeOracleBuilder typesSentToBrowserBuilder = createSerializableTypeOracleBuilder(
                        logger, context.getPropertyOracle(), context);
                SerializableTypeOracleBuilder typesSentFromBrowserBuilder = createSerializableTypeOracleBuilder(
                        logger, context.getPropertyOracle(), context);

                List<Class<?>> serializableTypes = new ArrayList();
                Collections.addAll(serializableTypes, annotation.value());
                for (Class<?> serializable : serializableTypes) {
                    int rank = 0;
                    if (serializable.isArray()) {
                        while (serializable.isArray()) {
                            serializable = (Class<?>) serializable.getComponentType();
                            rank++;
                        }
                    }

                    JType resolvedType = typeOracle.getType(serializable.getCanonicalName());
                    while (rank > 0) {
                        resolvedType = typeOracle.getArrayType(resolvedType);
                        rank--;
                    }

                    typesSentToBrowserBuilder.addRootType(logger, resolvedType);
                    typesSentFromBrowserBuilder.addRootType(logger, resolvedType);
                }

                // Create a resource file to receive all of the serialization information
                // computed by STOB and mark it as private so it does not end up in the
                // output.
                OutputStream pathInfo = context.tryCreateResource(logger, typeName + ".rpc.log");
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(pathInfo));
                writer.write("====================================\n");
                writer.write("Types potentially sent from server:\n");
                writer.write("====================================\n\n");
                writer.flush();

                typesSentToBrowserBuilder.setLogOutputWriter(writer);
                SerializableTypeOracle typesSentToBrowser = typesSentToBrowserBuilder.build(logger);

                writer.write("===================================\n");
                writer.write("Types potentially sent from browser:\n");
                writer.write("===================================\n\n");
                writer.flush();
                typesSentFromBrowserBuilder.setLogOutputWriter(writer);
                SerializableTypeOracle typesSentFromBrowser = typesSentFromBrowserBuilder.build(logger);

                writer.close();

                if (pathInfo != null) {
                    context.commitResource(logger, pathInfo).setVisibility(Visibility.Private);
                }

                // Create the serializer
                final String modifiedTypeName = typeName.replace('.', '_');
                TypeSerializerCreator tsc = new TypeSerializerCreator(logger, typesSentFromBrowser, typesSentToBrowser, context, "comet." + modifiedTypeName, modifiedTypeName);
                String realize = tsc.realize(logger);

                // Create the CometSerializer impl
                ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(packageName, className);

                composerFactory.addImport(Serializer.class.getName());
                composerFactory.addImport(SerializationException.class.getName());
                composerFactory.addImport(Serializable.class.getName());

                composerFactory.setSuperclass(typeName);
                SourceWriter sourceWriter = composerFactory.createSourceWriter(context, printWriter);
                sourceWriter.print("private Serializer SERIALIZER = new " + realize + "();");
                sourceWriter.print("protected Serializer getRPCSerializer() {return SERIALIZER;}");
                sourceWriter.commit(logger);

            } catch (NotFoundException e) {
                logger.log(TreeLogger.ERROR, "", e);
                throw new UnableToCompleteException();
            }
        }

        return new RebindResult(RebindMode.USE_ALL_NEW_WITH_NO_CACHING, packageName + '.' + className);
    }

}
