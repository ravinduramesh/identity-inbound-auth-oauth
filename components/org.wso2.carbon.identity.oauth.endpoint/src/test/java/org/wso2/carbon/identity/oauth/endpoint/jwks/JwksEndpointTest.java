/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.oauth.endpoint.jwks;

import com.nimbusds.jose.JWSAlgorithm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.tokenprocessor.TokenPersistenceProcessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.internal.OAuth2ServiceComponentHolder;
import org.wso2.carbon.identity.oauth2.keyidprovider.DefaultKeyIDProviderImpl;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.testutil.powermock.PowerMockIdentityBaseTest;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@PrepareForTest({CarbonUtils.class, IdentityTenantUtil.class, IdentityUtil.class, OAuthServerConfiguration.class,
        KeyStoreManager.class, OAuth2Util.class, FrameworkUtils.class})
public class JwksEndpointTest extends PowerMockIdentityBaseTest {

    @Mock
    ServerConfiguration serverConfiguration;

    @Mock
    OAuthServerConfiguration oAuthServerConfiguration;

    @Mock
    TokenPersistenceProcessor tokenPersistenceProcessor;

    @Mock
    KeyStoreManager keyStoreManager;

    private static final String CERT_THUMB_PRINT = "generatedCertThrumbPrint";
    private static final String ALG = "RS256";
    private static final String USE = "sig";
    private static final JSONArray X5CJA = new JSONArray();
    private static final String X5T = "ZHVtbXlUaHVtYlByaW50VmFsdWU";
    private JwksEndpoint jwksEndpoint;
    private Object identityUtilObj;

