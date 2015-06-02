package com.juan.test.spring.oauth2;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitResourceDetails;

@SpringApplicationConfiguration(classes=SampleAuthorizationServerApplication.class)
public class ImplicitProviderTests extends AbstractIntegrationTests{

	@Test
	@OAuth2ContextConfiguration(resource = NonAutoApproveImplicit.class, initialize = false)
	public void testPostForAutomaticApprovalToken() throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", getBasicAuthentication());
		context.getAccessTokenRequest().setHeaders(headers);
		
		assertNotNull(context.getAccessToken());
	}

	static class NonAutoApproveImplicit extends ImplicitResourceDetails {
		public NonAutoApproveImplicit(Object target) {
			super();
			setClientId("my-trusted-client");
			setId(getClientId());
			setPreEstablishedRedirectUri("http://anywhere");
		}
	}
}
