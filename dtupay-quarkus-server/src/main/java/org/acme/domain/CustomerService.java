package org.acme.domain;

import org.acme.domain.model.Customer;
import org.acme.exceptions.UnknownAccountException;

import java.util.HashMap;
import java.util.UUID;

public class CustomerService {
   private HashMap<String, Customer> customers = new HashMap<>();
   private static CustomerService instance;

   public static CustomerService getInstance() {
      if (instance == null) {
         instance = new CustomerService();
      }
      return instance;
   }

   public String register(Customer customer) {
      UUID uuid = UUID.randomUUID();
      customers.put(uuid.toString(), customer);
      return uuid.toString();
   }

   public boolean checkCustomer(String customerId) {
      customers.keySet().forEach(id -> System.out.println(id.toString()));
      return customers.containsKey(customerId);
   }

   public void unregister(String id) {
      if (!customers.containsKey(id)) {
         throw new UnknownAccountException("Customer with id " + id + " not found");
      }
      customers.remove(id);
   }

   public Customer getCustomer(String id) {
      return customers.get(id);
   }
}
