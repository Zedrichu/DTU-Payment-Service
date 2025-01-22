package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.domain.aggregate.PaymentReport;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReadModelRepository {

    private Map<String, Set<PaymentReport>> reports = new ConcurrentHashMap<>();

    public Set<PaymentReport> getCustomerReports(String customerId) {
        return reports.getOrDefault(customerId, new HashSet<PaymentReport>()).stream().collect(Collectors.toSet());
    }
}
