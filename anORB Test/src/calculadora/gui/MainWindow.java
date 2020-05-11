package calculadora.gui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import calculadora.Divisao;
import calculadora.DivisaoPorZeroException;
import calculadora.Multiplicacao;
import calculadora.Soma;
import calculadora.Subtracao;

import anorb.AnORB;
import anorb.AnRemoteException;
import anorb.namingservice.NamingService;

public class MainWindow {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="20,13"
	private Text tDisplay = null;
	private Button b9 = null;
	private Button b8 = null;
	private Button b7 = null;
	private Button b6 = null;
	private Button b5 = null;
	private Button b4 = null;
	private Button b3 = null;
	private Button b2 = null;
	private Button b1 = null;
	private Button b0 = null;
	private Button bDivisao = null;
	private Button bMultiplicacao = null;
	private Button bSoma = null;
	private Button bSubtracao = null;
	private Button bIgual = null;
	private Button bInverteSinal = null;
	private Button bVirgula = null;
	private Button cClear = null;

	private double a, b;
	private char operacao; 
	private Soma soma;
	private Subtracao subtracao;
	private Multiplicacao multiplicacao;
	private Divisao divisao;
	
	public MainWindow(Soma soma, Subtracao subtracao, Multiplicacao multiplicacao, Divisao divisao) {
		this.soma = soma;
		this.subtracao = subtracao;
		this.multiplicacao = multiplicacao;
		this.divisao = divisao;
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("AnCalculator");
		sShell.setSize(new org.eclipse.swt.graphics.Point(194,277));
		tDisplay = new Text(sShell, SWT.BORDER | SWT.RIGHT);
		tDisplay.setBounds(new org.eclipse.swt.graphics.Rectangle(7,8,170,19));
		tDisplay.setText("0");
		b9 = new Button(sShell, SWT.NONE);
		b9.setText("9");
		b9.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b9.setLocation(new org.eclipse.swt.graphics.Point(101,37));
		b9.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b9.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b9.getText());
				}
			}
		});
		b8 = new Button(sShell, SWT.NONE);
		b8.setText("8");
		b8.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b8.setLocation(new org.eclipse.swt.graphics.Point(56,37));
		b8.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b8.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b8.getText());
				}

			}
		});
		b7 = new Button(sShell, SWT.NONE);
		b7.setText("7");
		b7.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b7.setLocation(new org.eclipse.swt.graphics.Point(11,37));
		b7.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b7.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b7.getText());
				}
			}
		});
		b6 = new Button(sShell, SWT.NONE);
		b6.setText("6");
		b6.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b6.setLocation(new org.eclipse.swt.graphics.Point(101,82));
		b6.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b6.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b6.getText());
				}
			}
		});
		b5 = new Button(sShell, SWT.NONE);
		b5.setText("5");
		b5.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b5.setLocation(new org.eclipse.swt.graphics.Point(56,82));
		b5.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b5.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b5.getText());
				}
			}
		});
		b4 = new Button(sShell, SWT.NONE);
		b4.setText("4");
		b4.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b4.setLocation(new org.eclipse.swt.graphics.Point(11,82));
		b4.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b4.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b4.getText());
				}
			}
		});
		b3 = new Button(sShell, SWT.NONE);
		b3.setText("3");
		b3.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b3.setLocation(new org.eclipse.swt.graphics.Point(11,127));
		b3.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b3.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b3.getText());
				}
			}
		});
		b2 = new Button(sShell, SWT.NONE);
		b2.setText("2");
		b2.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b2.setLocation(new org.eclipse.swt.graphics.Point(56,127));
		b2.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b2.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b2.getText());
				}
			}
		});
		b1 = new Button(sShell, SWT.NONE);
		b1.setText("1");
		b1.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b1.setLocation(new org.eclipse.swt.graphics.Point(101,127));
		b1.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b1.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b1.getText());
				}
			}
		});
		b0 = new Button(sShell, SWT.NONE);
		b0.setText("0");
		b0.setSize(new org.eclipse.swt.graphics.Point(25,25));
		b0.setLocation(new org.eclipse.swt.graphics.Point(11,172));
		b0.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (tDisplay.getText().equals("0")){
					tDisplay.setText(b0.getText());
				} else {
					tDisplay.setText(tDisplay.getText() + b0.getText());
				}
			}
		});
		bDivisao = new Button(sShell, SWT.NONE);
		bDivisao.setText("/");
		bDivisao.setSize(new org.eclipse.swt.graphics.Point(25,25));
		bDivisao.setLocation(new org.eclipse.swt.graphics.Point(146,37));
		bDivisao.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				a = Double.parseDouble(tDisplay.getText());
				tDisplay.setText("0");
				operacao = bDivisao.getText().charAt(0);
			}
		});
		bMultiplicacao = new Button(sShell, SWT.NONE);
		bMultiplicacao.setText("*");
		bMultiplicacao.setSize(new org.eclipse.swt.graphics.Point(25,25));
		bMultiplicacao.setLocation(new org.eclipse.swt.graphics.Point(146,82));
		bMultiplicacao
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
						a = Double.parseDouble(tDisplay.getText());
						tDisplay.setText("0");
						operacao = bMultiplicacao.getText().charAt(0);
					}
				});
		bSoma = new Button(sShell, SWT.NONE);
		bSoma.setText("+");
		bSoma.setSize(new org.eclipse.swt.graphics.Point(25,25));
		bSoma.setLocation(new org.eclipse.swt.graphics.Point(146,127));
		bSoma.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				a = Double.parseDouble(tDisplay.getText());
				tDisplay.setText("0");
				operacao = bSoma.getText().charAt(0);
			}
		});
		bSubtracao = new Button(sShell, SWT.NONE);
		bSubtracao.setText("-");
		bSubtracao.setSize(new org.eclipse.swt.graphics.Point(25,25));
		bSubtracao.setLocation(new org.eclipse.swt.graphics.Point(146,172));
		bSubtracao.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				a = Double.parseDouble(tDisplay.getText());
				tDisplay.setText("0");
				operacao = bSubtracao.getText().charAt(0);
			}
		});
		bIgual = new Button(sShell, SWT.NONE);
		bIgual.setText("=");
		bIgual.setSize(new org.eclipse.swt.graphics.Point(115,25));
		bIgual.setLocation(new org.eclipse.swt.graphics.Point(56,217));
		bIgual.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				try {
					onIgualClicked();
				} catch (AnRemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		bInverteSinal = new Button(sShell, SWT.NONE);
		bInverteSinal.setText("+/-");
		bInverteSinal.setSize(new org.eclipse.swt.graphics.Point(25,25));
		bInverteSinal.setLocation(new org.eclipse.swt.graphics.Point(56,172));
		bInverteSinal
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
						if (!tDisplay.getText().equals("0")){
							if (tDisplay.getText().startsWith("-")){
								tDisplay.setText(tDisplay.getText().replaceFirst("-", ""));
							} else {
								tDisplay.setText("-" + tDisplay.getText());
							}
						}
					}
				});
		bVirgula = new Button(sShell, SWT.NONE);
		bVirgula.setText(",");
		bVirgula.setSize(new org.eclipse.swt.graphics.Point(25,25));
		bVirgula.setLocation(new org.eclipse.swt.graphics.Point(101,172));
		bVirgula.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				tDisplay.setText(tDisplay.getText() + ",");
			}
		});
		cClear = new Button(sShell, SWT.NONE);
		cClear.setText("C");
		cClear.setSize(new org.eclipse.swt.graphics.Point(25,25));
		cClear.setLocation(new org.eclipse.swt.graphics.Point(11,217));
		cClear.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				tDisplay.setText("0");
			}
		});
	}

	protected void onIgualClicked() throws AnRemoteException {
		b = Double.parseDouble(tDisplay.getText());
		
		switch (operacao) {
		case '/':
			try {
					a = divisao.dividir(a, b);
				} catch (DivisaoPorZeroException e) {
					MessageBox mb = new MessageBox(sShell);
					mb.setText("Exceção!");
					mb.setMessage(e.getMessage());
					mb.open();
					a = b = 0;					
				}
			break;
		case '*':
			a = multiplicacao.multiplicar(a, b);
			break;
		case '-':
			a = subtracao.subtrair(a, b);
			break;
		case '+':
			a = soma.somar(a, b);
			break;
		default:
			a = b;
			break;
		}
		
		tDisplay.setText(Double.toString(a));
	}

	public static void main(String[] args) throws IOException {
		AnORB orb = AnORB.init(2156, "localhost", 2178);		
		NamingService ns = orb.getNamingService();
		
		Soma soma = (Soma) ns.lookup("Soma");
		Subtracao subtracao = (Subtracao) ns.lookup("Subtracao");
		Multiplicacao multiplicacao = (Multiplicacao) ns.lookup("Multiplicacao");
		Divisao divisao = (Divisao) ns.lookup("Divisao");
		
		Display display = Display.getDefault();
		MainWindow thisClass = new MainWindow(soma, subtracao, multiplicacao, divisao);
		thisClass.createSShell();
		thisClass.testOperations();
		thisClass.sShell.open();

		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private void testOperations() {
		bSoma.setEnabled(soma != null);
		bSubtracao.setEnabled(subtracao != null);
		bMultiplicacao.setEnabled(multiplicacao != null);
		bDivisao.setEnabled(divisao != null);
	}
}
