package com.rbc.b2e.embark.admin.model.service.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rbc.b2e.embark.admin.model.Checklist;
import com.rbc.b2e.embark.admin.model.Label;
import com.rbc.b2e.embark.admin.model.LabelType;
import com.rbc.b2e.embark.admin.model.Labeled;
import com.rbc.b2e.embark.admin.model.Task;
import com.rbc.b2e.embark.admin.model.TaskGroup;
import com.rbc.b2e.embark.admin.model.service.AdminServiceException;

import net.objectof.sql.SqlConnection;

public class SqlChecklistService extends SqlConnection {

	public SqlChecklistService(Connection aConnection, Map<String, String> aBundle) {
		super(aConnection, aBundle);
	}

	private List<Task> retrieveTasks(long aTaskGroupId) {
		try (PreparedStatement stmt = bind("retrieveTasks", aTaskGroupId); ResultSet rs = stmt.executeQuery()) {
			List<Task> list = new ArrayList<>();
			while (rs.next()) {
				Task task = new Task();
				task.setId(rs.getLong("id"));
				task.setName(rs.getString("name"));
				task.setDueDate(rs.getString("dueDate"));
				task.setLabels(retrieveLabels(LabelType.TASK, task.getId()));
				list.add(task);
			}
			return list;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		}
	}

	private List<Label> retrieveLabels(LabelType aType, long aEntityId) {
		try (PreparedStatement stmt = bind("retrieveLabels", aType.id(), aEntityId);
				ResultSet rs = stmt.executeQuery()) {
			List<Label> list = new ArrayList<>();
			while (rs.next()) {
				Label label = new Label();
				label.setLanguage(rs.getString("language"));
				label.setText(rs.getString("text"));
				list.add(label);
			}
			return list;
		} catch (Exception e) {
			throw new AdminServiceException(e);
		}
	}

