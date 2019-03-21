package com.cripto.envd.servidor.mensageiro;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import com.cripto.endv.servidor.fila.GravarFilas;
import com.cripto.endv.servidor.fila.LeitorFila;
import com.cripto.envd.servidor.cripto.CriptoServer;

public class MensageiroImpl extends UnicastRemoteObject implements Mensageiro {
    
    private static Map mapFilas = Collections.synchronizedMap(new HashMap<String,List>()); 
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
        public List lerFila(byte[] nomeFila) throws RemoteException{
            
            synchronized(this){
            	List listaMensagens = new ArrayList<>();
            	List listaMsgCripto = new ArrayList<>();
                String nomeFilaDecriptado = new String(CriptoServer.decriptarComChaveSimetrica(nomeFila, bytesChaveSimetrica));
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
                    listaMsgCripto.add(i, CriptoServer.encriptarComChaveSimetrica(textoPuro.getBytes(), bytesChaveSimetrica));
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
            String mensagemDecriptada = new String(CriptoServer.decriptarComChaveSimetrica(mensagem, bytesChaveSimetrica));
            String nomeFilaDecriptado = new String(CriptoServer.decriptarComChaveSimetrica(nomeFila,bytesChaveSimetrica));
            
            GravarFilas gravarFilas=  new GravarFilas(nomeFilaDecriptado,mapFilas,mensagemDecriptada);
            gravarFilas.setPriority(Thread.MAX_PRIORITY);
            gravarFilas.start();
            
             String mensagemRetornoGravar = "[Servidor] Mensagem gravada na: " + nomeFilaDecriptado;
            
            return CriptoServer.encriptarComChaveSimetrica(mensagemRetornoGravar.getBytes(), bytesChaveSimetrica);
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
            String nomeFilaP = new String(CriptoServer.decriptarComChaveSimetrica(nomeFilaG,bytesChaveSimetrica));
            if (mapFilas.containsKey(nomeFilaP)){
                mapFilas.remove(nomeFilaP);
                mensagemRetornoDeletar = "[Servidor] " + nomeFilaP + " Excluída com sucesso!";
            } else {
                mensagemRetornoDeletar = "[Servidor] " + nomeFilaP + " Não EXISTE!";                 
            }
            return CriptoServer.encriptarComChaveSimetrica(mensagemRetornoDeletar.getBytes(), bytesChaveSimetrica);
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
            CriptoServer criptoServer = new CriptoServer();
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
            CriptoServer krp = new CriptoServer();            
            PrivateKey chavepriv = krp.getPrivateKeyFromFile();
            this.bytesChaveSimetrica = CriptoServer.decriptarComChavePrivada(bytesChaveSimetricaFromClient,chavepriv);
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
        
        if(CriptoServer.isSenhaKeyStoreNaoInformada()){
            JPasswordField passwordField = new JPasswordField();
            Integer option = JOptionPane.showConfirmDialog(null, passwordField, "Informe a senha", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if ((option == JOptionPane.CANCEL_OPTION)) {
                System.exit(1);
            }
            if(passwordField.getPassword().length == 0){
                throw new RuntimeException("Senha nao informada!");
            }
            CriptoServer.setPassword(new String(passwordField.getPassword()));
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
