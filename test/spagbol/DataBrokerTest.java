package spagbol;

import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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

    @Test
    public void data_access_should_not_return_until_data_is_available() throws InterruptedException {
        String dataId = "testdata";
        CountDownLatch initData = new CountDownLatch(1);
        List<Map<String, Object>> testData = testData();
        int interval = 5;
        TimeUnit intervalUnit = TimeUnit.SECONDS;
        BrokeredData brokeredData = new BrokeredData(dataId, newTestDataLoader(initData, testData), interval, intervalUnit);
        DataBroker dataBroker = new DataBroker(
                Executors.newSingleThreadScheduledExecutor(),
                brokeredData);

        // execute
        List<Map<String, Object>> dataBeforeInit = dataBroker.get(dataId);
        initData.countDown();
        dataBroker.awaitInitialisation();
        List<Map<String, Object>> dataAfterInit = dataBroker.get(dataId);

        //verify
        assertTrue("before initialisation should be empty!", dataBeforeInit.isEmpty());
        assertFalse("after initialisation  should NOT be empty!", dataAfterInit.isEmpty());
        assertEquals("different number of events", 1, dataBroker.getEvents().size());
        DataBroker.DataEvent dataEvent = dataBroker.getEvents().iterator().next();
        assertEquals("dataId should be the same", dataId, dataEvent.getDataId());
        assertTrue("should of been timeout event", dataEvent.getMessage().contains("Time") && dataEvent.getMessage().contains("out"));
    }

    @Test(expected = IOException.class)
    public void should_catch_record_any_data_load_errors() throws Throwable {
        String dataId = "testdata";
        int interval = 5;
        TimeUnit intervalUnit = TimeUnit.SECONDS;

        // execute
        DataBroker dataBroker = new DataBroker(
                Executors.newSingleThreadScheduledExecutor(),
                new BrokeredData(dataId, newTestExceptionDataLoader(), interval, intervalUnit));

        //verify
        assertEquals("different number of events", 2, dataBroker.getEvents().size());
        throw dataBroker.getEvents().iterator().next().getCause();
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
            public List<Map<String, Object>> loadData() throws Exception {
                return testData;
            }
        };
    }

    private DataLoader newTestDataLoader(final CountDownLatch dataLoaded) {
        return new DataLoader() {
            @Override
            public List<Map<String, Object>> loadData() throws Exception {
                dataLoaded.countDown();
                return null;
            }
        };
    }

    private DataLoader newTestDataLoader(final CountDownLatch initData, final List<Map<String, Object>> testData) {
        return new DataLoader() {
            @Override
            public List<Map<String, Object>> loadData() throws Exception {
                try {
                    if (!initData.await(5, TimeUnit.SECONDS)) {
                        return null;
                    }
                } catch (InterruptedException e) {
                    return null;
                }

                return testData;
            }
        };
    }

    private DataLoader newTestExceptionDataLoader() {
        return new DataLoader() {
            @Override
            public List<Map<String, Object>> loadData() throws Exception {
                throw new IOException();
            }
        };
    }
}
