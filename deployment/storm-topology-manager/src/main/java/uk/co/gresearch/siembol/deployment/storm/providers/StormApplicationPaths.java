package uk.co.gresearch.siembol.deployment.storm.providers;

public enum StormApplicationPaths {
    LIST_TOPOLOGIES("/api/v1/topology/summary"),
    KILL_TOPOLOGY("/api/v1/topology/%s/kill/%d");

    private final String name;
    StormApplicationPaths(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}
