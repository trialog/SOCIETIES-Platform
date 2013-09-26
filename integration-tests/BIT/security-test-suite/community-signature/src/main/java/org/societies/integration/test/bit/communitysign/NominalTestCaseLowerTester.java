package org.societies.integration.test.bit.communitysign;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.identity.IIdentity;
import org.societies.api.identity.IIdentityManager;
import org.societies.api.internal.domainauthority.UrlPath;
import org.societies.api.internal.security.digsig.XmlSignature;
import org.societies.api.internal.security.util.UrlParamName;
import org.societies.api.security.digsig.ISignatureMgr;

/**
 * @author Mitja Vardjan
 *
 */
public class NominalTestCaseLowerTester extends XMLTestCase {
	
	private static Logger LOG = LoggerFactory.getLogger(NominalTestCaseLowerTester.class);
	
	private static String daUrl;
	private static ISignatureMgr signatureMgr;
	private static IIdentityManager identityManager;
	
	private static final String id1 = "id1";
	private static final String xml = "<?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<xml><node1 Id='" + id1 + "'>abc</node1></xml>";
	private static final String xmlSigned1 = "<?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<xml><node1 Id='" + id1 + "'>abc</node1><Signature>foo</Signature></xml>";
	private static final String xmlSigned2 = "<?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<xml><node1 Id='" + id1 + "'>abc</node1><Signature>bar</Signature></xml>";
	private static final String path = "foo.xml";
	
	/**
	 * Test case number
	 */
	public static int testCaseNumber;

	public NominalTestCaseLowerTester() {
	}

	/**
	 * This method is called only one time, at the very beginning of the process
	 * (after the constructor) in order to initialize the process.
	 * Select the relevant service example: the Calculator
	 * @throws org.societies.api.internal.security.policynegotiator.NegotiationException 
	 * @throws java.net.URISyntaxException 
	 * @throws InterruptedException 
	 */
	@BeforeClass
	public static void initialization() throws Exception {
		
		LOG.info("[#2165] Initialization");
		LOG.info("[#2165] Prerequisite: The CSS is created");
		LOG.info("[#2165] Prerequisite: The user is logged to the CSS");
		
		daUrl = TestCase2165.getDaUrl();
		
		signatureMgr = TestCase2165.getSignatureMgr();
		assertNotNull(signatureMgr);
		
		identityManager = TestCase2165.getIdentityManager();
		assertNotNull(identityManager);
	}

	/**
	 * This method is called before every @Test methods.
	 * Verify that the service is installed
	 */
	@Before
	public void setUp() throws Exception {
		LOG.info("[#2165] NominalTestCaseLowerTester::setUp");
		initialization();  // The method is not called automatically despite @BeforeClass annotation
		deletePreviousDocument();
	}

	/**
	 * This method is called after every @Test methods
	 */
	@After
	public void tearDown() {
		LOG.info("[#2165] tearDown");
	}
	
	@Test
	public void testDocumentUploadDownload() throws Exception {

		LOG.info("[#2165] testDocumentUploadDownload()");
		LOG.info("[#2165] *** Domain Authority Rest server is required for this test! ***");

		t1_uploadDocument();
		t2_downloadOriginalDocument();
		t3_downloadDocumentInvalidSig();
		t4_mergeDocument();
		t5_downloadMergedDocument();
		
		deletePreviousDocument();
	}
	
	private void deletePreviousDocument() throws Exception {
		String urlStr = uriForFileDownload(daUrl, path, signatureMgr.sign(path, identityManager.getThisNetworkNode()));
		LOG.info("[#2165] deletePreviousDocument(): deleting previous document at {}", urlStr);
		URL url = new URL(urlStr);
		Net net = new Net(url);
		net.delete();
	}
	
	/**
	 * Initial document upload
	 * 
	 * @throws Exception 
	 */
	private void t1_uploadDocument() throws Exception {
		
		IIdentity id = identityManager.getThisNetworkNode();
		X509Certificate cert = signatureMgr.getCertificate(id);
		String certStr = signatureMgr.cert2str(cert);
//		String notificationEndpoint = identityManager.getThisNetworkNode().getJid();
		String notificationEndpoint = "http://localhost/societies/document-signed-by-enough-entities";
		String urlStr = uriForFileUpload(daUrl, path, certStr, notificationEndpoint);
		LOG.info("[#2165] t1_uploadDocument(): uploading initial document to {}", urlStr);
		URL url = new URL(urlStr);
		Net net = new Net(url);
		boolean success;
		
		success = net.put(path, xml.getBytes(), url.toURI());
		assertTrue(success);
		
		// The file already exists, should get an error
		success = net.put(path, xml.getBytes(), url.toURI());
		assertFalse(success);
	}
	
