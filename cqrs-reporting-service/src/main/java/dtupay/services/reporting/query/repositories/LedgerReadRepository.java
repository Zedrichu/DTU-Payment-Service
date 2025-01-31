package dtupay.services.reporting.query.repositories;

import dtupay.services.reporting.domain.entities.ReportingRole;
import dtupay.services.reporting.query.views.CustomerView;
import dtupay.services.reporting.query.views.ManagerView;
import dtupay.services.reporting.query.views.MerchantView;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LedgerReadRepository {

  // Mapping from LedgerId -> Set of Transaction Logs
  private final Map<String, Set<CustomerView>> customerViews = new HashMap<>();
  private final Map<String, Set<MerchantView>> merchantViews = new HashMap<>();
  @Getter
  private final Set<ManagerView> managerViews = new HashSet<>();

  public boolean contains(String ledgerId) {
    return customerViews.containsKey(ledgerId) || merchantViews.containsKey(ledgerId);
  }

  public Set<CustomerView> getCustomerViewsByLedger(String ledgerId) {
    return customerViews.getOrDefault(ledgerId, new HashSet<>());
  }

  public Set<MerchantView> getMerchantViewsByLedger(String ledgerId) {
    return merchantViews.getOrDefault(ledgerId, new HashSet<>());
  }

  public void addCustomerViews(String ledgerId, Set<CustomerView> views) {
    customerViews.computeIfAbsent(ledgerId, _ -> new HashSet<>()).addAll(views);
  }

  public void addMerchantViews(String ledgerId, Set<MerchantView> views) {
    merchantViews.computeIfAbsent(ledgerId, _ -> new HashSet<>()).addAll(views);
  }

  public void addManagerViews(Set<ManagerView> views) {
    managerViews.addAll(views);
  }

  public void initRoleLedger(String ledgerId, ReportingRole role) {
    switch (role) {
      case CUSTOMER: customerViews.put(ledgerId, new HashSet<>()); break;
      case MERCHANT: merchantViews.put(ledgerId, new HashSet<>()); break;
      default: managerViews.clear(); break;
    }
  }

  public void removeRoleLedger(String ledgerId) {
    customerViews.remove(ledgerId);
    merchantViews.remove(ledgerId);
    if (ledgerId.equals("ADMIN")) {
      managerViews.clear();
    }
  }

}
