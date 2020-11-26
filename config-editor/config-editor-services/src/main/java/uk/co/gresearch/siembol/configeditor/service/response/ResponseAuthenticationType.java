package uk.co.gresearch.siembol.configeditor.service.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.impl.client.CloseableHttpClient;
import uk.co.gresearch.siembol.common.utils.HttpProvider;

import java.util.function.Supplier;

public enum ResponseAuthenticationType {
    @JsonProperty("disabled") DISABLED("disabled", HttpProvider::getHttpClient),
    @JsonProperty("kerberos") KERBEROS("kerberos", HttpProvider::getKerberosHttpClient);
    private final String name;
    private final Supplier<CloseableHttpClient> httpClientFactory;
    ResponseAuthenticationType(String name, Supplier<CloseableHttpClient> httpClientFactory) {
        this.name = name;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public String toString() {
        return name;
    }

    public Supplier<CloseableHttpClient> getHttpClientFactory() {
        return httpClientFactory;
    }
}
