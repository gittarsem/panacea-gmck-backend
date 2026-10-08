package com.panacea.backend.utils;

import com.panacea.backend.Entity.UserEntity;
import com.panacea.backend.Entity.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

public class AppUtils {

    public static UserEntity giveMeCurrentUser() {
        UserPrincipal userPrincipal =
                (UserPrincipal) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        return userPrincipal.getUser();
    }


}