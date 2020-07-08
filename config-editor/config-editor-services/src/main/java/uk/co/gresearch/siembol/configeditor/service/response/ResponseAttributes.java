package uk.co.gresearch.siembol.configeditor.service.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseAttributes {
    @JsonProperty("response.url")
    private String responseUrl;
    @JsonProperty("response.authentication")
    private ResponseAuthenticationType responseAuthenticationType;

    public String getResponseUrl() {
        return responseUrl;
    }

    public void setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
    }

    public ResponseAuthenticationType getResponseAuthenticationType() {
        return responseAuthenticationType;
    }

    public void setResponseAuthenticationType(ResponseAuthenticationType responseAuthenticationType) {
        this.responseAuthenticationType = responseAuthenticationType;
    }
}