    @BeforeTest
    public void setUp() throws Exception {

        System.setProperty(
                CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString()
                          );
        jwksEndpoint = new JwksEndpoint();

        Class<?> clazz = IdentityUtil.class;
        identityUtilObj = clazz.newInstance();
        OAuth2ServiceComponentHolder.setKeyIDProvider(new DefaultKeyIDProviderImpl());

        X5CJA.put("MIIDdzCCAl+gAwIBAgIEdWyfgTANBgkqhkiG9w0BAQsFADBsMRAwDgYDVQQGEwdVbmtub3duMRAwDgYDVQQIEwdVbmtub3duMR" +
                "AwDgYDVQQHEwdVbmtub3duMRAwDgYDVQQKEwdVbmtub3duMRAwDgYDVQQLEwdVbmtub3duMRAwDgYDVQQDEwdVbmtub3duMB4XDT" +
                "E3MTAxNzEwMTIzN1oXDTE4MDExNTEwMTIzN1owbDEQMA4GA1UEBhMHVW5rbm93bjEQMA4GA1UECBMHVW5rbm93bjEQMA4GA1UEBx" +
                "MHVW5rbm93bjEQMA4GA1UEChMHVW5rbm93bjEQMA4GA1UECxMHVW5rbm93bjEQMA4GA1UEAxMHVW5rbm93bjCCASIwDQYJKoZIhv" +
                "cNAQEBBQADggEPADCCAQoCggEBAJo+aKKahtqFCIZ2xoJoqXLZ7fXg47xpcNT/RZEu3Fbn0jnK1CbjhuAlzs/Iy9WmlFCROt4UuZ" +
                "d6x23se9AwKJ/YqDKKUE24ofC5SG+aFWooNfXBN9l0BDNJxrml1KeGSzGoJ7inGW0JFHt3QCu1lOtpmK4hGWBnF3G8wsL0eu1nEy" +
                "O9GBzTSafcnXGhb09LyY9ABSj5ycM7ZIGO6o/afqGO+onVTkNyufsPfNR3+Bc2rqS290kDPhWrIUenN2QwC9cHjCe0zs5zbRwSWW" +
                "OQJ1ubr5WIP0BfeuafxdcxUjDMRYHLIRqiWALUWFFV3KkAdnFguggHfdHP7rrfKg6pDokCAwEAAaMhMB8wHQYDVR0OBBYEFNDI9T" +
                "3betMAjiEAH2NIwB0r2inLMA0GCSqGSIb3DQEBCwUAA4IBAQA3yJEkKywyQ2z4VyEH3aZ4ouEpkk4YgvW2qxwHbono7ZhmEPw4rl" +
                "R1C7ekRbwxpYpO8OY4scKsRWvb7ogX1MyTefLcpwxSMFqW4hVZbY69txdac8PmeQZOCWxGql0x4SezX0p+zhK+YEG6eLtvPOb1Lm" +
                "DckXNLGawrkUbaKzg0pVYVF+z3M20HcehfHILlfGGYim+qoo7K47guTrrulUnuLVDcJU4gWjX1zb7RzGLKs1s/JBYXGKfCzQqR6f" +
                "WiMn1IY5E5kfPa45xh3KndTlYP6jjpjR89Afvipv6Pus0LKk7fWotvDKM5L6j5ui/sowFe2k4/Q0Rfcskm3yf7IOZm");
        X5CJA.put("MIIDSTCCAjGgAwIBAgIEAoLQ/TANBgkqhkiG9w0BAQsFADBVMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExFjAUBgNVBAcTDU" +
                "1vdW50YWluIFZpZXcxDTALBgNVBAoTBFdTTzIxEjAQBgNVBAMTCWxvY2FsaG9zdDAeFw0xNzA3MTkwNjUyNTFaFw0yNzA3MTcwNj" +
                "UyNTFaMFUxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzENMAsGA1UEChMEV1NPMjESMB" +
                "AGA1UEAxMJbG9jYWxob3N0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAluZFdW1ynitztkWLC6xKegbRWxky+5P0p4" +
                "ShYEOkHs30QI2VCuR6Qo4Bz5rTgLBrky03W1GAVrZxuvKRGj9V9+PmjdGtau4CTXu9pLLcqnruaczoSdvBYA3lS9a7zgFU0+s6kM" +
                "l2EhB+rk7gXluEep7lIOenzfl2f6IoTKa2fVgVd3YKiSGsyL4tztS70vmmX121qm0sTJdKWP4HxXyqK9neolXI9fYyHOYILVNZ69" +
                "z/73OOVhkh/mvTmWZLM7GM6sApmyLX6OXUp8z0pkY+vT/9+zRxxQs7GurC4/C1nK3rI/0ySUgGEafO1atNjYmlFN+M3tZX6nEcA6" +
                "g94IavyQIDAQABoyEwHzAdBgNVHQ4EFgQUtS8kIYxQ8UVvVrZSdgyide9OHxUwDQYJKoZIhvcNAQELBQADggEBABfk5mqsVUrpFC" +
                "YTZZhOxTRRpGXqoW1G05bOxHxs42Paxw8rAJ06Pty9jqM1CgRPpqvZa2lPQBQqZrHkdDE06q4NG0DqMH8NT+tNkXBe9YTre3EJCS" +
                "fsvswtLVDZ7GDvTHKojJjQvdVCzRj6XH5Truwefb4BJz9APtnlyJIvjHk1hdozqyOniVZd0QOxLAbcdt946chNdQvCm6aUOputp8" +
                "Xogr0KBnEy3U8es2cAfNZaEkPU8Va5bU6Xjny8zGQnXCXxPKp7sMpgO93nPBt/liX1qfyXM7xEotWoxmm6HZx8oWQ8U5aiXjZ5RK" +
                "DWCCq4ZuXl6wVsUz1iE61suO5yWi8=");
    }

    @DataProvider(name = "provideTenantDomain")
    public Object[][] provideTenantDomain() {

        return new Object[][]{
                {null, MultitenantConstants.SUPER_TENANT_ID},
                {"", MultitenantConstants.SUPER_TENANT_ID},
                {"foo.com", 1},
                {"invalid.com", -1},
        };
    }

