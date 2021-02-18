package uk.co.gresearch.siembol.configeditor.sync.common;

import java.util.Optional;

public enum SynchronisationType {
    RELEASE,
    ADMIN_CONFIG,
    ALL;

    public static Optional<SynchronisationType> fromString(String syncTypeStr) {
        for (SynchronisationType type: SynchronisationType.values()) {
            if (type.toString().equalsIgnoreCase(syncTypeStr)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public boolean isReleaseEnabled() {
        return this == RELEASE || this == ALL;
    }

    public boolean isAdminConfigEnabled() {
        return this == ADMIN_CONFIG || this == ALL;
    }
}
