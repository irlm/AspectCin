package exercicio3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

public class Configurator {

	private ComponentRepository repository = new ComponentRepository();

	public static void main(String[] args) throws IOException {

		System.out
				.println("----------------------------------------------------------");
		System.out.println("IF749 - Exercicio 3");
		System.out
				.println("----------------------------------------------------------");
		System.out.println("start [SERVICE NAME] [PARAM1] [PARAM2]...");
		System.out.println("resume [SERVICE NAME]");
		System.out.println("suspend [SERVICE NAME]");
		System.out.println("stop [SERVICE NAME]");
		System.out.println("list");
		System.out.println("exit");
		System.out
				.println("----------------------------------------------------------");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Configurator configurator = new Configurator();

		System.out.print(">");
		String input = br.readLine();
		while (!input.equals("exit")) {
			StringTokenizer st = new StringTokenizer(input, " ");
			if (st.countTokens() > 0) {
				String command = st.nextToken();
				if (command.equals("start")) {
					String service = st.nextToken();
					System.out.print("Starting " + service + "...");
					Object[] parameters = null;
					int count = st.countTokens();
					if (count > 0) {
						parameters = new String[count];
						for (int i = 0; i < count; i++) {
							parameters[i] = st.nextToken();
						}
					}
					try {
						configurator.start(service, parameters);
						System.out.println("OK");
					} catch (Exception e) {
						System.out
								.println(" Error: " + e.getLocalizedMessage());
					}
				} else if (command.equals("resume")) {
					String service = st.nextToken();
					System.out.print("Resuming " + service + "...");
					if (configurator.resume(service)) {
						System.out.println("OK");
					} else {
						System.out.println(" Error: service not found.");
					}
				} else if (command.equals("suspend")) {
					String service = st.nextToken();
					System.out.print("Suspending " + service + "...");

					if (configurator.suspend(service)) {
						System.out.println("OK");
					} else {
						System.out.println(" Error: service not found.");
					}
				} else if (command.equals("stop")) {
					String service = st.nextToken();
					System.out.print("Stopping " + service + "...");
					if (configurator.stop(service)) {

						System.out.println("OK");
					} else {
						System.out.println(" Error: service not found.");
					}
				} else if (command.equals("list")) {
					List<Component> components = configurator.getComponents();
					System.out.println(components.size()
							+ " component(s) running.");
					for (Component c : components) {
						System.out.println("Name: " + c.getClass().getName());
						System.out.println("Info: " + c.info());
					}
				}
			}
			System.out.print(">");
			input = br.readLine();
		}

		configurator.shutdown();

		System.out.println("Bye");
	}

	private List<Component> getComponents() {
		return repository.getComponents();
	}

	public boolean stop(String service) {
		try {
			Component component = repository.getComponent(service);
			if (component == null)
				return false;
			repository.remove(component);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	public boolean resume(String service) {
		try {
			Component component = repository.getComponent(service);
			if (component == null)
				return false;
			component.resume();
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	public boolean suspend(String service) {
		try {
			Component component = repository.getComponent(service);
			if (component == null)
				return false;
			component.suspend();
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	public void shutdown() {
		repository.shutdown();
	}

	public void start(String service, Object[] parameters)
			throws RuntimeException {
		repository.insert(service, parameters);
	}

}
