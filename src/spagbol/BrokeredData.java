package spagbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class BrokeredData {
    private final String id;
    private final DataLoader dataLoader;
    private final int refreshInterval;
    private final TimeUnit refreshIntervalUnit;
    private List<Map<String,Object>> data = new ArrayList<Map<String, Object>>();
    private final CountDownLatch initialised = new CountDownLatch(1);

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

    void loadData() throws Exception {
        data = dataLoader.loadData();
        initialised.countDown();
    }

    public String getId() {
        return id;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public boolean awaitInitialisation() throws InterruptedException {
        return initialised.await(3, TimeUnit.SECONDS);
    }
}
