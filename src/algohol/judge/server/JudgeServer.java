package algohol.judge.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import julius.judge.JudgeResult;
import julius.judge.TestcaseResult;
import julius.judge.runtime.RuntimeStatus;
import algohol.com.DataIOInterface;
import algohol.judge.JudgeProcessor;
import algohol.judge.data.ProblemTestDataManager;
import algohol.judge.data.raw.RawCode;
import algohol.judge.data.raw.RawProblemTestData;

public class JudgeServer implements Runnable{
	private ProblemTestDataManager dataManager;
	private JudgeProcessor processor;
	private ServerSocket server;
	
	public JudgeServer(ProblemTestDataManager dataManager, JudgeProcessor processor, int port) throws IOException{
		this.dataManager = dataManager;
		this.processor = processor;
		this.server = new ServerSocket(port);
	}
	
	@Override
	public void run(){
		while(true){
			Socket con = null;
			DataIOInterface io = null;
			try{
				con = server.accept();
				io = new DataIOInterface(con.getInputStream(), con.getOutputStream());
				String id = io.readLine();
				String md5 = io.readLine();
				if(!dataManager.isExistent(id) || !md5.equals(dataManager.getHashVal(id))){//asks the client to send the data
					io.writeLine("S");
					dataManager.updateProblemTestData(id, new RawProblemTestData(io));
				} else
					io.writeLine("O");
				RawCode code = new RawCode(io);
				try{
					JudgeResult res = processor.judge(code, dataManager.getData(id));
					io.writeLine("R");
					io.writeLine(String.valueOf(res.getTotalScore()));
					io.writeLine(res.getCompileResult().isCompileOk() ? "O" : "F");
					io.writeBytes(res.getCompileResult().getCompileInfo().getBytes());
					if(res.getCompileResult().isCompileOk()){
						TestcaseResult[] tres = res.getTestcaseResults();
						for(int i = 0; i < tres.length; i ++){
							io.writeLine(String.valueOf(tres[i].getScore()));
							io.writeLine(tres[i].getRuntimeResult().getStatus().toString());
							io.writeLine(String.valueOf(tres[i].getRuntimeResult().getTimeUsed()));
							io.writeLine(String.valueOf(tres[i].getRuntimeResult().getMemoryUsed()));
							if(tres[i].getRuntimeResult().getStatus() == RuntimeStatus.NORMAL)
								io.writeBytes(tres[i].getCheckResult().getInfo().getBytes());
						}
					}
					
				} catch(Exception e){
					io.writeLine("F");
				}
			} catch(IOException e){
				e.printStackTrace();
			} finally{
				try{
					if(io != null)
						io.close();
					if(con != null)
						con.close();
				} catch(IOException e){}
			}
		}
	}
}
