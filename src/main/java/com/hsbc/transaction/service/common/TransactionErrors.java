package com.hsbc.transaction.service.common;

public interface TransactionErrors {
    int ERROR_REQUEST_PARAM = 10001;
    int USER_DOES_NO_EXIST = 10002;
    int ROLE_DOES_NO_EXIST = 10003;
    int USERNAME_ALREADY_EXIST = 10004;
    int ROLE_NAME_ALREADY_EXIST = 10005;
    int USERNAME_NOT_EXIST = 10006;
    int PASSWORD_INPUT_ERROR = 10007;
    int TOKEN_NOT_VALID = 10008;

    static BusinessException requestParamError() {
        return new BusinessException(ERROR_REQUEST_PARAM, "error request param!");
    }

    static BusinessException userNotExistError() {
        return new BusinessException(USER_DOES_NO_EXIST, "user does not exist!");
    }

    static BusinessException roleNotExistError() {
        return new BusinessException(ROLE_DOES_NO_EXIST, "role does not exist!");
    }

    static BusinessException usernameRepeatError() {
        return new BusinessException(USERNAME_ALREADY_EXIST, "username already exist!");
    }

    static BusinessException roleNameRepeatError() {
        return new BusinessException(ROLE_NAME_ALREADY_EXIST, "role name already exist!");
    }

    static BusinessException usernameNotExistError() {
        return new BusinessException(USERNAME_NOT_EXIST, "username not exist!");
    }

    static BusinessException passwordInputError() {
        return new BusinessException(PASSWORD_INPUT_ERROR, "incorrect password!");
    }
}
