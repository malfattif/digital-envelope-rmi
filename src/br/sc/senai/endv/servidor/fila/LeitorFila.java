package br.sc.senai.endv.servidor.fila;
import java.util.ArrayList;
import java.util.Map;

public class LeitorFila extends Thread{
    
    private Map<String,ArrayList> mapFilas;
    private ArrayList listMensagens = new ArrayList();
    private String nomeFila = "";
    
    public LeitorFila(String nomeFILA, Map<String,ArrayList> caixaMSG, ArrayList listaMensagens){
        super(nomeFILA);
        mapFilas = caixaMSG;
        listMensagens = listaMensagens;
        nomeFila= nomeFILA;
    }
    public void lerMensagensFila(){
        
       if(!mapFilas.containsKey(nomeFila)){
          listMensagens.add(nomeFila + " sem Mensagens!");
          return;
       }
       
       ArrayList listMensagensDaFila = new ArrayList();
       listMensagensDaFila = mapFilas.get(nomeFila);
       for (int i=0;i<listMensagensDaFila.size();i++){
            listMensagens.add(listMensagensDaFila.get(i));
            String mensagemFila = (String) listMensagensDaFila.get(i);
            System.out.println("Thread ler: " + nomeFila + " MSG: " + mensagemFila);
       }
    }

    @Override
    public void run(){
        lerMensagensFila();
    }
}