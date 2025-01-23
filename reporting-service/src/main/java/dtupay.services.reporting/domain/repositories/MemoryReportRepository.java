package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.annotations.ClassAuthor;
import dtupay.services.reporting.annotations.MethodAuthor;
import dtupay.services.reporting.domain.models.views.LogView;
import dtupay.services.reporting.domain.models.views.ManagerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@ClassAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
public class MemoryReportRepository<T extends LogView> implements ReportRepository<T> {
	private final Map<String, ArrayList<T>> accountLogs = new HashMap<>();

	@MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
	@Override
	public void addView(String accountId, T logView) {
		accountLogs.computeIfAbsent(accountId, k -> new ArrayList<>()).add(logView);
	}

	@MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
	@Override
	public ArrayList<T> getReport(String id) {
		return accountLogs.getOrDefault(id, new ArrayList<>());
	}

	@MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
	@Override
	public ArrayList<ManagerView> exportAllManagerViews(BiFunction<String, T, ManagerView> exporter) {
		return accountLogs.entrySet().stream().flatMap(
					entry -> entry.getValue().stream()
								.map(value -> exporter.apply(entry.getKey(), value)))
					.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

}
