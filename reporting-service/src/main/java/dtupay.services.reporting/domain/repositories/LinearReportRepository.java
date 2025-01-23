package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.domain.models.views.LogView;

import java.util.ArrayList;

public class LinearReportRepository {
	private final ArrayList<LogView> adminReport;

	public LinearReportRepository() {
		adminReport = new ArrayList<>();
	}

	public void addView(LogView view) {
		adminReport.add(view);
	}

	public ArrayList<LogView> exportReport() {
		return adminReport;
	}
}
