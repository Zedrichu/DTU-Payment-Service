package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.annotations.ClassAuthor;
import dtupay.services.reporting.domain.models.views.LogView;
import dtupay.services.reporting.domain.models.views.ManagerView;

import java.util.ArrayList;
import java.util.function.BiFunction;

@ClassAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
public interface ReportRepository<T extends LogView>{
	void addView(String id, T logView);
	ArrayList<T> getReport(String id);
	ArrayList<ManagerView> exportAllManagerViews(BiFunction<String, T, ManagerView> exporter);
}
