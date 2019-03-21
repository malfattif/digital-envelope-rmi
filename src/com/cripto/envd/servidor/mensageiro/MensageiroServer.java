package com.cripto.envd.servidor.mensageiro;

/**
 * Classe servidora que inicia o serviço RMI, disponibilizando o objeto da
 * classe MensageiroImpl, no rmiregistry onde a classe clinte pode consumir
 * os métodos
 */
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MensageiroServer {
	private static final String SERVIDOR_RMI = "localhost";
	private static final Integer PORTA_RMI = 1099;

	public MensageiroServer() {
		try {
			Mensageiro mensageiro = MensageiroImpl.getinstance();
			Registry registry = LocateRegistry.createRegistry(PORTA_RMI);
			registry = LocateRegistry.getRegistry(SERVIDOR_RMI, PORTA_RMI);
			registry.bind("ServicoEnvelopeDigital", mensageiro);

			System.out.println("MensageiroServer - Servidor RMI iniciado na porta 1099\n"
					+ "Servico Envelope com criptografia RSA e ARC4 no ar!\n");
		} catch (Exception e) {
			System.out.println("Erro no servidor: " + e + "\n Servidor saindo..");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		new MensageiroServer();
	}
}