package com.rbc.b2e.embark.admin.model.service.nosql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lightcouch.CouchDbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudant.client.api.View;
import com.cloudant.client.api.model.FindByIndexOptions;
import com.cloudant.client.api.model.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.internal.LinkedTreeMap;
import com.rbc.b2e.embark.admin.connection.CloudantClientMgr;
import com.rbc.b2e.embark.admin.exception.CloudantGenericException;
import com.rbc.b2e.embark.admin.exception.GenericEmbarkAdminException;
import com.rbc.b2e.embark.admin.exception.OperationFailException;
import com.rbc.b2e.embark.admin.model.Cohort;
import com.rbc.b2e.embark.admin.model.service.EmbarkService;
import com.rbc.b2e.embark.admin.security.EmbarkPasswordEncoder;
import com.rbc.b2e.embark.admin.util.Jsonflattener;
import com.rbc.b2e.embark.admin.util.SystemConstants;
import com.rbc.b2e.embark.admin.util.SystemMessageHandler;

public class CloudantEmbarkUserService implements EmbarkService {


	private static final Logger logger = LoggerFactory.getLogger(CloudantEmbarkUserService.class);

	private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final static String[] BASE_USER_ATTRIBUTE = {"firstName","lastName","email","location","offerStatus"};

	final private CloudantClientMgr cloudantClientMgr;
	final EmbarkPasswordEncoder encoder;

	public CloudantEmbarkUserService(CloudantClientMgr cloudantClientMgr) {
		this.cloudantClientMgr = cloudantClientMgr;
		this.encoder = null;
	}

	public CloudantEmbarkUserService(CloudantClientMgr cloudantClientMgr, EmbarkPasswordEncoder encoder) {
		this.cloudantClientMgr = cloudantClientMgr;
		this.encoder = encoder;
	}

	@SuppressWarnings("rawtypes")
	public List<Map> getPage(String roleId, String cohortId, int pageSize, int page) throws Exception {
		List<Map> allDocs;
		try {
			allDocs =  this.cloudantClientMgr.getDB().findByIndex("\"selector\": {\"roleId\":\""+roleId+"\",\"cohortId\":\""+cohortId+"\"}"
					, Map.class, new FindByIndexOptions()
			.fields("_id").fields("_rev")
			.fields("lastName").fields("firstName").fields("email")
			.limit(pageSize)
			.skip(pageSize*(page)));
		} catch (CouchDbException e) {
			logger.error(e.getMessage(),e);
			throw new CloudantGenericException(e.getMessage());
		}	
		return allDocs;
	}

