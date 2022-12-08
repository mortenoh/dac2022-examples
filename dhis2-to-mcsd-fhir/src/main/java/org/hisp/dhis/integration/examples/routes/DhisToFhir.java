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
package org.hisp.dhis.integration.examples.routes;

import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

@Component
public class DhisToFhir extends RouteBuilder
{
    @Override
    public void configure()
        throws Exception
    {
        from( "timer:foo?repeatCount=1" )
            .routeId( "Dhis2-to-mCSD-FHIR" )
            .setHeader( "CamelDhis2.queryParams", () -> Map.of(
                "order", List.of( "level" ),
                "filter", List.of( "level:le:2" ),
                "fields", List.of( "id,code,name,description,parent" ) ) )
            .to( "dhis2://get/collection?path=organisationUnits&fields=id,code,name,description,parent&itemType=org.hisp.dhis.api.model.v2_38_1.OrganisationUnit&client=#dhis2Client" )
            .split().body()
            .convertBodyTo( Bundle.class )
            // .marshal().fhirJson("R4")
            // .log( "Result = ${body}" );
            .to( "fhir://transaction/withBundle?client=#fhirClient" )
            .marshal().fhirJson( "R4" )
            .log( "Response = ${body}" );
    }
}