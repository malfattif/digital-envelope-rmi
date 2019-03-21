package com.cripto.endv.servidor.fila;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeitorFila extends Thread {

	private Map<String, List> mapFilas;
	private List listMensagens = new ArrayList<String>();
	private String nomeFila = "";

	public LeitorFila(String nomeFILA, Map<String, List> caixaMSG, List listaMensagens) {
		super(nomeFILA);
		mapFilas = caixaMSG;
		listMensagens = listaMensagens;
		nomeFila = nomeFILA;
	}

	public void lerMensagensFila() {

		if (!mapFilas.containsKey(nomeFila)) {
			listMensagens.add(nomeFila + " sem Mensagens!");
			return;
		}

		List listMensagensDaFila = new ArrayList();
		listMensagensDaFila =  mapFilas.get(nomeFila);
		for (int i = 0; i < listMensagensDaFila.size(); i++) {
			listMensagens.add(listMensagensDaFila.get(i));
			String mensagemFila = (String) listMensagensDaFila.get(i);
			System.out.println("Thread ler: " + nomeFila + " MSG: " + mensagemFila);
		}
	}

	@Override
	public void run() {
		lerMensagensFila();
	}
}