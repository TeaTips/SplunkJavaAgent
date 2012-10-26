package com.splunk.javaagent.test;

public class TestProgram {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Executing Test Program");
		TestProgram test = new TestProgram();
		test.zoo();
		test.boo();

		try {
			int f = 1;
			System.out.println(f);
			throw new Exception("foo");
		} catch (Exception e) {

		} catch (Error e) {

		}

	}

	private void zoo() {
		System.out.println("Executing zoo()");
		try {
			Thread.sleep(1000);
			new Foo().foo();
		} catch (InterruptedException e) {

		}
	}

	private void boo() {
		System.out.println("Executing boo()");
		try {
			Thread.sleep(500);
			Goo g = new Goo();
			g.goo();
		} catch (InterruptedException e) {

		}
	}

}
