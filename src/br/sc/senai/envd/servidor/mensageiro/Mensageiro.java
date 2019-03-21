package br.sc.senai.envd.servidor.mensageiro;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;

public interface Mensageiro extends Remote {

        public PublicKey obterChavePublicaDoServidor() throws RemoteException;
        public boolean gravarChaveSimetricaNoServidor(byte[] chaveSim) throws RemoteException;
        public byte[] gravarMensagemEncriptadaNaFila(byte[] nomeFila, byte[] mensagem) throws RemoteException;
        public byte[] deletarFila(byte[] nomeFila) throws RemoteException;
        public ArrayList lerFila(byte[] nomefilas) throws RemoteException;
}
