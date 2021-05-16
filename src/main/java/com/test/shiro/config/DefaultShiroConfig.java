package com.test.shiro.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.shiro.filter.DefaultUserFilter;



/***
 * 内部使用的shiro配置
 * 统一一个redis共享会话，，要求redis的链接参数一样
 * @author yuxue
 * @date 2019-08-15
 */
@Configuration
//@ConfigurationProperties(prefix = "spring") // application.yml中的spring下的属性
@ConditionalOnMissingBean(ShiroFilterFactoryBean.class)
public class DefaultShiroConfig {

	private Map<String, String> redis = new HashMap<>();

	public Map<String, String> getRedis() {
		return redis;
	}

	public void setRedis(Map<String, String> redis) {
		this.redis = redis;
	}

	@Bean
	public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {

		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

		// Shiro的核心安全接口,这个属性是必须的
		shiroFilterFactoryBean.setSecurityManager(securityManager);

		// 没有登陆的用户只能访问登陆页面
		shiroFilterFactoryBean.setLoginUrl("./login");

		// 登录成功后要跳转的链接
		shiroFilterFactoryBean.setSuccessUrl("./index");

		// 未授权界面; ----这个配置了没卵用，具体原因想深入了解的可以自行百度
		// shiroFilterFactoryBean.setUnauthorizedUrl("/auth/403");

		// 自定义拦截器
		Map<String, Filter> filtersMap = new LinkedHashMap<String, Filter>();
		// 限制同一帐号同时在线的个数
		// filtersMap.put("kickout", kickoutSessionControlFilter());
		// filtersMap.put("loginCheck", new LoginCheckFilter());
		filtersMap.put("userFilter", new DefaultUserFilter());
		shiroFilterFactoryBean.setFilters(filtersMap);

		// 权限控制map
		Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
		filterChainDefinitionMap.put("/static/**", "anon");
		filterChainDefinitionMap.put("/css/**", "anon");
		filterChainDefinitionMap.put("/js/**", "anon");
		filterChainDefinitionMap.put("/img/**", "anon");
		filterChainDefinitionMap.put("/login", "anon");
		filterChainDefinitionMap.put("/auth/doLogin", "anon");
		filterChainDefinitionMap.put("/auth/logout", "anon");

		filterChainDefinitionMap.put("/**", "anon");
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}



	/**
	 * 自定义身份认证realm;
	 * @return
	 */
	@Bean
	public DefaultRealm defaultRealm() {
		DefaultRealm myRealm = new DefaultRealm();
		return myRealm;
	}


	/**
	 * Session Manager
	 * 使用的是shiro-redis开源插件
	 */
	@Bean
	public DefaultWebSessionManager defaultWebSessionManager(RedisSessionDao redisSessionDao) {
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		sessionManager.setGlobalSessionTimeout(30 * 1000);
		sessionManager.setDeleteInvalidSessions(true);
		sessionManager.setSessionDAO(redisSessionDao);
		sessionManager.setSessionValidationSchedulerEnabled(true);
		sessionManager.setDeleteInvalidSessions(true);
		/**
		 * 修改Cookie中的SessionId的key，默认为JSESSIONID，自定义名称
		 */
		sessionManager.setSessionIdCookie(new SimpleCookie("JSESSIONID"));
		return sessionManager;
	}

	@Bean
	public SecurityManager securityManager(RedisSessionDao redisSessionDao) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(defaultRealm());
		// 取消Cookie中的RememberMe参数
		securityManager.setRememberMeManager(null);
		securityManager.setSessionManager(defaultWebSessionManager(redisSessionDao));
		return securityManager;
	}

	/***
	 * 授权所用配置
	 * @return
	 */
	@Bean
	public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
		return defaultAdvisorAutoProxyCreator;
	}

	/***
	 * 使授权注解起作用
	 * 如不想配置可以在pom文件中加入
	 * <dependency>
	 *<groupId>org.springframework.boot</groupId>
	 *<artifactId>spring-boot-starter-aop</artifactId>
	 *</dependency>
	 * @param securityManager
	 * @return
	 */
	/*@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}*/

	/**
	 * Shiro生命周期处理器
	 */
	@Bean
	public static LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

}