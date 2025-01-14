package org.acme.exceptions;

public class UnknownAccountException extends RuntimeException {
   public UnknownAccountException(String message) {
      super(message);
   }
}
