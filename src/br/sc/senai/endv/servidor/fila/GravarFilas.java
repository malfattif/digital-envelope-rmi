package br.sc.senai.endv.servidor.fila;

import java.util.ArrayList;
import java.util.Map;

public class GravarFilas extends Thread{
    
    private Map<String,ArrayList> mapFilas;
    private String nomeFila = "";
    private String mensagem = null;
    
    public GravarFilas(String nomeFila, Map<String,ArrayList> mapFilas, String mensagem){
        super(nomeFila);
        this.mapFilas = mapFilas;
        this.nomeFila= nomeFila;
        this.mensagem = mensagem;
    }
    
    public void gravarMensagemNaFila(){
        ArrayList listaMensagens = new ArrayList();
        if (mapFilas.containsKey(nomeFila)){
            listaMensagens = mapFilas.get(nomeFila);
        } else {
            listaMensagens.add(mensagem);
        }
        listaMensagens.add(mensagem);
        mapFilas.put(nomeFila, listaMensagens);
        System.out.println("Thread Gravar nova Mensagem na " + nomeFila +" MSG: " + mensagem.toString());
    }
    
    @Override
    public void run(){
        gravarMensagemNaFila();
    }
}
