package com.rbc.b2e.embark.admin.model.service;

import java.util.List;

import com.rbc.b2e.embark.admin.model.AdminUser;
import com.rbc.b2e.embark.admin.model.Cohort;
import com.rbc.b2e.embark.admin.model.Environment;
import com.rbc.b2e.embark.admin.model.Role;

public interface AdminService {
	// Environment

	List<Environment> retrieveEnvironments();
	
	List<Environment> retrieveEnvironmentsByAdminUser();

	Environment saveEnvironment(Environment aEnvironment);

	void deleteEnvironment(long aId);

	// Admin User

	List<AdminUser> retrieveAdminUsers();

	AdminUser retrieveAdminUser(String aEmail);

	AdminUser saveAdminUser(AdminUser aUser);

	void deleteAdminUser(long aId);
	
	AdminUser updatePassword(AdminUser aUser);
	
	AdminUser updateTemporaryPassword(AdminUser aUser);

	// Roles & Cohorts

	List<Role> retrieveRoles();
	
	List<Role> retrieveRolesByAdminUser(long aId, boolean isSuperuser);

	void saveRole(Role aRole);

	boolean deleteRole(Role aRole);

	void saveCohort(Cohort aCohort);

	boolean deleteCohort(Cohort aCohort);
	
	Cohort retrieveCohort(long aRoleId, long aCohortId);

}
