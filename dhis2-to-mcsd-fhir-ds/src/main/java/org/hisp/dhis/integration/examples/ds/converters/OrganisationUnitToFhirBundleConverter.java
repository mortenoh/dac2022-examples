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
package org.hisp.dhis.integration.examples.ds.converters;

import static org.springframework.util.StringUtils.hasText;

import lombok.RequiredArgsConstructor;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;
import org.apache.camel.component.fhir.internal.FhirConstants;
import org.hisp.dhis.integration.examples.ds.configuration.Dhis2Properties;
import org.hisp.dhis.integration.examples.ds.domain.OrganisationUnit;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganisationUnitToFhirBundleConverter implements TypeConverters
{
    private final Dhis2Properties dhis2Properties;

    @Converter
    public Bundle ouToFhir( OrganisationUnit organisationUnit, Exchange exchange )
    {
        Bundle bundle = new Bundle().setType( Bundle.BundleType.TRANSACTION );

        // Organization
        Organization organization = new Organization();
        organization.setId( organisationUnit.getId() );
        organization.setName( organisationUnit.getName() );

        organization.getIdentifier().add(
            new Identifier().setSystem( dhis2Properties.getBaseUrl() + "/api/organisationUnits" )
                .setValue( organisationUnit.getId() ) );

        if ( hasText( organisationUnit.getCode() ) )
        {
            organization.getIdentifier().add(
                new Identifier().setSystem( dhis2Properties.getBaseUrl() + "/api/organisationUnits" )
                    .setValue( organisationUnit.getCode() ) );
        }

        organization.addType(
            new CodeableConcept(
                new Coding( "http://terminology.hl7.org/CodeSystem/organization-type", "prov", "Facility" ) ) );

        bundle.addEntry().setResource( organization ).getRequest().setMethod( Bundle.HTTPVerb.PUT )
            .setUrl( "Organization?identifier=" + organisationUnit.getId() );

        // Location
        Location location = new Location();
        location.setId( organisationUnit.getId() );
        location.setName( organisationUnit.getName() );

        location.getMeta().getProfile()
            .add( new CanonicalType( "https://ihe.net/fhir/StructureDefinition/IHE_mCSD_Location" ) );

        location.getIdentifier().add(
            new Identifier().setSystem( dhis2Properties.getBaseUrl() + "/api/organisationUnits" )
                .setValue( organisationUnit.getId() ) );

        if ( hasText( organisationUnit.getCode() ) )
        {
            location.getIdentifier().add(
                new Identifier().setSystem( dhis2Properties.getBaseUrl() + "/api/organisationUnits" )
                    .setValue( organisationUnit.getCode() ) );
        }

        if ( hasText( organisationUnit.getDescription() ) )
        {
            location.setDescription( organisationUnit.getDescription() );
        }
        else
        {
            location.setDescription( organisationUnit.getName() );
        }

        location.getManagingOrganization().setReference( "Organization/" + organisationUnit.getId() );
        location.setMode( Location.LocationMode.INSTANCE );

        if ( organisationUnit.getParent() != null )
        {
            location.getPartOf().setReference( "Location/" + organisationUnit.getId() );
        }

        location.getPhysicalType().addCoding(
            new Coding().setSystem( "http://terminology.hl7.org/CodeSystem/location-physical-type" ).setCode( "si" ) );

        location.setStatus( Location.LocationStatus.ACTIVE );
        location.getType().add( new CodeableConcept( new Coding().setCode( "OF" ) ) );

        bundle.addEntry().setResource( location ).getRequest().setMethod( Bundle.HTTPVerb.PUT )
            .setUrl( "Location?identifier=" + organisationUnit.getId() );

        exchange.getIn().setHeader( FhirConstants.PROPERTY_PREFIX + "bundle", bundle );

        return bundle;
    }
}
