/**
 * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
 * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
 * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
 * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
 * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
 * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.societies.domainauthority.rest.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.internal.domainauthority.DaRestException;
import org.societies.api.internal.domainauthority.LocalPath;
import org.societies.api.internal.domainauthority.UrlPath;
import org.societies.api.internal.security.util.FileName;
import org.societies.api.security.digsig.DigsigException;
import org.societies.domainauthority.rest.control.ServiceClientJarAccess;
import org.societies.domainauthority.rest.util.Files;

/**
 * Class for hosting jar files for clients of 3rd party services.
 * 
 * @author Mitja Vardjan
 */
public class ServiceClientJar extends HttpServlet {

	private static final long serialVersionUID = 4625772782444356957L;

	private static Logger LOG = LoggerFactory.getLogger(ServiceClientJar.class);

	public ServiceClientJar() {
		LOG.info("Constructor");
	}

	/**
	 * Method processing HTTP GET requests, producing "application/java-archive" MIME media type.
	 * HTTP response: the requested file, e.g., service client in form of jar file.
	 * Error 401 if file name or signature not valid.
	 * Error 500 on server error.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		if (request.getPathInfo() == null) {
			LOG.warn("HTTP GET: request.getPathInfo() is null");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String path = request.getParameter(UrlPath.URL_PARAM_FILE);
		String serviceId = request.getParameter(UrlPath.URL_PARAM_SERVICE_ID);
		String signature = request.getParameter(UrlPath.URL_PARAM_SIGNATURE);
		
		LOG.info("HTTP GET: path = {}, service ID = {}, signature = " + signature, path, serviceId);
		if (path == null || serviceId == null || signature == null) {
			LOG.warn("Missing URL parameters");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		byte[] file;

		if (!ServiceClientJarAccess.isAuthorized(path, signature)) {
			LOG.warn("Invalid filename or key");
			// Return HTTP status code 401 - Unauthorized
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setHeader("WWW-Authenticate", "Digest realm=\"societies\"");
			return;
		}

		try {
			file = Files.getBytesFromFile(get3PServicePath(serviceId) + path);
		} catch (FileNotFoundException e) {
			try {
				file = Files.getBytesFromFile(path);
			} catch (IOException e2) {
				LOG.warn("Could not open file {}", path, e2);
				// Return HTTP status code 500 - Internal Server Error
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		} catch (IOException e) {
			LOG.warn("Could not open file {}", path, e);
			// Return HTTP status code 500 - Internal Server Error
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		LOG.info("Serving {}", path);
		
		response.setContentLength(file.length);
		//response.setContentType("application/java-archive");
		try {
			ServletOutputStream stream = response.getOutputStream();
			stream.write(file);
			stream.flush();
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			LOG.warn("Could not write response", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

	/**
	 * Method processing HTTP POST requests.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		String path = request.getParameter(UrlPath.URL_PARAM_FILE);
		String serviceId = request.getParameter(UrlPath.URL_PARAM_SERVICE_ID);
		String pubKey = request.getParameter(UrlPath.URL_PARAM_CERT);
	
		
		LOG.info("HTTP POST from {}; path = {}, service ID = " + serviceId + ", pubKey = " + pubKey,
				request.getRemoteHost(), path);
		LOG.warn("HTTP POST is not implemented. For uploading files, use HTTP PUT instead.");
		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

	/**
	 * Method processing HTTP PUT requests.
	 */
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		
		String path = request.getParameter(UrlPath.URL_PARAM_FILE);
		String serviceId = request.getParameter(UrlPath.URL_PARAM_SERVICE_ID);
		String cert = request.getParameter(UrlPath.URL_PARAM_CERT);

		LOG.info("HTTP PUT from {}; path = {}, service ID = " + serviceId + ", pubKey = " + cert,
				request.getRemoteHost(), path);

		if (path == null || serviceId == null || cert == null) {
			LOG.warn("Missing URL parameters");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		LOG.debug("HTTP PUT: cert fixed to {}", cert);

		InputStream is;
		try {
			is = Common.getInputStream(request);
		} catch (DaRestException e) {
			LOG.warn("HTTP PUT, ", e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		//path = path.replaceAll("[/\\\\]", File.separator);
		path = get3PServicePath(serviceId) + path;
		LOG.debug("Saving to file {}", path);
		try {
			Files.writeFile(is, path);
			ServiceClientJarAccess.addResource(path, cert);
			LOG.info("File {} stored successfuly.", path);
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		} catch (IOException e) {
			LOG.warn("Could not write to file {}", path, e);
			// Return HTTP status code 500 - Internal Server Error
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} catch (DigsigException e) {
			LOG.warn("Could not store public key", e);
			// Return HTTP status code 500 - Internal Server Error
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

	private String get3PServicePath(String serviceId) {

		serviceId = FileName.removeUnsupportedChars(serviceId);

		return LocalPath.PATH_3P_SERVICES + File.separator + serviceId + File.separator;
	}
}
