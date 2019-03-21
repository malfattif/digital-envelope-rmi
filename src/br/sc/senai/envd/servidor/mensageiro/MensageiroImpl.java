package br.sc.senai.envd.servidor.mensageiro;

/**
 * Classe principal da solução de Mensagens cliente-servidor, utilizando criptografia
 * no modelo de envelopamento digital. W3-Security.
 * Esta classe implementa os métodos da interface mensageiro, utilizando-se da 
 * classe de trabalho Cripto_Server.
 * A classe está desenvolvida como um Sigletron para controlar o disparo de serviços
 * através de classes de serviços baseadas em Threads.
 * 
 */
import br.sc.senai.endv.servidor.fila.GravarFilas;
import br.sc.senai.endv.servidor.fila.LeitorFila;
import br.sc.senai.envd.servidor.cripto.Cripto_Server;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class MensageiroImpl extends UnicastRemoteObject implements Mensageiro {
    
    private static Map mapFilas = Collections.synchronizedMap(new HashMap<String,ArrayList>()); 
    private static MensageiroImpl mensageiroImpl = null;
    private static byte[] bytesChaveSimetrica = null;

    private MensageiroImpl() throws RemoteException {
	super();
    }

    /**
    * Método para ler mensagens de uma fila previamente gravada.
    * @param nomeFila
    * @return ArrayList com as mensagens.
    * @throws RemoteException 
    */
    @Override
        public ArrayList lerFila(byte[] nomeFila) throws RemoteException{
            
            synchronized(this){
                ArrayList listaMensagens = new ArrayList();
                ArrayList listaMsgCripto = new ArrayList();
                String nomeFilaDecriptado = new String(Cripto_Server.decriptarComChaveSimetrica(nomeFila, bytesChaveSimetrica));
                LeitorFila lerFila = new LeitorFila(nomeFilaDecriptado, mapFilas, listaMensagens);
                lerFila.setPriority(Thread.NORM_PRIORITY);
                lerFila.setName(nomeFilaDecriptado);
                lerFila.start();
                if (lerFila.getName().equalsIgnoreCase(nomeFilaDecriptado)){
                    while (lerFila.isAlive()){
                        lerFila.getState();
                    }
                }
                for (int i=0;i<listaMensagens.size();i++){
                    String textoPuro = (String) listaMensagens.get(i);
                    listaMsgCripto.add(i, Cripto_Server.encriptarComChaveSimetrica(textoPuro.getBytes(), bytesChaveSimetrica));
               }
                return listaMsgCripto;
            }
        }
    
    /**
     * Método para gravar dados numa fila de mensagens.
     * @param nomeFila e MSG
     * @param mensagem
     * @return 
     */
    @Override
    public byte[] gravarMensagemEncriptadaNaFila(byte[] nomeFila, byte[] mensagem){
        synchronized(this){
            String mensagemDecriptada = new String(Cripto_Server.decriptarComChaveSimetrica(mensagem, bytesChaveSimetrica));
            String nomeFilaDecriptado = new String(Cripto_Server.decriptarComChaveSimetrica(nomeFila,bytesChaveSimetrica));
            
            GravarFilas gravarFilas=  new GravarFilas(nomeFilaDecriptado,mapFilas,mensagemDecriptada);
            gravarFilas.setPriority(Thread.MAX_PRIORITY);
            gravarFilas.start();
            
             String mensagemRetornoGravar = "[Servidor] Mensagem gravada na: " + nomeFilaDecriptado;
            
            return Cripto_Server.encriptarComChaveSimetrica(mensagemRetornoGravar.getBytes(), bytesChaveSimetrica);
        }
    }
    
    /**
     * Método para excluir (deletar) uma fila de mensagens.
     * @param nomeFilaG
     * @return mensagem de exclusão.
     */
    @Override
    public byte[] deletarFila(byte[] nomeFilaG){
        synchronized(this){
            String mensagemRetornoDeletar;
            String nomeFilaP = new String(Cripto_Server.decriptarComChaveSimetrica(nomeFilaG,bytesChaveSimetrica));
            if (mapFilas.containsKey(nomeFilaP)){
                mapFilas.remove(nomeFilaP);
                mensagemRetornoDeletar = "[Servidor] " + nomeFilaP + " Excluída com sucesso!";
            } else {
                mensagemRetornoDeletar = "[Servidor] " + nomeFilaP + " Não EXISTE!";                 
            }
            return Cripto_Server.encriptarComChaveSimetrica(mensagemRetornoDeletar.getBytes(), bytesChaveSimetrica);
        }
    }
    
    /**
     * Método para retornar a chave Pública do Keystore e disponibilizá-la via serviço
     * apresentado aos clientes por intermédio da interface mensageiro.
     * @return
     * @throws RemoteException 
     */
    @Override
    public PublicKey obterChavePublicaDoServidor() throws RemoteException {
        try {
            Cripto_Server criptoServer = new Cripto_Server();
            return criptoServer.getPublicKeyFromFile();
        } catch (Exception ex) {
            Logger.getLogger(MensageiroImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }
    
    /**
     * Método para inicializar a variável global chaveSim, com o valor de uma chave
     * recebida do cliente que deseja se comunicar com o servidor de mensagens.
     * @param bytesChaveSimetricaFromClient
     * @return 
     */
    public boolean gravarChaveSimetricaNoServidor(byte[] bytesChaveSimetricaFromClient) {
        try {
            Cripto_Server krp = new Cripto_Server();            
            PrivateKey chavepriv = krp.getPrivateKeyFromFile();
            this.bytesChaveSimetrica = Cripto_Server.decriptarComChavePrivada(bytesChaveSimetricaFromClient,chavepriv);
            return true;           
        } catch (Exception ex) {
            Logger.getLogger(MensageiroImpl.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    /**
     * Método para obtenção de uma instância desta classe
     * 
     * @return 
     */
     
    public static synchronized MensageiroImpl getinstance() {
        
        if(Cripto_Server.isSenhaKeyStoreNaoInformada()){
            JPasswordField passwordField = new JPasswordField();
            Integer option = JOptionPane.showConfirmDialog(null, passwordField, "Informe a senha", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if ((option == JOptionPane.CANCEL_OPTION)) {
                System.exit(1);
            }
            if(passwordField.getPassword().length == 0){
                throw new RuntimeException("Senha nao informada!");
            }
            Cripto_Server.setPassword(new String(passwordField.getPassword()));
        }
        
        if(mensageiroImpl != null){
            return mensageiroImpl;
        }
        try {
            mensageiroImpl = new MensageiroImpl();
            return mensageiroImpl;
        } catch (RemoteException ex) {
            Logger.getLogger(MensageiroImpl.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro grave: " + ex.getMessage());
            return null;
        }
    }
}
