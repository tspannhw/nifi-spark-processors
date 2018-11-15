/*
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
package org.apache.nifi.processors.sparkProcessors;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;

@Tags({"spark","DataFrame"})
@CapabilityDescription("Create a spark dataframe")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class SaveDataFrame extends AbstractProcessor {

    public static final PropertyDescriptor DF_NAME = new PropertyDescriptor
            .Builder().name("DF_NAME")
            .displayName("Name of the DF")
            .description("Name for your DF, ex. salesDF")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor FORMAT = new PropertyDescriptor
            .Builder().name("FORMAT")
            .displayName("Format of the Data")
            .description("Format of the data")
            .required(true)
            .defaultValue("CSV")
            .allowableValues("JSON","CSV")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor LOCATION = new PropertyDescriptor
            .Builder().name("LOCATION")
            .displayName("HDFS Location for your data")
            .description("HDFS location for your data, ex. /sales/data")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("Success relationship")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(DF_NAME);
        descriptors.add(LOCATION);
        descriptors.add(FORMAT);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(SUCCESS);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            return;
        }
        String df_name=context.getProperty(DF_NAME).getValue();
        final StringBuilder sb = new StringBuilder();
        //sb.append("from pyspark.sql import SQLContext\n");
        //sb.append("sqlContext = SQLContext(sc)\n");
        String format = context.getProperty(FORMAT).getValue();
        String location = context.getProperty(LOCATION).getValue();
        sb.append("\n");
        if("JSON".equals(format)){
        	sb.append(df_name+".write.json(\""+location+"\")\n");
        }else {
        sb.append(df_name+".write.csv(\""+ location+"\")\n");
        }
        sb.append("\n");
        //final String data = sb.toString();
        flowFile = session.append(flowFile, new OutputStreamCallback() {
                public void process(final OutputStream out) throws IOException {
                    out.write(sb.toString().getBytes());
                }
            });
        session.transfer(flowFile, SUCCESS);
        // TODO implement
    }
}
