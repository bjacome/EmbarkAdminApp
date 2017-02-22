package test.service.integration;

import org.junit.Test;

import test.IntegrationTester;

/*
 * By not having "Test" in this name, it won't be run by Maven.
 *
 */
public class ServiceIntegationSuite {

	public static final String LOGIN = "http://localhost:8080/rbcEmbarkAdminApp/api/security/login?loginId=test@yahoo.com&LoginPassword=test1234!a%21(http://localhost:8080/rbcEmbarkAdminApp/api/security/login?loginId=test@yahoo.com&LoginPassword=test1234%21a)";

	@Test
	public void test() throws Exception {
		IntegrationTester t = new IntegrationTester();
		t.login(LOGIN, "{}");
		String result = t.test("http://localhost:8080/rbcEmbarkAdminApp/api/rest/admin/environment/retrieve-all", "{}");
		System.out.println(result);
	}
}
