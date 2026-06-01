package Rede;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GestorNos {
    private final Set<NoRemoto> nos = ConcurrentHashMap.newKeySet();

    public boolean adicionarNo(String ip, int port) {
        return nos.add(new NoRemoto(ip, port));
    }

    public boolean removerNo(String ip, int port) {
        return nos.remove(new NoRemoto(ip, port));
    }

    public List<NoRemoto> listarNos() {
        return new ArrayList<>(nos);
    }

    public boolean existeNo(String ip, int port) {
        return nos.contains(new NoRemoto(ip, port));
    }

    public void limpar() {
        nos.clear();
    }
}