package dbs.branches.comm;

public class BodySingleString implements MessageBody {

	private String str1;

	public BodySingleString(String str1) {
		this.str1 = str1;
	}

	public synchronized String getStr1() {
		return str1;
	}

	public synchronized void setStr1(String str1) {
		this.str1 = str1;
	}

}
