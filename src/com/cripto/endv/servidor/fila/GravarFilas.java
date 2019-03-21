package com.cripto.endv.servidor.fila;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GravarFilas extends Thread {

	private Map<String, List> mapFilas;
	private String nomeFila = "";
	private String mensagem = null;

	public GravarFilas(String nomeFila, Map<String, List> mapFilas, String mensagem) {
		super(nomeFila);
		this.mapFilas = mapFilas;
		this.nomeFila = nomeFila;
		this.mensagem = mensagem;
	}

	public void gravarMensagemNaFila() {
		List listaMensagens = new ArrayList<String>();
		if (mapFilas.containsKey(nomeFila)) {
			listaMensagens = mapFilas.get(nomeFila);
		} else {
			listaMensagens.add(mensagem);
		}
		listaMensagens.add(mensagem);
		mapFilas.put(nomeFila, listaMensagens);
		System.out.println("Thread Gravar nova Mensagem na " + nomeFila + " MSG: " + mensagem.toString());
	}

	@Override
	public void run() {
		gravarMensagemNaFila();
	}
}
