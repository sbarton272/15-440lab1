package message;

import migratableprocess.MigratableProcess;

public class LaunchMessage implements Message{

	private static final long serialVersionUID = 5796312885553857386L;
	private MigratableProcess mProcess;
	
	public LaunchMessage(MigratableProcess process) {
		mProcess = process;
	}

	public MigratableProcess getProcess() {
		return mProcess;
	}

}
