package spagbol;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static spagbol.BrokeredData.DataEvent.*;

public class BrokeredData {
    private String id;
    private final DataLoader dataLoader;
    private final int refreshInterval;
    private final TimeUnit refreshIntervalUnit;
    private List<Map<String,Object>> data = new ArrayList<Map<String, Object>>();
    private final CountDownLatch initialised = new CountDownLatch(1);
    private final Queue<DataEvent> dataEvents = new ConcurrentLinkedQueue<DataEvent>();

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
        initialised.countDown();
    }

    public String getId() {
        return id;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void awaitInitialisation() {
        try {
            if (!initialised.await(3, TimeUnit.SECONDS)) {
                dataEvents.offer(error(format("Timed out waiting for '%s' to load data", getId())));
            }
        } catch (InterruptedException e) {
            dataEvents.offer(error(format("Interrupted while waiting for %s to load data", getId()), e));
        }
    }

    public Collection<DataEvent> getEvents() {
        return Collections.unmodifiableCollection(dataEvents);
    }

    public static final class DataEvent {
        private final boolean error;
        private final String message;
        private final Throwable cause;

        public static DataEvent error(String message) {
            return new DataEvent(true, message);
        }

        public static DataEvent error(String message, Throwable cause) {
            return new DataEvent(true, message, cause);
        }

        public DataEvent(boolean error, String message) {
            this.error = error;
            this.message = message;
            cause = null;
        }

        public DataEvent(boolean error, String message, Throwable cause) {
            this.error = error;
            this.message = message;
            this.cause = cause;
        }

        public boolean isError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }
    }
}
