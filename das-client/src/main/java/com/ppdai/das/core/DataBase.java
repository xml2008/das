package com.ppdai.das.core;


import com.google.common.base.Strings;

import java.util.Objects;

public class DataBase {
	private String name;
	private boolean master;
	private String sharding;
	private String connectionString;

	private MGRInfo mgrInfo = new MGRInfo();

	static public class MGRInfo {
		String id;
		String host;
		String state;
		String role;

		public MGRInfo(String id, String host, String state, String role) {
			this.id = id;
			this.host = host;
			this.state = state;
			this.role = role;
		}
		public MGRInfo() {}
	}

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
		this.mgrInfo.host = host;
		return this;
	}

	public String getHost() {
		return this.mgrInfo.host;
	}

	public String getMgrId() {
		return this.mgrInfo.id;
	}

	public DataBase setMgrId(String mgrId) {
		this.mgrInfo.id = mgrId;
		return this;
	}

	public String getMgrState() {
		return this.mgrInfo.state;
	}

	public DataBase setMgrState(String mgrState) {
		this.mgrInfo.state = mgrState;
		return this;
	}

	public String getMgrRole() {
		return this.mgrInfo.role;
	}

	public DataBase setMgrRole(String mgrRole) {
		this.mgrInfo.role = mgrRole;
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
