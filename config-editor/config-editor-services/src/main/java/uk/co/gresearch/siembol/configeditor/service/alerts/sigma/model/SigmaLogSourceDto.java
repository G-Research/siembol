package uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model;

public class SigmaLogSourceDto {
    private String category;
    private String product;
    private String service;
    private String definition;

    public void setCategory(String category) {
        this.category = category;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
