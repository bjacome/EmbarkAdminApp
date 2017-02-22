package com.rbc.b2e.embark.admin.model.service.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rbc.b2e.embark.admin.model.AdminUser;
import com.rbc.b2e.embark.admin.model.AdminUserRole;
import com.rbc.b2e.embark.admin.model.Checklist;
import com.rbc.b2e.embark.admin.model.Cohort;
import com.rbc.b2e.embark.admin.model.Environment;
import com.rbc.b2e.embark.admin.model.LabelType;
import com.rbc.b2e.embark.admin.model.Role;
import com.rbc.b2e.embark.admin.model.service.AdminService;
import com.rbc.b2e.embark.admin.model.service.AdminServiceError;
import com.rbc.b2e.embark.admin.model.service.AdminServiceException;

import net.objectof.sql.SqlConnection;
import net.objectof.sql.SqlException;

public class SqlAdminService extends SqlConnection implements AdminService {
	
	
	public SqlAdminService(Connection aConnection, Map<String, String> aBundle) {
		super(aConnection, aBundle);
	}

	@Override
	public Environment saveEnvironment(Environment aEnv) {
		return aEnv.getId() == 0 ? createEnvironment(aEnv) : updateEnvironment(aEnv);
	}

	@Override
	public void saveRole(Role aRole) {
		try {
			if (aRole.getId() == 0) {
				createRole(aRole);
			} else {
				edit("editRole", aRole.getId(), aRole.getRevision());
				long rev = System.currentTimeMillis();
				execute("updateRole", rev, aRole.getId());
				aRole.setRevision(rev);
			}
			saveStatuses(aRole);
			saveChecklist(aRole);
			commit();
		} catch (AdminServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	private final AdminUser createAdminUser(AdminUser aUser) {
		try {
			long rev = System.currentTimeMillis();

			long id = create("createAdminUser", rev, aUser.getEmail(), aUser.getFullName(), aUser.isSuperUser(),
					aUser.getFailedLoginAttempts());
			aUser.setId(id);
			aUser.setRevision(rev);
			saveAdminUserRoles(aUser);
			commit();
			return aUser;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	private final Environment createEnvironment(Environment aEnv) {
		try {
			long newRev = System.currentTimeMillis();
			long id = create("createEnvironment", newRev, aEnv.getName(), aEnv.getLocation(), aEnv.getUrl(),
					aEnv.getHost(), aEnv.getPort(), aEnv.getUserName(), aEnv.getPassword());
			aEnv.setId(id);
			aEnv.setRevision(newRev);
			commit();
			return aEnv;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	@Override
	public AdminUser retrieveAdminUser(String aEmail) {
		try (PreparedStatement stmt = bind("retrieveAdminUser", aEmail); ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				AdminUser user = new AdminUser();
				long id = rs.getLong("id");
				user.setEmail(aEmail);
				user.setId(id);
				user.setRevision(rs.getLong("rev"));
				user.setFullName(rs.getString("fullName"));
				user.setSuperUser(rs.getBoolean("isSuperUser"));
				user.setFailedLoginAttempts(rs.getInt("failedLoginAttempts"));
				user.setTemporaryPassword(rs.getString("temporaryPassword"));
				user.setPassword(rs.getString("password"));
				user.setPasswordValid(rs.getBoolean("isPasswordValid"));
				List<AdminUserRole> roles = retrieveAdminUserRoles(id);
				user.setRoles(roles);
				return user;
			}
			return null;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	private List<AdminUserRole> retrieveAdminUserRoles(long aAdminUserId) {
		try (PreparedStatement stmt = bind("retrieveAdminUserRoles", LabelType.ROLE, aAdminUserId);
				ResultSet rs = stmt.executeQuery()) {
			List<AdminUserRole> roles = new ArrayList<>();
			while (rs.next()) {
				AdminUserRole role = new AdminUserRole();
				role.setRole(rs.getString("name"));
				role.setTitle(rs.getString("title"));
				roles.add(role);
			}
			return roles;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	@Override
	public List<Environment> retrieveEnvironments() {
		try (PreparedStatement stmt = bind("retrieveEnvironments"); ResultSet rs = stmt.executeQuery()) {
			List<Environment> list = new ArrayList<>();
			while (rs.next()) {
				Environment env = new Environment();
				env.setId(rs.getInt("id"));
				env.setRevision(rs.getLong("rev"));
				env.setName(rs.getString("name"));
				env.setLocation(rs.getString("location"));
				env.setUrl(rs.getString("url"));
				env.setHost(rs.getString("host"));
				env.setPort(rs.getInt("port"));
				env.setUserName(rs.getString("userName"));
				env.setPassword(rs.getString("password"));
				list.add(env);
			}
			return list;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}
	
	@Override
	public List<Environment> retrieveEnvironmentsByAdminUser() {
		try (PreparedStatement stmt = bind("retrieveEnvironments"); ResultSet rs = stmt.executeQuery()) {
			List<Environment> list = new ArrayList<>();
			while (rs.next()) {
				Environment env = new Environment();
				env.setId(rs.getInt("id"));
				env.setRevision(rs.getLong("rev"));
				env.setName(rs.getString("name"));
				env.setLocation(rs.getString("location"));
				list.add(env);
			}
			return list;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	@Override
	public void deleteEnvironment(long aId) {
		try {
			execute("deleteEnvironment", aId);
			commit();
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	@Override
	public void deleteAdminUser(long aId) {
		PreparedStatement stmt = null;
		try {
			stmt = bind("deleteAdminUser", aId);
			stmt.executeUpdate();
			commit();
		} catch (SQLException e) {
			throw new SqlException(e);
		} finally {
			close(stmt);
			close();
		}
	}

	@Override
	public AdminUser saveAdminUser(AdminUser aUser) {
		return aUser.getId() == 0 ? createAdminUser(aUser) : updateAdminUser(aUser);
	}

	private AdminUser updateAdminUser(AdminUser aUser) {
		long id = aUser.getId();
		long rev = aUser.getRevision();
		edit("editAdminUser", id, rev);
		try {
			rev = System.currentTimeMillis();
			execute("updateAdminUser", rev, aUser.getEmail(), aUser.getFullName(), aUser.isSuperUser(),
					aUser.getFailedLoginAttempts(), id);
			aUser.setRevision(rev);
			saveAdminUserRoles(aUser);
			commit();
			return aUser;
		} catch (AdminServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}
	
	public AdminUser updatePassword(AdminUser aUser) {
		long id = aUser.getId();
		long rev = aUser.getRevision();
		edit("editAdminUser", id, rev);
		try {
			rev = System.currentTimeMillis();
			execute("updateAdminUserPassword", rev,  aUser.getPassword(), id);
			aUser.setRevision(rev);
			commit();
			return aUser;
		} catch (AdminServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}
	
	public AdminUser updateTemporaryPassword(AdminUser aUser) {
		long id = aUser.getId();
		long rev = aUser.getRevision();
		edit("editAdminUser", id, rev);
		try {
			rev = System.currentTimeMillis();
			execute("updateAdminUserTemporaryPassword", rev,  aUser.getTemporaryPassword(), id);
			aUser.setRevision(rev);
			commit();
			return aUser;
		} catch (AdminServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	private final Environment updateEnvironment(Environment aEnv) {
		long id = aEnv.getId();
		long rev = aEnv.getRevision();
		edit("editEnvironment", id, rev);
		try {
			rev = System.currentTimeMillis();
			execute("updateEnvironment", rev, aEnv.getName(), aEnv.getLocation(), aEnv.getUrl(), aEnv.getHost(),
					aEnv.getPort(), aEnv.getUserName(), aEnv.getPassword(), id);
			aEnv.setRevision(rev);
			commit();
			return aEnv;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	/**
	 * Ensures an object matching a row in the database is good for update. If
	 * the statement is "select ... for update", the row will be reserved
	 * (locked) for this service call. edit only needs to be called on
	 * objects/rows that can be independently modified: When a child node in an
	 * object graph can only be modified via a parent node the child doesn't
	 * need to be locked because other transactions must first pass the parent
	 * edit gate.
	 *
	 * @param aStatement
	 *            generally should be a "select ... for update" to lock a role.
	 * @param aId
	 * @param aRev
	 */
	protected void edit(String aStatement, long aId, long aRev) {
		try (//
				PreparedStatement stmt = bind(aStatement, aId); //
				ResultSet rs = stmt.executeQuery(); //
		) {
			if (!rs.next()) {
				throw new AdminServiceError(1000302, "(" + aId + ")");
			}
			if (aRev != rs.getLong("rev")) {
				throw new AdminServiceError(1000303, "");
			}
		} catch (AdminServiceException e) {
			close();
			throw e;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	@Override
	public List<Role> retrieveRoles() {
		try (PreparedStatement stmt = bind("retrieveRoles"); ResultSet rs = stmt.executeQuery()) {
			SqlChecklistService service = new SqlChecklistService(getConnection(), getStatements());
			List<Role> list = new ArrayList<>();
			while (rs.next()) {
				long id = rs.getLong("id");
				String name = rs.getString("name");
				Role role = new Role();
				role.setId(id);
				role.setRevision(rs.getLong("rev"));
				role.setName(name);
				List<Cohort> cohorts = retrieveCohorts(id);
				role.setCohorts(cohorts);
				Set<String> statuses = retrieveStatuses(id);
				role.setStatuses(statuses);
				service.loadChecklist(role);
				list.add(role);
			}
			return list;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}
	
	@Override
	public List<Role> retrieveRolesByAdminUser(long aId, boolean isSuperuser) {
		try (PreparedStatement stmt = isSuperuser ? bind("retrieveRolesBySuperAdminUser") : bind("retrieveRolesByAdminUser",aId); ResultSet rs = stmt.executeQuery()) {
			SqlChecklistService service = new SqlChecklistService(getConnection(), getStatements());
			List<Role> list = new ArrayList<>();
			while (rs.next()) {
				long id = rs.getLong("id");
				String name = rs.getString("name");
				Role role = new Role();
				role.setId(id);
				role.setRevision(rs.getLong("rev"));
				role.setName(name);
				List<Cohort> cohorts = retrieveCohorts(id);
				role.setCohorts(cohorts);
				Set<String> statuses = retrieveStatuses(id);
				role.setStatuses(statuses);
				service.loadChecklist(role);
				list.add(role);
			}
			return list;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private List<Cohort> retrieveCohorts(long aRoleId) {
		try (PreparedStatement stmt = bind("retrieveCohorts", aRoleId); ResultSet rs = stmt.executeQuery()) {
			SqlChecklistService service = new SqlChecklistService(getConnection(), getStatements());
			List<Cohort> list = new ArrayList<>();
			while (rs.next()) {
				long id = rs.getLong("id");
				Cohort cohort = new Cohort();
				cohort.setId(id);
				cohort.setRevision(rs.getLong("rev"));
				cohort.setRoleId(aRoleId);
				cohort.setRoleName(rs.getString("roleName"));
				cohort.setName(rs.getString("name"));
				cohort.setStartDate(rs.getString("startDate"));
				cohort.setDisableDate(rs.getString("disableDate"));
				service.loadChecklist(cohort);
				list.add(cohort);
			}
			return list;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}
	
	public Cohort retrieveCohort(long aRoleId, long aCohortId) {
		try (PreparedStatement stmt = bind("retrieveCohort", aRoleId, aCohortId); ResultSet rs = stmt.executeQuery()) {
			SqlChecklistService service = new SqlChecklistService(getConnection(), getStatements());
			Cohort cohort;
			if (rs.next()) {
				long id = rs.getLong("id");
				cohort = new Cohort();
				cohort.setId(id);
				cohort.setRevision(rs.getLong("rev"));
				cohort.setRoleId(aRoleId);
				cohort.setRoleName(rs.getString("roleName"));
				cohort.setName(rs.getString("name"));
				cohort.setStartDate(rs.getString("startDate"));
				cohort.setDisableDate(rs.getString("disableDate"));
				service.loadChecklist(cohort);
			} else {
				cohort = null;
			}
			return cohort;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private final void saveChecklist(Checklist aChecklist) throws SQLException {
		SqlChecklistService service = new SqlChecklistService(getConnection(), getStatements());
		service.saveChecklist(aChecklist);
	}

	private final void saveStatuses(Role aRole) {
		long id = aRole.getId();
		if (id != 0) {
			deleteStatuses(id);
		}
		insertStatuses(id, aRole.getStatuses());
	}

	private final void saveAdminUserRoles(AdminUser aUser) {
		long id = aUser.getId();
		if (id != 0) {
			deleteAdminUserRoles(id);
		}
		insertAdminUserRoles(id, aUser.getRoles());
	}

	private void insertAdminUserRoles(long aAdminUserId, List<AdminUserRole> aRoles) {
		Map<String, Long> roleMap = mapRoles();
		try (PreparedStatement stmt = prepare("insertAdminUserRole")) {
			for (AdminUserRole role : aRoles) {
				String name = role.getRole();
				Long roleId = roleMap.get(name);
				if (roleId == null) {
					throw new AdminServiceError(1000601, name);
				}
				stmt.setLong(1, aAdminUserId);
				stmt.setLong(2, roleId);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (AdminServiceException e) {
			close();
			throw e;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private Map<String, Long> mapRoles() {
		try (PreparedStatement stmt = bind("retrieveRoles"); ResultSet rs = stmt.executeQuery()) {
			Map<String, Long> map = new HashMap<String, Long>();
			while (rs.next()) {
				long id = rs.getLong("id");
				String name = rs.getString("name");
				map.put(name, id);
			}
			return map;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private void deleteAdminUserRoles(long aId) {
		execute("deleteAdminUserRoles", aId);
	}

	private void insertStatuses(long aId, Set<String> aStatuses) {
		try (PreparedStatement stmt = prepare("insertStatus")) {
			for (String status : aStatuses) {
				stmt.setLong(1, aId);
				stmt.setString(2, status);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private void deleteStatuses(long aId) {
		try (PreparedStatement stmt = bind("deleteStatuses", aId)) {
			stmt.execute();
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private final void createRole(Role aRole) {
		try {
			long newRev = System.currentTimeMillis();
			long id = (int) create("createRole", newRev, aRole.getName());
			aRole.setId(id);
			aRole.setRevision(newRev);
		} catch (SQLException e) {
			close();
			if(e.getErrorCode() == 1062 ) {
				throw new AdminServiceException("Role name already exist.");
			} else {
				throw new AdminServiceException(e);
			}
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private final void createCohort(Cohort aCohort) {
		try {
			Long roleId = aCohort.getRoleId();
			long newRev = System.currentTimeMillis();
			long id = (int) create("createCohort", newRev, aCohort.getName(), roleId, aCohort.getStartDate(),
					aCohort.getDisableDate());
			aCohort.setId(id);
			aCohort.setRevision(newRev);
		} catch (SQLException e) {
			close();
			if(e.getErrorCode() == 1062 ) {
				throw new AdminServiceException("Cohort name already exist.");
			} else {
				throw new AdminServiceException(e);
			}
		} catch (AdminServiceException e) {
			close();
			throw e;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	@Override
	//TODO this should be changed to a one transaction
	public boolean deleteRole(Role aRole) {
		long roleId = aRole.getId();
		long rev = aRole.getRevision();
		try (PreparedStatement stmt = bind("retrieveRoleByRev", roleId, rev); ResultSet rs = stmt.executeQuery();) {
			if (rs.next()) {
				execute("deleteCohortTaskGroupByRole", LabelType.COHORT.id(), roleId);
				execute("deleteCohortLabelsByRole", LabelType.COHORT.id(), roleId);
				execute("deleteCohortByRole", roleId);
				execute("deleteTaskGroupByEntity", LabelType.ROLE.id(), roleId);
				execute("deleteStatuses", roleId);
				execute("deleteLabels", LabelType.ROLE.id(), roleId);
				execute("deleteRole", roleId);
				commit();
			} else {
				//TODO throw exception invalid record
			}
		} catch (Exception e) {
			rollback();
			close();
			throw new AdminServiceException(e);
		}
		return true;
	}

	private final Set<String> retrieveStatuses(long aId) {
		try ( //
				PreparedStatement stmt = bind("retrieveRoleStatuses", aId); //
				ResultSet rs = stmt.executeQuery(); //
		) {
			Set<String> set = new HashSet<>();
			while (rs.next()) {
				set.add(rs.getString("name"));
			}
			return set;
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}

	}

	@Override
	public void saveCohort(Cohort aCohort) {
		try {
			if (aCohort.getId() == 0) {
				createCohort(aCohort);
			} else {
				edit("editCohort", aCohort.getId(), aCohort.getRevision());
				long rev = System.currentTimeMillis();
				execute("updateCohort", rev, aCohort.getStartDate(), aCohort.getDisableDate(), aCohort.getId());
				aCohort.setRevision(rev);
			}
			saveChecklist(aCohort);
			commit();
		} catch (AdminServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}

	@Override
	public boolean deleteCohort(Cohort aCohort) {
		long cohortId = aCohort.getId();
		long rev = aCohort.getRevision();
		try (PreparedStatement stmt = bind("retrieveCohortByRev", cohortId, rev); ResultSet rs = stmt.executeQuery();) {
			if (rs.next()) {
				execute("deleteTaskGroupByEntity", LabelType.COHORT.id(), cohortId);
				execute("deleteLabels", LabelType.COHORT.id(), cohortId);
				execute("deleteCohort", cohortId);
				commit();
			} else {
				//TODO throw exception invalid record
			}
		} catch (Exception e) {
			rollback();
			close();
			throw new AdminServiceException(e);
		}
		return true;
	}

	@Override
	public List<AdminUser> retrieveAdminUsers() {
		try (PreparedStatement stmt = bind("retrieveAdminUsers"); ResultSet rs = stmt.executeQuery()) {
			List<AdminUser> list = new ArrayList<>();
			while (rs.next()) {
				AdminUser user = new AdminUser();
				long id = rs.getLong("id");
				user.setEmail(rs.getString("email"));
				user.setId(id);
				user.setRevision(rs.getLong("rev"));
				user.setFullName(rs.getString("fullName"));
				user.setSuperUser(rs.getBoolean("isSuperUser"));
				user.setFailedLoginAttempts(rs.getInt("failedLoginAttempts"));
				user.setTemporaryPassword(rs.getString("temporaryPassword"));
				user.setPassword(rs.getString("password"));
				List<AdminUserRole> roles = retrieveAdminUserRoles(id);
				user.setRoles(roles);
				list.add(user);
			}
			return list;
		} catch (AdminServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		} finally {
			close();
		}
	}
}
