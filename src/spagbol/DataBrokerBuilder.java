package spagbol;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataBrokerBuilder {
    private ScheduledExecutorService scheduledExecutorService;

    public DataBroker build() {
        return null;
    }

    public DataBrokerBuilder scheduling(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
        return this;
    }

    public DataBrokerBuilder refresh(final DataLoader dataLoader, long interval, TimeUnit intervalUnit) {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                dataLoader.loadData();
            }
        }, interval, intervalUnit);
        return this;
    }
}
