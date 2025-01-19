package service.behaviours.tests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CucumberOptions.SnippetType;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
			plugin="summary",
			snippets = SnippetType.CAMELCASE,
			features="src/features")
public class CucumberTest {
}