package com.ppdai.das.core;


import com.google.common.base.Strings;

import java.util.Objects;

public class DataBase {
	private String name;
	private boolean master;
	private String sharding;
	private String connectionString;

	private String host;
	private String mgrId;
	private String mgrState;
	private String mgrRole;

	public DataBase(String name, 
			boolean master, 
			String sharding, 
			String connectionString) {
		this.name = name;
		this.master = master;
		this.sharding = sharding;
		this.connectionString = connectionString;
	}

	public DataBase setHost(String host) {
		this.host = host;
		return this;
	}

	public String getHost() {
		return host;
	}

	public String getMgrId() {
		return mgrId;
	}

	public DataBase setMgrId(String mgrId) {
		this.mgrId = mgrId;
		return this;
	}

	public String getMgrState() {
		return mgrState;
	}

	public DataBase setMgrState(String mgrState) {
		this.mgrState = mgrState;
		return this;
	}

	public String getMgrRole() {
		return mgrRole;
	}

	public DataBase setMgrRole(String mgrRole) {
		this.mgrRole = mgrRole;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DataBase dataBase = (DataBase) o;
		return master == dataBase.master &&
				Objects.equals(name, dataBase.name) &&
				Objects.equals(sharding, dataBase.sharding) &&
				Objects.equals(connectionString, dataBase.connectionString);
	}

	@Override
	public int hashCode() {

		return Objects.hash(name, master, sharding, connectionString);
	}

	public String getName() {
		return name;
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster() {
		master = true;
	}

	public void setSlave() {
		master = false;
	}

	public String getSharding() {
		return sharding;
	}

	public String getConnectionString() {
		return connectionString;
	}
}