	@Override
	public Map<?, ?> getUser(String id) throws Exception  {
		try {
			Map<?, ?> user = this.cloudantClientMgr.getDB().find(Map.class,id);
			if (user.containsKey(SystemConstants.PASSWORD)) {
				user.remove(SystemConstants.PASSWORD);
			}
			if (user.containsKey(SystemConstants.PASSWORD_STATUS)) {
				user.remove(SystemConstants.PASSWORD_STATUS);
			}
			if (user.containsKey(SystemConstants.TEMP_PASSWORD)) {
				user.remove(SystemConstants.TEMP_PASSWORD);
				user.remove(SystemConstants.LOGIN_INTENT);
			}
			return user;
		} catch (CouchDbException e){
			logger.error(e.getMessage(),e);
			throw new CloudantGenericException(e.getMessage());
		}	
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<?, ?> saveUser(Map user) throws GenericEmbarkAdminException, CloudantGenericException {
		if (user.containsKey(SystemConstants.PASSWORD)) {
			user.remove(SystemConstants.PASSWORD);
		}
		if (user.containsKey(SystemConstants.PASSWORD_STATUS)) {
			user.remove(SystemConstants.PASSWORD_STATUS);
		}
		if (user.containsKey(SystemConstants.TEMP_PASSWORD)) {
			user.put(SystemConstants.TEMP_PASSWORD, encoder.encodePassword((String)user.get(SystemConstants.TEMP_PASSWORD)));
			user.put(SystemConstants.LOGIN_INTENT,null);
		}

		try {
			boolean isNew = !user.containsKey(SystemConstants.ID);
			final Response response;
			if (isNew) {
				response = this.cloudantClientMgr.getDB().save(processUser(user));
			} else {
				response = this.cloudantClientMgr.getDB().update(processUser(user));
			}
			user.put(SystemConstants.ID, response.getId());
			user.put(SystemConstants.REV,response.getRev());

			if (user.containsKey(SystemConstants.PASSWORD)) {
				user.remove(SystemConstants.PASSWORD);
			}
			if (user.containsKey(SystemConstants.TEMP_PASSWORD)) {
				user.remove(SystemConstants.TEMP_PASSWORD);
				user.remove(SystemConstants.LOGIN_INTENT);
			}

			return user;
		} catch (CouchDbException e) {
			logger.error(e.getMessage(),e);
			throw new CloudantGenericException(e.getMessage());
		}	
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map processUser(Map user) throws GenericEmbarkAdminException {
		String email = ((String)user.get(SystemConstants.EMAIL)).toLowerCase();

		boolean isNew = !user.containsKey(SystemConstants.ID);
		if (email == null) {
			throw new OperationFailException(SystemMessageHandler.EMBARK_USER_EMAIL_REQUIRED);
		}

		user.put(SystemConstants.EMAIL,email);

		List<Map> dbUserList = this.cloudantClientMgr.getDB()
				.findByIndex("\"selector\": {\"email\":\""+email+"\"}", Map.class);

		// To validate if it is a new record and the email has not been used already on create
		if (isNew && !(dbUserList).isEmpty()) {
			throw new OperationFailException(SystemMessageHandler.EMBARK_USER_EXISTS);
		}

		// To validate if it is a existing record and the email has not been used already on update
		if (!isNew && !(dbUserList).isEmpty()) {
			String id = (String)user.get(SystemConstants.ID);

			Map dbUser = dbUserList.get(0);
			String dbUserEmail = (String)dbUser.get(SystemConstants.EMAIL);
			String dbId = (String)user.get(SystemConstants.ID);

			if (!dbId.equalsIgnoreCase(id) && dbUserEmail.equalsIgnoreCase(email)) {
				throw new OperationFailException(SystemMessageHandler.EMBARK_USER_EXISTS);
			}

			for(Object key : user.keySet()){
				dbUser.put(key, user.get(key));				
			}
			return dbUser;
		} else {
			return user;
		}
	}

	public boolean removeUser(String id, String rev) throws CloudantGenericException, GenericEmbarkAdminException  {
		try {
			Response response = this.cloudantClientMgr.getDB().remove(id, rev);
			if (response.getError() != null) {
				throw new CloudantGenericException( response.getError());
			}
		} catch (CouchDbException e) {
			logger.error(e.getMessage(),e);
			throw new CloudantGenericException(e.getMessage());
		}	
		return true;
	}

	public LinkedTreeMap<String,Object> importUsers(String fileName, Cohort cohort, String password, String userName) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(SystemConstants.TMP_FOLDER+fileName));
			LinkedTreeMap<String,Object> response = new LinkedTreeMap<String,Object>();
			List<LinkedTreeMap<String,Object>> usersResponse = new ArrayList<LinkedTreeMap<String,Object>>();
			Long validRecords = 0L;
			Long invalidRecords = 0L;
			String[] columns = null;

			String line = null;

			ObjectMapper mapper = new ObjectMapper();

			String labelString = mapper.writeValueAsString(cohort.getLabels());
			logger.debug(labelString);

			String taskString = mapper.writeValueAsString(cohort.getTaskGroups());
			logger.debug(taskString);

			Object labels = mapper.readValue(labelString, Object.class);

			Object tasks = mapper.readValue(taskString, Object.class);


			while ( (line = reader.readLine()) != null ) {

				String[] valuesArray = line.split(",");

				if (columns == null) {

					columns = valuesArray;
					if (!this.isFileValid(columns)) {
						throw new Exception("Invalid file");
					}

				} else {

					LinkedTreeMap<String,Object> user = this.getBaseAttributes(cohort,  password,  userName);
					for (int i = 0; i < columns.length; i++) {
						user.put(columns[i], valuesArray[i]);
					}

					user.put(SystemConstants.COHORT,labels);
					user.put(SystemConstants.JOURNEY_STATUS_LIST,tasks);
					LinkedTreeMap<String,Object> result = new LinkedTreeMap<String,Object>();
					result.put(SystemConstants.EMAIL, user.get(SystemConstants.EMAIL));

					try {
						this.cloudantClientMgr.getDB().save(processUser(user));
						result.put(SystemConstants.STATUS, SystemConstants.VALID);
						validRecords++;
					} catch (Exception e) {
						result.put(SystemConstants.STATUS, e.getMessage());
						invalidRecords++;
					} finally {
						usersResponse.add(result);
					}
				}
			}
			response.put("totalRecords",new Long(validRecords+invalidRecords));
			response.put("totalValidRecords",new Long(validRecords));
			response.put("totalInvalidRecords",new Long(invalidRecords));
			response.put("results",usersResponse);
			return response;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private boolean isFileValid(String[] header) {
		boolean valid = true;
		final int attrLength = BASE_USER_ATTRIBUTE.length;
		final int headerLength = header != null ? header.length : 0;
		int attrIndex = 0;
		while (attrIndex<attrLength && valid) {
			int headerIndex = 0;
			valid = false;
			while (headerIndex < headerLength && !valid) {
				valid = BASE_USER_ATTRIBUTE[attrIndex].equalsIgnoreCase(header[headerIndex++]);
			}
			attrIndex++;
		}
		return valid;
	}

	private LinkedTreeMap<String,Object> getBaseAttributes( Cohort cohort, String password, String userName){
		LinkedTreeMap<String,Object> user = new LinkedTreeMap<String,Object>();
		user.put(SystemConstants.SCHEMA, SystemConstants.USER);
		user.put(SystemConstants.ROLE_ID, cohort.getRoleName());
		user.put(SystemConstants.COHORT_ID, cohort.getName());
		user.put(SystemConstants.PASSWORD_STATUS, SystemConstants.PASSWORD_STATUS_INVALID);
		user.put(SystemConstants.PASSWORD,password);
		user.put(SystemConstants.START_DATE, cohort.getStartDate());
		user.put(SystemConstants.DISABLED_DATE, cohort.getDisableDate());
		user.put(SystemConstants.CREATED_BY, userName);
		user.put(SystemConstants.CREATED_TIMESTAMP,format.format(new Date()));		
		return user;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String exportView(String designName, String viewName, Cohort cohort) throws Exception {

		final View view = this.cloudantClientMgr.getDB().view( designName+"/"+viewName).reduce(false);
		Object[] arguments = { cohort.getRoleName(),cohort.getName()	};
		Object listObject = view.key(arguments).limit(2000).query(LinkedTreeMap.class);
		List<LinkedTreeMap<String,Object>> list = (List<LinkedTreeMap<String,Object>>)listObject;
		int totalResult = list.size();

		LinkedTreeMap<String,Object> headerMap = new LinkedTreeMap<String,Object>();

		String header = null;
		List headerList = null;
		
		Jsonflattener flattener = new Jsonflattener();
		
		String fileName = System.currentTimeMillis()+viewName+".csv";
	
		
		FileWriter fileWriter = this.createFile(fileName);

		for (Iterator<LinkedTreeMap<String,Object>> iterator = list.iterator();iterator.hasNext();) {
			LinkedTreeMap<String,Object> doc = iterator.next();
			doc = (LinkedTreeMap<String,Object>) doc.get("value");

			if ( headerMap.isEmpty()) {
				headerMap = this.updateHeaderMap(headerMap, doc);
				header = this.buildHeader(headerMap,"");
				headerList = this.buildHeaderList(doc, "");
				fileWriter.append(header);
			}
			
			List<LinkedTreeMap<String,Object>> newlist = new ArrayList<LinkedTreeMap<String,Object>>();
			LinkedTreeMap<String,Object> rootDoc = flattener.addSimpleAttributes(new LinkedTreeMap<String,Object>(),doc,"");
			newlist.add(rootDoc);
			List<String> arrayAttribute =  flattener.getArrayAttributes(doc);
			for (Iterator<String> arrayAttributeIterator = arrayAttribute.iterator(); arrayAttributeIterator.hasNext();) {
				String key = arrayAttributeIterator.next();
				List<LinkedTreeMap<String,Object>> arryAttr = (List<LinkedTreeMap<String,Object>>)doc.get(key);
				newlist = flattener.flatList(newlist, arryAttr, key+"__");
			}
			
			List<String> complexAttribute =  flattener.getComplexAttributes(doc);
			for (Iterator<String> complexAttributeIterator = complexAttribute.iterator(); complexAttributeIterator.hasNext();) {
				String key = complexAttributeIterator.next();
				LinkedTreeMap<String,Object> complexAttr = (LinkedTreeMap<String,Object>)doc.get(key);
				List<LinkedTreeMap<String,Object>> listAux = new ArrayList<LinkedTreeMap<String,Object>>();
				listAux.add(complexAttr);
				newlist = flattener.flatList(newlist, listAux, key+"__");
			}
			
			
			
			for(Iterator<LinkedTreeMap<String,Object>> newIterator = newlist.iterator();newIterator.hasNext();) {
				final LinkedTreeMap<String, Object> newDoc = newIterator.next();
				fileWriter.append(SystemConstants.NEW_LINE_SEPARATOR);			
				fileWriter.append(this.buildRow(headerList, newDoc));
			}
		}

		if (fileWriter != null) {
			fileWriter.close();
		}

		return fileName;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LinkedTreeMap<String,Object> updateHeaderMap(LinkedTreeMap<String,Object> headerMap, LinkedTreeMap<String, ?> doc) {
		final Set<String> keys = doc.keySet();
		for (final String key : keys) {
			Object value = doc.get(key);
			if (!headerMap.containsKey(key)) {
				if (value instanceof LinkedTreeMap || value instanceof ArrayList) {
					final LinkedTreeMap branchMap = new LinkedTreeMap();
					if (value instanceof ArrayList) {
						ArrayList valueArrayList = (ArrayList)value;
						if (!valueArrayList.isEmpty()) {
							LinkedTreeMap firstElement = (LinkedTreeMap) valueArrayList.get(0);
							this.updateHeaderMap(branchMap, firstElement);
						}
					} else {
						this.updateHeaderMap(branchMap, (LinkedTreeMap)doc.get(key));
					}
					headerMap.put(key, branchMap);
				} else {	
					headerMap.put(key, false);
				}
			}
		}
		return headerMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String buildHeader(LinkedTreeMap<String,Object> headerMap, String prefix) {
		final StringBuffer sb = new StringBuffer();
		final Set<String> keys = headerMap.keySet();
		for (final String key : keys) {
			Object value = headerMap.get(key);
			final boolean isObject  = (value instanceof LinkedTreeMap);
			if (isObject) {
				sb.append(this.buildHeader((LinkedTreeMap)value, prefix+key+"__"));
			} else {
				sb.append(prefix+key);
			}
			sb.append(SystemConstants.COMMA_DELIMITER);
		}
		sb.deleteCharAt(sb.lastIndexOf(SystemConstants.COMMA_DELIMITER));
		return sb.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> buildHeaderList(LinkedTreeMap<String,Object> headerMap, String prefix) {
		final List<String> list = new ArrayList<String>();
		final Set<String> keys = headerMap.keySet();
		for (final String key : keys) {
			Object value = headerMap.get(key);			
			if (value instanceof LinkedTreeMap) {
				final List<String> auxlist = this.buildHeaderList((LinkedTreeMap)value, prefix+key+"__");
				for (Iterator<String> i = auxlist.listIterator(); i.hasNext();) {
					list.add(i.next());
				}
			} else if (value instanceof ArrayList) {
				ArrayList valueArrayList = (ArrayList)value;
				if (!valueArrayList.isEmpty()) {
					LinkedTreeMap firstElement = (LinkedTreeMap) valueArrayList.get(0);
					final List<String> auxlist = this.buildHeaderList((LinkedTreeMap)firstElement, prefix+key+"__");
					for (Iterator<String> i = auxlist.listIterator(); i.hasNext();) {
						list.add(i.next());
					}
				}
			} else {
				list.add(prefix+key);
			}
		}
		return list;
	}
	
	private FileWriter createFile(String fileName) throws Exception {
		
		final File folder = new File(SystemConstants.TMP_FOLDER);
		if (!folder.exists()) {
			folder.mkdir();
			logger.debug("Folder created - " + SystemConstants.TMP_FOLDER);
		}
		
		File file = new File(SystemConstants.TMP_FOLDER+fileName);
		if (file.exists() && file.isDirectory()) {
			throw new Exception("Invalid file name");
		}
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file, false);
		return fileWriter;
	}
	
	private String buildRow(List<String> attributes, LinkedTreeMap<String, Object> doc) {
		final StringBuffer row = new StringBuffer();
		
		for (Iterator<String> i = attributes.iterator();i.hasNext(); ) {
			String key = i.next();
			Object value = doc.get(key);
			if (value != null) {
				String stringValue = doc.get(key).toString();
				if (stringValue.contains(",")) { 
					stringValue = "\""+stringValue+"\"";
				}
				row.append(stringValue);
			}
			row.append(SystemConstants.COMMA_DELIMITER);
		}
		row.deleteCharAt(row.lastIndexOf(SystemConstants.COMMA_DELIMITER));
		return row.toString();
	}

}
