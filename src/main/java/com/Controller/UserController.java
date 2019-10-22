package com.Controller;

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;




import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.BulkConnection;
import com.sforce.async.CSVReader;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;

import com.sforce.async.OperationEnum;


import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;

import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;

import com.sforce.soap.tooling.CodeCoverageResult;
import com.sforce.soap.tooling.RunTestsRequest;
import com.sforce.soap.tooling.RunTestsResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.soap.tooling.sobject.ApexTrigger;

import com.sforce.soap.tooling.sobject.ValidationRule;
import com.sforce.soap.tooling.sobject.WorkflowRule;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

@Controller
public class UserController {

	@RequestMapping(value = "/")
	public String index() throws ServletException {
		
		/*
		 * Tomcat tomcat = new Tomcat();
		 * 
		 * //Set Port # tomcat.setPort(8082);
		 * 
		 * 
		 * StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new
		 * File(webAppDirLocation).getAbsolutePath());
		 * 
		 * tomcat.start(); tomcat.getServer().await();
		 * 
		 * 
		 * tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));
		 * tomcat.addWebapp("/SalesforceCustomX", new
		 * File("src/main/webapp").getAbsolutePath()); tomcat.start();
		 * System.out.print("STARTED"); tomcat.getServer().await();
		 */
		 
		return "index";

	}

	@RequestMapping(value = "/login_proceed", method = RequestMethod.POST)
	public String dashboard(@RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam("loginurl") String loginurl, HttpServletRequest request, HttpSession session) {
		String url = null;
		try {

			String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

			ConnectorConfig config = new ConnectorConfig();
			config.setAuthEndpoint(authEndPoint);

			config.setUsername(username);
			config.setPassword(password);

			PartnerConnection connection = new PartnerConnection(config);
			GetUserInfoResult userInfo = connection.getUserInfo();

			request.setAttribute("username", userInfo.getUserFullName());
			System.out.println("UserID: " + userInfo.getUserId());
			System.out.println("User Full Name: " + userInfo.getUserFullName());
			System.out.println("User Email: " + userInfo.getUserEmail());
			System.out.println();
			System.out.println("SessionID: " + config.getSessionId());
			System.out.println("Auth End Point: " + config.getAuthEndpoint());
			System.out.println("Service End Point: " + config.getServiceEndpoint());

			LoginResult lr = connection.login(config.getUsername(), config.getPassword());

			ConnectorConfig toolingConfig = new ConnectorConfig();
			toolingConfig.setManualLogin(false);
			toolingConfig.setSessionId(lr.getSessionId());

			toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
			ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

			System.out.println("Auth Endpoint" + toolingConfig.getAuthEndpoint() + config.getAuthEndpoint());

			session = request.getSession(true);

			session.setAttribute("username", username);
			session.setAttribute("password", password);
			session.setAttribute("loginurl", loginurl);

			url = "login_success";

		} catch (Exception e) {
			e.printStackTrace();
			url = "login_error";
		}
		return url;

	}

	@RequestMapping(value = "/dashboard")
	public String dashboard(HttpServletRequest request, HttpSession session, Model m) throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				// Make the describeGlobal() call
				DescribeGlobalResult describeGlobalResult = connection.describeGlobal();

				// Get the sObjects from the describe global result
				DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();

