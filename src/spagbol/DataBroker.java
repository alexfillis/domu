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
        this.scheduledExecutorService = scheduledExecutorService;
        brokeredDatas = buildBrokeredDatas(brokeredData);
        initCache();
        scheduledDataRefresh();
    }

    private Map<String, BrokeredData> buildBrokeredDatas(BrokeredData brokeredData) {
        return Maps.uniqueIndex(asList(brokeredData), new Function<BrokeredData, String>() {
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
