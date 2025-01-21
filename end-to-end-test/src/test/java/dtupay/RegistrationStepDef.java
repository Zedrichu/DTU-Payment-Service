package dtupay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import dtupay.model.Customer;
import dtupay.model.Merchant;
import dtupay.services.CustomerService;
import dtupay.services.MerchantService;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RegistrationStepDef {

  private User user;
  private BankService bankService = new BankServiceService().getBankServicePort();
  private String bankAccountNo;
  private CustomerService customerService = new CustomerService();
  private Customer registeredCustomer;
  private Customer registeredCustomer2;
  private List<String> bankAccounts = new ArrayList<>();

  @Given("an unregistered user with CPR {string} and name {string} and lastname {string}")
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
    try {
      registeredCustomer = customerService.register(new Customer(user.getFirstName(),
                                          user.getLastName(),
                                          user.getCprNumber(),
                                          bankAccountNo, null));
    } catch (Exception e) {
      exception = e;
    }

  }
  @Then("the customer is registered with a non-empty customer id")
  public void theCustomerIsRegisteredWithANonEmptyCustomerId() {
    assertNotNull(registeredCustomer.payId());
  }

  @After
  public void cleanupBankAccounts() throws BankServiceException_Exception {
    for (String accountNo : bankAccounts) {
      bankService.retireAccount(accountNo);
    }
  }

  private Exception exception;

  @When("the second user is registered as a customer in DTUPay")
  public void theSecondUserIsRegisteredAsACustomerInDTUPay() {
    try {
      registeredCustomer2 = customerService.register(new Customer(user.getFirstName(),
          user.getLastName(),
          user.getCprNumber(),
          bankAccountNo, null));
    } catch (Exception e) {
      exception = e;
    }
  }

  @When("the user is registered as a merchant in DTUPay")
  public void theUserIsRegisteredAsAMerchantInDTUPay() {
    try {
      registeredMerchant = merchantService.register(new Merchant(user.getFirstName(),
            user.getLastName(),
            user.getCprNumber(),
            bankAccountNo, null));
    } catch (Exception e) {
      exception = e;
    }
  }

  @Then("the customer IDs are different")
  public void theCustomerIDsAreDifferent() {
    assertNotEquals(registeredCustomer.payId(), registeredCustomer2.payId());
  }

  @And("the user does not have a bank account")
  public void theUserDoesNotHaveABankAccount() {
    bankAccountNo = null;
  }

  @Then("the user gets an error message {string} and is not registered")
  public void theUserGetsAnErrorMessage(String errorMessage) {
    assertNull(registeredCustomer);
    assertTrue(exception instanceof AccountCreationException);
    assertEquals(errorMessage, exception.getMessage());
  }

  private Merchant registeredMerchant;
  private MerchantService merchantService = new MerchantService();

  @When("user is registered as a merchant in DTUPay")
  public void userIsRegisteredAsAMerchantInDTUPay() {
    try {
      registeredMerchant = merchantService.register(
            new Merchant(user.getFirstName(),
                          user.getLastName(),
                          user.getCprNumber(),
                  "21314",
                          null));
    } catch (Exception e) {
      exception = e;
    }
  }

  @Then("the merchant is registered with a non-empty merchant id")
  public void theMerchantIsRegisteredWithANonEmptyMerchantId() {
    assertNotNull(registeredMerchant.payId());
  }



}
