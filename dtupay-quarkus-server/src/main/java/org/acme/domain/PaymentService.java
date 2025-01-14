package org.acme.domain;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import org.acme.domain.model.Customer;
import org.acme.domain.model.Merchant;
import org.acme.domain.model.Payment;
import org.acme.exceptions.UnknownAccountException;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PaymentService {
   ArrayList<Payment> payments = new ArrayList<>();
   CustomerService customerService = CustomerService.getInstance();
   MerchantService merchantService = MerchantService.getInstance();
   private BankService bankService = new BankServiceService().getBankServicePort();

   public ArrayList<Payment> getPayments() {
      return payments;
   }

   public void addPayment(Payment payment) throws UnknownAccountException, BankServiceException_Exception {
      if (!customerService.checkCustomer(payment.customerId())) {
         throw new UnknownAccountException( String.format(
                     "customer with id \"%s\" is unknown", payment.customerId()));
      } else if (!merchantService.checkMerchant(payment.merchantId())) {
         throw new UnknownAccountException( String.format(
                     "merchant with id \"%s\" is unknown", payment.merchantId()
         ));
      }
      Customer customer = customerService.getCustomer(payment.customerId());
      Merchant merchant = merchantService.getMerchant(payment.merchantId());

      bankService.transferMoneyFromTo(customer.bankAccountNo(),
                                       merchant.bankAccountNo(),
                                       BigDecimal.valueOf(payment.amount()),
                           "fee");
      payments.add(payment);
   }
}
