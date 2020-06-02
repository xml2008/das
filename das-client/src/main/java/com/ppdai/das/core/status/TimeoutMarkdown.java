package com.ppdai.das.core.status;

import java.util.HashSet;
import java.util.Set;

import com.ppdai.das.client.Hints;
import com.ppdai.das.core.HintEnum;

public class TimeoutMarkdown extends BaseStatus implements TimeoutMarkdownMBean {
	private volatile boolean enableTimeoutMarkDown;

    private volatile int samplingDuration = 120;

	private volatile int timeoutThreshold = 60;

    private volatile int errorCountThreshold = 300;

    private volatile float errorPercentThreshold = 0.5f;

	private volatile int errorPercentReferCount = 400;
	
    private volatile String mySqlErrorCodes = "0";

    private volatile String sqlServerErrorCodes = "-2";
    
    private volatile Set<Integer> mysqlTimeoutMarkdownCodes = new HashSet<Integer>();

    private volatile Set<Integer> sqlServerTimeoutMarkdownCodes = new HashSet<Integer>();
	
	@Override
	public boolean isEnabled() {
		return enableTimeoutMarkDown;
	}
	@Override
	public void setEnabled(boolean enabled) {
		this.enableTimeoutMarkDown = enabled;
		changed();
	}

	@Override
	public int getSamplingDuration() {
		return samplingDuration;
	}
	@Override
	public void setSamplingDuration(int samplingDuration) {
		this.samplingDuration = samplingDuration;
		changed();
	}

	@Override
	public int getErrorCountThreshold() {
		return errorCountThreshold;
	}
	@Override
	public void setErrorCountThreshold(int errorCountBaseLine) {
		this.errorCountThreshold = errorCountBaseLine;
		changed();
	}

	@Override
	public float getErrorPercentThreshold() {
		return errorPercentThreshold;
	}
	@Override
	public void setErrorPercentThreshold(float errorPercent) {
		this.errorPercentThreshold = errorPercent;
		changed();
	}

	@Override
	public String getMySqlErrorCodes() {
		return mySqlErrorCodes;
	}
	@Override
	public void setMySqlErrorCodes(String mySqlErrorCodes) {
		mysqlTimeoutMarkdownCodes = parseErrorCodes(mySqlErrorCodes);
		this.mySqlErrorCodes = mySqlErrorCodes;
		changed();
	}

	@Override
	public int getErrorPercentReferCount() {
		return errorPercentReferCount;
	}
	@Override
	public void setErrorPercentReferCount(int errorPercentBaseLine) {
		this.errorPercentReferCount = errorPercentBaseLine;
		changed();
	}

	@Override
	public String getSqlServerErrorCodes() {
		return sqlServerErrorCodes;
	}
	@Override
	public void setSqlServerErrorCodes(String sqlServerErrorCodes) {
		sqlServerTimeoutMarkdownCodes = parseErrorCodes(sqlServerErrorCodes);
		this.sqlServerErrorCodes = sqlServerErrorCodes;
		changed();
	}

	@Override
	public int getTimeoutThreshold() {
		return timeoutThreshold;
	}
	@Override
	public void setTimeoutThreshold(int minTimeOut) {
		this.timeoutThreshold = minTimeOut;
		changed();
	}

	public Set<Integer> getMysqlTimeoutMarkdownCodes() {
		return mysqlTimeoutMarkdownCodes;
	}
	public Set<Integer> getSqlServerTimeoutMarkdownCodes() {
		return sqlServerTimeoutMarkdownCodes;
	}	
	
	private Set<Integer> parseErrorCodes(String codes){
		Set<Integer> temp = new HashSet<Integer>();
		if(codes == null || codes.isEmpty()) {
            return temp;
        }
		String[] tokens = codes.split(",");
		for (String token : tokens) {
			temp.add(Integer.valueOf(token));
		}
		return temp;
	}
}
