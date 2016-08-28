package top.knos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import top.knos.user.services.JpaUserDetailsManager;
import top.knos.support.BCryptEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired JpaUserDetailsManager jpaUserDetailsManager;
	@Autowired BCryptEncoder bcryptEncoder;
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
	      .authorizeRequests()
//	        .antMatchers("/signup","/about*","/register","/logout","/","/index","/account/pwRecovery","/avatar/**").permitAll() 
//	        .antMatchers("/admin/**").hasRole("ADMIN") 
//	        .antMatchers("/lottery/**","/prize*/**","/nameList*/**","/house*/**").hasRole("USER") 
//	        .anyRequest().authenticated()
	      	.anyRequest().permitAll()
	        .and()
	      .formLogin()
	      	.usernameParameter("username")
	      	.passwordParameter("password")
	        .loginPage("/login") 
	        .permitAll()
	       .and()
	       	.rememberMe()
	       	.key("knos.top")
	       	.tokenValiditySeconds(30*24*60*60)
	       .and()
	       	.logout()
	       	.logoutUrl("/logout")
	       	.logoutSuccessUrl("/login")
	       	.deleteCookies("JSESSIONID","remember-me")
	       .and()
	       	.csrf().disable();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(jpaUserDetailsManager)
				.passwordEncoder(bcryptEncoder);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web
		.ignoring()
		.antMatchers("/static/**","/resources/**","/dist/**","/img/**","/js/**","/css/**","/plugins/**","/jspm_packages/**","/avatar/**"); 
	}
	
	@Bean(name = "accessDecisionManager")
	public AccessDecisionManager accessDecisionManager() {
		List<AccessDecisionVoter<? extends Object>> decisionVoters = new ArrayList<AccessDecisionVoter<? extends Object>>();
        decisionVoters.add(new RoleVoter());  
        decisionVoters.add(new AuthenticatedVoter());  

		AffirmativeBased accessDecisionManager = new AffirmativeBased(decisionVoters);

		return accessDecisionManager;
	}
	  
}