	/**
	 * Download original (not merged yet) document and compare it to the source
	 * 
	 * @throws Exception
	 */
	private void t2_downloadOriginalDocument() throws Exception {
		
		byte[] downloaded = download();
		assertXMLEqual(xml, new String(downloaded));
	}
	
	/**
	 * URL with invalid signature => download should be rejected
	 */
	private void t3_downloadDocumentInvalidSig() throws Exception {
		
		String urlStr = uriForFileDownload(daUrl, path, signatureMgr.sign(path, identityManager.getThisNetworkNode()));
		String sigKeyword = UrlPath.URL_PARAM_SIGNATURE + "=";
		int sigKeywordEnd = urlStr.indexOf(sigKeyword) + sigKeyword.length();
		String urlStrInvalid = urlStr.substring(0, sigKeywordEnd) + "123456789012345678901234567890" +
				urlStr.substring(sigKeywordEnd + 30);
		LOG.info("[#2165] t3_downloadDocumentInvalidSig(): URL with invalid signature: {}", urlStrInvalid);
		
		assertEquals(urlStr.length(), urlStrInvalid.length(), 0.0);
		int httpCode = getHttpCode(new URL(urlStrInvalid));
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, httpCode, 0.0);
	}

	/**
	 * Merge new signature into the existing document
	 */
	private void t4_mergeDocument() throws Exception {

		String urlStr = uriForFileDownload(daUrl, path, signatureMgr.sign(path, identityManager.getThisNetworkNode()));
		LOG.info("[#2165] t4_mergeDocument(): uploading new document to {}", urlStr);
		URL url = new URL(urlStr);
		Net net = new Net(url);
		boolean success;
		
		success = net.put(path, xmlSigned1.getBytes(), url.toURI());
		assertTrue(success);
		success = net.put(path, xmlSigned2.getBytes(), url.toURI());
		assertTrue(success);
	}
	
	private void t5_downloadMergedDocument() throws Exception {
		byte[] downloaded = download();
		String downloadedXml = new String(downloaded);
		assertXMLNotEqual(xml, downloadedXml);
		assertXpathNotExists(XmlSignature.XML_SIGNATURE_XPATH, xml);
		assertXpathExists(XmlSignature.XML_SIGNATURE_XPATH, downloadedXml);
		assertXpathEvaluatesTo("0", "count(/xml/Signature)",xml);
		assertXpathEvaluatesTo("2", "count(/xml/Signature)",downloadedXml);
		assertXpathEvaluatesTo("foo", "/xml/Signature[1]",downloadedXml);
		assertXpathEvaluatesTo("bar", "/xml/Signature[2]",downloadedXml);
	}
	
	private byte[] download() throws Exception {
		
		String urlStr = uriForFileDownload(daUrl, path, signatureMgr.sign(path, identityManager.getThisNetworkNode()));
		int httpCode = getHttpCode(new URL(urlStr));
		assertEquals(HttpURLConnection.HTTP_OK, httpCode, 0.0);
		URL url = new URL(urlStr);
		Net net = new Net(url);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		net.download(os);
		byte[] downloaded = os.toByteArray();
		assertNotNull(downloaded);
		LOG.info("Size of downloaded xml: {}", downloaded.length);
		
		return downloaded;
	}
	
	/**
	 * URI for file download and file merge
	 */
	private String uriForFileDownload(String host, String path, String signature) {
		
		String uriStr;
		
		LOG.debug("uriForFileDownload({}, {}, ...)", host, path);
		
		uriStr = host + UrlPath.BASE + UrlPath.PATH_XML_DOCUMENTS + "/" + path.replaceAll(".*/", "") +
				"?" + UrlPath.URL_PARAM_SIGNATURE + "=" + signature;

		LOG.debug("uriForFileDownload(): uri = {}", uriStr);
		return uriStr;
	}
	
	private String uriForFileUpload(String host, String path, String cert, String notificationEndpoint) {
		
		String uriStr;

		LOG.debug("uriForFileUpload({}, {}, ...)", host, path);

		cert = UrlParamName.base64ToUrl(cert);
		
		uriStr = host + UrlPath.BASE + UrlPath.PATH_XML_DOCUMENTS + "/" + path.replaceAll(".*/", "") +
				"?" + UrlPath.URL_PARAM_CERT + "=" + cert +
				"&" + UrlPath.URL_PARAM_NOTIFICATION_ENDPOINT + "=" + notificationEndpoint +
				"&" + UrlPath.URL_PARAM_NUM_SIGNERS_THRESHOLD + "=" + 2; 

		LOG.debug("uriForFileUpload(): uri = {}", uriStr);
		return uriStr;
	}
	
	private int getHttpCode(URL resource) throws IOException {
		HttpURLConnection.setFollowRedirects(false);
		//HttpURLConnection.setInstanceFollowRedirects(false);
		HttpURLConnection con = (HttpURLConnection) resource.openConnection();
		con.setRequestMethod("HEAD");
		return con.getResponseCode();
	}
}