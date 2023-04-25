/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package fim.unipassau.de.scratchLog.spring.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The SAML2 properties necessary to complete the authentication process with the service. This class has been adapted
 * from the Artemis project.
 */
@Component
public class SAML2Properties {

    /**
     * The name of the regular expression capture group which should match the actual value.
     */
    public static final String ATTRIBUTE_VALUE_EXTRACTION_GROUP_NAME = "value";

    /**
     * The username pattern for retrieving the username from the SAML2 authentication.
     */
    @Value("${saml.username}")
    private String usernamePattern;

    /**
     * The email pattern for retrieving the username from the SAML2 authentication.
     */
    @Value("${saml.email}")
    private String emailPattern;

    /**
     * The metadata necessary for communicating with the IdP.
     */
    @Value("${saml.metadata}")
    private String metadata;

    /**
     * The name of the IdP.
     */
    @Value("${saml.idp}")
    private String idpName;

    /**
     * The id of the SP.
     */
    @Value("${saml.entity}")
    private String entityId;

    /**
     * The path to the certificate.
     */
    @Value("${saml.certificate}")
    private String certFile;

    /**
     * The path to the key.
     */
    @Value("${saml.key}")
    private String keyFile;

    /**
     * The key pattern for extracting parts of attribute values.
     */
    @Value("${saml.extraction.key}")
    private String key;

    /**
     * The value pattern for the key pattern.
     */
    @Value("${saml.extraction.value}")
    private String valuePattern;

    /**
     * Returns the username pattern.
     *
     * @return The username pattern.
     */
    public String getUsernamePattern() {
        return usernamePattern;
    }

    /**
     * Returns the email pattern.
     *
     * @return The email pattern.
     */
    public String getEmailPattern() {
        return emailPattern;
    }

    /**
     * Returns the path to the metadata file.
     *
     * @return The file path.
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Returns the name of the IdP.
     *
     * @return The IdP name.
     */
    public String getIdpName() {
        return idpName;
    }

    /**
     * Returns the name of the SP.
     *
     * @return The SP name.
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Returns the path to the certificate.
     *
     * @return The certificate file path.
     */
    public String getCertFile() {
        return certFile;
    }

    /**
     * Returns the path to the key file.
     *
     * @return The key file path.
     */
    public String getKeyFile() {
        return keyFile;
    }

    /**
     * Returns the key pattern.
     *
     * @return The key pattern.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the value pattern.
     *
     * @return The value pattern.
     */
    public String getValuePattern() {
        return valuePattern;
    }

}
