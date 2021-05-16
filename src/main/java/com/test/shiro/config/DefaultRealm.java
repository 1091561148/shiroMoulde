package com.test.shiro.config;

import java.util.Set;

import com.test.shiro.constant.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.test.shiro.entity.UserEntity;


/**
 * 自定义shiro验证规则
 * @author gjf
 * @date 2018-09-07
 */
// @Slf4j
// @Service 已经在DefaultShiroConfig中声明并注入了该对象的bean， 不去掉service注解，会实例化两次
public class DefaultRealm extends AuthorizingRealm {

	@Autowired
	@Lazy
	private LoginService loginService;
	
	
	/**
	 * 仅支持UsernamePasswordToken类型的Token  
	 */
	@Override  
    public boolean supports(AuthenticationToken token) {  
        return token instanceof UsernamePasswordToken;
    }  

	/**
	 * 认证信息.(身份验证) : Authentication 是用来验证用户身份
	 * 如果返回一个SimpleAccount对象则认证通过，如果返回值为空或者异常，则认证不通过。 
	 * 1、检查提交的进行认证的令牌信息 
	 * 2、根据令牌信息从数据源(通常为数据库)中获取用户信息
	 * 3、对用户信息进行匹配验证
	 * 4、验证通过将返回一个封装了用户信息的AuthenticationInfo实例
	 * 5、验证失败则抛出AuthenticationException异常信息
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
		// 获取用户的输入的账号.
		//String username = (String)token.getPrincipal();  //得到用户名  
        // String password = new String((char[])token.getCredentials()); //得到密码  
		/*if (StringUtils.isEmpty(username)) {
			return null;
		}*/
		
		// 按照shiro的设计，应该在此处实现登录逻辑，但是已经在loginService里面实现，并且登录成功之后，将用户信息放到了session
		// 所以此处通过session获取到user信息即可
		UserEntity user = loginService.getSystemUser();
		if(null == user || StringUtils.isEmpty(user.getLoginName())) {
			return null;
		}
		
		//如果身份认证验证成功，返回一个AuthenticationInfo实现；  
		SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(user, // 用户名
				user.getLoginPasswd(), // 密码
				this.getName() // realm name
		);
		return authenticationInfo;
	}

	/**
	 * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用,负责在应用程序中决定用户的访问控制的方法
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		
		UserEntity user = (UserEntity) principals.getPrimaryPrincipal();
		// 已经在登录接口，查询到用户的权限，并且放入了Permission属性当中
		// 所以这里不需要再次实现获取用户权限的逻辑，拿到session中的权限即可	
		// 这样处理，是因为前端需要权限编码，，会导致后端redis保存两份权限数据
		Set<String> permission = user.getPermission();

		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

		info.setStringPermissions(permission);
		return info;

	}

}
