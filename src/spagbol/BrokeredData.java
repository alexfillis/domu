package spagbol;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BrokeredData {
    private String id;
    private final DataLoader dataLoader;
    private final int refreshInterval;
    private final TimeUnit refreshIntervalUnit;
    private List<Map<String,Object>> data;

    public BrokeredData(String id, DataLoader dataLoader, int refreshInterval, TimeUnit refreshIntervalUnit) {
        this.id = id;
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
        data = dataLoader.loadData();
    }

    public String getId() {
        return id;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }
}
