package uk.co.gresearch.siembol.common.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.function.Supplier;
/**
 * An object that implements an HTTP client
 *
 * <p>This class implements an HTTP client used in various Siembol components.
 * It supports kerberos authentication and methods for GET and POST requests.
 *
 * @author  Marian Novotny
 */
public class HttpProvider {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private static final int DEFAULT_TIMEOUT_MS = 60 * 1000;
    private final String host;
    private final Supplier<CloseableHttpClient> clientFactory;

    public HttpProvider(String uri, Supplier<CloseableHttpClient> clientFactory) {
        this.host = uri;
        this.clientFactory = clientFactory;
    }

    private String readBody(InputStream input) throws IOException {
        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(input))) {

            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        }
    }

    public String get(String path) throws IOException {
        try (CloseableHttpClient httpClient = clientFactory.get()) {
            String url = host + path;
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    LOG.error("Unsuccessful get request on url: {} return status line: {}",
                            url, response.getStatusLine().toString());
                    throw new IOException("Unsuccessful GET");
                }

                return readBody(response.getEntity().getContent());
            }
        } catch (Exception e) {
            LOG.error("Exception during executing GET request on url: {}{}", host, path);
            throw e;
        }
    }

    public String post(String path, String jsonBody) throws IOException {
        try (CloseableHttpClient httpClient = getKerberosHttpClient()) {
            String url = host + path;
            HttpPost httpPost = new HttpPost(url);

            StringEntity requestEntity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED &&
                        response.getStatusLine().getStatusCode() != HttpStatus.SC_OK &&
                        response.getStatusLine().getStatusCode() != HttpStatus.SC_BAD_REQUEST) {
                    LOG.error(String.format(
                            "Unsuccessful post request on url: %s, req body %s, status line: %s",
                            url, jsonBody, response.getStatusLine().toString()));
                    throw new IOException("Unsuccessful POST");
                }
                return readBody(response.getEntity().getContent());

            }
        } catch (Exception e) {
            LOG.error(String.format("Exception during executing POST request on url: %s%s\nbody: %s\nstack_trace: %s",
                    host, path, jsonBody, ExceptionUtils.getStackTrace(e)));
            throw e;
        }
    }

    public static CloseableHttpClient getKerberosHttpClient() {
        Credentials useJaasCreds = new Credentials() {
            public String getPassword() {
                return null;
            }

            public Principal getUserPrincipal() {
                return null;
            }
        };

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(null, -1, null), useJaasCreds);
        Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true, false))
                .build();

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(DEFAULT_TIMEOUT_MS)
                .setConnectTimeout(DEFAULT_TIMEOUT_MS)
                .setSocketTimeout(DEFAULT_TIMEOUT_MS).build();

        return HttpClients
                .custom()
                .setDefaultAuthSchemeRegistry(authSchemeRegistry)
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static CloseableHttpClient getHttpClient() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(DEFAULT_TIMEOUT_MS)
                .setConnectTimeout(DEFAULT_TIMEOUT_MS)
                .setSocketTimeout(DEFAULT_TIMEOUT_MS)
                .build();

        return HttpClients
                .custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}
