package com.codeim.coxin.task;

public interface TaskListener {
	String getName();

	void onPreExecute(GenericTask task);

	void onPostExecute(GenericTask task, TaskResult result);
	void onPostExecute(GenericTask task, TaskResult result, Object param);

	void onProgressUpdate(GenericTask task, Object param);

	void onCancelled(GenericTask task);
}
