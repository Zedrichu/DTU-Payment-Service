package dtupay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import dtupay.model.Customer;
import dtupay.model.Merchant;
import dtupay.model.PaymentRequest;
import dtupay.services.CustomerService;
import dtupay.services.MerchantService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.After;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PaymentStepDefs {
    private User user;
    private BankService bankService = new BankServiceService().getBankServicePort();
    private String bankAccountNo;
    private CustomerService customerService = new CustomerService();
    private MerchantService merchantService = new MerchantService();
    private Customer registeredCustomer;
    private Customer registeredCustomer2;
    private Merchant registeredMerchant;
    private List<String> bankAccounts = new ArrayList<>();
    private Exception exception;
    private PaymentRequest paymentRequest;
    private Boolean paymentSucceeded;

    @Given("a registered customer with DTUPay with balance {int} in the bank")
    public void aRegisteredCustomerWithDTUPayWithBalanceInTheBank(int balance) throws BankServiceException_Exception {
        user = new User();
        user.setFirstName("Jeppe");
        user.setLastName("Jeppeson");
        user.setCprNumber("141414-1414");
        BigDecimal newBalance = new BigDecimal(balance);
        bankAccountNo = bankService.createAccountWithBalance(user, newBalance);
        bankAccounts.add(bankAccountNo);
        try {
            registeredCustomer = customerService.register(new Customer(user.getFirstName(),
                    user.getLastName(),
                    user.getCprNumber(),
                    bankAccountNo, null));
        } catch (Exception e) {
            exception = e;
        }
    }

    @And("a registered merchant with DTUPay with balance {int} in the bank")
    public void aRegisteredMerchantWithDTUPayWithBalanceInTheBank(int balance) throws BankServiceException_Exception {
        user = new User();
        user.setFirstName("Simp");
        user.setLastName("Jeppesen");
        user.setCprNumber("141414-1415");
        BigDecimal newBalance = BigDecimal.valueOf(balance);
        bankAccountNo = bankService.createAccountWithBalance(user, newBalance);
        bankAccounts.add(bankAccountNo);
        try {
            registeredMerchant = merchantService.register(new Merchant(user.getFirstName(),
                    user.getLastName(),
                    user.getCprNumber(),
                    bankAccountNo, null));
        } catch (Exception e) {
            exception = e;
        }
    }

    @When("the merchant initiates a payment of {int}")
    public void theMerchantInitiatesAPaymentOf(int amount) {
        paymentRequest = new PaymentRequest(registeredMerchant.payId(),"",amount);
        paymentSucceeded = merchantService.pay(paymentRequest);
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assertTrue(paymentSucceeded);
    }

    @And("the customers balance in the bank is {int}")
    public void theCustomersBalanceInTheBankIs(int balance) throws BankServiceException_Exception {
        assertEquals(bankService.getAccount(registeredCustomer.bankAccountNo()).getBalance(), BigDecimal.valueOf(balance));
    }

    @And("the merchants balance in the bank is {int}")
    public void theMerchantsBalanceInTheBankIs(int balance) throws BankServiceException_Exception {
        assertEquals(bankService.getAccount(registeredMerchant.bankAccountNo()).getBalance(), BigDecimal.valueOf(balance));
    }

    @After
    public void after() throws BankServiceException_Exception {
        for (String bankAccountNo : bankAccounts) {
            bankService.retireAccount(bankAccountNo);
        }
    }
}
