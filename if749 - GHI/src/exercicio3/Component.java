package exercicio3;

public interface Component extends Runnable {

	boolean init(ComponentRepository repository, Object... parameters);

	void resume();

	void suspend();

	String info();

	void fini();
}
