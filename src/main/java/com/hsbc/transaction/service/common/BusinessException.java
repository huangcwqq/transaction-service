package com.hsbc.transaction.service.common;

import java.util.Arrays;
import java.util.StringJoiner;

/** Business exception handler class */
public class BusinessException extends RuntimeException {

  private final int code;

  private Object[] args;

  public BusinessException(int code, String error, Object... args) {
    super(error);

    this.code = code;
    this.args = args;
  }

  public BusinessException(int code, String error, Throwable cause, Object... args) {
    super(error, cause);

    this.code = code;
    this.args = args;
  }

  public BusinessException withArgs(Object... args) {
    this.args = args;
    return this;
  }

  public int getCode() {
    return this.code;
  }

  public Object[] getArgs() {
    return this.args;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", BusinessException.class.getSimpleName() + "[", "]")
        .add("error=" + getMessage())
        .add("code=" + code)
        .add("args=" + Arrays.toString(args))
        .toString();
  }
}
