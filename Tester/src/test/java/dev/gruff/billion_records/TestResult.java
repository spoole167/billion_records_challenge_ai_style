package dev.gruff.billion_records;

import java.util.HashMap;
import java.util.Map;

public enum TestResult {PASS(1), OOM(2), ERROR(3), INCORRECT(4), FAIL(7), TIMEOUT(8),UNKNOWN(9);

    private static final Map<Integer,TestResult> codes=new HashMap<>();
    static{
        for(TestResult r:values())codes.put(r.code,r);
    }
    private int code;
   private TestResult(int i) {
        this.code=i;
    }

    public int code() {

        return code;
    }

    public static TestResult result(int exitCode) {


       TestResult tr=codes.get(exitCode);
       if(exitCode==56) tr=OOM;
       if(tr==null) tr=UNKNOWN;

       return tr;
    }
}
