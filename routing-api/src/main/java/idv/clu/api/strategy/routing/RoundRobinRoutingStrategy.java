package idv.clu.api.strategy.routing;

import idv.clu.api.common.RoutingConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author clu
 */
@ApplicationScoped
public class RoundRobinRoutingStrategy implements RoutingStrategy {

    private final static Logger LOG = LoggerFactory.getLogger(RoundRobinRoutingStrategy.class);

    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    @Inject
    RoutingConfig routingConfig;

    // TODO bad smell
    List<String> availableInstances;

    @PostConstruct
    public void init() {
        LOG.info("Initializing RoundRobinRoutingStrategy.");

        this.availableInstances = routingConfig.getAvailableInstances();

        if (availableInstances.isEmpty()) {
            throw new IllegalStateException("No API instances configured in routingConfig.");
        }

        LOG.info("Available API instances: {}", availableInstances);
    }

    @Override
    public String getNextTargetUrl(String urlPath) {
        if (availableInstances == null || availableInstances.isEmpty()) {
            throw new IllegalArgumentException("No available instances for routing");
        }

        // TODO check node's health status here according to the list
        final int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % availableInstances.size());
        final String nextUrl = availableInstances.get(index).concat(urlPath);
        LOG.debug("Next target URL selected by round robin: {}", nextUrl);
        return nextUrl;
    }

}

