package spagbol;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Arrays.asList;
import static spagbol.DataBroker.DataEvent.error;

public class DataBroker {
    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<String, BrokeredData> brokeredDatas;
    private final Queue<DataEvent> dataEvents = new ConcurrentLinkedQueue<DataEvent>();

    public DataBroker(ScheduledExecutorService scheduledExecutorService, BrokeredData brokeredData) {
        this(scheduledExecutorService, asList(brokeredData));
    }

    public DataBroker(ScheduledExecutorService scheduledExecutorService, List<BrokeredData> brokeredDatas) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.brokeredDatas = buildBrokeredDatas(brokeredDatas);
        initCache();
        scheduledDataRefresh();
        awaitInitialisation();
    }

    private Map<String, BrokeredData> buildBrokeredDatas(List<BrokeredData> brokeredDatas) {
        return Maps.uniqueIndex(brokeredDatas, new Function<BrokeredData, String>() {
            @Override
            public String apply(BrokeredData input) {
                return input.getId();
            }
        });
    }

    private void initCache() {
        for (final BrokeredData brokeredData : brokeredDatas.values()) {
            if (brokeredData.isRefreshable()) {
                scheduledExecutorService.submit(newLoadDataRunner(brokeredData));
            }
        }
    }

    public void awaitInitialisation() {
        for (final BrokeredData brokeredData : brokeredDatas.values()) {
            try {
                if (!brokeredData.awaitInitialisation()) {
                    dataEvents.offer(error(brokeredData.getId(), "Timed out waiting for data to load"));
                }
            } catch (InterruptedException e) {
                dataEvents.offer(error(brokeredData.getId(), "Interrupted while loading data", e));
            }
        }
    }

    private void scheduledDataRefresh() {
        for (final BrokeredData brokeredData : brokeredDatas.values()) {
            if (brokeredData.isRefreshable()) {
                scheduledExecutorService.schedule(newLoadDataRunner(brokeredData), brokeredData.getRefreshInterval(), brokeredData.getRefreshIntervalUnit());
            }
        }
    }

    private Runnable newLoadDataRunner(final BrokeredData brokeredData) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    brokeredData.loadData();
                } catch (Exception e) {
                    dataEvents.offer(error(brokeredData.getId(), "Data load failure", e));
                }
            }
        };
    }

    public List<Map<String, Object>> get(String dataId) {
        BrokeredData brokeredData = brokeredDatas.get(dataId);
        if (brokeredData != null) {
            return brokeredData.getData();
        }
        return null;
    }

    public Collection<DataEvent> getEvents() {
        return Collections.unmodifiableCollection(dataEvents);
    }

    public static final class DataEvent {
        private final boolean error;
        private final String dataId;
        private final String message;
        private final Throwable cause;

        public static DataEvent error(String dataId, String message) {
            return new DataEvent(true, dataId, message);
        }

        public static DataEvent error(String dataId, String message, Throwable cause) {
            return new DataEvent(true, dataId, message, cause);
        }

        public DataEvent(boolean error, String dataId, String message) {
            this.error = error;
            this.dataId = dataId;
            this.message = message;
            cause = null;
        }

        public DataEvent(boolean error, String dataId, String message, Throwable cause) {
            this.error = error;
            this.dataId = dataId;
            this.message = message;
            this.cause = cause;
        }

        public boolean isError() {
            return error;
        }

        public String getDataId() {
            return dataId;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }
    }
}
