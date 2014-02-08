package spagbol;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Arrays.asList;

public class DataBroker {
    private final ScheduledExecutorService scheduledExecutorService;
    private final List<BrokeredData> brokeredDatas;

    public DataBroker(ScheduledExecutorService scheduledExecutorService, BrokeredData brokeredData) {
        this.scheduledExecutorService = scheduledExecutorService;
        brokeredDatas = Collections.unmodifiableList(asList(brokeredData));
        scheduledDataRefresh();
    }

    private void scheduledDataRefresh() {
        for (final BrokeredData brokeredData : brokeredDatas) {
            if (brokeredData.isRefreshable()) {
                scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        brokeredData.loadData();
                    }
                }, brokeredData.getRefreshInterval(), brokeredData.getRefreshIntervalUnit());
            }
        }
    }
}