	private List<TaskGroup> retrieveTaskGroups(Checklist aChecklist) {
		int type = aChecklist.getType().id();
		long id = aChecklist.getId();
		try (PreparedStatement stmt = bind("retrieveTaskGroups", type, id); ResultSet rs = stmt.executeQuery()) {
			List<TaskGroup> list = new ArrayList<>();
			while (rs.next()) {
				TaskGroup group = new TaskGroup();
				long taskGroupId = rs.getLong("id");
				group.setId(taskGroupId);
				group.setName(rs.getString("name"));
				group.setLabels(retrieveLabels(LabelType.TASKLIST, taskGroupId));
				group.setTasks(retrieveTasks(taskGroupId));
				list.add(group);
			}
			return list;
		} catch (SQLException e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	public void loadChecklist(Checklist aChecklist) {
		long id = aChecklist.getId();
		List<TaskGroup> taskGroups = retrieveTaskGroups(aChecklist);
		List<Label> labels = retrieveLabels(aChecklist.getType(), id);
		aChecklist.setTaskGroups(taskGroups);
		aChecklist.setLabels(labels);
	}

	public void saveChecklist(Checklist aChecklist) throws SQLException {
		saveLabels(aChecklist);
		if (aChecklist.getId() != 0) {
			deleteTaskGroups(aChecklist);
		}
		for (TaskGroup taskGroup : aChecklist.getTaskGroups()) {
			saveTaskGroup(aChecklist, taskGroup);
		}
	}

	private void saveTaskGroup(Checklist aChecklist, TaskGroup aTaskGroup) throws SQLException {
		if (aTaskGroup.getId() == 0) {
			createTaskGroup(aChecklist, aTaskGroup);
		} else {
			updateTaskGroup(aTaskGroup);
			deleteTasks(aTaskGroup);
		}
		saveLabels(aTaskGroup);
		for (Task task : aTaskGroup.getTasks()) {
			saveTask(aTaskGroup, task);
		}
	}

	private void saveTask(TaskGroup aTaskGroup, Task aTask) throws SQLException {
		if (aTask.getId() == 0) {
			createTask(aTaskGroup, aTask);
		} else {
			updateTask(aTask);
		}
		saveLabels(aTask);
	}

	private void updateTask(Task aTask) {
		execute("updateTask", aTask.getDueDate(), aTask.getId());
	}

	private void createTask(TaskGroup aTaskGroup, Task aTask) throws SQLException {
		long id = create("createTask", aTaskGroup.getId(), aTask.getName(), aTask.getDueDate());
		aTask.setId(id);
	}

	private void createTaskGroup(Checklist aChecklist, TaskGroup aTaskGroup) throws SQLException {
		long id = create("createTaskGroup", aChecklist.getType().id(), aChecklist.getId(), aTaskGroup.getName());
		aTaskGroup.setId(id);
	}

	/**
	 * Only deletes TaskGroups that are NOT in the Checklist.
	 *
	 * @param aChecklist
	 */
	private void deleteTaskGroups(Checklist aChecklist) {
		int type = aChecklist.getType().id();
		long id = aChecklist.getId();
		Set<Long> list = extractTaskGroupIds(aChecklist);
		try (PreparedStatement stmt = bind("retrieveTaskGroups", type, id); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				long taskGroupId = rs.getLong("id");
				if (!list.contains(taskGroupId)) {
					deleteTaskGroupandTask(taskGroupId);
				}
			}
		} catch (SQLException e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private void deleteTaskGroupandTask(long aTaskGroupId) {
		deleteLabels(LabelType.TASKLIST, aTaskGroupId);
		try (PreparedStatement stmt = bind("retrieveTasks", aTaskGroupId); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				long taskId = rs.getLong("id");
				deleteTask(taskId);
			}
			deleteTaskGroup(aTaskGroupId);
		} catch (SQLException e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private void deleteTasks(TaskGroup aTaskGroup) {
		Set<Long> list = extractTaskIds(aTaskGroup);
		try (PreparedStatement stmt = bind("retrieveTasks", aTaskGroup.getId()); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				long taskId = rs.getLong("id");
				if (!list.contains(taskId)) {
					deleteTask(taskId);
				}
			}
		} catch (SQLException e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private Set<Long> extractTaskIds(TaskGroup aTaskGroup) {
		Set<Long> ids = new HashSet<>();
		for (Task task : aTaskGroup.getTasks()) {
			ids.add(task.getId());
		}
		return ids;
	}
	
	private void deleteTaskGroup(long aTaskGroupId) {
		deleteLabels(LabelType.TASKLIST, aTaskGroupId);
		execute("deleteTaskGroup", aTaskGroupId);
	}

	private void deleteTask(long aTaskId) {
		deleteLabels(LabelType.TASK, aTaskId);
		execute("deleteTask", aTaskId);
	}

	private Set<Long> extractTaskGroupIds(Checklist aChecklist) {
		Set<Long> ids = new HashSet<>();
		for (TaskGroup tg : aChecklist.getTaskGroups()) {
			ids.add(tg.getId());
		}
		return ids;
	}

	private void updateTaskGroup(TaskGroup aTaskGroup) {
		// TODO Nothing to do unless name can be updated.
	}

	private void saveLabels(Labeled aEntity) {
		deleteLabels(aEntity.getType(), aEntity.getId());
		createLabels(aEntity);
	}

	private void deleteLabels(LabelType aType, long aId) {
		try (PreparedStatement stmt = bind("deleteLabels", aType.id(), aId)) {
			stmt.execute();
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}

	private void createLabels(Labeled aEntity) {
		long id = aEntity.getId();
		try (PreparedStatement stmt = prepare("insertLabel")) {
			for (Label label : aEntity.getLabels()) {
				stmt.setInt(1, aEntity.getType().id());
				stmt.setLong(2, id);
				stmt.setString(3, label.getLanguage());
				stmt.setString(4, label.getText());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (Exception e) {
			close();
			throw new AdminServiceException(e);
		}
	}
}
