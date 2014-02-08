package spagbol;

import java.util.concurrent.TimeUnit;

public class BrokeredData {
    private final DataLoader dataLoader;
    private final int refreshInterval;
    private final TimeUnit refreshIntervalUnit;

    public BrokeredData(DataLoader dataLoader, int refreshInterval, TimeUnit refreshIntervalUnit) {
        this.dataLoader = dataLoader;
        this.refreshInterval = refreshInterval;
        this.refreshIntervalUnit = refreshIntervalUnit;
    }

    boolean isRefreshable() {
        return true;
    }

    long getRefreshInterval() {
        return refreshInterval;
    }

    TimeUnit getRefreshIntervalUnit() {
        return refreshIntervalUnit;
    }

    void loadData() {
        dataLoader.loadData();
    }
}
