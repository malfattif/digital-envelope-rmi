package com.cripto.envd.cliente.mensageiro;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cripto.envd.cliente.cripto.CriptoClient;
import com.cripto.envd.servidor.mensageiro.Mensageiro;

public class MensageiroCliente {

    private static final String servidorRMI = "localhost";
    private static String nomeFila = "";
    private static String mensagem = "";
    private static List listaMensagens = new ArrayList();
    private static final Integer numeroFilas = 2;
    private static final Integer numeroMensagens = 2;
    private static byte[] chaveSimetrica = null;
    private static PublicKey chavePublica = null;

    public static void main(String args[]) {
        try {
            Mensageiro mensageiroServer = (Mensageiro) Naming.lookup("rmi://" + servidorRMI + "/ServicoEnvelopeDigital");

            chaveSimetrica = CriptoClient.obterChaveSimetrica();
            chavePublica = mensageiroServer.obterChavePublicaDoServidor();

            byte[] chaveSimetricaEncriptada = CriptoClient.encriptarComChavePublica(chaveSimetrica, chavePublica);

            if (!mensageiroServer.gravarChaveSimetricaNoServidor(chaveSimetricaEncriptada)) {
                System.out.println("Erro ao enviar chave simétrica ao servidor!");
                System.exit(1);
            }
     
            // Cria as filas e mensagens, encripta e grava no servidor                     
            for (int i = 1; i <= numeroFilas; i++) {
                nomeFila = "Fila [" + i + "]";
                for (int k = 1; k <= numeroMensagens; k++) {
                    mensagem = " Mensagem nr: " + k;
                    
                    byte[] bytesMensagemEncriptado = CriptoClient.encriptarComChaveSimetrica(mensagem.getBytes(), chaveSimetrica);
                    byte[] bytesNomeFilaEncriptado = CriptoClient.encriptarComChaveSimetrica(nomeFila.getBytes(), chaveSimetrica);
                    
                    byte[] retornoGravarEncriptado = mensageiroServer.gravarMensagemEncriptadaNaFila(bytesNomeFilaEncriptado, bytesMensagemEncriptado);
                    String retornoGravarDecriptado = new String(CriptoClient.decriptarComChaveSimetrica(retornoGravarEncriptado, chaveSimetrica));
                    
                    System.out.println(retornoGravarDecriptado);
                }
            }
                     
            // Define os nomes das filas, encripta e consulta no servidor
            for (int j = 1; j <= numeroFilas; j++) {
                nomeFila = "Fila [" + j + "]";
                byte[] bytesNomeFilaCriptografado = CriptoClient.encriptarComChaveSimetrica(nomeFila.getBytes(), chaveSimetrica);
                listaMensagens = mensageiroServer.lerFila(bytesNomeFilaCriptografado);
                System.out.print(nomeFila);
                byte[] bytesMensagemEncriptado = null;
                Iterator iterator = listaMensagens.iterator();
                System.out.print(" - msg decriptadas: ");
                while (iterator.hasNext()) {
                    bytesMensagemEncriptado = (byte[]) iterator.next();
                    String mensagemDecriptada = new String(CriptoClient.decriptarComChaveSimetrica(bytesMensagemEncriptado, chaveSimetrica));
                    System.out.print("[" + mensagemDecriptada + "]");
                }
                System.out.print("\n");
            }
            
            // Define as filas, encripta e solicita a exclusão do servidor
            for (int k = 1; k <= numeroFilas; k++) {
                nomeFila = "Fila [" + k + "]";
                byte[] bytesNomeFilaEncriptado = CriptoClient.encriptarComChaveSimetrica(nomeFila.getBytes(), chaveSimetrica);
                byte[] retornoDeletarEncriptado = mensageiroServer.deletarFila(bytesNomeFilaEncriptado);
                
                String retornoDeletarDecriptado = new String(CriptoClient.decriptarComChaveSimetrica(retornoDeletarEncriptado, chaveSimetrica));
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
