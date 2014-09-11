package message;

import migratableprocess.MigratableProcess;

public class LaunchRequest extends RequestMessage {

	private static final long serialVersionUID = 5796312885553857386L;
	private MigratableProcess mProcess;
	
	public LaunchRequest(MigratableProcess process) {
		mProcess = process;
	}

	@Override
	public boolean isLaunch() {
		return true;
	}
	
	public MigratableProcess getProcess() {
		return mProcess;
	}

}
