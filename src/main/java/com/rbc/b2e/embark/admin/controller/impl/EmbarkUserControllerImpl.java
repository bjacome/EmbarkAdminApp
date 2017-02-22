package com.rbc.b2e.embark.admin.controller.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.internal.LinkedTreeMap;
import com.rbc.b2e.embark.admin.connection.CloudantClientMgr;
import com.rbc.b2e.embark.admin.connection.CloudantClientMgrProvider;
import com.rbc.b2e.embark.admin.controller.EmbarkUserController;
import com.rbc.b2e.embark.admin.exception.OperationErrorException;
import com.rbc.b2e.embark.admin.model.AdminUser;
import com.rbc.b2e.embark.admin.model.Cohort;
import com.rbc.b2e.embark.admin.model.service.AdminServiceError;
import com.rbc.b2e.embark.admin.model.service.EmbarkService;
import com.rbc.b2e.embark.admin.model.service.nosql.CloudantEmbarkUserService;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.security.EmbarkPasswordEncoder;
import com.rbc.b2e.embark.admin.util.SystemConstants;
import com.rbc.b2e.embark.admin.util.SystemMessageHandler;

@Controller
@RequestMapping(EmbarkUserController.EMBARK_USER_REST_URI)
public class EmbarkUserControllerImpl extends AbstractController implements EmbarkUserController {

	private static final Logger logger = LoggerFactory.getLogger(EmbarkUserControllerImpl.class);