    @Test(dataProvider = "provideTenantDomain")
    public void testJwks(String tenantDomain, int tenantId) throws Exception {

        Path keystorePath = Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), "repository", "resources",
                "security", "wso2carbon.jks");
        mockOAuthServerConfiguration();
        mockStatic(CarbonUtils.class);
        when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        when(serverConfiguration.getFirstProperty("Security.KeyStore.Location")).thenReturn(keystorePath.toString());
        when(serverConfiguration.getFirstProperty("Security.KeyStore.Password")).thenReturn("wso2carbon");
        when(serverConfiguration.getFirstProperty("Security.KeyStore.KeyAlias")).thenReturn("wso2carbon");

        ThreadLocal<Map<String, Object>> threadLocalProperties = new ThreadLocal() {
            protected Map<String, Object> initialValue() {

                return new HashMap();
            }
        };

        threadLocalProperties.get().put(OAuthConstants.TENANT_NAME_FROM_CONTEXT, tenantDomain);

        Field threadLocalPropertiesField = identityUtilObj.getClass().getDeclaredField("threadLocalProperties");
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(threadLocalPropertiesField, threadLocalPropertiesField.getModifiers() & ~Modifier.FINAL);
        threadLocalPropertiesField.setAccessible(true);
        threadLocalPropertiesField.set(identityUtilObj, threadLocalProperties);

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(anyString())).thenReturn(tenantId);

        mockStatic(FrameworkUtils.class);
        doNothing().when(FrameworkUtils.class, "startTenantFlow", "foo.com");
        doNothing().when(FrameworkUtils.class, "endTenantFlow");

        mockStatic(OAuth2Util.class);

        if (tenantDomain == null) {
            when(OAuth2Util.getKID(any(), any(), anyString())).thenThrow(new IdentityOAuth2Exception("error"));

        } else {
            when(OAuth2Util.getKID(any(), any(), anyString())).thenReturn(CERT_THUMB_PRINT);
        }
        when(OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm("SHA256withRSA")).thenReturn(JWSAlgorithm.RS256);
        when(OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm("SHA512withRSA")).thenReturn(JWSAlgorithm.RS512);
        when(OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm("SHA384withRSA")).thenReturn(JWSAlgorithm.RS384);
        if ("foo.com".equals(tenantDomain)) {
            when(OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm("SHA512withRSA")).thenReturn(JWSAlgorithm.RS256);
        }
        when(OAuth2Util.getThumbPrint(any(), anyString())).thenReturn("dummyThumbPrintValue");
        mockStatic(KeyStoreManager.class);
        when(KeyStoreManager.getInstance(anyInt())).thenReturn(keyStoreManager);
        when(keyStoreManager.getKeyStore("foo-com.jks")).thenReturn(getKeyStoreFromFile("foo-com.jks", "foo.com"));

        String result = jwksEndpoint.jwks();

        try {
            JSONObject jwksJson = new JSONObject(result);
            JSONArray objectArray = jwksJson.getJSONArray("keys");
            JSONObject keyObject = objectArray.getJSONObject(0);
            assertEquals(keyObject.get("kid"), CERT_THUMB_PRINT, "Incorrect kid value");
            assertEquals(keyObject.get("alg"), ALG, "Incorrect alg value");
            assertEquals(keyObject.get("use"), USE, "Incorrect use value");
            assertEquals(keyObject.get("kty"), "RSA", "Incorrect kty value");
            assertEquals(keyObject.get("x5t#S256"), X5T, "Incorrect x5t#S256 value");
            if ("foo.com".equals(tenantDomain)) {
                assertEquals(objectArray.length(), 2, "Incorrect no of keysets");
                assertEquals(((JSONArray) keyObject.get("x5c")).get(0), X5CJA.get(0), "Incorrect x5c value");
            } else {
                assertEquals(objectArray.length(), 3, "Incorrect no of keysets");
                assertEquals(((JSONArray) keyObject.get("x5c")).get(0), X5CJA.get(1), "Incorrect x5c value");
            }
        } catch (JSONException e) {
            if ("invalid.com".equals(tenantDomain)) {
                // This is expected. We don't validate for invalid tenants.
                assertTrue(true);
            } else if (tenantDomain == null) {
                assertTrue(result.contains("Error while generating the keyset for"),
                        "Error message for thrown exception is not found");
            } else {
                fail("Unexpected exception: " + e.getMessage());
            }
        }

        threadLocalProperties.get().remove(OAuthConstants.TENANT_NAME_FROM_CONTEXT);
    }

    private void mockOAuthServerConfiguration() throws Exception {

        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        when(oAuthServerConfiguration.getPersistenceProcessor()).thenReturn(tokenPersistenceProcessor);
        when(oAuthServerConfiguration.getIdTokenSignatureAlgorithm()).thenReturn("SHA512withRSA");
        when(oAuthServerConfiguration.getSignatureAlgorithm()).thenReturn("SHA256withRSA");
        when(oAuthServerConfiguration.getUserInfoJWTSignatureAlgorithm()).thenReturn("SHA384withRSA");
    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password) throws Exception {

        Path tenantKeystorePath = Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), "repository",
                "resources", "security", keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(file, password.toCharArray());
        return keystore;
    }
}
