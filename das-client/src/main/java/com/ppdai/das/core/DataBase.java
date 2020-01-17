package com.ppdai.das.core;


import java.util.Objects;

public class DataBase {
	private String name;
	private boolean master;
	private String sharding;
	private String connectionString;

	public DataBase(String name,
			boolean master,
			String sharding,
			String connectionString) {
		this.name = name;
		this.master = master;
		this.sharding = sharding;
		this.connectionString = connectionString;
	}

	public DataBase deepCopy() {
		return new DataBase(name, master, sharding,connectionString);
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
