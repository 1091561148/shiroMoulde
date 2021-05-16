package com.test.shiro.constant;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Service;

import com.test.shiro.entity.UserEntity;


@Service
public interface LoginService {

	public UserEntity getSystemUser() ;

}