	private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	@RequestMapping(value = EmbarkUserController.GET_USERS_REST_URI, method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response getUsers(@RequestBody Request<Map<?,?>> request, HttpServletRequest servletRequest)
			throws Exception {
		CloudantClientMgrProvider cloudantClientMgrProvider = getContext().getBean(CloudantClientMgrProvider.class);
		CloudantClientMgr cloudantClientMgr;
		logger.debug("EmbarkUserControllerImpl - getUsers");
		Response finalResponse = new Response();
		Map<?,?> data = request.getData();
		if ( data != null) {
			long roleId;
			long environmentId;
			long cohortId;
			int page;
			int pageSize;
			try {
				roleId =  Long.parseLong(data.get("roleId").toString());
				cohortId = Long.parseLong(data.get("cohortId").toString());
				pageSize = Integer.parseInt(data.get("pageSize").toString());
				page = Integer.parseInt(data.get("page").toString());
				environmentId = Long.parseLong(data.get("environmentId").toString());
				cloudantClientMgr = cloudantClientMgrProvider.get(environmentId);
			} catch (NullPointerException e) {
				throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
			}
			Cohort cohort = this.getAdminService().retrieveCohort(roleId, cohortId);
			EmbarkService embarkService = new CloudantEmbarkUserService(cloudantClientMgr);
			finalResponse.setData(embarkService.getPage(cohort.getRoleName(), cohort.getName(), pageSize, page));
			return finalResponse;
		} else {
			throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
		}
	}

	@Override
	@RequestMapping(value = EmbarkUserController.GET_USER_REST_URI, method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response getUser(@RequestBody Request<Map<?,?>> request, HttpServletRequest servletRequest) throws Exception {
		CloudantClientMgrProvider cloudantClientMgrProvider = getContext().getBean(CloudantClientMgrProvider.class);
		CloudantClientMgr cloudantClientMgr;


		Response finalResponse = new Response();

		Map<?,?> data = request.getData();
		if (data != null) {
			String userId;
			try {
				userId = data.get("_id").toString();
				long environmentId = Long.parseLong(data.get("environmentId").toString());
				cloudantClientMgr = cloudantClientMgrProvider.get(environmentId);
				logger.debug("Get user:" + userId);
			} catch (NullPointerException e) {
				throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
			}
			EmbarkService embarkService = new CloudantEmbarkUserService(cloudantClientMgr);
			finalResponse.setData(embarkService.getUser(userId));
			return finalResponse;
		} else {
			throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@RequestMapping(value = EmbarkUserController.SAVE_USER_REST_URI, method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response updateUser(@RequestBody Request<Map<?,?>> request,
			HttpServletRequest servletRequest) throws Exception {
		EmbarkPasswordEncoder encoder = 
				getContext().getBean(EmbarkPasswordEncoder.class);

		CloudantClientMgrProvider cloudantClientMgrProvider = getContext().getBean(CloudantClientMgrProvider.class);
		CloudantClientMgr cloudantClientMgr;

		Response finalResponse = new Response();

		Map<?,?> data = request.getData();
		if (data != null) {
			Map user = (Map<?, ?>) data.get("user");
			if (user == null) {
				logger.error(SystemMessageHandler.getMessage(SystemMessageHandler.INVALID_REQUEST));
				throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
			}
			long environmentId = Long.parseLong(data.get("environmentId").toString());
			cloudantClientMgr = cloudantClientMgrProvider.get(environmentId);
			EmbarkService embarkService = new CloudantEmbarkUserService(cloudantClientMgr, encoder);
			AdminUser adminUser = (AdminUser)servletRequest.getSession().getAttribute(SystemConstants.LOGGED_USER);

			if (user.containsKey("_id")) {
				user.put(SystemConstants.CREATED_BY, adminUser.getFullName());
				user.put(SystemConstants.CREATED_TIMESTAMP,format.format(new Date()));
			} else {
				user.put(SystemConstants.SCHEMA, SystemConstants.USER);
				user.put(SystemConstants.UPDATED_BY, adminUser.getFullName());
				user.put(SystemConstants.LAST_UPDATE,format.format(new Date()));
			}
			finalResponse.setData(embarkService.saveUser(user));
			return finalResponse;
		} else {
			throw new OperationErrorException(1000401);
		}
	}

	@Override
	@RequestMapping(value = EmbarkUserController.DELETE_USER_REST_URI, method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response removeUser(@RequestBody Request<Map<?,?>> request,
			HttpServletRequest servletRequest) throws Exception {
		CloudantClientMgrProvider cloudantClientMgrProvider = getContext().getBean(CloudantClientMgrProvider.class);
		CloudantClientMgr cloudantClientMgr;

		Response finalResponse = new Response();

		Map<?,?> data = request.getData();
		if (data != null) {
			String id = data.get(SystemConstants.ID).toString();
			String rev = data.get(SystemConstants.REV).toString();
			long environmentId = Long.parseLong(data.get("environmentId").toString());
			cloudantClientMgr = cloudantClientMgrProvider.get(environmentId);
			EmbarkService embarkService = new CloudantEmbarkUserService(cloudantClientMgr);
			embarkService.removeUser(id,rev);
			return finalResponse;
		} else {
			throw new OperationErrorException(1000401);
		}

	}

	@Override
	@RequestMapping(value = EmbarkUserController.IMPORT_USER_MULTIPART_URI, method = {RequestMethod.OPTIONS, RequestMethod.POST})
	public @ResponseBody Response importUsers(
			@RequestParam(value = "environmentId") long environmentId, 
			@RequestParam(value = "roleId") long roleId,
			@RequestParam(value = "cohortId") long cohortId, 
			@RequestParam(value = "file") MultipartFile file,
			HttpServletRequest servletRequest) throws Exception {
		logger.debug("importUsers");
		Response response = new Response();
		CloudantClientMgrProvider cloudantClientMgrProvider = getContext().getBean(CloudantClientMgrProvider.class);

		if (!file.isEmpty()) {

			logger.debug("importUsers file "+file.getOriginalFilename()+"is not empty");
			if (file.getOriginalFilename().toLowerCase().endsWith(SystemConstants.CSV_EXTENSION_TYPE)) {				
				final File folder = new File(SystemConstants.TMP_FOLDER);
				if (!folder.exists()) {
					folder.mkdir();
					logger.debug("Folder created - " + SystemConstants.TMP_FOLDER);
				}
				final String fileName = System.currentTimeMillis()+SystemConstants.TMP_FILE_NAME;
				final BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(new File(SystemConstants.TMP_FOLDER+fileName)));
				logger.debug("BufferedOutputStream is on for environmentId"+ environmentId+ " - " + fileName);
				FileCopyUtils.copy(file.getInputStream(), stream);
				stream.close();
				logger.debug("file closed");

				final EmbarkPasswordEncoder encoder = getContext().getBean(EmbarkPasswordEncoder.class);
				String importUserPassword = encoder.encodePassword(System.getenv("IMPORT_USER_PASSWORD"));

				logger.debug("encoded Password: "+importUserPassword);

				AdminUser adminUser = (AdminUser)servletRequest.getSession().getAttribute(SystemConstants.LOGGED_USER);

				Cohort cohort = this.getAdminService().retrieveCohort(roleId, cohortId);
				EmbarkService embarkService = new CloudantEmbarkUserService(cloudantClientMgrProvider.get(environmentId));

				LinkedTreeMap<String,Object> result = embarkService.importUsers(fileName,cohort, importUserPassword ,adminUser.getFullName());
				if ((Long)result.get("totalInvalidRecords") > 0) {
					response.setStatus(Response.Status.STATUS_FAIL);
				}
				response.setData(result);
			} else {
				throw new AdminServiceError(1000820,"Invalid file");
			}

		}
		return response;
	}

	@Override
	@RequestMapping(value = EmbarkUserController.EXPORT_USER_REST_URI, method = {RequestMethod.OPTIONS, RequestMethod.POST})
	public void exportUsers(@RequestBody Request<Map<?,?>> request,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws Exception {

		logger.debug("exportUsers in");
		
		CloudantClientMgrProvider cloudantClientMgrProvider = getContext().getBean(CloudantClientMgrProvider.class);
		CloudantClientMgr cloudantClientMgr;

		Map<?,?> data = request.getData();
		if ( data != null) {
			
			logger.debug("exportUsers has data");
			long roleId;
			long environmentId;
			long cohortId;
			try {
				roleId =  Long.parseLong(data.get("roleId").toString());
				cohortId = Long.parseLong(data.get("cohortId").toString());
				environmentId = Long.parseLong(data.get("environmentId").toString());
				cloudantClientMgr = cloudantClientMgrProvider.get(environmentId);
			} catch (NullPointerException e) {
				throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
			}
			Cohort cohort = this.getAdminService().retrieveCohort(roleId, cohortId);
			
			if (cohort == null) {
				throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
			}
			logger.debug("exportUsers has cohort");
			EmbarkService embarkService = new CloudantEmbarkUserService(cloudantClientMgr);

			String fileName = embarkService.exportView("count-valid","count-task-list", cohort);

			logger.debug("exportUsers file name: " +fileName );
			
			String line;
			BufferedReader reader = new BufferedReader(new FileReader(SystemConstants.TMP_FOLDER+fileName));

			servletResponse.setHeader("Content-Type",	"application/csv");
			servletResponse.setHeader("Content-Disposition",String.format("data:attachment; filename=\"%s\"",fileName));

			while ( (line = reader.readLine()) != null ) {
				servletResponse.getWriter().write(line);
				servletResponse.getWriter().write(SystemConstants.NEW_LINE_SEPARATOR);
			}
			reader.close();
			servletResponse.getWriter().flush();
			servletResponse.getWriter().close();

		} else {
			throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
		}
	}

}
