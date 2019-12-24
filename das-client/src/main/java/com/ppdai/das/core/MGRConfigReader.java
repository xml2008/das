package com.ppdai.das.core;

import java.util.List;
import java.util.Map;

public interface MGRConfigReader {

   Map<String, List<DasConfigure.MGRInfo>> readMGRConfig();

   long mgrValidate(String connectionString);
}
