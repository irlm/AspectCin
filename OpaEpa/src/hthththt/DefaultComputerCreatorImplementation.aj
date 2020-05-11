package hthththt;

public aspect DefaultComputerCreatorImplementation {

	public void ComputerCreator.createComputerAndPrintInventory(String serial) {
		System.out.println("Inventory of computerparts:");
		System.out.println(this.createComputer(serial).toString());
	}
}
