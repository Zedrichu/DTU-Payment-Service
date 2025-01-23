package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.domain.models.views.LogView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemoryReportRepository<T extends LogView> implements ReportRepository<T> {
	private final Map<String, ArrayList<T>> accountLogs = new HashMap<>();

	@Override
	public void addView(String accountId, T logView) {
		accountLogs.computeIfAbsent(accountId, k -> new ArrayList<>()).add(logView);
	}

	@Override
	public ArrayList<T> exportReport(String id) {
		return accountLogs.getOrDefault(id, new ArrayList<>());
	}
}
