package br.sc.senai.envd.cliente.mensageiro;

/**
 * Classe que implementa os métodos do Cliente no sistema de mensagens. Esta
 * classe implementa os métodos necessários para a comunicação segura com o
 * servidor, através do uso da criptografia forte com RSA e criptografia
 * simétrica o algoritmo AES A solução implementa o conceito de envelopamento
 * digital no padrão W3-security.
 */
import br.sc.senai.envd.servidor.mensageiro.Mensageiro;
import br.sc.senai.envd.cliente.cripto.Cripto_Cliente;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;

public class MensageiroCliente {

    private static final String servidorRMI = "localhost";
    private static String nomeFila = "";
    private static String mensagem = "";
    private static ArrayList listaMensagens = new ArrayList();
    private static final Integer numeroFilas = 2;
    private static final Integer numeroMensagens = 2;
    private static byte[] chaveSimetrica = null;
    private static PublicKey chavePublica = null;

    public static void main(String args[]) {
        try {
            Mensageiro mensageiroServer = (Mensageiro) Naming.lookup("rmi://" + servidorRMI + "/ServicoEnvelopeDigital");

            chaveSimetrica = Cripto_Cliente.obterChaveSimetrica();
            chavePublica = mensageiroServer.obterChavePublicaDoServidor();

            byte[] chaveSimetricaEncriptada = Cripto_Cliente.encriptarComChavePublica(chaveSimetrica, chavePublica);

            if (!mensageiroServer.gravarChaveSimetricaNoServidor(chaveSimetricaEncriptada)) {
                System.out.println("Erro ao enviar chave simétrica ao servidor!");
                System.exit(1);
            }
     
            // Cria as filas e mensagens, encripta e grava no servidor                     
            for (int i = 1; i <= numeroFilas; i++) {
                nomeFila = "Fila [" + i + "]";
                for (int k = 1; k <= numeroMensagens; k++) {
                    mensagem = " Mensagem nr: " + k;
                    
                    byte[] bytesMensagemEncriptado = Cripto_Cliente.encriptarComChaveSimetrica(mensagem.getBytes(), chaveSimetrica);
                    byte[] bytesNomeFilaEncriptado = Cripto_Cliente.encriptarComChaveSimetrica(nomeFila.getBytes(), chaveSimetrica);
                    
                    byte[] retornoGravarEncriptado = mensageiroServer.gravarMensagemEncriptadaNaFila(bytesNomeFilaEncriptado, bytesMensagemEncriptado);
                    String retornoGravarDecriptado = new String(Cripto_Cliente.decriptarComChaveSimetrica(retornoGravarEncriptado, chaveSimetrica));
                    
                    System.out.println(retornoGravarDecriptado);
                }
            }
                     
            // Define os nomes das filas, encripta e consulta no servidor
            for (int j = 1; j <= numeroFilas; j++) {
                nomeFila = "Fila [" + j + "]";
                byte[] bytesNomeFilaCriptografado = Cripto_Cliente.encriptarComChaveSimetrica(nomeFila.getBytes(), chaveSimetrica);
                listaMensagens = mensageiroServer.lerFila(bytesNomeFilaCriptografado);
                System.out.print(nomeFila);
                byte[] bytesMensagemEncriptado = null;
                Iterator iterator = listaMensagens.iterator();
                System.out.print(" - msg decriptadas: ");
                while (iterator.hasNext()) {
                    bytesMensagemEncriptado = (byte[]) iterator.next();
                    String mensagemDecriptada = new String(Cripto_Cliente.decriptarComChaveSimetrica(bytesMensagemEncriptado, chaveSimetrica));
                    System.out.print("[" + mensagemDecriptada + "]");
                }
                System.out.print("\n");
            }
            
            // Define as filas, encripta e solicita a exclusão do servidor
            for (int k = 1; k <= numeroFilas; k++) {
                nomeFila = "Fila [" + k + "]";
                byte[] bytesNomeFilaEncriptado = Cripto_Cliente.encriptarComChaveSimetrica(nomeFila.getBytes(), chaveSimetrica);
                byte[] retornoDeletarEncriptado = mensageiroServer.deletarFila(bytesNomeFilaEncriptado);
                
                String retornoDeletarDecriptado = new String(Cripto_Cliente.decriptarComChaveSimetrica(retornoDeletarEncriptado, chaveSimetrica));
                System.out.println("Excluíndo: " + nomeFila + " " + retornoDeletarDecriptado);
            }
            
        } catch (MalformedURLException e) {
            System.out.println();
            System.out.println("MalformedURLException: " + e.toString());
        } catch (RemoteException e) {
            System.out.println();
            System.out.println("RemoteException: " + e.toString());
        } catch (NotBoundException e) {
            System.out.println();
            System.out.println("NotBoundException: " + e.toString());
        } catch (Exception e) {
            System.out.println();
            System.out.println("Exception: " + e.getCause());
        }
    }
}
