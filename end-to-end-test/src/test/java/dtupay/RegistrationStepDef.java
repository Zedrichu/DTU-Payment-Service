package dtupay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;

public class RegistrationStepDef {

  private User user;
  private BankService bankService = new BankServiceService().getBankServicePort();
  private String bankAccountNo;
  private CustomerService customerService = new CustomerService();
  private String customerId;
  private String customerId2;
  private List<String> bankAccounts = new ArrayList<>();

  @Given("a unregistered user with CPR {string} and name {string} and lastname {string}")
  public void aUserWithCPRAndNameAndLastname(String cpr, String firstName, String lastName) {
    user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setCprNumber(cpr);
  }

  @And("a registered bank account for the user with balance {int}")
  public void aRegisteredBankAccountForTheUser(int balance) throws BankServiceException_Exception {
    BigDecimal newBalance = new BigDecimal(balance);
    bankAccountNo = bankService.createAccountWithBalance(user, newBalance);
    bankAccounts.add(bankAccountNo);
  }

  @When("the user is registered as a customer in DTUPay")
  public void theUserIsRegisteredAsACustomerInDTUPay() {
    customerId = customerService.register(new Customer(user.getFirstName(),
                                          user.getLastName(),
                                          user.getCprNumber(),
                                          bankAccountNo, null));
  }
  @Then("the customer is registered with a non-empty customer id")
  public void theCustomerIsRegisteredWithANonEmptyCustomerId() {
    System.out.println("Customer ID: " + customerId);
    assertNotNull(customerId);
  }

  @After
  public void cleanupBankAccounts() throws BankServiceException_Exception {
    for (String accountNo : bankAccounts) {
      bankService.retireAccount(accountNo);
    }
  }

  @When("the second user is registered as a customer in DTUPay")
  public void theSecondUserIsRegisteredAsACustomerInDTUPay() {
    customerId2 = customerService.register(new Customer(user.getFirstName(),
          user.getLastName(),
          user.getCprNumber(),
          bankAccountNo, null));
  }

  @Then("the customer IDs are different")
  public void theCustomerIDsAreDifferent() {
    assertNotEquals(customerId,customerId2);
  }

}
