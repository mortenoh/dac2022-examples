/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.integration.examples;

import javax.jms.ConnectionFactory;

import lombok.RequiredArgsConstructor;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.hisp.dhis.integration.examples.configuration.Dhis2Properties;
import org.hisp.dhis.integration.examples.configuration.FhirProperties;
import org.hisp.dhis.integration.sdk.Dhis2ClientBuilder;
import org.hisp.dhis.integration.sdk.api.Dhis2Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;

@RequiredArgsConstructor
@SpringBootApplication
@EnableConfigurationProperties( { Dhis2Properties.class, FhirProperties.class } )
public class Main
{
    private final Dhis2Properties dhis2Properties;

    private final FhirProperties fhirProperties;

    public static void main( String[] args )
    {
        SpringApplication.run( Main.class, args );
    }

    @Bean
    public Dhis2Client dhis2Client()
    {
        return Dhis2ClientBuilder
            .newClient( dhis2Properties.getBaseUrl(), dhis2Properties.getUsername(), dhis2Properties.getPassword() )
            .build();
    }

    @Bean
    public FhirContext fhirContext()
    {
        return FhirVersionEnum.R4.newContext();
    }

    @Bean
    public IGenericClient fhirClient( FhirContext fhirContext )
    {
        return fhirContext.newRestfulGenericClient( fhirProperties.getServerUrl() );
    }

    @Bean
    public ConnectionFactory connectionFactory()
    {
        return new ActiveMQConnectionFactory( "tcp://localhost:61616" );
    }
}
