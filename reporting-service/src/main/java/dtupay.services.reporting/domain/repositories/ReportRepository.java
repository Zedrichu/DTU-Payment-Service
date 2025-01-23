package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.domain.models.views.LogView;

import java.util.ArrayList;

public interface ReportRepository<T extends LogView>{
	void addView(String id, T logView);
	ArrayList<T> exportReport(String id);
}
