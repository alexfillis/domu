package spagbol;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataBrokerTest {
    @Test
    public void should_refresh_data_at_specified_interval() throws InterruptedException {
        CountDownLatch dataLoaded = new CountDownLatch(1);
        int interval = 5;
        TimeUnit intervalUnit = TimeUnit.SECONDS;
        new DataBroker(
                Executors.newSingleThreadScheduledExecutor(),
                new BrokeredData(newTestDataLoader(dataLoaded), interval, intervalUnit));

        // execute
        long start = currentTimeMillis();
        boolean timedOut = !dataLoaded.await(intervalUnit.toMillis(interval) + 500, TimeUnit.MILLISECONDS);
        long end = currentTimeMillis();

        //verify
        assertFalse("Timed out!", timedOut);
        assertTrue((end - start) > (intervalUnit.toMillis(interval) - 500));
        assertTrue((end - start) < (intervalUnit.toMillis(interval) + 500));
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
