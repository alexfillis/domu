package spagbol;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Arrays.asList;

public class DataBroker {
    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<String, BrokeredData> brokeredDatas;

    public DataBroker(ScheduledExecutorService scheduledExecutorService, BrokeredData brokeredData) {
        this(scheduledExecutorService, asList(brokeredData));
    }

    public DataBroker(ScheduledExecutorService scheduledExecutorService, List<BrokeredData> brokeredDatas) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.brokeredDatas = buildBrokeredDatas(brokeredDatas);
        initCache();
        scheduledDataRefresh();
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
                scheduledExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        brokeredData.loadData();
                    }
                });
            }
        }
    }

    private void scheduledDataRefresh() {
        for (final BrokeredData brokeredData : brokeredDatas.values()) {
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

    public List<Map<String, Object>> get(String dataId) {
        BrokeredData brokeredData = brokeredDatas.get(dataId);
        if (brokeredData != null) {
            return brokeredData.getData();
        }
        return null;
    }
}
