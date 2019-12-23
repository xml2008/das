package com.ppdai.das.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface MGRConfigReader {

   Map<String, List<DasConfigure.MGRInfo>> readMGRConfig();
}
