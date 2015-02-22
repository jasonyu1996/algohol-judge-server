package algohol.judge;

import java.io.File;
import java.io.IOException;

import julius.judge.JudgeResult;
import julius.judge.Judger;
import julius.judge.check.DefaultComparator;
import julius.judge.check.DiffChecker;
import julius.judge.compile.ExternalCompiler;
import julius.judge.runtime.Sandbox;
import julius.judge.runtime.SandboxNotReadyException;
import julius.org.Pattern;
import julius.org.Testcase;
import algohol.judge.data.ProblemTestData;
import algohol.judge.data.TestcaseData;
import algohol.judge.data.raw.RawCode;

public class JudgeProcessor {
	private File dir;
	private Sandbox sandbox;
	
	public JudgeProcessor(File dir, Sandbox sandbox){
		dir = this.dir;
	}
	
	public JudgeResult judge(RawCode code, ProblemTestData data) throws IOException, SandboxNotReadyException{
		File source = code.writeToFile(new File(dir, "code").toString());
		TestcaseData[] tdata = data.getTestcases();
		Testcase[] testcases = new Testcase[tdata.length];
		for(int i = 0; i < tdata.length; i ++)
			testcases[i] = getTestcase(tdata[i]);
		if(data.isRedirect())
			return Judger.judge(source, testcases, new ExternalCompiler(new Pattern("g++ % -o %<")), sandbox, new Pattern("./%<"), data.getInput(), data.getOutput(), true);
		return Judger.judge(source, testcases, new ExternalCompiler(new Pattern("g++ % -o %<")), sandbox, new Pattern("./%<"), "test.in", "test.out", false);
	}
	
	private Testcase getTestcase(TestcaseData tdata){
		return new Testcase(tdata.getIn(), tdata.getOut(), 1, 
				new DiffChecker(new DefaultComparator()), tdata.getTimeLimit(), tdata.getMemoryLimit());
	}
}
