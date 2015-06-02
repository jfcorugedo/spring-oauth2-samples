package com.juan.test.spring.oauth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.test.BeforeOAuth2Context;
import org.springframework.security.oauth2.client.test.OAuth2ContextSetup;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.OAuth2AccessTokenSupport;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.client.token.grant.redirect.AbstractRedirectResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
public abstract class AbstractIntegrationTests {

	private static String globalTokenPath;

	private static String globalTokenKeyPath;

	private static String globalCheckTokenPath;

	private static String globalAuthorizePath;

	@Value("${local.server.port}")
	private int port;

	@Rule
	public HttpTestUtils http = HttpTestUtils.standard();

	@Rule
	public OAuth2ContextSetup context = OAuth2ContextSetup.standard(http);

	@Autowired
	private EmbeddedWebApplicationContext container;

	@Autowired(required = false)
	private TokenStore tokenStore;

	@Autowired(required = false)
	private ApprovalStore approvalStore;

	@Autowired(required = false)
	private DataSource dataSource;

	@Autowired
	private SecurityProperties security;

	@Autowired
	private ServerProperties server;

	@Autowired(required=false)
	@Qualifier("consumerTokenServices")
	private ConsumerTokenServices tokenServices;

	@After
	public void cancelToken() {
		try {
			OAuth2AccessToken token = context.getOAuth2ClientContext().getAccessToken();
			if (token != null) {
				tokenServices.revokeToken(token.getValue());
			}
		}
		catch (Exception e) {
			// ignore
		}
	}

	protected void cancelToken(String value) {
		try {
			tokenServices.revokeToken(value);
		}
		catch (Exception e) {
			// ignore
		}
	}

	protected AccessTokenProvider createAccessTokenProvider() {
		return null;
	}

	@Before
	public void init() {
		String prefix = server.getServletPrefix();
		http.setPort(port);
		http.setPrefix(prefix);
	}

	@BeforeOAuth2Context
	public void setupAccessTokenProvider() {
		AccessTokenProvider accessTokenProvider = createAccessTokenProvider();
		if (accessTokenProvider instanceof OAuth2AccessTokenSupport) {
			((OAuth2AccessTokenSupport) accessTokenProvider).setRequestFactory(context
					.getRestTemplate().getRequestFactory());
			context.setAccessTokenProvider(accessTokenProvider);
		}
	}

	@BeforeOAuth2Context
	public void fixPaths() {
		String prefix = server.getServletPrefix();
		http.setPort(port);
		http.setPrefix(prefix);
		BaseOAuth2ProtectedResourceDetails resource = (BaseOAuth2ProtectedResourceDetails) context.getResource();
		List<HttpMessageConverter<?>> converters = new ArrayList<>(context.getRestTemplate().getMessageConverters());
		converters.addAll(getAdditionalConverters());
		context.getRestTemplate().setMessageConverters(converters);
		context.getRestTemplate().setInterceptors(getInterceptors());
		resource.setAccessTokenUri(http.getUrl(tokenPath()));
		if (resource instanceof AbstractRedirectResourceDetails) {
			((AbstractRedirectResourceDetails) resource).setUserAuthorizationUri(http.getUrl(authorizePath()));
		}
		if (resource instanceof ImplicitResourceDetails) {
			resource.setAccessTokenUri(http.getUrl(authorizePath()));
		}
		if (resource instanceof ResourceOwnerPasswordResourceDetails && !(resource instanceof DoNotOverride)) {
			((ResourceOwnerPasswordResourceDetails) resource).setUsername(getUsername());
			((ResourceOwnerPasswordResourceDetails) resource).setPassword(getPassword());
		}
	}

	protected List<ClientHttpRequestInterceptor> getInterceptors() {
		return Collections.emptyList();
	}

	protected Collection<? extends HttpMessageConverter<?>> getAdditionalConverters() {
		return Collections.emptySet();
	}

	protected String getPassword() {
		return security.getUser().getPassword();
	}

	protected String getUsername() {
		return security.getUser().getName();
	}

	public interface DoNotOverride {

	}

	@After
	public void close() throws Exception {
		clear(tokenStore);
		clear(approvalStore);
	}

	protected String getBasicAuthentication() {
		return "Basic " + new String(Base64.encode((getUsername() + ":" + getPassword()).getBytes()));
	}

	private void clear(ApprovalStore approvalStore) throws Exception {
		if (approvalStore instanceof Advised) {
			Advised advised = (Advised) tokenStore;
			ApprovalStore target = (ApprovalStore) advised.getTargetSource().getTarget();
			clear(target);
			return;
		}
		if (approvalStore instanceof InMemoryApprovalStore) {
			((InMemoryApprovalStore) approvalStore).clear();
		}
		if (approvalStore instanceof JdbcApprovalStore) {
			JdbcTemplate template = new JdbcTemplate(dataSource);
			template.execute("delete from oauth_approvals");
		}
	}

	private void clear(TokenStore tokenStore) throws Exception {
		if (tokenStore instanceof Advised) {
			Advised advised = (Advised) tokenStore;
			TokenStore target = (TokenStore) advised.getTargetSource().getTarget();
			clear(target);
			return;
		}
		if (tokenStore instanceof InMemoryTokenStore) {
			((InMemoryTokenStore) tokenStore).clear();
		}
		if (tokenStore instanceof JdbcTokenStore) {
			JdbcTemplate template = new JdbcTemplate(dataSource);
			template.execute("delete from oauth_access_token");
			template.execute("delete from oauth_refresh_token");
			template.execute("delete from oauth_client_token");
			template.execute("delete from oauth_code");
		}
	}

	@Value("${oauth.paths.token:/oauth/token}")
	public void setTokenPath(String tokenPath) {
		globalTokenPath = tokenPath;
	}

	@Value("${oauth.paths.token_key:/oauth/token_key}")
	public void setTokenKeyPath(String tokenKeyPath) {
		globalTokenKeyPath = tokenKeyPath;
	}

	@Value("${oauth.paths.check_token:/oauth/check_token}")
	public void setCheckTokenPath(String tokenPath) {
		globalCheckTokenPath = tokenPath;
	}

	@Value("${oauth.paths.authorize:/oauth/authorize}")
	public void setAuthorizePath(String authorizePath) {
		globalAuthorizePath = authorizePath;
	}

	public static String tokenPath() {
		return globalTokenPath;
	}

	public static String tokenKeyPath() {
		return globalTokenKeyPath;
	}

	public static String checkTokenPath() {
		return globalCheckTokenPath;
	}

	public static String authorizePath() {
		return globalAuthorizePath;
	}
}