				for (int i = 0; i < sobjectResults.length; i++) {

					// session.setAttribute("sobjlist",sobjectResults[i].getName().toString());
					sobjectResults[i].getName().toString();
					ArrayList stdobjlist = new ArrayList();
					

					for (DescribeGlobalSObjectResult stdobj : sobjectResults) {

						if (!stdobj.isCustom() && stdobj.isUndeletable()) {
							@SuppressWarnings("rawtypes")
							Map map = new HashMap();
							map.put("Label", stdobj.getLabel().toString());
							map.put("Name", stdobj.getName().toString());
							stdobjlist.add(map);
							request.setAttribute("slist", stdobjlist);
						}

					}

				}
			}

		}
		return "standard_object";

	}

	@RequestMapping(value = "/login_error")
	public String login_error() {

		return "login_error";

	}

	@RequestMapping(value = "/login")
	public String login() {

		return "login";

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object")
	public String standardobject(HttpServletRequest request, HttpSession session, Model m) throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				// Make the describeGlobal() call
				DescribeGlobalResult describeGlobalResult = connection.describeGlobal();

				// Get the sObjects from the describe global result
				DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();

				for (int i = 0; i < sobjectResults.length; i++) {

					// session.setAttribute("sobjlist",sobjectResults[i].getName().toString());
					sobjectResults[i].getName().toString();
					ArrayList stdobjlist = new ArrayList();
					;

					for (DescribeGlobalSObjectResult stdobj : sobjectResults) {

						if (!stdobj.isCustom() && stdobj.isUndeletable()) {
							@SuppressWarnings("rawtypes")
							Map map = new HashMap();
							map.put("Label", stdobj.getLabel().toString());
							map.put("Name", stdobj.getName().toString());
							stdobjlist.add(map);
							request.setAttribute("slist", stdobjlist);
						}

					}

				}
			}

		}
		return "standard_object";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object")
	public String customobject(HttpServletRequest request, HttpSession session, Model m) throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				// Make the describeGlobal() call
				DescribeGlobalResult describeGlobalResult = connection.describeGlobal();

				// Get the sObjects from the describe global result
				DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();

				for (int i = 0; i < sobjectResults.length; i++) {

					// session.setAttribute("sobjlist",sobjectResults[i].getName().toString());
					sobjectResults[i].getName().toString();
					ArrayList customobjlist = new ArrayList();

					for (DescribeGlobalSObjectResult stdobj : sobjectResults) {

						if (stdobj.isCustom()) {
							@SuppressWarnings("rawtypes")
							Map map = new HashMap();
							map.put("Label", stdobj.getLabel().toString());
							map.put("Name", stdobj.getName().toString());
							customobjlist.add(map);
							request.setAttribute("clist", customobjlist);
						}

					}

				}
			}

		}
		return "custom_object";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_field/{name}", method = RequestMethod.GET)
	public String customobjectfield(@PathVariable("name") String name, HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				DescribeSObjectResult[] describeSObjectResults;
				describeSObjectResults = connection.describeSObjects(new String[] { name });

				// Iterate through the list of describe sObject results
				for (int i = 0; i < describeSObjectResults.length; i++) {
					for (DescribeSObjectResult desObj : describeSObjectResults) {
						// desObj = describeSObjectResults[i];
						// Get the name of the sObject
						String objectName = desObj.getName();
						/*
						 * System.out.println("Object Name: " + objectName);
						 * 
						 * System.out.println("Field Label"+","+"Field Name"+","+"Data Type");
						 */

						Field[] fields = desObj.getFields();

						for (int j = 0; j < fields.length; j++) {
							ArrayList flist = new ArrayList();

							for (Field field : fields) {
								@SuppressWarnings("rawtypes")
								Map map = new HashMap();
								map.put("Label", field.getLabel().toString());
								map.put("Name", field.getName().toString());
								map.put("Datatype", field.getType());
								// System.out.println(field.getName());
								flist.add(map);
								request.setAttribute("flist", flist);

							}

							// Field field = fields[j];
							// System.out.println(field.getName());

						}
					}

				}

			}

		}

		return "custom_object_field";

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_field/{name}", method = RequestMethod.GET)
	public String standardobjectfield(@PathVariable("name") String name, HttpServletRequest request,
			HttpSession session) throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				DescribeSObjectResult[] describeSObjectResults;
				describeSObjectResults = connection.describeSObjects(new String[] { name });

				// Iterate through the list of describe sObject results
				for (int i = 0; i < describeSObjectResults.length; i++) {
					for (DescribeSObjectResult desObj : describeSObjectResults) {
						// desObj = describeSObjectResults[i];
						// Get the name of the sObject
						String objectName = desObj.getName();
						/*
						 * System.out.println("Object Name: " + objectName);
						 * 
						 * System.out.println("Field Label"+","+"Field Name"+","+"Data Type");
						 */

						Field[] fields = desObj.getFields();

						for (int j = 0; j < fields.length; j++) {
							ArrayList flist = new ArrayList();

							for (Field field : fields) {
								@SuppressWarnings("rawtypes")
								Map map = new HashMap();
								map.put("Label", field.getLabel().toString());
								map.put("Name", field.getName().toString());
								map.put("Datatype", field.getType());
								// System.out.println(field.getName());
								flist.add(map);
								request.setAttribute("flist", flist);

							}

							// Field field = fields[j];
							// System.out.println(field.getName());

						}
					}

				}

			}

		}

		return "standard_object_field";

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_dataloader", method = RequestMethod.GET)
	public String standardObjectDataloader(HttpServletRequest request, HttpSession session) throws ConnectionException {
		String name = null;
		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());

				// Make the describeGlobal() call
				DescribeGlobalResult describeGlobalResult = connection.describeGlobal();

				// Get the sObjects from the describe global result
				DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();
				ArrayList stdobjlist = new ArrayList();
				;
				for (int i = 0; i < sobjectResults.length; i++) {

					// session.setAttribute("sobjlist",sobjectResults[i].getName().toString());

					// for (DescribeGlobalSObjectResult stdobj : sobjectResults) {

					// }
					if (!sobjectResults[i].isCustom() && sobjectResults[i].isUndeletable()) {
						@SuppressWarnings("rawtypes")
						Map map = new HashMap();
						map.put("Label", sobjectResults[i].getLabel().toString());
						map.put("Name", sobjectResults[i].getName().toString());
						stdobjlist.add(map);
						session.setAttribute("namesobj", sobjectResults[i].getName().toString());

						request.setAttribute("slist", stdobjlist);
					}
				}

			}
		}

		return "standard_object_dataloader";

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_details/{name}", method = RequestMethod.GET)
	public String standardObjectDetails(@PathVariable("name") String name, HttpServletRequest request,
			HttpSession session) throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

				DescribeSObjectResult[] describeSObjectResults;
				describeSObjectResults = connection.describeSObjects(new String[] { name });
				System.out.println("name->" + name);
				request.setAttribute("name", name);
				session.setAttribute("namenew", name);
				// Iterate through the list of describe sObject results
				for (int i = 0; i < describeSObjectResults.length; i++) {
					for (DescribeSObjectResult desObj : describeSObjectResults) {
						// desObj = describeSObjectResults[i];
						// Get the name of the sObject
						String objectName = desObj.getName();
						/*
						 * System.out.println("Object Name: " + objectName);
						 * 
						 * System.out.println("Field Label"+","+"Field Name"+","+"Data Type");
						 */

						com.sforce.soap.tooling.QueryResult qResult1 = null;
						String soqlQuery = "Select Id,Active,Description,ValidationName,EntityDefinition.DeveloperName,ErrorDisplayField, ErrorMessage From ValidationRule where EntityDefinition.QualifiedApiName='"
								+ name + "'";
						System.out.println("name->->" + name);
						// request.setAttribute("Sobjname", name.toUpperCase());
						qResult1 = toolingConnection.query(soqlQuery);
						boolean done = false;
						if (qResult1.getSize() > 0) {

							com.sforce.soap.tooling.sobject.SObject[] records = qResult1.getRecords();

							// ArrayList arr=new ArrayList();
							// ArrayList arr1=new ArrayList();
							@SuppressWarnings("rawtypes")
							ArrayList vrule = new ArrayList();
							for (int j = 0; j < qResult1.getSize(); j++) {

								ValidationRule vr = new ValidationRule();
								vr = (ValidationRule) records[j];
								System.out.println("Name" + vr.getValidationName());
								System.out.println("Description::" + vr.getDescription());
								if (vr.getActive().equals(true)) {
									Map map = new HashMap();

									map.put("Name", vr.getValidationName());
									map.put("Active", vr.getActive());
									map.put("Desc", vr.getDescription());
									map.put("ObjName", name);
									vrule.add(map);
									request.setAttribute("vrulelist", vrule);
								}

							}

							System.out.println(
									"Logged-in user can see a total of " + qResult1.getSize() + " validation rule.");
							// System.out.println(arr1.toString());
							request.setAttribute("vsize", qResult1.getSize());

						}

						// -----------Workflow rule------------------------------------//

						com.sforce.soap.tooling.QueryResult qResult2 = null;
						String soqlQuery1 = "Select Name,TableEnumOrId From WorkflowRule where TableEnumOrId='" + name
								+ "'";
						System.out.println("name->->" + name);
						// request.setAttribute("Sobjname", name.toUpperCase());
						qResult2 = toolingConnection.query(soqlQuery1);
						boolean done1 = false;
						if (qResult2.getSize() > 0) {

							com.sforce.soap.tooling.sobject.SObject[] records = qResult2.getRecords();

							// ArrayList arr=new ArrayList();
							// ArrayList arr1=new ArrayList();
							@SuppressWarnings("rawtypes")
							ArrayList wrule = new ArrayList();
							for (int j = 0; j < qResult2.getSize(); j++) {

								WorkflowRule vr = new WorkflowRule();
								vr = (WorkflowRule) records[j];
								System.out.println("Name" + vr.getName());
								System.out.println("Description::" + vr.getMetadata());

								Map map = new HashMap();

								map.put("Name", vr.getName());
								map.put("Enum", vr.getTableEnumOrId());

								map.put("ObjName", name);
								wrule.add(map);
								request.setAttribute("wrulelist", wrule);

							}

							System.out.println(
									"Logged-in user can see a total of " + qResult2.getSize() + " workflow rule.");
							request.setAttribute("wsize", qResult2.getSize());
							// System.out.println(arr1.toString());

						}

						com.sforce.soap.tooling.QueryResult qResult3 = null;
						String soqlQuery2 = "SELECT Name FROM ApexTrigger WHERE TableEnumOrId='" + name + "'";
						System.out.println("name->->" + name);
						// request.setAttribute("Sobjname", name.toUpperCase());
						qResult3 = toolingConnection.query(soqlQuery2);
						boolean done2 = false;
						if (qResult3.getSize() > 0) {

							com.sforce.soap.tooling.sobject.SObject[] records = qResult3.getRecords();

							// ArrayList arr=new ArrayList();
							// ArrayList arr1=new ArrayList();
							@SuppressWarnings("rawtypes")
							ArrayList tr = new ArrayList();
							for (int j = 0; j < qResult3.getSize(); j++) {

								ApexTrigger vr = new ApexTrigger();
								vr = (ApexTrigger) records[j];
								System.out.println("Name" + vr.getName());
								System.out.println("Status::" + vr.getStatus());

								Map map = new HashMap();

								map.put("Name", vr.getName());
								map.put("Status", vr.getStatus());

								map.put("ObjName", name);
								tr.add(map);
								request.setAttribute("trlist", tr);

							}

							System.out.println("Logged-in user can see a total of " + qResult3.getSize() + " trigger.");
							request.setAttribute("trsize", qResult3.getSize());
							// System.out.println(arr1.toString());

						}

					}
				}

			}

		}

		return "standard_object_details";

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_bulk_upload_insert_display", method = RequestMethod.GET)
	public String standardObjectDetailsBulkUploadDisplay(HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

			}
		}
		return "standard_object_bulk_upload_insert_display";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_bulk_upload_insert_details", method = RequestMethod.POST)
	public String standardObjectDetailsBulkUpload(@RequestPart("file") MultipartFile file,
			@RequestParam("batchsize") int batchsize, HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

				try {
					String soapEndpoint = config.getServiceEndpoint();
					String apiVersion = "46.0";
					String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/"
							+ apiVersion;
					config.setRestEndpoint(restEndpoint);
					// This should only be false when doing debugging.
					config.setCompression(true);
					// Set this to true to see HTTP requests and responses on stdout
					config.setTraceMessage(false);
					BulkConnection bulkconn = new BulkConnection(config);
					JobInfo job = new JobInfo();
					System.out.println("NAME_NEW:" + session.getAttribute("namenew"));
					job.setObject((String) session.getAttribute("namenew"));
					job.setOperation(OperationEnum.insert);
					// job.setExternalIdFieldName("ExternalId__c");
					job.setContentType(ContentType.CSV);
					job.setConcurrencyMode(ConcurrencyMode.Parallel);
					job = bulkconn.createJob(job);
					System.out.println(job);

					List<BatchInfo> batchInfoList = new ArrayList<BatchInfo>();

					if (file != null && !((MultipartFile) file).isEmpty()) {

						byte[] bytes = ((MultipartFile) file).getBytes();
						// String
						// filePath="C:\\Users\\sagnik.biswas\\eclipse-workspace-2019\\SalesforceCustomX\\src\\main\\webapp\\assets\\images";
						String fileName = String.valueOf(session.getAttribute("namenew")) + ".csv";
						File serverFile = new File(fileName);
						FileOutputStream fos = new FileOutputStream(serverFile);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						bos.write(bytes);
						int maxBytesPerBatch = 10000000; // 10 million bytes per batch
						int maxRowsPerBatch = batchsize; // 10 thousand rows per batch
						System.out.println("BATCHSIZE:" + maxRowsPerBatch);
						int currentBytes = 0;
						int currentLines = 0;

						if (currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) {
							// createBatch(tmpOut, tmpFile, batchInfos, connection, jobInfo);
							FileInputStream tmpInputStream = new FileInputStream(serverFile);
							BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
							System.out.println(batchInfo);
							batchInfoList.add(batchInfo);
							currentBytes = 0;
							currentLines = 0;
						}
						if (currentBytes == 0) {
							// tmpOut = fos;
							fos.write(bytes);
							currentBytes = bytes.length;
							currentLines = 1;
						}
						fos.write(bytes);
						currentBytes += bytes.length;
						currentLines++;

						// Finished processing all rows
						// Create a final batch for any remaining data
						if (currentLines > 1) {
							FileInputStream tmpInputStream = new FileInputStream(serverFile);
							BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
							System.out.println(batchInfo);

							batchInfoList.add(batchInfo);
							

							System.out.println("Failed" + batchInfo.getNumberRecordsFailed());
							

						}
						long sleepTime = 0L;
						Set<String> incomplete = new HashSet<String>();
						for (BatchInfo bi : batchInfoList) {
							incomplete.add(bi.getId());
						}
						while (!incomplete.isEmpty()) {
							try {
								Thread.sleep(sleepTime);
							} catch (InterruptedException e) {
							}
							System.out.println("Awaiting results..." + incomplete.size());
							sleepTime = 10000L;
							BatchInfo[] statusList = bulkconn.getBatchInfoList(job.getId()).getBatchInfo();
							for (BatchInfo b : statusList) {
								if (b.getState() == BatchStateEnum.Completed || b.getState() == BatchStateEnum.Failed) {
									if (incomplete.remove(b.getId())) {
										System.out.println("BATCH STATUS:\n" + b);
										@SuppressWarnings("rawtypes")
										ArrayList blist = new ArrayList();
										@SuppressWarnings("rawtypes")
										Map map = new HashMap();
										map.put("jobId", b.getJobId());
										map.put("recordsFailed", b.getNumberRecordsFailed());
										map.put("recordsProcessed", b.getNumberRecordsProcessed());
										map.put("state", b.getState());
										map.put("statemsg", b.getStateMessage());
										map.put("apiProcessingTime", b.getApiActiveProcessingTime());
										map.put("apexProcessingTime", b.getApexProcessingTime());
										blist.add(map);
										request.setAttribute("blist", blist);
									}
								}
							}
						}

						for (BatchInfo b : batchInfoList) {
							CSVReader rdr = new CSVReader(bulkconn.getBatchResultStream(job.getId(), b.getId()));
							List<String> resultHeader = rdr.nextRecord();
							int resultCols = resultHeader.size();

							List<String> row;
							while ((row = rdr.nextRecord()) != null) {
								Map<String, String> resultInfo = new HashMap<String, String>();
								for (int a = 0; a < resultCols; a++) {
									resultInfo.put(resultHeader.get(a), row.get(a));
								}
								boolean success = Boolean.valueOf(resultInfo.get("Success"));
								boolean created = Boolean.valueOf(resultInfo.get("Created"));
								String id = resultInfo.get("Id");
								String error = resultInfo.get("Error");
								if (success && created) {
									System.out.println("Created row with id " + id);
									
								} else if (!success) {
									System.out.println("Failed with error: " + error);
								}
							}
						}

					} else {
						System.out.println("File is empty");
					}

				}

				catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return "standard_object_bulk_upload_insert_details";
	}

	// ------------------------Standard Object
	// Update--------------------------------//

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_bulk_upload_update_display", method = RequestMethod.GET)
	public String standardObjectDisplayBulkUploadDisplayUpdate(HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

			}
		}
		return "standard_object_bulk_upload_update_display";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_bulk_upload_update_details", method = RequestMethod.POST)
	public String standardObjectDetailsBulkUploadUpdate(@RequestPart("file") MultipartFile file,
			@RequestParam("batchsize") int batchsize, HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/24.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				
				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());


				
				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

				try {
					String soapEndpoint = config.getServiceEndpoint();
					String apiVersion = "46.0";
					String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/"
							+ apiVersion;
					config.setRestEndpoint(restEndpoint);
					// This should only be false when doing debugging.
					config.setCompression(true);
					// Set this to true to see HTTP requests and responses on stdout
					config.setTraceMessage(false);
					BulkConnection bulkconn = new BulkConnection(config);
					JobInfo job = new JobInfo();
					System.out.println("NAME_NEW:" + session.getAttribute("namenew"));
					
					
					job.setObject((String) session.getAttribute("namenew"));
					//job.setExternalIdFieldName("EXTERNALID__C");
					job.setOperation(OperationEnum.update);
					job.setConcurrencyMode(ConcurrencyMode.Parallel);
					job.setContentType(ContentType.CSV);
					job = bulkconn.createJob(job);
				
					    assert job.getId() != null;

					List<BatchInfo> batchInfoList = new ArrayList<BatchInfo>();

					if (file != null && !((MultipartFile) file).isEmpty()) {

						byte[] bytes = ((MultipartFile) file).getBytes();
						// String
						// filePath="C:\\Users\\sagnik.biswas\\eclipse-workspace-2019\\SalesforceCustomX\\src\\main\\webapp\\assets\\images";
						String fileName = String.valueOf(session.getAttribute("namenew")) + ".csv";
						File serverFile = new File(fileName);
						FileOutputStream fos = new FileOutputStream(serverFile);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						bos.write(bytes);
						int maxBytesPerBatch = 10000000; // 10 million bytes per batch
						int maxRowsPerBatch = batchsize; // 10 thousand rows per batch
						System.out.println("BATCHSIZE:" + maxRowsPerBatch);
						int currentBytes = 0;
						int currentLines = 0;

						if (currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) {
							// createBatch(tmpOut, tmpFile, batchInfos, connection, jobInfo);
							FileInputStream tmpInputStream = new FileInputStream(serverFile);
							BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
							System.out.println(batchInfo);
							batchInfoList.add(batchInfo);
							currentBytes = 0;
							currentLines = 0;
						}
						if (currentBytes == 0) {
							// tmpOut = fos;
							fos.write(bytes);
							currentBytes = bytes.length;
							currentLines = 1;
						}
						fos.write(bytes);
						currentBytes += bytes.length;
						currentLines++;

						// Finished processing all rows
						// Create a final batch for any remaining data
						if (currentLines > 1) {
							FileInputStream tmpInputStream = new FileInputStream(serverFile);
							BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
							System.out.println(batchInfo);

							batchInfoList.add(batchInfo);
							

							//System.out.println("Failed" + batchInfo.getNumberRecordsFailed());
							

						 

					}

						/*Fillo fillo=new Fillo();
						Connection con=fillo.getConnection(serverFile.toString());
						String query="Select Id,Name,Stagename,Type from Opportunity ";
						Recordset recordset=con.executeQuery(query);
						 
						while(recordset.next()){
						System.out.println(recordset.getField("Id"));*/
						//}	
					    /*String query = "Select Id,Name,Stagename,Type from Opportunity";
					    
					    long start = System.currentTimeMillis();
					    
					    BatchInfo info = null;
					    ByteArrayInputStream bout = 
					        new ByteArrayInputStream(query.getBytes());
					    info = bulkconn.createBatchFromStream(job, bout);
					    
					    String[] queryResults = null;
					    
					    for(int i=0; i<10000; i++) {
					      Thread.sleep(30000); //30 sec
					      info = bulkconn.getBatchInfo(job.getId(), 
					          info.getId());
					      
					      if (info.getState() == BatchStateEnum.Completed) {
					        QueryResultList list = 
					        		bulkconn.getQueryResultList(job.getId(), 
					                info.getId());
					        queryResults = list.getResult();
					        
					        System.out.println("-------------- Completed ----------" 
						            + info + queryResults);
					        break;
					      } else if (info.getState() == BatchStateEnum.Failed) {
					        System.out.println("-------------- failed ----------" 
					            + info);
					        break;
					      } else {
					        System.out.println("-------------- waiting ----------" 
					            + info);
					      }
					    }
					    
					    if (queryResults != null) {
					        for (String resultId : queryResults) {
					          bulkconn.getQueryResultStream(job.getId(), 
					              info.getId(), resultId);*/
					          
					            
								//job = bulkconn.updateJob(job);
								
								
								  long sleepTime = 0L; Set<String> incomplete = new HashSet<String>(); for
								  (BatchInfo bi : batchInfoList) { incomplete.add(bi.getId()); } while
								  (!incomplete.isEmpty()) { try { Thread.sleep(sleepTime); } catch
								  (InterruptedException e) {} System.out.println("Awaiting results..." +
								  incomplete.size()); sleepTime = 10000L; BatchInfo[] statusList =
								  bulkconn.getBatchInfoList(job.getId()).getBatchInfo(); for (BatchInfo b :
								  statusList) { if (b.getState() == BatchStateEnum.Completed || b.getState() ==
								  BatchStateEnum.Failed) { if (incomplete.remove(b.getId())) {
								  System.out.println("BATCH STATUS:\n" + b); 
								  @SuppressWarnings("rawtypes")
									ArrayList blist = new ArrayList();
									@SuppressWarnings("rawtypes")
									Map map = new HashMap();
									map.put("jobId", b.getJobId());
									map.put("recordsFailed", b.getNumberRecordsFailed());
									map.put("recordsProcessed", b.getNumberRecordsProcessed());
									map.put("state", b.getState());
									map.put("statemsg", b.getStateMessage());
									map.put("apiProcessingTime", b.getApiActiveProcessingTime());
									map.put("apexProcessingTime", b.getApexProcessingTime());
									blist.add(map);
									request.setAttribute("blist", blist);
								  } } } }
								 
						        for (BatchInfo b : batchInfoList) {
						            CSVReader rdr =
						              new CSVReader(bulkconn.getBatchResultStream(job.getId(), b.getId()));
						            List<String> resultHeader = rdr.nextRecord();
						            int resultCols = resultHeader.size();

						            List<String> row;
						            while ((row = rdr.nextRecord()) != null) {
						                Map<String, String> resultInfo = new HashMap<String, String>();
						                for (int i = 0; i < resultCols; i++) {
						                    resultInfo.put(resultHeader.get(i), row.get(i));
						                }
						                
						                boolean success = Boolean.valueOf(resultInfo.get("Success"));
						                boolean updated = Boolean.valueOf(resultInfo.get("Update"));
						               
						                String id = resultInfo.get("Id");
						                String error = resultInfo.get("Error");
						                if (success && updated) {
						                    System.out.println("Updated row with id " + id);
						                } else if (!success) {
						                    System.out.println("Failed with error: " + error);
						                }
						            }
						        }
					        
					      } 
					     
						 
					        
								
							
					      
					    
					 
						
						/*
						 * long sleepTime = 0L; Set<String> incomplete = new HashSet<String>(); for
						 * (BatchInfo bi : batchInfoList) { incomplete.add(bi.getId()); } while
						 * (!incomplete.isEmpty()) { try { Thread.sleep(sleepTime); } catch
						 * (InterruptedException e) { } System.out.println("Awaiting results..." +
						 * incomplete.size()); sleepTime = 10000L; BatchInfo[] statusList =
						 * bulkconn.getBatchInfoList(job.getId()).getBatchInfo(); for (BatchInfo b :
						 * statusList) { if (b.getState() == BatchStateEnum.Completed || b.getState() ==
						 * BatchStateEnum.Failed) { if (incomplete.remove(b.getId())) {
						 * System.out.println("BATCH STATUS:\n" + b);
						 * 
						 * @SuppressWarnings("rawtypes") ArrayList blist = new ArrayList();
						 * 
						 * @SuppressWarnings("rawtypes") Map map = new HashMap(); map.put("jobId",
						 * b.getJobId()); map.put("recordsFailed", b.getNumberRecordsFailed());
						 * map.put("recordsProcessed", b.getNumberRecordsProcessed()); map.put("state",
						 * b.getState()); map.put("statemsg", b.getStateMessage());
						 * map.put("apiProcessingTime", b.getApiActiveProcessingTime());
						 * map.put("apexProcessingTime", b.getApexProcessingTime()); blist.add(map);
						 * request.setAttribute("blist", blist); } } } }
						 */

							

				
					else {
						System.out.println("File is empty");
					}	
					}
			catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return "standard_object_bulk_upload_update_details";
	}

	// ------------------------Standard Object Update
	// End----------------------------//
	
	// ------------------------Standard Object Delete
		// Start----------------------------//
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_bulk_upload_delete_display", method = RequestMethod.GET)
	public String standardObjectDisplayBulkUploadDisplayUpsert(HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

			}
		}
		return "standard_object_bulk_upload_delete_display";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/standard_object_bulk_upload_delete_details", method = RequestMethod.POST)
	public String standardObjectDetailsBulkUploadUpsert(@RequestPart("file") MultipartFile file,
			@RequestParam("batchsize") int batchsize, HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/24.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				
				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());


				
				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

				try {
					String soapEndpoint = config.getServiceEndpoint();
					String apiVersion = "46.0";
					String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/"
							+ apiVersion;
					config.setRestEndpoint(restEndpoint);
					// This should only be false when doing debugging.
					config.setCompression(true);
					// Set this to true to see HTTP requests and responses on stdout
					config.setTraceMessage(false);
					BulkConnection bulkconn = new BulkConnection(config);
					JobInfo job = new JobInfo();
					System.out.println("NAME_NEW:" + session.getAttribute("namenew"));
					job.setObject((String) session.getAttribute("namenew"));
					job.setOperation(OperationEnum.delete);
					
					//System.out.println("UPSERT..");
					//job.setExternalIdFieldName("ExternalId__c"); 
					
					
				//else
				//{
					//job.setOperation(OperationEnum.update);	
					//System.out.println("UPDATE..");
				//}
				
				//job.setConcurrencyMode(ConcurrencyMode.Parallel);
				   job.setContentType(ContentType.CSV);
					
				
							job = bulkconn.createJob(job);
							
								    assert job.getId() != null;
                               
                                	
                               
								List<BatchInfo> batchInfoList = new ArrayList<BatchInfo>();

								if (file != null && !((MultipartFile) file).isEmpty()) {

									byte[] bytes = ((MultipartFile) file).getBytes();
									// String
									// filePath="C:\\Users\\sagnik.biswas\\eclipse-workspace-2019\\SalesforceCustomX\\src\\main\\webapp\\assets\\images";
									String fileName = String.valueOf(session.getAttribute("namenew")) + ".csv";
									File serverFile = new File(fileName);
									FileOutputStream fos = new FileOutputStream(serverFile);
									BufferedOutputStream bos = new BufferedOutputStream(fos);
									bos.write(bytes);
									int maxBytesPerBatch = 10000000; // 10 million bytes per batch
									int maxRowsPerBatch = batchsize; // 10 thousand rows per batch
									System.out.println("BATCHSIZE:" + maxRowsPerBatch);
									int currentBytes = 0;
									int currentLines = 0;

									if (currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) {
										// createBatch(tmpOut, tmpFile, batchInfos, connection, jobInfo);
										FileInputStream tmpInputStream = new FileInputStream(serverFile);
										BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
										System.out.println(batchInfo);
										batchInfoList.add(batchInfo);
										currentBytes = 0;
										currentLines = 0;
									}
									if (currentBytes == 0) {
										// tmpOut = fos;
										fos.write(bytes);
										currentBytes = bytes.length;
										currentLines = 1;
									}
									fos.write(bytes);
									currentBytes += bytes.length;
									currentLines++;

									// Finished processing all rows
									// Create a final batch for any remaining data
									if (currentLines > 1) {
										FileInputStream tmpInputStream = new FileInputStream(serverFile);
										BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
										System.out.println(batchInfo);

										batchInfoList.add(batchInfo);
										

										//System.out.println("Failed" + batchInfo.getNumberRecordsFailed());
										

									 

								}

									/*Fillo fillo=new Fillo();
									Connection con=fillo.getConnection(serverFile.toString());
									String query="Select Id,Name,Stagename,Type from Opportunity ";
									Recordset recordset=con.executeQuery(query);
									 
									while(recordset.next()){
									System.out.println(recordset.getField("Id"));*/
									//}	
								    /*String query = "Select Id,Name,Stagename,Type from Opportunity";
								    
								    long start = System.currentTimeMillis();
								    
								    BatchInfo info = null;
								    ByteArrayInputStream bout = 
								        new ByteArrayInputStream(query.getBytes());
								    info = bulkconn.createBatchFromStream(job, bout);
								    
								    String[] queryResults = null;
								    
								    for(int i=0; i<10000; i++) {
								      Thread.sleep(30000); //30 sec
								      info = bulkconn.getBatchInfo(job.getId(), 
								          info.getId());
								      
								      if (info.getState() == BatchStateEnum.Completed) {
								        QueryResultList list = 
								        		bulkconn.getQueryResultList(job.getId(), 
								                info.getId());
								        queryResults = list.getResult();
								        
								        System.out.println("-------------- Completed ----------" 
									            + info + queryResults);
								        break;
								      } else if (info.getState() == BatchStateEnum.Failed) {
								        System.out.println("-------------- failed ----------" 
								            + info);
								        break;
								      } else {
								        System.out.println("-------------- waiting ----------" 
								            + info);
								      }
								    }
								    
								    if (queryResults != null) {
								        for (String resultId : queryResults) {
								          bulkconn.getQueryResultStream(job.getId(), 
								              info.getId(), resultId);*/
								          
								            
											//job = bulkconn.updateJob(job);
											
											
											  long sleepTime = 0L; Set<String> incomplete = new HashSet<String>(); for
											  (BatchInfo bi : batchInfoList) { incomplete.add(bi.getId()); } while
											  (!incomplete.isEmpty()) { try { Thread.sleep(sleepTime); } catch
											  (InterruptedException e) {} System.out.println("Awaiting results..." +
											  incomplete.size()); sleepTime = 10000L; BatchInfo[] statusList =
											  bulkconn.getBatchInfoList(job.getId()).getBatchInfo(); for (BatchInfo b :
											  statusList) { if (b.getState() == BatchStateEnum.Completed || b.getState() ==
											  BatchStateEnum.Failed) { if (incomplete.remove(b.getId())) {
											  System.out.println("BATCH STATUS:\n" + b); 
											  @SuppressWarnings("rawtypes")
												ArrayList blist = new ArrayList();
												@SuppressWarnings("rawtypes")
												Map map = new HashMap();
												map.put("jobId", b.getJobId());
												map.put("recordsFailed", b.getNumberRecordsFailed());
												map.put("recordsProcessed", b.getNumberRecordsProcessed());
												map.put("state", b.getState());
												map.put("statemsg", b.getStateMessage());
												map.put("apiProcessingTime", b.getApiActiveProcessingTime());
												map.put("apexProcessingTime", b.getApexProcessingTime());
												blist.add(map);
												request.setAttribute("blist", blist);
											  } } } }
											 
									        for (BatchInfo b : batchInfoList) {
									            CSVReader rdr =
									              new CSVReader(bulkconn.getBatchResultStream(job.getId(), b.getId()));
									            List<String> resultHeader = rdr.nextRecord();
									            int resultCols = resultHeader.size();

									            List<String> row;
									            while ((row = rdr.nextRecord()) != null) {
									                Map<String, String> resultInfo = new HashMap<String, String>();
									                for (int i = 0; i < resultCols; i++) {
									                    resultInfo.put(resultHeader.get(i), row.get(i));
									                }
									                
									                boolean success = Boolean.valueOf(resultInfo.get("Success"));
									                boolean inserted = Boolean.valueOf(resultInfo.get("Insert"));
									                //boolean updated = Boolean.valueOf(resultInfo.get("Update"));
									                
									                String ids = resultInfo.get("Id");
									                String error = resultInfo.get("Error");
									                if (success && inserted) {
									                    System.out.println("Deleated row with id " + ids);
									                }
									                
									                else if (!success) {
									                    System.out.println("Failed with error: " + error);
									                }
									            }
									        }
									        
									        	
								}
								        
								      
								else
								{
									System.out.println("File is empty");
								}
								
								
								
							
								
							
				}
			catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return "standard_object_bulk_upload_delete_details";
	}

	// ------------------------Standard Object Delete
	// End----------------------------//

	// ------------------------Custom
	// Object----------------------------------------//

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_dataloader", method = RequestMethod.GET)
	public String customObjectDataloader(HttpServletRequest request, HttpSession session) throws ConnectionException {
		String name = null;
		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());

				// Make the describeGlobal() call
				DescribeGlobalResult describeGlobalResult = connection.describeGlobal();

				// Get the sObjects from the describe global result
				DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();
				ArrayList stdobjlist = new ArrayList();
				;
				for (int i = 0; i < sobjectResults.length; i++) {

					// session.setAttribute("sobjlist",sobjectResults[i].getName().toString());

					// for (DescribeGlobalSObjectResult stdobj : sobjectResults) {

					// }
					if (sobjectResults[i].isCustom()) {
						@SuppressWarnings("rawtypes")
						Map map = new HashMap();
						map.put("Label", sobjectResults[i].getLabel().toString());
						map.put("Name", sobjectResults[i].getName().toString());
						stdobjlist.add(map);
						session.setAttribute("namesobj", sobjectResults[i].getName().toString());

						request.setAttribute("slist", stdobjlist);
					}
				}

			}
		}

		return "custom_object_dataloader";

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_details/{name}", method = RequestMethod.GET)
	public String customObjectDetails(@PathVariable("name") String name, HttpServletRequest request,
			HttpSession session) throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

				DescribeSObjectResult[] describeSObjectResults;
				describeSObjectResults = connection.describeSObjects(new String[] { name });
				System.out.println("name->" + name);
				request.setAttribute("name", name);
				session.setAttribute("namenew", name);
				// Iterate through the list of describe sObject results
				for (int i = 0; i < describeSObjectResults.length; i++) {
					for (DescribeSObjectResult desObj : describeSObjectResults) {
						// desObj = describeSObjectResults[i];
						// Get the name of the sObject
						String objectName = desObj.getName();
						/*
						 * System.out.println("Object Name: " + objectName);
						 * 
						 * System.out.println("Field Label"+","+"Field Name"+","+"Data Type");
						 */

						com.sforce.soap.tooling.QueryResult qResult1 = null;
						String soqlQuery = "Select Id,Active,Description,ValidationName,EntityDefinition.DeveloperName,ErrorDisplayField, ErrorMessage From ValidationRule where EntityDefinition.QualifiedApiName='"
								+ name + "'";
						System.out.println("name->->" + name);
						// request.setAttribute("Sobjname", name.toUpperCase());
						qResult1 = toolingConnection.query(soqlQuery);
						boolean done = false;
						if (qResult1.getSize() > 0) {

							com.sforce.soap.tooling.sobject.SObject[] records = qResult1.getRecords();

							// ArrayList arr=new ArrayList();
							// ArrayList arr1=new ArrayList();
							@SuppressWarnings("rawtypes")
							ArrayList vrule = new ArrayList();
							for (int j = 0; j < qResult1.getSize(); j++) {

								ValidationRule vr = new ValidationRule();
								vr = (ValidationRule) records[j];
								System.out.println("Name" + vr.getValidationName());
								System.out.println("Description::" + vr.getDescription());
								if (vr.getActive().equals(true)) {
									Map map = new HashMap();

									map.put("Name", vr.getValidationName());
									map.put("Active", vr.getActive());
									map.put("Desc", vr.getDescription());
									map.put("ObjName", name);
									vrule.add(map);
									request.setAttribute("vrulelist", vrule);
								}

							}

							System.out.println(
									"Logged-in user can see a total of " + qResult1.getSize() + " validation rule.");
							// System.out.println(arr1.toString());
							request.setAttribute("vsize", qResult1.getSize());

						}

						// -----------Workflow rule------------------------------------//

						com.sforce.soap.tooling.QueryResult qResult2 = null;
						String soqlQuery1 = "Select Name,TableEnumOrId From WorkflowRule where TableEnumOrId='" + name
								+ "'";
						System.out.println("name->->" + name);
						// request.setAttribute("Sobjname", name.toUpperCase());
						qResult2 = toolingConnection.query(soqlQuery1);
						boolean done1 = false;
						if (qResult2.getSize() > 0) {

							com.sforce.soap.tooling.sobject.SObject[] records = qResult2.getRecords();

							// ArrayList arr=new ArrayList();
							// ArrayList arr1=new ArrayList();
							@SuppressWarnings("rawtypes")
							ArrayList wrule = new ArrayList();
							for (int j = 0; j < qResult2.getSize(); j++) {

								WorkflowRule vr = new WorkflowRule();
								vr = (WorkflowRule) records[j];
								System.out.println("Name" + vr.getName());
								System.out.println("Description::" + vr.getMetadata());

								Map map = new HashMap();

								map.put("Name", vr.getName());
								map.put("Enum", vr.getTableEnumOrId());

								map.put("ObjName", name);
								wrule.add(map);
								request.setAttribute("wrulelist", wrule);

							}

							System.out.println(
									"Logged-in user can see a total of " + qResult2.getSize() + " workflow rule.");
							request.setAttribute("wsize", qResult2.getSize());
							// System.out.println(arr1.toString());

						}

						com.sforce.soap.tooling.QueryResult qResult3 = null;
						String soqlQuery2 = "SELECT Name FROM ApexTrigger WHERE TableEnumOrId='" + name + "'";
						System.out.println("name->->" + name);
						// request.setAttribute("Sobjname", name.toUpperCase());
						qResult3 = toolingConnection.query(soqlQuery2);
						boolean done2 = false;
						if (qResult3.getSize() > 0) {

							com.sforce.soap.tooling.sobject.SObject[] records = qResult3.getRecords();

							// ArrayList arr=new ArrayList();
							// ArrayList arr1=new ArrayList();
							@SuppressWarnings("rawtypes")
							ArrayList tr = new ArrayList();
							for (int j = 0; j < qResult3.getSize(); j++) {

								ApexTrigger vr = new ApexTrigger();
								vr = (ApexTrigger) records[j];
								System.out.println("Name" + vr.getName());
								System.out.println("Status::" + vr.getStatus());

								Map map = new HashMap();

								map.put("Name", vr.getName());
								map.put("Status", vr.getStatus());

								map.put("ObjName", name);
								tr.add(map);
								request.setAttribute("trlist", tr);

							}

							System.out.println("Logged-in user can see a total of " + qResult3.getSize() + " trigger.");
							request.setAttribute("trsize", qResult3.getSize());
							// System.out.println(arr1.toString());

						}

					}
				}

			}

		}

		return "custom_object_details";

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_bulk_upload_insert_display", method = RequestMethod.GET)
	public String customObjectDetailsBulkUploadDisplay(HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

			}
		}
		return "custom_object_bulk_upload_insert_display";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_bulk_upload_insert_details", method = RequestMethod.POST)
	public String customObjectDetailsBulkUpload(@RequestPart("file") MultipartFile file,
			@RequestParam("batchsize") int batchsize, HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

				try {
					String soapEndpoint = config.getServiceEndpoint();
					String apiVersion = "46.0";
					String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/"
							+ apiVersion;
					config.setRestEndpoint(restEndpoint);
					// This should only be false when doing debugging.
					config.setCompression(true);
					// Set this to true to see HTTP requests and responses on stdout
					config.setTraceMessage(false);
					BulkConnection bulkconn = new BulkConnection(config);
					JobInfo job = new JobInfo();
					System.out.println("NAME_NEW:" + session.getAttribute("namenew"));
					job.setObject((String) session.getAttribute("namenew"));
					job.setOperation(OperationEnum.insert);
					job.setContentType(ContentType.CSV);
					job = bulkconn.createJob(job);
					System.out.println(job);

					List<BatchInfo> batchInfoList = new ArrayList<BatchInfo>();

					if (file != null && !((MultipartFile) file).isEmpty()) {

						byte[] bytes = ((MultipartFile) file).getBytes();
						// String
						// filePath="C:\\Users\\sagnik.biswas\\eclipse-workspace-2019\\SalesforceCustomX\\src\\main\\webapp\\assets\\images";
						String fileName = String.valueOf(session.getAttribute("namenew")) + ".csv";
						File serverFile = new File(fileName);
						FileOutputStream fos = new FileOutputStream(serverFile);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						bos.write(bytes);
						int maxBytesPerBatch = 10000000; // 10 million bytes per batch
						int maxRowsPerBatch = batchsize; // 10 thousand rows per batch
						System.out.println("BATCHSIZE:" + maxRowsPerBatch);
						int currentBytes = 0;
						int currentLines = 0;

						if (currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) {
							// createBatch(tmpOut, tmpFile, batchInfos, connection, jobInfo);
							FileInputStream tmpInputStream = new FileInputStream(serverFile);
							BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
							System.out.println(batchInfo);
							batchInfoList.add(batchInfo);
							currentBytes = 0;
							currentLines = 0;
						}
						if (currentBytes == 0) {
							// tmpOut = fos;
							fos.write(bytes);
							currentBytes = bytes.length;
							currentLines = 1;
						}
						fos.write(bytes);
						currentBytes += bytes.length;
						currentLines++;

						// Finished processing all rows
						// Create a final batch for any remaining data
						if (currentLines > 1) {
							FileInputStream tmpInputStream = new FileInputStream(serverFile);
							BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
							System.out.println(batchInfo);

							batchInfoList.add(batchInfo);
							@SuppressWarnings("rawtypes")
							ArrayList blist = new ArrayList();
							@SuppressWarnings("rawtypes")
							Map map = new HashMap();
							map.put("jobId", batchInfo.getJobId());
							map.put("recordsFailed", batchInfo.getNumberRecordsFailed());
							map.put("recordsProcessed", batchInfo.getNumberRecordsProcessed());
							map.put("state", batchInfo.getState());
							map.put("statemsg", batchInfo.getStateMessage());
							map.put("apiProcessingTime", batchInfo.getApiActiveProcessingTime());
							map.put("apexProcessingTime", batchInfo.getApexProcessingTime());
							blist.add(map);

							System.out.println("Failed" + batchInfo.getNumberRecordsFailed());
							request.setAttribute("blist", blist);

						}
						long sleepTime = 0L;
						Set<String> incomplete = new HashSet<String>();
						for (BatchInfo bi : batchInfoList) {
							incomplete.add(bi.getId());
						}
						while (!incomplete.isEmpty()) {
							try {
								Thread.sleep(sleepTime);
							} catch (InterruptedException e) {
							}
							System.out.println("Awaiting results..." + incomplete.size());
							sleepTime = 10000L;
							BatchInfo[] statusList = bulkconn.getBatchInfoList(job.getId()).getBatchInfo();
							for (BatchInfo b : statusList) {
								if (b.getState() == BatchStateEnum.Completed || b.getState() == BatchStateEnum.Failed) {
									if (incomplete.remove(b.getId())) {
										System.out.println("BATCH STATUS:\n" + b);
									}
								}
							}
						}

						for (BatchInfo b : batchInfoList) {
							CSVReader rdr = new CSVReader(bulkconn.getBatchResultStream(job.getId(), b.getId()));
							List<String> resultHeader = rdr.nextRecord();
							int resultCols = resultHeader.size();

							List<String> row;
							while ((row = rdr.nextRecord()) != null) {
								Map<String, String> resultInfo = new HashMap<String, String>();
								for (int a = 0; a < resultCols; a++) {
									resultInfo.put(resultHeader.get(a), row.get(a));
								}
								boolean success = Boolean.valueOf(resultInfo.get("Success"));
								boolean created = Boolean.valueOf(resultInfo.get("Created"));
								String id = resultInfo.get("Id");
								String error = resultInfo.get("Error");
								if (success && created) {
									System.out.println("Created row with id " + id);
								} else if (!success) {
									System.out.println("Failed with error: " + error);
								}
							}
						}

					} else {
						System.out.println("File is empty");
					}

				}

				catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return "custom_object_bulk_upload_insert_details";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_bulk_upload_update_display", method = RequestMethod.GET)
	public String customObjectDisplayBulkUploadDisplayUpdate(HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

			}
		}
		return "custom_object_bulk_upload_update_display";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_bulk_upload_update_details", method = RequestMethod.POST)
	public String customObjectDetailsBulkUploadUpdate(@RequestPart("file") MultipartFile file,
			@RequestParam("batchsize") int batchsize, HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/24.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				
				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());


				
				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

				try {
					String soapEndpoint = config.getServiceEndpoint();
					String apiVersion = "46.0";
					String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/"
							+ apiVersion;
					config.setRestEndpoint(restEndpoint);
					// This should only be false when doing debugging.
					config.setCompression(true);
					// Set this to true to see HTTP requests and responses on stdout
					config.setTraceMessage(false);
					BulkConnection bulkconn = new BulkConnection(config);
					JobInfo job = new JobInfo();
					System.out.println("NAME_NEW:" + session.getAttribute("namenew"));
					
					
					job.setObject((String) session.getAttribute("namenew"));
					//job.setExternalIdFieldName("EXTERNALID__C");
					job.setOperation(OperationEnum.update);
					job.setConcurrencyMode(ConcurrencyMode.Parallel);
					job.setContentType(ContentType.CSV);
					job = bulkconn.createJob(job);
				
					    assert job.getId() != null;

					List<BatchInfo> batchInfoList = new ArrayList<BatchInfo>();

					if (file != null && !((MultipartFile) file).isEmpty()) {

						byte[] bytes = ((MultipartFile) file).getBytes();
						// String
						// filePath="C:\\Users\\sagnik.biswas\\eclipse-workspace-2019\\SalesforceCustomX\\src\\main\\webapp\\assets\\images";
						String fileName = String.valueOf(session.getAttribute("namenew")) + ".csv";
						File serverFile = new File(fileName);
						FileOutputStream fos = new FileOutputStream(serverFile);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						bos.write(bytes);
						int maxBytesPerBatch = 10000000; // 10 million bytes per batch
						int maxRowsPerBatch = batchsize; // 10 thousand rows per batch
						System.out.println("BATCHSIZE:" + maxRowsPerBatch);
						int currentBytes = 0;
						int currentLines = 0;

						if (currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) {
							// createBatch(tmpOut, tmpFile, batchInfos, connection, jobInfo);
							FileInputStream tmpInputStream = new FileInputStream(serverFile);
							BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
							System.out.println(batchInfo);
							batchInfoList.add(batchInfo);
							currentBytes = 0;
							currentLines = 0;
						}
						if (currentBytes == 0) {
							// tmpOut = fos;
							fos.write(bytes);
							currentBytes = bytes.length;
							currentLines = 1;
						}
						fos.write(bytes);
						currentBytes += bytes.length;
						currentLines++;

						// Finished processing all rows
						// Create a final batch for any remaining data
						if (currentLines > 1) {
							FileInputStream tmpInputStream = new FileInputStream(serverFile);
							BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
							System.out.println(batchInfo);

							batchInfoList.add(batchInfo);
							

							//System.out.println("Failed" + batchInfo.getNumberRecordsFailed());
							

						 

					}

						/*Fillo fillo=new Fillo();
						Connection con=fillo.getConnection(serverFile.toString());
						String query="Select Id,Name,Stagename,Type from Opportunity ";
						Recordset recordset=con.executeQuery(query);
						 
						while(recordset.next()){
						System.out.println(recordset.getField("Id"));*/
						//}	
					    /*String query = "Select Id,Name,Stagename,Type from Opportunity";
					    
					    long start = System.currentTimeMillis();
					    
					    BatchInfo info = null;
					    ByteArrayInputStream bout = 
					        new ByteArrayInputStream(query.getBytes());
					    info = bulkconn.createBatchFromStream(job, bout);
					    
					    String[] queryResults = null;
					    
					    for(int i=0; i<10000; i++) {
					      Thread.sleep(30000); //30 sec
					      info = bulkconn.getBatchInfo(job.getId(), 
					          info.getId());
					      
					      if (info.getState() == BatchStateEnum.Completed) {
					        QueryResultList list = 
					        		bulkconn.getQueryResultList(job.getId(), 
					                info.getId());
					        queryResults = list.getResult();
					        
					        System.out.println("-------------- Completed ----------" 
						            + info + queryResults);
					        break;
					      } else if (info.getState() == BatchStateEnum.Failed) {
					        System.out.println("-------------- failed ----------" 
					            + info);
					        break;
					      } else {
					        System.out.println("-------------- waiting ----------" 
					            + info);
					      }
					    }
					    
					    if (queryResults != null) {
					        for (String resultId : queryResults) {
					          bulkconn.getQueryResultStream(job.getId(), 
					              info.getId(), resultId);*/
					          
					            
								//job = bulkconn.updateJob(job);
								
								
								  long sleepTime = 0L; Set<String> incomplete = new HashSet<String>(); for
								  (BatchInfo bi : batchInfoList) { incomplete.add(bi.getId()); } while
								  (!incomplete.isEmpty()) { try { Thread.sleep(sleepTime); } catch
								  (InterruptedException e) {} System.out.println("Awaiting results..." +
								  incomplete.size()); sleepTime = 10000L; BatchInfo[] statusList =
								  bulkconn.getBatchInfoList(job.getId()).getBatchInfo(); for (BatchInfo b :
								  statusList) { if (b.getState() == BatchStateEnum.Completed || b.getState() ==
								  BatchStateEnum.Failed) { if (incomplete.remove(b.getId())) {
								  System.out.println("BATCH STATUS:\n" + b); 
								  @SuppressWarnings("rawtypes")
									ArrayList blist = new ArrayList();
									@SuppressWarnings("rawtypes")
									Map map = new HashMap();
									map.put("jobId", b.getJobId());
									map.put("recordsFailed", b.getNumberRecordsFailed());
									map.put("recordsProcessed", b.getNumberRecordsProcessed());
									map.put("state", b.getState());
									map.put("statemsg", b.getStateMessage());
									map.put("apiProcessingTime", b.getApiActiveProcessingTime());
									map.put("apexProcessingTime", b.getApexProcessingTime());
									blist.add(map);
									request.setAttribute("blist", blist);
								  } } } }
								 
						        for (BatchInfo b : batchInfoList) {
						            CSVReader rdr =
						              new CSVReader(bulkconn.getBatchResultStream(job.getId(), b.getId()));
						            List<String> resultHeader = rdr.nextRecord();
						            int resultCols = resultHeader.size();

						            List<String> row;
						            while ((row = rdr.nextRecord()) != null) {
						                Map<String, String> resultInfo = new HashMap<String, String>();
						                for (int i = 0; i < resultCols; i++) {
						                    resultInfo.put(resultHeader.get(i), row.get(i));
						                }
						                
						                boolean success = Boolean.valueOf(resultInfo.get("Success"));
						                boolean updated = Boolean.valueOf(resultInfo.get("Update"));
						               
						                String id = resultInfo.get("Id");
						                String error = resultInfo.get("Error");
						                if (success && updated) {
						                    System.out.println("Updated row with id " + id);
						                } else if (!success) {
						                    System.out.println("Failed with error: " + error);
						                }
						            }
						        }
					        
					      } 
					     
						 
					        
								
							
					      
					    
					 
						
						/*
						 * long sleepTime = 0L; Set<String> incomplete = new HashSet<String>(); for
						 * (BatchInfo bi : batchInfoList) { incomplete.add(bi.getId()); } while
						 * (!incomplete.isEmpty()) { try { Thread.sleep(sleepTime); } catch
						 * (InterruptedException e) { } System.out.println("Awaiting results..." +
						 * incomplete.size()); sleepTime = 10000L; BatchInfo[] statusList =
						 * bulkconn.getBatchInfoList(job.getId()).getBatchInfo(); for (BatchInfo b :
						 * statusList) { if (b.getState() == BatchStateEnum.Completed || b.getState() ==
						 * BatchStateEnum.Failed) { if (incomplete.remove(b.getId())) {
						 * System.out.println("BATCH STATUS:\n" + b);
						 * 
						 * @SuppressWarnings("rawtypes") ArrayList blist = new ArrayList();
						 * 
						 * @SuppressWarnings("rawtypes") Map map = new HashMap(); map.put("jobId",
						 * b.getJobId()); map.put("recordsFailed", b.getNumberRecordsFailed());
						 * map.put("recordsProcessed", b.getNumberRecordsProcessed()); map.put("state",
						 * b.getState()); map.put("statemsg", b.getStateMessage());
						 * map.put("apiProcessingTime", b.getApiActiveProcessingTime());
						 * map.put("apexProcessingTime", b.getApexProcessingTime()); blist.add(map);
						 * request.setAttribute("blist", blist); } } } }
						 */

							

				
					else {
						System.out.println("File is empty");
					}	
					}
			catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return "custom_object_bulk_upload_update_details";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_bulk_upload_delete_display", method = RequestMethod.GET)
	public String customObjectDisplayBulkUploadDisplayUpsert(HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

			}
		}
		return "custom_object_bulk_upload_delete_display";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/custom_object_bulk_upload_delete_details", method = RequestMethod.POST)
	public String customObjectDetailsBulkUploadUpsert(@RequestPart("file") MultipartFile file,
			@RequestParam("batchsize") int batchsize, HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/24.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				
				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());


				
				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);

				try {
					String soapEndpoint = config.getServiceEndpoint();
					String apiVersion = "46.0";
					String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/"
							+ apiVersion;
					config.setRestEndpoint(restEndpoint);
					// This should only be false when doing debugging.
					config.setCompression(true);
					// Set this to true to see HTTP requests and responses on stdout
					config.setTraceMessage(false);
					BulkConnection bulkconn = new BulkConnection(config);
					JobInfo job = new JobInfo();
					System.out.println("NAME_NEW:" + session.getAttribute("namenew"));
					job.setObject((String) session.getAttribute("namenew"));
					job.setOperation(OperationEnum.delete);
					
					//System.out.println("UPSERT..");
					//job.setExternalIdFieldName("ExternalId__c"); 
					
					
				//else
				//{
					//job.setOperation(OperationEnum.update);	
					//System.out.println("UPDATE..");
				//}
				
				//job.setConcurrencyMode(ConcurrencyMode.Parallel);
				   job.setContentType(ContentType.CSV);
					
				
							job = bulkconn.createJob(job);
							
								    assert job.getId() != null;
                               
                                	
                               
								List<BatchInfo> batchInfoList = new ArrayList<BatchInfo>();

								if (file != null && !((MultipartFile) file).isEmpty()) {

									byte[] bytes = ((MultipartFile) file).getBytes();
									// String
									// filePath="C:\\Users\\sagnik.biswas\\eclipse-workspace-2019\\SalesforceCustomX\\src\\main\\webapp\\assets\\images";
									String fileName = String.valueOf(session.getAttribute("namenew")) + ".csv";
									File serverFile = new File(fileName);
									FileOutputStream fos = new FileOutputStream(serverFile);
									BufferedOutputStream bos = new BufferedOutputStream(fos);
									bos.write(bytes);
									int maxBytesPerBatch = 10000000; // 10 million bytes per batch
									int maxRowsPerBatch = batchsize; // 10 thousand rows per batch
									System.out.println("BATCHSIZE:" + maxRowsPerBatch);
									int currentBytes = 0;
									int currentLines = 0;

									if (currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) {
										// createBatch(tmpOut, tmpFile, batchInfos, connection, jobInfo);
										FileInputStream tmpInputStream = new FileInputStream(serverFile);
										BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
										System.out.println(batchInfo);
										batchInfoList.add(batchInfo);
										currentBytes = 0;
										currentLines = 0;
									}
									if (currentBytes == 0) {
										// tmpOut = fos;
										fos.write(bytes);
										currentBytes = bytes.length;
										currentLines = 1;
									}
									fos.write(bytes);
									currentBytes += bytes.length;
									currentLines++;

									// Finished processing all rows
									// Create a final batch for any remaining data
									if (currentLines > 1) {
										FileInputStream tmpInputStream = new FileInputStream(serverFile);
										BatchInfo batchInfo = bulkconn.createBatchFromStream(job, tmpInputStream);
										System.out.println(batchInfo);

										batchInfoList.add(batchInfo);
										

										//System.out.println("Failed" + batchInfo.getNumberRecordsFailed());
										

									 

								}

									/*Fillo fillo=new Fillo();
									Connection con=fillo.getConnection(serverFile.toString());
									String query="Select Id,Name,Stagename,Type from Opportunity ";
									Recordset recordset=con.executeQuery(query);
									 
									while(recordset.next()){
									System.out.println(recordset.getField("Id"));*/
									//}	
								    /*String query = "Select Id,Name,Stagename,Type from Opportunity";
								    
								    long start = System.currentTimeMillis();
								    
								    BatchInfo info = null;
								    ByteArrayInputStream bout = 
								        new ByteArrayInputStream(query.getBytes());
								    info = bulkconn.createBatchFromStream(job, bout);
								    
								    String[] queryResults = null;
								    
								    for(int i=0; i<10000; i++) {
								      Thread.sleep(30000); //30 sec
								      info = bulkconn.getBatchInfo(job.getId(), 
								          info.getId());
								      
								      if (info.getState() == BatchStateEnum.Completed) {
								        QueryResultList list = 
								        		bulkconn.getQueryResultList(job.getId(), 
								                info.getId());
								        queryResults = list.getResult();
								        
								        System.out.println("-------------- Completed ----------" 
									            + info + queryResults);
								        break;
								      } else if (info.getState() == BatchStateEnum.Failed) {
								        System.out.println("-------------- failed ----------" 
								            + info);
								        break;
								      } else {
								        System.out.println("-------------- waiting ----------" 
								            + info);
								      }
								    }
								    
								    if (queryResults != null) {
								        for (String resultId : queryResults) {
								          bulkconn.getQueryResultStream(job.getId(), 
								              info.getId(), resultId);*/
								          
								            
											//job = bulkconn.updateJob(job);
											
											
											  long sleepTime = 0L; Set<String> incomplete = new HashSet<String>(); for
											  (BatchInfo bi : batchInfoList) { incomplete.add(bi.getId()); } while
											  (!incomplete.isEmpty()) { try { Thread.sleep(sleepTime); } catch
											  (InterruptedException e) {} System.out.println("Awaiting results..." +
											  incomplete.size()); sleepTime = 10000L; BatchInfo[] statusList =
											  bulkconn.getBatchInfoList(job.getId()).getBatchInfo(); for (BatchInfo b :
											  statusList) { if (b.getState() == BatchStateEnum.Completed || b.getState() ==
											  BatchStateEnum.Failed) { if (incomplete.remove(b.getId())) {
											  System.out.println("BATCH STATUS:\n" + b); 
											  @SuppressWarnings("rawtypes")
												ArrayList blist = new ArrayList();
												@SuppressWarnings("rawtypes")
												Map map = new HashMap();
												map.put("jobId", b.getJobId());
												map.put("recordsFailed", b.getNumberRecordsFailed());
												map.put("recordsProcessed", b.getNumberRecordsProcessed());
												map.put("state", b.getState());
												map.put("statemsg", b.getStateMessage());
												map.put("apiProcessingTime", b.getApiActiveProcessingTime());
												map.put("apexProcessingTime", b.getApexProcessingTime());
												blist.add(map);
												request.setAttribute("blist", blist);
											  } } } }
											 
									        for (BatchInfo b : batchInfoList) {
									            CSVReader rdr =
									              new CSVReader(bulkconn.getBatchResultStream(job.getId(), b.getId()));
									            List<String> resultHeader = rdr.nextRecord();
									            int resultCols = resultHeader.size();

									            List<String> row;
									            while ((row = rdr.nextRecord()) != null) {
									                Map<String, String> resultInfo = new HashMap<String, String>();
									                for (int i = 0; i < resultCols; i++) {
									                    resultInfo.put(resultHeader.get(i), row.get(i));
									                }
									                
									                boolean success = Boolean.valueOf(resultInfo.get("Success"));
									                boolean inserted = Boolean.valueOf(resultInfo.get("Insert"));
									                //boolean updated = Boolean.valueOf(resultInfo.get("Update"));
									                
									                String ids = resultInfo.get("Id");
									                String error = resultInfo.get("Error");
									                if (success && inserted) {
									                    System.out.println("Deleated row with id " + ids);
									                }
									                
									                else if (!success) {
									                    System.out.println("Failed with error: " + error);
									                }
									            }
									        }
									        
									        	
								}
								        
								      
								else
								{
									System.out.println("File is empty");
								}
								
								
								
							
								
							
				}
			catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return "custom_object_bulk_upload_delete_details";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/code_coverage", method = RequestMethod.GET)
	public String codeCoverage(HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		String username = null;
		String password = null;
		String loginurl = null;

		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			loginurl = (String) session.getAttribute("loginurl");

			System.out.println("Session Created");
			System.out.println("USER:" + username);

			if (username != null || password != null || loginurl != null) {
				// Do stuff here
				System.out.println("USER:" + username);
				System.out.println("PASS:" + password);
				System.out.println("LOGINURL:" + loginurl);

				/*
				 * List<String>listStdObj=stdObjDao.retrieveStandardObj();
				 * m.addAttribute("stdObjList",listStdObj);
				 */

				String authEndPoint = "https://" + loginurl + "/services/Soap/u/37.0/";

				ConnectorConfig config = new ConnectorConfig();
				config.setAuthEndpoint(authEndPoint);

				config.setUsername(username);
				config.setPassword(password);

				PartnerConnection connection = new PartnerConnection(config);
				GetUserInfoResult userInfo = connection.getUserInfo();
				request.setAttribute("userfullname", userInfo.getUserFullName());
				LoginResult lr = connection.login(config.getUsername(), config.getPassword());

				ConnectorConfig toolingConfig = new ConnectorConfig();
				toolingConfig.setManualLogin(false);
				toolingConfig.setSessionId(lr.getSessionId());

				toolingConfig.setServiceEndpoint(lr.getServerUrl().replace("u", "T"));
				ToolingConnection toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);


	            CodeCoverageResult[] codeCoverageResults;
				RunTestsRequest rtr=new RunTestsRequest(); 
				rtr.setAllTests(true);
				RunTestsResult res=null;
     			
    			 try {
					res=toolingConnection.runTests(rtr);
				} catch (ConnectionException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
    			 
           	  codeCoverageResults = res.getCodeCoverage();
	     	    
	     	    try
	     	    {
	     	    	ArrayList blist = new ArrayList();
	     	    	for (CodeCoverageResult ccr : res.getCodeCoverage())
	  		        {
	
		  		       String lines= (ccr.getNumLocations()-ccr.getNumLocationsNotCovered())+"/"+ccr.getNumLocations();
                       Double percent=0.0;
                       if((ccr.getNumLocations()-ccr.getNumLocationsNotCovered())==0)
		  		       {
		  		           percent=0.0;
		  		       }
		  		       else
		  		       {
		  		           percent=(double) (((ccr.getNumLocations()-ccr.getNumLocationsNotCovered())*100)/ccr.getNumLocations());
		  		       }
                       
                        
						
						@SuppressWarnings("rawtypes")
						Map map = new HashMap();
						map.put("name", ccr.getName());
						map.put("type", ccr.getType());
						map.put("lines", lines);
						map.put("percent", percent);
						blist.add(map);
						request.setAttribute("blist", blist);
	     	        }
	     	    }	
	     	    catch(Exception e)
	     	    {
	     	    	e.printStackTrace();
	     	    }
			}
		}
		return "code_coverage";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpServletRequest request, HttpSession session)
			throws ConnectionException {

		session = request.getSession(false);
		// False because we do not want it to create a new session if it does not exist.
		session.invalidate();
		
			
		
		return "index";
	}
}
