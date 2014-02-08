package spagbol;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class TestDataLoader implements DataLoader{
    private final CountDownLatch dataLoaded;

    public TestDataLoader(CountDownLatch dataLoaded) {
        this.dataLoaded = dataLoaded;
    }

    @Override
    public List<Map<String, Object>> loadData() {
        dataLoaded.countDown();
        return null;
    }
}
