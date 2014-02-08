package spagbol;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

public class DataBrokerTest {
    @Test
    public void should_refresh_data_at_specified_interval() throws InterruptedException {
        CountDownLatch dataLoaded = new CountDownLatch(2);
        int interval = 5;
        TimeUnit intervalUnit = TimeUnit.SECONDS;
        new DataBroker(
                Executors.newSingleThreadScheduledExecutor(),
                new BrokeredData("", newTestDataLoader(dataLoaded), interval, intervalUnit));

        // execute
        long start = currentTimeMillis();
        boolean timedOut = !dataLoaded.await(intervalUnit.toMillis(interval) + 500, TimeUnit.MILLISECONDS);
        long end = currentTimeMillis();

        //verify
        assertFalse("Timed out!", timedOut);
        assertTrue("refreshed too soon", (end - start) > earliestAllowedRefresh(interval, intervalUnit));
        assertTrue("refreshed too late", (end - start) < latestAllowedRefresh(interval, intervalUnit));
    }

    @Test
    public void should_not_have_to_wait_for_interval_for_data_to_be_available() {
        String dataId = "testdata";
        List<Map<String, Object>> testData = testData();
        int interval = 5;
        TimeUnit intervalUnit = TimeUnit.SECONDS;
        DataBroker dataBroker = new DataBroker(
                Executors.newSingleThreadScheduledExecutor(),
                new BrokeredData(dataId, newTestDataLoader(testData), interval, intervalUnit));

        // execute
        List<Map<String, Object>> data = dataBroker.get(dataId);

        //verify
        assertNotNull("data not found!", data);
        assertFalse("no data returned for that id!", data.isEmpty());
    }

    private List<Map<String, Object>> testData() {
        Map<String, Object> dataItem = new HashMap<String, Object>();
        dataItem.put("firstName", "John");
        dataItem.put("lastName", "Smith");
        List<Map<String, Object>> testData = new ArrayList<Map<String, Object>>();
        testData.add(Collections.unmodifiableMap(dataItem));
        return Collections.unmodifiableList(testData);
    }

    private long latestAllowedRefresh(int interval, TimeUnit intervalUnit) {
        return (intervalUnit.toMillis(interval) + 500);
    }

    private long earliestAllowedRefresh(int interval, TimeUnit intervalUnit) {
        return (intervalUnit.toMillis(interval) - 500);
    }

    private DataLoader newTestDataLoader(final List<Map<String, Object>> testData) {
        return new DataLoader() {
            @Override
            public List<Map<String, Object>> loadData() {
                return testData;
            }
        };
    }

    private DataLoader newTestDataLoader(final CountDownLatch dataLoaded) {
        return new DataLoader() {
            @Override
            public List<Map<String, Object>> loadData() {
                dataLoaded.countDown();
                return null;
            }
        };
    }
}
