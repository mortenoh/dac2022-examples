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
package org.hisp.dhis.integration.examples.ds.routes;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.fhir.internal.FhirConstants;
import org.hisp.dhis.integration.examples.ds.configuration.Dhis2Properties;
import org.hisp.dhis.integration.examples.ds.domain.OrganisationUnit;
import org.hisp.dhis.integration.examples.ds.domain.OrganisationUnits;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DhisToFhir extends RouteBuilder
{
    private final Dhis2Properties dhis2Properties;

    @Override
    public void configure()
        throws Exception
    {
        from( "timer:foo?repeatCount=1" )
            .routeId( "Dhis2-to-mCSD-FHIR-DS" )
            .setHeader( "CamelDhis2.queryParams", () -> Map.of(
                "order", List.of( "level" ),
                "paging", List.of( "false" ),
                "fields", List.of( "id,code,name,description,parent" ) ) )
            .to( "dhis2://get/resource?path=organisationUnits&client=#dhis2Client" )
            .unmarshal().json( OrganisationUnits.class )
            .split().method( new SplitterBean(), "splitOrgUnits" )
            .process( x -> x.getMessage().setHeader( "baseUrl", dhis2Properties.getBaseUrl().replace( "/api", "" ) ) )
            .transform( datasonnet( "resource:classpath:organisationUnit.ds", Map.class, "application/x-java-object",
                "application/x-java-object" ) )
            .marshal().json( String.class )
            .unmarshal().fhirJson( "R4" )
            .process( x -> x.getIn().setHeader( FhirConstants.PROPERTY_PREFIX + "bundle", x.getMessage().getBody() ) )
            .to( "fhir://transaction/withBundle?client=#fhirClient" )
            .marshal().fhirJson( "R4" )
            .log( "Result = ${body}" );
    }
}

class SplitterBean
{
    public List<OrganisationUnit> splitOrgUnits( OrganisationUnits organisationUnits )
    {
        return organisationUnits.getOrganisationUnits();
    }
}
